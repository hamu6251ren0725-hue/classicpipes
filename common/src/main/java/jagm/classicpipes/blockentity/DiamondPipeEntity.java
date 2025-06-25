package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DiamondPipeEntity extends RoundRobinPipeEntity {

    public DiamondPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.DIAMOND_PIPE_ENTITY, pos, state);
    }

}
