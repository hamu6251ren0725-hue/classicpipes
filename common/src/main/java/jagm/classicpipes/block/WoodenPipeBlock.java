package jagm.classicpipes.block;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.blockentity.StandardPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WoodenPipeBlock extends TransportPipeBlock {

    public WoodenPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StandardPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.WOODEN_PIPE_ENTITY.get() ? StandardPipeEntity::tick : null;
    }

    @Override
    protected boolean canConnectToPipe(AbstractPipeEntity pipe){
        Block block = pipe.getBlockState().getBlock();
        return !(block instanceof WoodenPipeBlock) || block == this;
    }

}
