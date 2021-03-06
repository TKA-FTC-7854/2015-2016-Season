package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.ftcrobotcontroller.common.StateMachine;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

public class TestStateMachineThing extends OpMode {

    private StateMachine<States> stateMachine;

    private DcMotor left;
    private DcMotor right;

    @Override
    public void init() {
        stateMachine = new StateMachine<States>(States.RESET_ENCODERS, this);
        left = hardwareMap.dcMotor.get("left");
        right = hardwareMap.dcMotor.get("right");
    }

    @Override
    public void loop() {
        stateMachine.tick();
        telemetry.addData("State", stateMachine.getCurrentState().toString());
    }

    public boolean hasReachedTarget(DcMotor... motors) {
        for (DcMotor motor : motors) {
            if (Math.abs(motor.getTargetPosition() - motor.getCurrentPosition()) > 10)
                return false;
        }
        return true;
    }

    enum States implements StateMachine.State {
        RESET_ENCODERS {
            TestStateMachineThing parent;

            @Override
            public boolean shouldChangeState() {
                return parent.left.getCurrentPosition() == 0 && parent.right.getCurrentPosition() == 0;
            }

            @Override
            public void runState() {
                parent.left.setMode(DcMotorController.RunMode.RESET_ENCODERS);
                parent.right.setMode(DcMotorController.RunMode.RESET_ENCODERS);
            }

            @Override
            public void end() {
                parent.left.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
                parent.right.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
            }

            @Override
            public void tick() {

            }
        },
        DRIVE_FORWARD {
            TestStateMachineThing parent;

            @Override
            public boolean shouldChangeState() {
                return parent.hasReachedTarget(parent.left, parent.right);
            }

            @Override
            public void runState() {
                parent.left.setTargetPosition(1000);
                parent.right.setTargetPosition(1000);
            }

            @Override
            public void end() {

            }

            @Override
            public void tick() {

            }
        }
    }
}
