package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CraftingPipeEntity extends NetworkedPipeEntity {

    public CraftingPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.CRAFTING_PIPE_ENTITY, pos, state);
    }

}
