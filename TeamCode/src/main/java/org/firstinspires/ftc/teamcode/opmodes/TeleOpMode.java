/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "TeleOpMode")
public class TeleOpMode extends LinearOpMode {
    // Constants
    final private double MAX_TURN = 0.6;
    final private double MAX_CRAWL_SPEED = 0.3;
    final private double MAX_PRECISE_SPEED = 0.15;

    final private double ARM_POWER = 0.3;

    // Gamepads to determine state changes.
    Gamepad currentGamepad1 = new Gamepad();
    Gamepad currentGamepad2 = new Gamepad();
    Gamepad previousGamepad1 = new Gamepad();
    Gamepad previousGamepad2 = new Gamepad();

    private ElapsedTime runtime = new ElapsedTime();
    private Robot robot = new Robot(this);

    private boolean crawlingMode = false;
    private boolean preciseMode = false;

    @Override
    public void runOpMode() {
        robot.init();
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();
        while (opModeIsActive()) {
            // Update gamepads
            previousGamepad1.copy(currentGamepad1);
            previousGamepad2.copy(currentGamepad2);
            currentGamepad1.copy(gamepad1);
            currentGamepad2.copy(gamepad2);

            updateDrive();
            updateArm();

            // Send telemetry
            telemetry.addLine("Driver (Gamepad 1):");
            telemetry.addData("Drive (Left Stick)", "left (%.2f), right (%.2f)", robot.leftSpeed, robot.rightSpeed);
            telemetry.addData("Crawling (Left Bumber): Precise (Right Bumber)", "%b : %b", crawlingMode, preciseMode);

            telemetry.addLine("\nOperator (Gamepad 2):");
            telemetry.addData("Arm (Left Stick)", "power (%.2f), position (%d), velocity (%.2f)", robot.armMotor.getPower(), robot.arm.getCurrentPosition(), robot.armMotor.getVelocity());
            telemetry.addData("Hand (A,B,Y)", "position (%.2f), state (%s)", robot.handPosition, robot.handState);
            telemetry.addData("Gripper (X)", "position (%.2f), closed (%b)", robot.gripperPosition, robot.gripperClosed);

            telemetry.addData("\nStatus", "Run Time: %.2f", runtime.seconds());
            telemetry.update();
        }
    }

    void updateDrive() {
        if (currentGamepad1.left_bumper && !previousGamepad1.left_bumper)
            crawlingMode = !crawlingMode;
        preciseMode = currentGamepad1.right_bumper;

        // Calculate drive and turn
        double drive = -gamepad1.left_stick_y;
        double turn = gamepad1.right_stick_x;

        if (crawlingMode) {
            drive *= MAX_CRAWL_SPEED;
            turn *= MAX_CRAWL_SPEED;
        } else if (preciseMode) {
            drive *= MAX_PRECISE_SPEED;
            turn *= MAX_PRECISE_SPEED;
        } else {
            turn *= MAX_TURN;
        }

        // Update motors power
        robot.driveRobot(drive, turn);
    }

    void updateArm() {
        // Update arm power
        robot.arm.updateArmState(
                -currentGamepad2.left_stick_y * (ARM_POWER + ((1 - ARM_POWER) * (double) currentGamepad2.right_trigger)),
                !currentGamepad2.left_bumper && currentGamepad2.right_trigger == 0,
                currentGamepad2.right_bumper, currentGamepad2.right_trigger == 0);

        if (currentGamepad2.dpad_right && !previousGamepad2.dpad_right) {
            robot.arm.encoderOffset += 20;
        } else if (currentGamepad2.dpad_left && !previousGamepad2.dpad_left) {
            robot.arm.encoderOffset -= 20;
        }

        if (currentGamepad2.dpad_up) {
            robot.setHandPosition(Range.clip(robot.handPosition + 0.003, 0, 1));
        } else if (currentGamepad2.dpad_down) {
            robot.setHandPosition(Range.clip(robot.handPosition - 0.003, 0, 1));
        }
        // Change hand position using A, B, and Y buttons
        if (currentGamepad2.a && !previousGamepad2.a) {
            robot.setHandState(Robot.HandState.DOWN);
        } else if (currentGamepad2.b && !previousGamepad2.b && !currentGamepad2.start) {
            robot.setHandState(Robot.HandState.BACKDROP);
        } else if (currentGamepad2.y && !previousGamepad2.y) {
            robot.setHandState(Robot.HandState.UP);
        }
        // Toggle gripper position using X button
        if (currentGamepad2.x && !previousGamepad2.x) {
            robot.setGripperState(!robot.gripperClosed);
        }
    }
}
