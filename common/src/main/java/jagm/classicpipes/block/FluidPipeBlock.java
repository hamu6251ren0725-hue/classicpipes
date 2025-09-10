package jagm.classicpipes.block;

import jagm.classicpipes.services.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FluidPipeBlock extends BooleanDirectionsPipeBlock {

    public FluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canConnect(Level level, BlockPos pipePos, Direction direction) {
        BlockPos neighbourPos = pipePos.relative(direction);
        if (level.getBlockState(neighbourPos).getBlock() instanceof PipeBlock pipeBlock) {
            return canConnectToPipeBothWays(this, pipeBlock);
        }
        return Services.LOADER_SERVICE.canAccessFluidContainer(level, neighbourPos, direction.getOpposite());
    }

    @Override
    protected boolean canConnectToPipe(PipeBlock pipeBlock){
        return pipeBlock instanceof FluidPipeBlock;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        // TODO
        return 0;
    }

}
