// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.command3.Scheduler;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.command3.button.RobotModeTriggers;
import org.wpilib.math.filter.SlewRateLimiter;
import first.robot.constants.DriverConstants;
import first.robot.constants.DrivetrainConstants;
import first.robot.subsystems.Drivetrain;

/**
 * Holds subsystem instances and defines the controller bindings, adapted from last year's robot
 * code (3560frc/Game-2026), which also used two controllers: one for driving and one for the rest of
 * the robot's mechanisms.
 */
public final class RobotContainer {
  public final Drivetrain drivetrain = new Drivetrain();

  /** Driver's controller: drives the robot (movement and rotation). */
  private final CommandXboxController driverController =
      new CommandXboxController(DriverConstants.DRIVER_CONTROLLER_PORT);

  /**
   * Operator's controller. Nothing is bound to it yet; add mechanism bindings in {@link
   * #configureOperatorBindings} as those subsystems come online.
   */
  private final CommandXboxController operatorController =
      new CommandXboxController(DriverConstants.OPERATOR_CONTROLLER_PORT);

  // Slew limiting keeps the robot from lurching when the driver slams the stick, like last year's
  // drive code did. Turning is left un-slewed so the robot stays responsive to rotation.
  private final SlewRateLimiter throttleLimiter =
      new SlewRateLimiter(DrivetrainConstants.DRIVE_SLEW_RATE);

  public RobotContainer() {
    configureDriverBindings();
    configureOperatorBindings();
  }

  /** Left stick moves the robot forward/back, right stick rotates it. */
  private void configureDriverBindings() {
    Scheduler.getDefault()
        .setDefaultCommand(
            drivetrain,
            drivetrain.arcadeDriveCommand(
                () -> throttleLimiter.calculate(deadband(-driverController.getLeftY())),
                () -> deadband(-driverController.getRightX())));

    // Disable-safe: while the robot is disabled, hold the drivetrain still.
    RobotModeTriggers.disabled().whileTrue(drivetrain.stopCommand());
  }

  /** The operator's controller is intentionally empty for now. */
  private void configureOperatorBindings() {
    // Add mechanism bindings here as they are built, for example:
    // operatorController.a().whileTrue(intake.intakeCommand());
    // operatorController.b().whileTrue(shooter.feedCommand());
  }

  /** Zeroes out small stick values so a resting joystick doesn't creep the robot. */
  private static double deadband(double input) {
    return Math.abs(input) > DrivetrainConstants.DRIVE_DEADBAND ? input : 0.0;
  }
}
