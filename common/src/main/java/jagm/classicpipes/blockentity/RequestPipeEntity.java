package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RequestPipeEntity extends LogisticalPipeEntity {

    public RequestPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.REQUEST_PIPE_ENTITY, pos, state);
    }

}
