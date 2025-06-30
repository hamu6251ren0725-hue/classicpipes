package jagm.classicpipes.block;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.blockentity.DiamondPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DiamondPipeBlock extends AbstractPipeBlock {

    public DiamondPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiamondPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.DIAMOND_PIPE_ENTITY ? DiamondPipeEntity::tick : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof DiamondPipeEntity diamondPipe) {
            player.openMenu(diamondPipe);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            if (blockEntity instanceof DiamondPipeEntity pipe) {
                pipe.getFilter().dropAllItems(serverLevel, pos);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

}
