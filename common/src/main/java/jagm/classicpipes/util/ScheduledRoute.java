package jagm.classicpipes.util;

import net.minecraft.core.Direction;

public class ScheduledRoute {

    private static final short MAX_AGE = 1200;

    private final Direction direction;
    private short age;

    public ScheduledRoute(Direction direction) {
        this.direction = direction;
        this.age = 0;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean timedOut() {
        return age >= MAX_AGE;
    }

    public void tick() {
        this.age++;
    }

}
