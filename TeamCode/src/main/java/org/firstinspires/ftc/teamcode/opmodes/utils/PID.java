package org.firstinspires.ftc.teamcode.opmodes.utils;

import com.qualcomm.robotcore.util.ElapsedTime;

public class PID {
    protected double Kp;
    protected double Ki;
    protected double Kd;

    protected double lastReference = 0;
    protected double integralSum = 0;
    protected double lastError = 0;

    ElapsedTime timer = new ElapsedTime();

    public PID(double Kp, double Ki, double Kd) {
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
    }

    public double update(double reference, double state) {
        if (Math.signum(lastReference) != Math.signum(reference) || reference == 0) integralSum = 0;

        double error = reference - state;
        double derivative = (error - lastError) / timer.seconds();
        integralSum = integralSum + (error * timer.seconds());
        return (Kp * error) + (Ki * integralSum) + (Kd * derivative);
    }

    public void reset() {
        integralSum = 0;
        lastError = 0;
        timer.reset();
    }
}
