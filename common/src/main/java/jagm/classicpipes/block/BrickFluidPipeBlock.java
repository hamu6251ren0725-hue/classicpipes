package jagm.classicpipes.block;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.FluidPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BrickFluidPipeBlock extends FluidPipeBlock {

    public BrickFluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.FLUID_PIPE_ENTITY ? FluidPipeEntity::tick : null;
    }

    @Override
    protected boolean canConnect(Level level, BlockPos pipePos, Direction direction) {
        if (level.getBlockState(pipePos.relative(direction)).getBlock() instanceof FluidPipeBlock pipeBlock) {
            return canConnectToPipeBothWays(this, pipeBlock);
        }
        return false;
    }

}
