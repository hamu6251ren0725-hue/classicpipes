package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class WoodenPipeEntity extends TransportPipeEntity {

    public WoodenPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.WOODEN_PIPE_ENTITY.get(), pos, state);
    }

}
