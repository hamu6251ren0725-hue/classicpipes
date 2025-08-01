package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class StockingPipeEntity extends NetworkedPipeEntity {

    public StockingPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.STOCKING_PIPE_ENTITY, pos, state);
    }

}
