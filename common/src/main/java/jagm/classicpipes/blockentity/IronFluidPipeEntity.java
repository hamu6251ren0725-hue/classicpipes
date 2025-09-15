package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.IronFluidPipeBlock;
import jagm.classicpipes.util.FluidInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class IronFluidPipeEntity extends FluidPipeEntity {

    public IronFluidPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.IRON_FLUID_PIPE_ENTITY, pos, state);
    }

    @Override
    public void routePacket(BlockState state, FluidInPipe fluidPacket) {
        fluidPacket.setTargetDirection(state.getValue(IronFluidPipeBlock.FACING));
    }

}
