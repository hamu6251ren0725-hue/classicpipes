package jagm.classicpipes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WoodenFluidPipeBlock extends FluidPipeBlock {

    public WoodenFluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
        //return new FluidPipeEntity(ClassicPipes.FLUID_PIPE_ENTITY, pos, state); // TODO loader-specific implementations
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return null;
        //return blockEntityType == ClassicPipes.FLUID_PIPE_ENTITY ? FluidPipeEntity::tick : null;  // TODO loader-specific implementations
    }

    @Override
    protected boolean canConnectToPipe(PipeBlock pipeBlock){
        return super.canConnectToPipe(pipeBlock) && (!(pipeBlock instanceof WoodenFluidPipeBlock) || pipeBlock == this);
    }

}
