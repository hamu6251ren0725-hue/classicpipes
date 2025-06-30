package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.LapisPipeBlock;
import jagm.classicpipes.util.ItemInPipe;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class LapisPipeEntity extends RoundRobinPipeEntity {

    private Direction entryDirection;

    public LapisPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.LAPIS_PIPE_ENTITY, pos, state);
        this.entryDirection = Direction.DOWN;
    }

    @Override
    public void insertPipeItem(Level level, ItemInPipe item) {
        this.entryDirection = item.getFromDirection();
        super.insertPipeItem(level, item);
    }

    @Override
    protected List<Direction> getValidDirections(BlockState state, ItemInPipe item) {
        List<Direction> validDirections = new ArrayList<>();
        Direction facing = state.getValue(LapisPipeBlock.FACING);
        boolean attached = state.getValue(LapisPipeBlock.ATTACHED);
        if (!item.getFromDirection().equals(facing) && this.isPipeConnected(state, facing) && attached) {
            validDirections.add(facing);
        } else {
            Direction direction = MiscUtil.nextDirection(item.getFromDirection());
            for (int i = 0; i < 5; i++) {
                if (this.isPipeConnected(state, direction) && !direction.equals(this.entryDirection)) {
                    validDirections.add(direction);
                }
                direction = MiscUtil.nextDirection(direction);
            }
            if (validDirections.isEmpty() && this.isPipeConnected(state, this.entryDirection) && attached) {
                validDirections.add(this.entryDirection);
            }
        }
        return validDirections;
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.entryDirection = Direction.from3DDataValue(valueInput.getByteOr("entry_direction", (byte) 0));
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putByte("entry_direction", (byte) this.entryDirection.get3DDataValue());
    }

}
