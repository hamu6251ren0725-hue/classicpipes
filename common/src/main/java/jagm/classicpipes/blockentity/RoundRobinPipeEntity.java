package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.util.ItemInPipe;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundRobinPipeEntity extends AbstractPipeEntity {

    protected Direction nextDirection;

    public RoundRobinPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.WOODEN_PIPE_ENTITY, pos, state);
        this.nextDirection = Direction.DOWN;
    }

    public RoundRobinPipeEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.nextDirection = Direction.DOWN;
    }

    protected void updateRoundRobin(List<Direction> validDirections) {
        if (!validDirections.isEmpty()) {
            do {
                this.nextDirection = MiscUtil.nextDirection(this.nextDirection);
            } while (!validDirections.contains(this.nextDirection));
        }
    }

    @Override
    public void routeItem(BlockState state, ItemInPipe item) {
        List<Direction> validDirections = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (this.isPipeConnected(state, direction) && !direction.equals(item.getFromDirection())) {
                validDirections.add(direction);
            }
        }
        if (validDirections.isEmpty()) {
            item.setTargetDirection(item.getFromDirection().getOpposite());
            item.setEjecting(true);
        } else if (validDirections.size() == 1) {
            item.setTargetDirection(validDirections.getFirst());
            item.setEjecting(false);
        } else if (this.nextDirection != null) {
            if (!validDirections.contains(this.nextDirection)) {
                this.updateRoundRobin(validDirections);
            }
            int count = item.getStack().getCount();
            if (count == 1) {
                item.setTargetDirection(this.nextDirection);
                item.setEjecting(false);
                this.updateRoundRobin(validDirections);
            } else {
                Map<Direction, Integer> routeMap = new HashMap<>();
                validDirections.forEach(direction -> routeMap.put(direction, 0));
                for (int i = 0; i < count; i++) {
                    routeMap.put(this.nextDirection, routeMap.get(this.nextDirection) + 1);
                    this.updateRoundRobin(validDirections);
                }
                boolean inputRouted = false;
                for (Direction direction : validDirections) {
                    int routeCount = routeMap.get(direction);
                    if (routeCount > 0) {
                        if (!inputRouted) {
                            inputRouted = true;
                            item.setStack(item.getStack().copyWithCount(routeCount));
                            item.setTargetDirection(direction);
                            item.setEjecting(false);
                        } else {
                            this.queued.add(new ItemInPipe(item.getStack().copyWithCount(routeCount), item.getSpeed(), item.getProgress(), item.getFromDirection(), direction, false));
                        }
                    }
                }
            }
        }
    }

    public int getTargetSpeed() {
        return ItemInPipe.DEFAULT_SPEED;
    }

    public int getAcceleration() {
        return ItemInPipe.DEFAULT_ACCELERATION;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        super.loadAdditional(tag, levelRegistry);
        this.nextDirection = Direction.from3DDataValue(tag.getByteOr("next_direction", (byte) 0));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        super.saveAdditional(tag, levelRegistry);
        tag.putByte("next_direction", (byte) this.nextDirection.get3DDataValue());
    }

}
