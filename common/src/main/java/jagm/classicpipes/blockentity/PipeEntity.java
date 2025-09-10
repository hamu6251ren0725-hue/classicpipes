package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;

public abstract class PipeEntity extends BlockEntity {

    public PipeEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof PipeEntity pipe) {
            if (level instanceof ServerLevel serverLevel) {
                pipe.tickServer(serverLevel, pos, state);
            } else {
                pipe.tickClient(level, pos);
            }
        }
    }

    public abstract void tickServer(ServerLevel level, BlockPos pos, BlockState state);

    public abstract void tickClient(Level level, BlockPos pos);

    public abstract void update(ServerLevel level, BlockState state, BlockPos pos, Direction direction, boolean wasConnected);

    public abstract int getComparatorOutput();

    public abstract short getTargetSpeed();

    public abstract short getAcceleration();

    protected final boolean isPipeConnected(BlockState state, Direction direction) {
        if (state.getBlock() instanceof PipeBlock pipeBlock) {
            return pipeBlock.isPipeConnected(state, direction);
        }
        return false;
    }

    protected final int countConnections(BlockState state) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (isPipeConnected(state, direction)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider levelRegistry) {
        ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), ClassicPipes.LOGGER);
        CompoundTag tag;
        try {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, levelRegistry);
            this.saveAdditional(valueOutput);
            tag = valueOutput.buildResult();
        } catch (Throwable error) {
            try {
                scopedCollector.close();
            } catch (Throwable error2) {
                error.addSuppressed(error2);
            }
            throw error;
        }
        scopedCollector.close();
        return tag;
    }

}
