package jagm.classicpipes.mixin;

import jagm.classicpipes.block.ProviderPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
@SuppressWarnings("unused")
public abstract class ProviderPipeUpdaterMixin {

    @Inject(at = @At("HEAD"), method = "updateNeighbourForOutputSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V")
    @SuppressWarnings("DataFlowIssue")
    public void injectProviderPipeUpdater(BlockPos pos, Block block, CallbackInfo info) {
        if ((Level) (Object) this instanceof ServerLevel level) {
            for (Direction direction : Direction.values()) {
                BlockPos nextPos = pos.relative(direction);
                if (level.hasChunk(SectionPos.blockToSectionCoord(nextPos.getX()), SectionPos.blockToSectionCoord(nextPos.getZ()))) {
                    BlockState state = level.getBlockState(nextPos);
                    if (state.getBlock() instanceof ProviderPipeBlock pipeBlock) {
                        pipeBlock.onNeighborChange(state, level, nextPos, pos);
                    }
                }
            }
        }
    }

}
