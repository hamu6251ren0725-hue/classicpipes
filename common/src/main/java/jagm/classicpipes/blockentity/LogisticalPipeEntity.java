package jagm.classicpipes.blockentity;

import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

public abstract class LogisticalPipeEntity extends RoundRobinPipeEntity {

    private final Map<ItemStack, Direction> routingSchedule;

    public LogisticalPipeEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.routingSchedule = new HashMap<>();
    }

    @Override
    public void routeItem(BlockState state, ItemInPipe item) {
        Iterator<ItemStack> iterator = routingSchedule.keySet().iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if (ItemStack.isSameItemSameComponents(stack, item.getStack())) {
                item.setEjecting(false);
                item.setTargetDirection(routingSchedule.get(stack));
                iterator.remove();
                return;
            }
        }
        super.routeItem(state, item);
    }

    public void schedule(ItemStack stack, Direction direction) {
        routingSchedule.put(stack, direction);
    }

    public void schedulePath(ServerLevel level, ItemStack stack, LogisticalPipeEntity target) {
        Map<LogisticalPipeEntity, Tuple<LogisticalPipeEntity, Direction>> cameFrom = new HashMap<>();
        Map<LogisticalPipeEntity, Integer> gScore = new HashMap<>();
        gScore.put(this, 0);
        Map<LogisticalPipeEntity, Integer> fScore = new HashMap<>();
        fScore.put(this, 0);
        PriorityQueue<LogisticalPipeEntity> openSet = new PriorityQueue<>(Comparator.comparingInt(fScore::get));
        openSet.add(this);
        while (!openSet.isEmpty()) {
            LogisticalPipeEntity current = openSet.poll();
            if (current == target) {
                while (cameFrom.containsKey(current)) {
                    Tuple<LogisticalPipeEntity, Direction> tuple = cameFrom.get(current);
                    tuple.getA().schedule(stack, tuple.getB());
                    current = tuple.getA();
                }
            }
            for (Direction side : current.logistics.keySet()) {
                Tuple<BlockPos, Integer> tuple = current.logistics.get(side);
                if (level.getBlockEntity(tuple.getA()) instanceof LogisticalPipeEntity neighbour) {
                    int newScore = gScore.get(current) + tuple.getB();
                    if (newScore < gScore.get(neighbour)) {
                        cameFrom.put(neighbour, new Tuple<>(current, side));
                        gScore.put(neighbour, newScore);
                        fScore.put(neighbour, newScore + this.worldPosition.distChessboard(neighbour.worldPosition));
                        if (!openSet.contains(neighbour)) {
                            openSet.add(neighbour);
                        }
                    }
                }
            }
        }
    }

    @Override
    public short getTargetSpeed() {
        return ItemInPipe.SPEED_LIMIT;
    }

    @Override
    public short getAcceleration() {
        return ItemInPipe.DEFAULT_ACCELERATION * 64;
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        // TODO
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        // TODO
    }

}
