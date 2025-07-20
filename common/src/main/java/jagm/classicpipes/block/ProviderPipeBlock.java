package jagm.classicpipes.block;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.ProviderPipeEntity;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ProviderPipeBlock extends NetheritePipeBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;

    public ProviderPipeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.DOWN)
                .setValue(ATTACHED, false)
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProviderPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.PROVIDER_PIPE_ENTITY ? ProviderPipeEntity::tick : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ATTACHED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState superState = super.getStateForPlacement(context);
        if (superState != null) {
            for (Direction direction : Direction.values()) {
                if (superState.getValue(PROPERTY_BY_DIRECTION.get(direction)) && Services.LOADER_SERVICE.canAccessContainer(context.getLevel(), context.getClickedPos().relative(direction), direction.getOpposite())) {
                    return superState.trySetValue(FACING, direction).trySetValue(ATTACHED, true);
                }
            }
            return superState.trySetValue(ATTACHED, false);
        }
        return this.defaultBlockState();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pipePos, Direction initialDirection, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockState superState = super.updateShape(state, level, scheduledTickAccess, pipePos, initialDirection, neighborPos, neighborState, random);
        Direction direction = state.getValue(FACING);
        for (int i = 0; i < 6; i++) {
            if (superState.getValue(PROPERTY_BY_DIRECTION.get(direction)) && Services.LOADER_SERVICE.canAccessContainer((Level) level, pipePos.relative(direction), direction.getOpposite())) {
                return superState.setValue(FACING, direction).setValue(ATTACHED, true);
            }
            direction = MiscUtil.nextDirection(direction);
        }
        return superState.setValue(ATTACHED, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!MiscUtil.itemIsPipe(player.getMainHandItem())) {
            if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof ProviderPipeEntity providerPipe) {
                Services.LOADER_SERVICE.openMenu((ServerPlayer) player, providerPipe, providerPipe.shouldMatchComponents(), ByteBufCodecs.BOOL);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

}
