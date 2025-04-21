package jagm.classicpipes.util;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class ItemInPipe {

    public static final int PIPE_LENGTH = 64;
    public static final int HALFWAY = PIPE_LENGTH / 2;
    public static final int DEFAULT_SPEED = 2;

    private ItemStack stack;
    private int speed;
    private int progress;
    private Direction fromDirection;
    private Direction targetDirection;
    private boolean ejecting;
    private boolean destroyed;

    public ItemInPipe(ItemStack stack, int speed, int progress, Direction fromDirection, Direction targetDirection, boolean ejecting){
        this.stack = stack;
        this.speed = Math.min(speed, HALFWAY);
        this.progress = progress;
        this.fromDirection = fromDirection;
        this.targetDirection = targetDirection;
        this.ejecting = ejecting;
        this.destroyed = false;
    }

    public ItemInPipe(ItemStack stack, Direction fromDirection, Direction toDirection){
        this(stack, fromDirection, toDirection, false);
    }

    public ItemInPipe(ItemStack stack, Direction fromDirection, Direction toDirection, boolean ejecting){
        this(stack, DEFAULT_SPEED, 0, fromDirection, toDirection, ejecting);
    }

    public void move(){
        this.progress += this.speed;
    }

    public void drop(ServerLevel level, BlockPos pos){
        if(!this.stack.isEmpty()){
            ClassicPipes.LOGGER.info("Dropping {}x {}!", this.getStack().getCount(), this.getStack().getDisplayName().getString());
            Direction currentDirection = this.progress < HALFWAY ? fromDirection : targetDirection;
            int absolutePosition = HALFWAY + Math.abs(HALFWAY - this.progress);
            float xOffset = currentDirection.equals(Direction.EAST) ? absolutePosition : (currentDirection.equals(Direction.WEST) ? -absolutePosition : HALFWAY) / (float) PIPE_LENGTH;
            float yOffset = currentDirection.equals(Direction.UP) ? absolutePosition : (currentDirection.equals(Direction.DOWN) ? -absolutePosition : HALFWAY) / (float) PIPE_LENGTH;
            float zOffset = currentDirection.equals(Direction.SOUTH) ? absolutePosition : (currentDirection.equals(Direction.NORTH) ? -absolutePosition : HALFWAY) / (float) PIPE_LENGTH;
            ItemEntity droppedItem = new ItemEntity(level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, this.stack);
            droppedItem.setDefaultPickUpDelay();
            level.addFreshEntity(droppedItem);
        }
    }

    public ItemStack getStack(){
        return this.stack;
    }

    public void setStack(ItemStack stack){
        this.stack = stack;
    }

    public int getProgress(){
        return this.progress;
    }

    public void resetProgress(){
        this.progress -= PIPE_LENGTH;
    }

    public void eject(){
        this.ejecting = true;
    }

    public void uneject(){
        this.ejecting = false;
    }

    public boolean isEjecting(){
        return ejecting;
    }

    public Direction getFromDirection(){
        return fromDirection;
    }

    public Direction getTargetDirection(){
        return targetDirection;
    }

    public void setFromDirection(Direction direction){
        this.fromDirection = direction;
    }

    public void setTargetDirection(Direction direction){
        this.targetDirection = direction;
    }

    public void destroy(){
        this.destroyed = true;
    }

    public boolean isDestroyed(){
        return this.destroyed;
    }

    public Tag save(HolderLookup.Provider levelRegistry){
        CompoundTag tag = new CompoundTag();
        tag.put("item", this.stack.save(levelRegistry));
        tag.putByte("speed", (byte) this.speed);
        tag.putByte("progress", (byte) this.progress);
        tag.putByte("from_direction", (byte) this.fromDirection.get3DDataValue());
        tag.putByte("target_direction", (byte) this.targetDirection.get3DDataValue());
        tag.putBoolean("ejecting", this.ejecting);
        return tag;
    }

    public static ItemInPipe parse(CompoundTag tag, HolderLookup.Provider levelRegistry){
        return ItemStack.parse(levelRegistry, tag.getCompoundOrEmpty("item")).map(stack -> new ItemInPipe(
                stack,
                tag.getByteOr("speed", (byte) DEFAULT_SPEED),
                tag.getByteOr("progress", (byte) (PIPE_LENGTH / 2)),
                Direction.from3DDataValue(tag.getByteOr("from_direction", (byte) 0)),
                Direction.from3DDataValue(tag.getByteOr("target_direction", (byte) 0)).getOpposite(),
                tag.getBooleanOr("ejecting", true)
        )).orElse(null);
    }

}
