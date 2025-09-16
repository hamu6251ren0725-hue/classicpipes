package jagm.classicpipes.block;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.ObsidianFluidPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ObsidianFluidPipeBlock extends FluidPipeBlock {

    public ObsidianFluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ObsidianFluidPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.OBSIDIAN_FLUID_PIPE_ENTITY ? ObsidianFluidPipeEntity::tick : null;
    }

    @Override
    protected boolean canConnectToPipe(PipeBlock pipeBlock){
        return super.canConnectToPipe(pipeBlock) && pipeBlock != this;
    }

}
