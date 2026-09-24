// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Teleop;

/** Driver control: tank drive on the controller sticks, as bound in {@link RobotContainer}. */
@Teleop
public class TeleopDrive implements OpMode {
  public TeleopDrive(Robot robot) {
    // Drive bindings are registered once in RobotContainer's constructor; the
    // opmode just needs the robot running its scheduler (done in robotPeriodic).
  }
}
