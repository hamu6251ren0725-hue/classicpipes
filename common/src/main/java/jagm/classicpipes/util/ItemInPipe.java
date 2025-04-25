package jagm.classicpipes.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemInPipe {

    public static final int PIPE_LENGTH = 64;
    public static final int HALFWAY = PIPE_LENGTH / 2;
    public static final int DEFAULT_SPEED = 2;
    public static final int SPEED_LIMIT = HALFWAY;

    private ItemStack stack;
    private int speed;
    private int progress;
    private Direction fromDirection;
    private Direction targetDirection;
    private boolean ejecting;

    public ItemInPipe(ItemStack stack, int speed, int progress, Direction fromDirection, Direction targetDirection, boolean ejecting) {
        this.stack = stack;
        this.speed = Math.min(speed, HALFWAY);
        this.progress = progress;
        this.fromDirection = fromDirection;
        this.targetDirection = targetDirection;
        this.ejecting = ejecting;
    }

    public ItemInPipe(ItemStack stack, Direction fromDirection, Direction toDirection) {
        this(stack, fromDirection, toDirection, false);
    }

    public ItemInPipe(ItemStack stack, Direction fromDirection, Direction toDirection, boolean ejecting) {
        this(stack, DEFAULT_SPEED, 0, fromDirection, toDirection, ejecting);
    }

    public void move(int targetSpeed, int acceleration) {
        if (this.speed < targetSpeed) {
            this.speed = Math.min(this.speed + acceleration, Math.min(targetSpeed, SPEED_LIMIT));
        } else if (this.speed > targetSpeed) {
            this.speed = Math.max(this.speed - acceleration, Math.max(targetSpeed, 1));
        }
        this.progress += this.speed;
    }

    public void drop(ServerLevel level, BlockPos pos) {
        if (!this.stack.isEmpty()) {
            Vec3 offset = this.getRenderPosition(0.0F);
            ItemEntity droppedItem = new ItemEntity(level, pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z, this.stack);
            droppedItem.setDefaultPickUpDelay();
            level.addFreshEntity(droppedItem);
        }
    }

    public Vec3 getRenderPosition(float partialTicks){
        float p = partialTicks * (float) this.speed / PIPE_LENGTH + (float) this.progress / PIPE_LENGTH;
        if (this.progress < HALFWAY) {
            return new Vec3(
                    this.fromDirection == Direction.WEST ? p : (this.fromDirection == Direction.EAST ? 1.0F - p : 0.5F),
                    this.fromDirection == Direction.DOWN ? p : (this.fromDirection == Direction.UP ? 1.0F - p : 0.5F),
                    this.fromDirection == Direction.NORTH ? p : (this.fromDirection == Direction.SOUTH ? 1.0F - p : 0.5F)
            );
        } else {
            return new Vec3(
                    this.targetDirection == Direction.EAST ? p : (this.targetDirection == Direction.WEST ? 1.0F - p : 0.5F),
                    this.targetDirection == Direction.UP ? p : (this.targetDirection == Direction.DOWN ? 1.0F - p : 0.5F),
                    this.targetDirection == Direction.SOUTH ? p : (this.targetDirection == Direction.NORTH ? 1.0F - p : 0.5F)
            );
        }
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public int getProgress() {
        return this.progress;
    }

    public void resetProgress(Direction direction) {
        this.progress -= PIPE_LENGTH;
        this.fromDirection = direction;
    }

    public boolean isEjecting() {
        return this.ejecting;
    }

    public void setEjecting(boolean ejecting) {
        this.ejecting = ejecting;
    }

    public Direction getFromDirection() {
        return fromDirection;
    }

    public Direction getTargetDirection() {
        return targetDirection;
    }

    public void setFromDirection(Direction direction) {
        this.fromDirection = direction;
    }

    public void setTargetDirection(Direction direction) {
        this.targetDirection = direction;
    }

    public int getSpeed() {
        return this.speed;
    }

    public Tag save(HolderLookup.Provider levelRegistry) {
        CompoundTag tag = new CompoundTag();
        tag.put("item", this.stack.save(levelRegistry));
        tag.putByte("speed", (byte) this.speed);
        tag.putByte("progress", (byte) this.progress);
        tag.putByte("from_direction", (byte) this.fromDirection.get3DDataValue());
        tag.putByte("target_direction", (byte) this.targetDirection.get3DDataValue());
        tag.putBoolean("ejecting", this.ejecting);
        return tag;
    }

    public static ItemInPipe parse(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        ItemStack stack = ItemStack.parse(levelRegistry, tag.getCompoundOrEmpty("item")).orElse(ItemStack.EMPTY);
        return new ItemInPipe(
                stack,
                tag.getByteOr("speed", (byte) DEFAULT_SPEED),
                tag.getByteOr("progress", (byte) HALFWAY),
                Direction.from3DDataValue(tag.getByteOr("from_direction", (byte) 0)),
                Direction.from3DDataValue(tag.getByteOr("target_direction", (byte) 0)),
                tag.getBooleanOr("ejecting", true)
        );
    }

}
