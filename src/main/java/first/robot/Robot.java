// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import com.ctre.phoenix6.HootAutoReplay;
import com.ctre.phoenix6.SignalLogger;
import org.wpilib.command3.Scheduler;
import org.wpilib.framework.OpModeRobot;

/** The robot class. Subsystems live in {@link RobotContainer}, which is passed to each opmode. */
public class Robot extends OpModeRobot {
  final RobotContainer robotContainer = new RobotContainer();

  /**
   * Logs and replays timestamp, joystick, driver station and match info so a hoot log can be
   * replayed, like last year's robot. Commented out on the driver station only if the logs grow too
   * large.
   */
  private final HootAutoReplay timeAndJoystickReplay =
      new HootAutoReplay()
          .withTimestampReplay()
          .withJoystickReplay()
          .withDriverStationReplay()
          .withMatchInfoReplay();

  public Robot() {
    SignalLogger.start();
  }

  @Override
  public void robotPeriodic() {
    timeAndJoystickReplay.update();
    Scheduler.getDefault().run();
  }
}
