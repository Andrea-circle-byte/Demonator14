package frc.robot.subsystems.coraller;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotContainer;
import frc.robot.Constants.CorallerConfig;
import frc.robot.subsystems.Elevator;

public class Coraller extends SubsystemBase {
  private final Elevator elevator = RobotContainer.kElevator;
  private final Angler angler = new Angler();
  private final Intake intake = new Intake();

  public Coraller() {
    SmartDashboard.putData(this);
  }

  /** {@inheritDoc} */
  @Override
  public void periodic() {
    updateSetpointsForDisabledMode();
  }

  public Command prepareToScoreReef(ReefScoringConfiguration cfg) {
    return Commands.parallel(
      elevator.setHeightCommand(cfg.elevatorPosition),
      angler.setAngleCommand(cfg.anglerPosition)
    ).withName("PrepareToScoreReef");
  }

  public Command intakeCoral() {
    var cmd = startEnd(intake::start, intake::stop);
    cmd.addRequirements(intake);
    return cmd
      .until(intake::hasCoral)
      .withTimeout(3)
      .withName("IntakeCoral");
  }

  public Command outtakeCoral() {
    var cmd = startEnd(intake::reverse, intake::stop);
    cmd.addRequirements(intake);
    return cmd
      .withTimeout(.5)
      .withName("ReleaseCoral");
  }

  public Command stopCommand() {
    return Commands.parallel(
      elevator.stopCommand(),
      angler.stopCommand(),
      intake.stopCommand()
    ).withName("StopCoraller");
  }

  public Command stow() {
    var cmd = Commands.parallel(
      angler.setAngleCommand(CorallerConfig.STOW_ANGLE),
      elevator.stow()
    );
    cmd.addRequirements(this);

    return cmd;
  }

  private void stop() {
    angler.stop();
    intake.stop();
  }

  /** Updates the setpoints to the current positions. */
  private void updateSetpointsForDisabledMode() {
    // only in disabled
    if (RobotState.isDisabled()) {
      // angler
      angler.setSetpointAngle(angler.getAngle());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void initSendable(SendableBuilder builder) {
    super.initSendable(builder);
    builder.setSmartDashboardType(getName());
    builder.setSafeState(this::stop);
    builder.setActuator(true);
  }

  /** Heights and angles to score on the reef. */
  public enum ReefScoringConfiguration {
    L1(CorallerConfig.L1_HEIGHT, CorallerConfig.L1_ANGLE),
    L2(CorallerConfig.L2_HEIGHT, CorallerConfig.L2_ANGLE),
    L3(CorallerConfig.L3_HEIGHT, CorallerConfig.L3_ANGLE),
    L4(CorallerConfig.L4_HEIGHT, CorallerConfig.L4_ANGLE),
    RECEIVE(CorallerConfig.RECEIVE_HEIGHT, CorallerConfig.RECEIVE_ANGLE);

    /** Inches */
    private final double elevatorPosition;
    /** Degrees */
    private final double anglerPosition;

    private ReefScoringConfiguration(double elevatorPosition, double anglerPosition) {
      this.elevatorPosition = elevatorPosition;
      this.anglerPosition = anglerPosition;
    }
  }
}
