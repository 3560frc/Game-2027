// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.constants;

public final class DriverConstants {
  /** Port of the driver's controller: movement and rotation of the drivetrain. */
  public static final int DRIVER_CONTROLLER_PORT = 1;

  /**
   * Port of the operator's controller. Reserved for mechanisms that don't exist yet (intake,
   * shooter, ...) - see {@link first.robot.RobotContainer#configureOperatorBindings}.
   */
  public static final int OPERATOR_CONTROLLER_PORT = 0;

  private DriverConstants() {}
}
