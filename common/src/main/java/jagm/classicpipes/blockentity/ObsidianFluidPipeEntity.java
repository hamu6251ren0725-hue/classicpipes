package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.util.FluidInPipe;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;

public class ObsidianFluidPipeEntity extends FluidPipeEntity {

    private static final int MIN_GURGLE_DELAY = 80;
    private static final int MAX_GURGLE_DELAY = 160;

    private long lastGurgle;

    public ObsidianFluidPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.OBSIDIAN_FLUID_PIPE_ENTITY, pos, state);
        this.lastGurgle = 0;
    }

    @Override
    public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        super.tickServer(level, pos, state);
        if (!this.isEmpty()) {
            boolean sendBlockUpdate = false;
            Iterator<FluidInPipe> iterator = this.contents.listIterator();
            while (iterator.hasNext()) {
                FluidInPipe fluidPacket = iterator.next();
                if (fluidPacket.getProgress() >= ItemInPipe.HALFWAY) {
                    iterator.remove();
                    sendBlockUpdate = true;
                    long timeSinceLastGurgle = level.getGameTime() - this.lastGurgle;
                    if (timeSinceLastGurgle > MAX_GURGLE_DELAY || (timeSinceLastGurgle > MIN_GURGLE_DELAY && level.getRandom().nextInt(10) == 0)) {
                        level.playSound(null, pos, ClassicPipes.OBSIDIAN_FLUID_PIPE_GURGLE, SoundSource.BLOCKS);
                        this.lastGurgle = level.getGameTime();
                    }
                }
            }
            if (sendBlockUpdate) {
                level.sendBlockUpdated(pos, state, state, 2);
            }
        }
    }

    @Override
    public void routePacket(BlockState state, FluidInPipe fluidPacket) {
        fluidPacket.setTargetDirection(fluidPacket.getFromDirection());
    }

}
