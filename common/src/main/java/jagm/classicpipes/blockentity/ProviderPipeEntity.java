package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ProviderPipeEntity extends LogisticalPipeEntity {

    public ProviderPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.PROVIDER_PIPE_ENTITY, pos, state);
    }

}
