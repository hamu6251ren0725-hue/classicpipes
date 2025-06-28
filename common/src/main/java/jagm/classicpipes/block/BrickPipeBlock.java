package jagm.classicpipes.block;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.blockentity.RoundRobinPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BrickPipeBlock extends AbstractPipeBlock {

    public BrickPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RoundRobinPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.BASIC_PIPE_ENTITY ? RoundRobinPipeEntity::tick : null;
    }

    protected boolean canConnect(Container container, Direction direction) {
        if (container != null) {
            if (container instanceof AbstractPipeEntity pipe) {
                return this.canConnectToPipe(pipe);
            }
        }
        return false;
    }

}
