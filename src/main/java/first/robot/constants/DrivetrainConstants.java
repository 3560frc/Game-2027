// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.constants;

import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import org.wpilib.hardware.bus.CANPort;

/** Constants for the {@link first.robot.subsystems.Drivetrain}. */
public final class DrivetrainConstants {
  /**
   * The SystemCore CAN bus the drivetrain motors are wired to. SystemCore exposes ports CAN_S0
   * through CAN_S4; change this to match the port on the robot (Motioncore ports are CAN_D0 through
   * CAN_D19, and a CANivore is referenced by its name).
   */
  public static final CANPort CAN_PORT = CANPort.CAN_S1;

  // CAN device IDs. Defaults are 1-4; update these to match the robot.
  /** CAN ID of the front left (lead) drive motor. */
  public static final int LEFT_LEAD_ID = 1;
  /** CAN ID of the rear left motor, following the front left. */
  public static final int LEFT_FOLLOW_ID = 2;
  /** CAN ID of the front right (lead) drive motor. */
  public static final int RIGHT_LEAD_ID = 3;
  /** CAN ID of the rear right motor, following the front right. */
  public static final int RIGHT_FOLLOW_ID = 4;

  /** Motor rotations per wheel rotation (CTRE sensor-to-mechanism ratio). */
  public static final double DRIVE_GEAR_RATIO = 1.0;

  /** Stator (torque producing) current limit per motor, in amps. */
  public static final double STATOR_CURRENT_LIMIT_AMPS = 60.0;
  /** Supply (battery draw) current limit per motor, in amps. */
  public static final double SUPPLY_CURRENT_LIMIT_AMPS = 40.0;

  /** Direction the left side motors spin for a positive output. */
  public static final InvertedValue LEFT_DIRECTION = InvertedValue.CounterClockwise_Positive;
  /** Direction the right side motors spin for a positive output (the right side is mirrored). */
  public static final InvertedValue RIGHT_DIRECTION = InvertedValue.Clockwise_Positive;

  /** Brake holds the robot in place; Coast lets it roll free. */
  public static final NeutralModeValue NEUTRAL_MODE = NeutralModeValue.Brake;

  /** Deadband applied to joystick inputs so a resting stick reads as zero. */
  public static final double DRIVE_DEADBAND = 0.05;

  /** Joystick input units per second before the drive command ramps to full speed. */
  public static final double DRIVE_SLEW_RATE = 2.9;

  private DrivetrainConstants() {}
}
