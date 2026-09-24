# 3560-DEV — 2027 Robot Code

Robot code for FRC Team 3560 (Mechawolves), built on **WPILib `2027.0.0-alpha-7`** targeting
**SystemCore**. The structure follows our 2026 robot (`3560frc/Game-2026`): `Robot` +
`RobotContainer` + one subsystem per mechanism, with a single `Drivetrain` subsystem for now.

## Requirements

| Thing | Version / location |
| --- | --- |
| WPILib | `2027.0.0-alpha-7` (install root `C:\Users\Public\wpilib\2027_alpha7`) |
| Java | 25 (bundled JDK: `C:\Users\Public\wpilib\2027_alpha7\jdk`) |
| CTRE Phoenix 6 devices | firmware **26.70.x** (SystemCore alpha image 14) |
| Team number | 3560 (`.wpilib/wpilib_preferences.json`) |

Building from a terminal needs `JAVA_HOME` pointed at the bundled JDK — VS Code sets this for you,
a plain terminal does not:

```powershell
# PowerShell
$env:JAVA_HOME="C:\Users\Public\wpilib\2027_alpha7\jdk"
```

```cmd
:: Command Prompt
set JAVA_HOME=C:\Users\Public\wpilib\2027_alpha7\jdk
```

## Project layout

```
src/main/java/first/
├── Main.java                     # entry point (RobotBase.startRobot)
└── robot/
    ├── Robot.java                # holds RobotContainer, runs the scheduler, CTRE logging/replay
    ├── RobotContainer.java       # subsystems + controller bindings
    ├── TeleopDrive.java          # @Teleop opmode
    ├── DriveForwardAuto.java     # @Autonomous opmode (placeholder: drive forward 3s)
    ├── constants/
    │   ├── DriverConstants.java      # controller ports
    │   └── DrivetrainConstants.java  # CAN bus, motor IDs, limits, gearing, tuning
    └── subsystems/
        └── Drivetrain.java       # the one subsystem: 4x TalonFX tank drive
```

`Robot` is an `OpModeRobot`; opmodes are the classes annotated `@Teleop` / `@Autonomous`, which the
new framework discovers automatically.

## Controls

Two Xbox controllers, matching last year's setup where one controller drove and the other handled
the robot's mechanisms:

| Port | Controller | Bindings |
| --- | --- | --- |
| 1 | Driver | Left stick = forward/back, right stick = rotate (arcade drive, with deadband and a slew-rate limiter on throttle). The drivetrain is held still while the robot is disabled. |
| 0 | Operator | **Nothing bound yet.** Add mechanism bindings in `RobotContainer.configureOperatorBindings()` as subsystems come online. |

Controller ports live in `constants/DriverConstants.java`. The drivetrain also exposes
`tankDriveCommand(...)` if you'd rather go back to split-stick tank drive.

## Drivetrain hardware / CAN map

| CAN ID | Motor | Side |
| --- | --- | --- |
| 1 | Front left (lead) | Left |
| 2 | Rear left (follows 1) | Left |
| 3 | Front right (lead) | Right |
| 4 | Rear right (follows 3) | Right |

- **Bus:** `CANPort.CAN_S1` — change `DrivetrainConstants.CAN_PORT` if the motors are on a different
  SystemCore port (`CAN_S0`–`CAN_S4`), a Motioncore port (`CAN_D0`–`CAN_D19`) or a CANivore (by name).
- **Direction:** right side inverted so positive input drives forward on both sides.
- **Neutral mode:** Brake. **Current limits:** 60 A stator, 40 A supply per motor.
- **Control:** open loop (`setThrottle`) through WPILib's `DifferentialDrive`.

Set the CAN IDs in Phoenix Tuner X (each device must be unique and match `DrivetrainConstants`).

## Vendordeps

| Vendordep | Version | Why |
| --- | --- | --- |
| `CommandsV3.json` | 1.0.0 | WPILib Commands v3 (shipped with the project) |
| `Phoenix6-26.70.0-alpha-2.json` | 26.70.0-alpha-2 | CTRE Phoenix 6 (TalonFX) |

Install or update them with the CLI task (or *WPILib: Manage Vendor Libraries → Install new
libraries (online)* in VS Code):

```bash
./gradlew vendordep --url="https://frcmaven.wpi.edu/artifactory/vendordeps/vendordep-marketplace/2027_alpha7/Phoenix6-26.70.0-alpha-2.json"
./gradlew vendordep --update   # refresh the ones already installed
```

> **Windows note:** the alpha `vendordep` task sometimes drops the downloaded JSON in the project
> root instead of `vendordeps/` (it builds the destination path with `/`). If that happens, just
> move the file into `vendordeps/`.

## Build, deploy, test

```bash
./gradlew build        # compile everything and run checks
./gradlew deploy       # build and deploy to the SystemCore (team 3560)
./gradlew compileJava  # quick compile only
```

Desktop simulation is **disabled** in `build.gradle` (`def includeDesktopSupport = false`). Set it to
`true` to get the desktop/simulation tasks back, then run `./gradlew simulateJava`.

## Alpha-season gotchas

- This Phoenix 6 release is **SystemCore-only — it does not support the roboRIO.**
- Device firmware must be **26.70.x**; anything older will report a version mismatch.
- API renames to remember: `TalonFX.set()` → `setThrottle()`, `setNeutralMode()` →
  `configNeutralMode()`, every device constructor now needs a `CANBus`, swerve requests use
  `...Velocity` instead of `...Speeds`.
- Tuner X cannot generate mechanisms/swerve projects for this release yet, so swerve constants have
  to be written or copied by hand (`corvus 26.70.0` / the Phoenix6-Examples repo have updated
  `CommandSwerveDrivetrain` and `TunerConstants`).
- PathPlanner is not published for this alpha; **ChoreoLib** is the trajectory option available.
- `SmartDashboard` is gone in 2027 — use the `org.wpilib.telemetry.Telemetry` API (the drivetrain
  already publishes velocity, position, current and temperature on the `Drivetrain` table).

## What the drivetrain publishes

`Telemetry` table `Drivetrain`: `LeftVelocityRps`, `RightVelocityRps`, `LeftPositionRot`,
`RightPositionRot`, `LeftStatorCurrentAmps`, `RightStatorCurrentAmps`, `LeftTempCelsius`,
`RightTempCelsius`. Signals are refreshed once per cycle (non-blocking) and motors are held stopped
while the robot is disabled.
