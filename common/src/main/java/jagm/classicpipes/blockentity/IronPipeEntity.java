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
        item.setTargetDirection(state.getValue(IronPipeBlock.FACING));
    }

    @Override
    protected boolean canJoinNetwork() {
        return false;
    }

    @Override
    public short getTargetSpeed() {
        return ItemInPipe.DEFAULT_SPEED;
    }

    @Override
    public short getAcceleration() {
        return ItemInPipe.DEFAULT_ACCELERATION;
    }

}
