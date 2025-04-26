package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandardPipeEntity extends AbstractPipeEntity {

    public StandardPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.WOODEN_PIPE_ENTITY.get(), pos, state);
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
        } else if (this.level != null) {
            int count = item.getStack().getCount();
            RandomSource random = this.level.getRandom();
            if (count == 1) {
                item.setTargetDirection(validDirections.get(random.nextInt(validDirections.size())));
                item.setEjecting(false);
            } else {
                Map<Direction, Integer> map = new HashMap<>();
                validDirections.forEach(direction -> map.put(direction, 0));
                for (int i = 0; i < count; i++) {
                    Direction randomDirection = validDirections.get(random.nextInt(validDirections.size()));
                    map.put(randomDirection, map.get(randomDirection) + 1);
                }
                boolean inputRouted = false;
                for (Direction direction : validDirections) {
                    int randomCount = map.get(direction);
                    if (randomCount > 0) {
                        if (!inputRouted) {
                            inputRouted = true;
                            item.setStack(item.getStack().copyWithCount(randomCount));
                            item.setTargetDirection(direction);
                            item.setEjecting(false);
                        } else {
                            this.queued.add(new ItemInPipe(item.getStack().copyWithCount(randomCount), item.getSpeed(), item.getProgress(), item.getFromDirection(), direction, false));
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
        return 1;
    }

}
