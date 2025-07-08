package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.CopperPipeBlock;
import jagm.classicpipes.services.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CopperPipeEntity extends RoundRobinPipeEntity {

    private static final byte DEFAULT_COOLDOWN = 8;

    private byte cooldown;

    public CopperPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.COPPER_PIPE_ENTITY, pos, state);
        this.cooldown = DEFAULT_COOLDOWN;
    }

    @Override
    public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        super.tickServer(level, pos, state);
        if (state.getValue(CopperPipeBlock.ENABLED) && state.getValue(CopperPipeBlock.ATTACHED)) {
            if (this.cooldown-- <= 0) {
                Direction direction = state.getValue(CopperPipeBlock.FACING);
                if (Services.BLOCK_ENTITY_HELPER.handleItemExtraction(this, state, level, pos.relative(direction), direction.getOpposite(), 1)) {
                    level.sendBlockUpdated(pos, state, state, 2);
                    this.setChanged();
                }
                this.cooldown = DEFAULT_COOLDOWN;
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.cooldown = valueInput.getByteOr("cooldown", DEFAULT_COOLDOWN);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putByte("cooldown", this.cooldown);
    }

}
