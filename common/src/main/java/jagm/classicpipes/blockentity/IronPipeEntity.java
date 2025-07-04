package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.IronPipeBlock;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class IronPipeEntity extends AbstractPipeEntity {

    public IronPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.IRON_PIPE_ENTITY, pos, state);
    }

    @Override
    public void routeItem(BlockState state, ItemInPipe item) {
        item.setEjecting(false);
        item.setTargetDirection(state.getValue(IronPipeBlock.ENABLED) ? state.getValue(IronPipeBlock.FACING_SECONDARY) : state.getValue(IronPipeBlock.FACING_PRIMARY));
    }

    @Override
    protected boolean canJoinLogisticalNetwork() {
        return false;
    }

    @Override
    public int getTargetSpeed() {
        return ItemInPipe.DEFAULT_SPEED;
    }

    @Override
    public int getAcceleration() {
        return ItemInPipe.DEFAULT_ACCELERATION;
    }

}
