// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.OpMode;
import static org.wpilib.units.Units.Seconds;
import org.wpilib.units.measure.Time;

/** Placeholder autonomous: drives forward for three seconds, then stops. */
@Autonomous
public class DriveForwardAuto implements OpMode {
  private final Robot robot;
  private Command autoCommand;

  public DriveForwardAuto(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void start() {
    Command driveForward =
        robot.robotContainer.drivetrain.tankDriveCommand(() -> 0.5, () -> 0.5);
    Command stop = robot.robotContainer.drivetrain.stopCommand();
    autoCommand = driveForward.withTimeout(Seconds.of(3)).andThen(stop).named("Drive Forward Auto");
    Scheduler.getDefault().schedule(autoCommand);
  }

  @Override
  public void end() {
    if (autoCommand != null) {
      Scheduler.getDefault().cancel(autoCommand);
    }
  }
}
