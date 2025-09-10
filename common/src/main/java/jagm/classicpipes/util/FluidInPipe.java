package jagm.classicpipes.util;

import net.minecraft.core.Direction;

public class FluidInPipe {

    private int amount;
    private short speed;
    private short progress;
    private Direction fromDirection;
    private Direction targetDirection;
    private short age;

    public FluidInPipe(int amount, short speed, short progress, Direction fromDirection, Direction targetDirection, short age) {
        this.amount = amount;
        this.speed = speed;
        this.progress = progress;
        this.fromDirection = fromDirection;
        this.targetDirection = targetDirection;
        this.age = age;
    }

    public void move(short targetSpeed, short acceleration) {
        if (this.speed < targetSpeed) {
            this.speed = (short) Math.min(this.speed + acceleration, Math.min(targetSpeed, ItemInPipe.SPEED_LIMIT));
        } else if (this.speed > targetSpeed) {
            this.speed = (short) Math.max(this.speed - acceleration, Math.max(targetSpeed, 1));
        }
        this.progress += this.speed;
        this.age++;
    }

    public void resetProgress(Direction direction) {
        this.progress -= ItemInPipe.PIPE_LENGTH;
        this.fromDirection = direction;
        this.targetDirection = direction;
    }

    public FluidInPipe copyWithAmount(int amount) {
        return new FluidInPipe(amount, this.speed, this.progress, this.fromDirection, this.targetDirection, this.age);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public short getProgress() {
        return progress;
    }

    public Direction getFromDirection() {
        return fromDirection;
    }

    public Direction getTargetDirection() {
        return targetDirection;
    }

    public void setTargetDirection(Direction targetDirection) {
        this.targetDirection = targetDirection;
    }

    public short getAge() {
        return age;
    }

}
