package com.qualcomm.ftcrobotcontroller.opmodes;


import com.qualcomm.ftcrobotcontroller.common.Values;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

public class TreadBot extends OpMode {


    private final double updateFreq = 1000;
    private final double servoInc = 0.01;

    private final int updateMs = (int) Math.floor(1000 / updateFreq);

    private DcMotor left;
    private DcMotor right;
    private DcMotor hangArm;
    private DcMotor plow;
    private Servo leftTriggerServo, rightTriggerServo, climberServo;

    private boolean reverse = true;

    private boolean encReset = false;
    private boolean reversePressed = false;
    private boolean climberOpen = false;
    private boolean startPressed = false;

    private long nextJoy1;
    private long nextJoy2;

    @Override
    public void init() {
        left = hardwareMap.dcMotor.get("left");
        right = hardwareMap.dcMotor.get("right");
        hangArm = hardwareMap.dcMotor.get("hangArm");
        plow = hardwareMap.dcMotor.get("plow");

        leftTriggerServo = hardwareMap.servo.get("trigger_left");
        rightTriggerServo = hardwareMap.servo.get("trigger_right");
        climberServo = hardwareMap.servo.get("climber");

        left.setDirection(DcMotor.Direction.FORWARD);
        right.setDirection(DcMotor.Direction.REVERSE);
        plow.setDirection(DcMotor.Direction.FORWARD);
        hangArm.setDirection(DcMotor.Direction.REVERSE);

        hangArm.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        plow.setMode(DcMotorController.RunMode.RESET_ENCODERS);

        leftTriggerServo.setPosition(Values.TRIGGER_LEFT_RETRACT);
        rightTriggerServo.setPosition(Values.TRIGGER_RIGHT_RETRACT);
        climberServo.setPosition(Values.CLIMBER_CLOSE);
    }

    @Override
    public void loop() {
        if (!encReset) {
            hangArm.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
            plow.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
            encReset = true;
        }


        // Hours spent: 2
        right.setPower((reverse) ? -gamepad1.right_stick_y : gamepad1.left_stick_y);
        left.setPower((reverse) ? -gamepad1.left_stick_y : gamepad1.right_stick_y);

        telemetry.addData("climberOpen", climberOpen);

        telemetry.addData("rtPos", rightTriggerServo.getPosition());
        telemetry.addData("ltPos", leftTriggerServo.getPosition());
        telemetry.addData("left_power", left.getPower());
        telemetry.addData("right_power", right.getPower());
        telemetry.addData("hangArmPos", hangArm.getCurrentPosition());
        telemetry.addData("plowPos", plow.getCurrentPosition());
        telemetry.addData("rightBumper", gamepad1.right_trigger);
        telemetry.addData("reverse", reverse);
        telemetry.addData("joy1Next", nextJoy1);
        telemetry.addData("joy2Next", nextJoy2);
        updateArm();
        updatePlow();
        updateDrive();
        updateClimbers();
        updateTrigger();

    }

    private void updateArm() {
        int armInc = 200;
        //game pad 1
        if (gamepad1.dpad_up) {
            hangArm.setTargetPosition(hangArm.getCurrentPosition() + armInc);
            hangArm.setPower(-1);
        } else if (gamepad1.dpad_down) {
            hangArm.setTargetPosition(hangArm.getCurrentPosition() - armInc);
            hangArm.setPower(1);
        }
        //game pad 2
        else if (gamepad2.dpad_up) {
            hangArm.setTargetPosition(hangArm.getCurrentPosition() + armInc);
            hangArm.setPower(-1);
        } else if (gamepad2.dpad_down) {
            hangArm.setTargetPosition(hangArm.getCurrentPosition() - armInc);
            hangArm.setPower(1);
        }
        //
        else {
            hangArm.setTargetPosition(hangArm.getCurrentPosition());
        }
    }

    private void updatePlow() {
        int plowInc = 200;

        //game pad 1
        if (gamepad1.a) {
            plow.setTargetPosition(plow.getCurrentPosition() + plowInc);
            plow.setPower(1);
        } else if (gamepad1.x) {
            plow.setTargetPosition(plow.getCurrentPosition() - plowInc);
            plow.setPower(-1);
        }
        //game pad 2
        else if (gamepad2.a) {
            plow.setTargetPosition(plow.getCurrentPosition() + plowInc);
            plow.setPower(1);
        } else if (gamepad2.x) {
            plow.setTargetPosition(plow.getCurrentPosition() - plowInc);
            plow.setPower(-1);
        } else {
            plow.setTargetPosition(plow.getCurrentPosition());
            plow.setPower(0);
        }
    }

    private void updateDrive() {
        if ((gamepad1.b) && !reversePressed) {
            reverse = !reverse;
            reversePressed = true;
        } else {
            if (!(gamepad1.b)) {
                reversePressed = false;
            }
        }
    }

    private void updateClimbers() {
        if ((gamepad2.start || gamepad1.start) && !startPressed) {
            startPressed = true;
            climberServo.setPosition(climberOpen ? Values.CLIMBER_CLOSE : Values.CLIMBER_OPEN);
            climberOpen = !climberOpen;
        }
        if (!(gamepad2.start || gamepad1.start)) {
            startPressed = false;
        }
    }

    private boolean timeExpired(boolean joy1) {
        if (System.currentTimeMillis() > (joy1 ? nextJoy1 : nextJoy2)) {
            if (joy1) {
                nextJoy1 = System.currentTimeMillis() + updateMs;
                return true;
            } else {
                nextJoy2 = System.currentTimeMillis() + updateMs;
                return true;
            }
        }
        return false;
    }

    private void addServoPos(Servo servo, double servoPos) {
        double newPos = Range.clip(servo.getPosition() - servoPos, 0, 1);
        servo.setPosition(newPos);
    }

    private void updateTrigger() {
        // Left mountain triggers
        if (gamepad2.left_bumper) {
            rightTriggerServo.setPosition(Values.TRIGGER_RIGHT_RETRACT);
        }
        if (gamepad1.left_bumper) {
            rightTriggerServo.setPosition(Values.TRIGGER_RIGHT_RETRACT);
        }
        // Right mountain triggers
        if (gamepad2.right_bumper) {
            leftTriggerServo.setPosition(Values.TRIGGER_LEFT_RETRACT);
        }
        if (gamepad1.right_bumper) {
            leftTriggerServo.setPosition(Values.TRIGGER_LEFT_RETRACT);
        }

        if (gamepad2.left_trigger > 0.5) {
            if (timeExpired(false))
                addServoPos(rightTriggerServo, servoInc);
        }
        if (gamepad1.left_trigger > 0.5) {
            if (timeExpired(true))
                addServoPos(rightTriggerServo, servoInc);
        }
        if (gamepad2.right_trigger > 0.5) {
            if (timeExpired(false))
                addServoPos(leftTriggerServo, -servoInc);
        }
        if (gamepad1.right_trigger > 0.5)
            if (timeExpired(true))
                addServoPos(leftTriggerServo, -servoInc);
    }
}

