// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.subsystems;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Celsius;
import static org.wpilib.units.Units.Rotations;
import static org.wpilib.units.Units.RotationsPerSecond;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import java.util.function.DoubleSupplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.driverstation.RobotState;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Temperature;
import first.robot.constants.DrivetrainConstants;

/**
 * Drivetrain subsystem for a four motor tank drive using CTRE TalonFX motors.
 *
 * <p>Motors 1 and 2 drive the left side (motor 1 is the front left lead, motor 2 follows it), and
 * motors 3 and 4 drive the right side (motor 3 is the front right lead, motor 4 follows it). The
 * followers mirror their side's lead motor, and every motor gets brake mode and current limits.
 */
public class Drivetrain implements Mechanism {
  private final TalonFX leftLead;
  private final TalonFX leftFollow;
  private final TalonFX rightLead;
  private final TalonFX rightFollow;

  private final DifferentialDrive drive;

  // Cached status signals, refreshed once per cycle so telemetry reads don't block on CAN.
  private final StatusSignal<AngularVelocity> leftVelocity;
  private final StatusSignal<AngularVelocity> rightVelocity;
  private final StatusSignal<Angle> leftPosition;
  private final StatusSignal<Angle> rightPosition;
  private final StatusSignal<Current> leftStatorCurrent;
  private final StatusSignal<Current> rightStatorCurrent;
  private final StatusSignal<Temperature> leftTemp;
  private final StatusSignal<Temperature> rightTemp;

  private final TelemetryTable telemetry = Telemetry.getTable("Drivetrain");

  /** Creates a new drivetrain with TalonFX motors on the default CAN IDs 1, 2, 3 and 4. */
  public Drivetrain() {
    CANBus canBus = new CANBus(DrivetrainConstants.CAN_PORT);

    leftLead = new TalonFX(DrivetrainConstants.LEFT_LEAD_ID, canBus);
    leftFollow = new TalonFX(DrivetrainConstants.LEFT_FOLLOW_ID, canBus);
    rightLead = new TalonFX(DrivetrainConstants.RIGHT_LEAD_ID, canBus);
    rightFollow = new TalonFX(DrivetrainConstants.RIGHT_FOLLOW_ID, canBus);

    configureMotor(leftLead, DrivetrainConstants.LEFT_DIRECTION);
    configureMotor(leftFollow, DrivetrainConstants.LEFT_DIRECTION);
    configureMotor(rightLead, DrivetrainConstants.RIGHT_DIRECTION);
    configureMotor(rightFollow, DrivetrainConstants.RIGHT_DIRECTION);

    // The rear motors mirror their side's lead motor.
    leftFollow.setControl(
        new Follower(DrivetrainConstants.LEFT_LEAD_ID, MotorAlignmentValue.Aligned));
    rightFollow.setControl(
        new Follower(DrivetrainConstants.RIGHT_LEAD_ID, MotorAlignmentValue.Aligned));

    // Last year's code drove the drivetrain through a DifferentialDrive; TalonFX motors are driven
    // by writing a throttle, so the drive is built from throttle consumers.
    drive = new DifferentialDrive(leftLead::setThrottle, rightLead::setThrottle);

    leftVelocity = leftLead.getVelocity();
    rightVelocity = rightLead.getVelocity();
    leftPosition = leftLead.getPosition();
    rightPosition = rightLead.getPosition();
    leftStatorCurrent = leftLead.getStatorCurrent();
    rightStatorCurrent = rightLead.getStatorCurrent();
    leftTemp = leftLead.getDeviceTemp();
    rightTemp = rightLead.getDeviceTemp();

    // Mechanisms have no periodic() callback in Commands v3, so register one with the scheduler.
    Scheduler.getDefault().addPeriodic(this::periodic);
  }

  /** Applies current limits, gearing, neutral mode and direction to one drive motor. */
  private static void configureMotor(TalonFX motor, InvertedValue direction) {
    TalonFXConfiguration config =
        new TalonFXConfiguration()
            .withMotorOutput(
                new MotorOutputConfigs()
                    .withInverted(direction)
                    .withNeutralMode(DrivetrainConstants.NEUTRAL_MODE))
            .withCurrentLimits(
                new CurrentLimitsConfigs()
                    .withStatorCurrentLimit(Amps.of(DrivetrainConstants.STATOR_CURRENT_LIMIT_AMPS))
                    .withStatorCurrentLimitEnable(true)
                    .withSupplyCurrentLimit(Amps.of(DrivetrainConstants.SUPPLY_CURRENT_LIMIT_AMPS))
                    .withSupplyCurrentLimitEnable(true))
            .withFeedback(
                new FeedbackConfigs()
                    .withSensorToMechanismRatio(DrivetrainConstants.DRIVE_GEAR_RATIO));

    motor.getConfigurator().apply(config);
  }

  /** Drives the robot with a tank style scheme. */
  public void tankDrive(double leftSpeed, double rightSpeed) {
    drive.tankDrive(leftSpeed, rightSpeed);
  }

  /** Drives the robot with an arcade style scheme. */
  public void arcadeDrive(double xSpeed, double zRotation) {
    drive.arcadeDrive(xSpeed, zRotation);
  }

  /** Stops all four motors. */
  public void stop() {
    drive.stopMotor();
  }

  /** Returns the left side velocity, in rotations per second. */
  public double getLeftVelocityRps() {
    return leftVelocity.getValue().in(RotationsPerSecond);
  }

  /** Returns the right side velocity, in rotations per second. */
  public double getRightVelocityRps() {
    return rightVelocity.getValue().in(RotationsPerSecond);
  }

  /** Returns a command that drives the robot with tank controls until interrupted. */
  public Command tankDriveCommand(DoubleSupplier leftSpeed, DoubleSupplier rightSpeed) {
    return runRepeatedly(() -> tankDrive(leftSpeed.getAsDouble(), rightSpeed.getAsDouble()))
        .named("Tank Drive");
  }

  /**
   * Returns a command that drives the robot with arcade controls until interrupted.
   *
   * @param xSpeed forward/backward speed, positive being forward
   * @param zRotation rotation rate, positive being counterclockwise
   */
  public Command arcadeDriveCommand(DoubleSupplier xSpeed, DoubleSupplier zRotation) {
    return runRepeatedly(() -> arcadeDrive(xSpeed.getAsDouble(), zRotation.getAsDouble()))
        .named("Arcade Drive");
  }

  /** Returns a command that stops the drivetrain. */
  public Command stopCommand() {
    return run(coroutine -> stop()).named("Stop Drivetrain");
  }

  /** Runs every scheduler cycle: holds the motors still while disabled, then publishes telemetry. */
  private void periodic() {
    if (RobotState.isDisabled()) {
      stop();
    }

    BaseStatusSignal.refreshAll(
        leftVelocity,
        rightVelocity,
        leftPosition,
        rightPosition,
        leftStatorCurrent,
        rightStatorCurrent,
        leftTemp,
        rightTemp);

    telemetry.log("LeftVelocityRps", leftVelocity.getValue().in(RotationsPerSecond));
    telemetry.log("RightVelocityRps", rightVelocity.getValue().in(RotationsPerSecond));
    telemetry.log("LeftPositionRot", leftPosition.getValue().in(Rotations));
    telemetry.log("RightPositionRot", rightPosition.getValue().in(Rotations));
    telemetry.log("LeftStatorCurrentAmps", leftStatorCurrent.getValue().in(Amps));
    telemetry.log("RightStatorCurrentAmps", rightStatorCurrent.getValue().in(Amps));
    telemetry.log("LeftTempCelsius", leftTemp.getValue().in(Celsius));
    telemetry.log("RightTempCelsius", rightTemp.getValue().in(Celsius));
  }
}
