package jagm.classicpipes.block;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.LogisticalPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NetheritePipeBlock extends AbstractPipeBlock {

    public NetheritePipeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogisticalPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.LOGISTICAL_PIPE_ENTITY ? LogisticalPipeEntity::tick : null;
    }

}
