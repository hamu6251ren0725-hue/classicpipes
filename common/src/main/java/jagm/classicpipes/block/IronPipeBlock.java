package jagm.classicpipes.block;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.IronPipeEntity;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class IronPipeBlock extends AbstractPipeBlock {

    public static final EnumProperty<Direction> FACING_PRIMARY = EnumProperty.create("primary", Direction.class, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    public static final EnumProperty<Direction> FACING_SECONDARY = EnumProperty.create("secondary", Direction.class, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public IronPipeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING_PRIMARY, Direction.DOWN)
                .setValue(FACING_SECONDARY, Direction.UP)
                .setValue(ENABLED, false)
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IronPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.IRON_PIPE_ENTITY ? IronPipeEntity::tick : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING_PRIMARY, FACING_SECONDARY, ENABLED);
    }

    private static Direction getSecondaryDirection(Direction primaryDirection, BlockState state) {
        if (state.getValue(PROPERTY_BY_DIRECTION.get(primaryDirection.getOpposite()))) {
            return primaryDirection.getOpposite();
        } else {
            Direction direction = primaryDirection;
            for (int i = 0; i < 5; i++) {
                direction = MiscUtil.nextDirection(direction);
                if (state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                    return direction;
                }
            }
        }
        return primaryDirection;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState superState = super.getStateForPlacement(context);
        if (superState != null) {
            for (Direction direction : Direction.values()) {
                if (superState.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                    return superState.trySetValue(FACING_PRIMARY, direction).trySetValue(FACING_SECONDARY, getSecondaryDirection(direction, superState));
                }
            }
            return superState;
        }
        return this.defaultBlockState();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pipePos, Direction initialDirection, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockState superState = super.updateShape(state, level, scheduledTickAccess, pipePos, initialDirection, neighborPos, neighborState, random);
        Direction direction = superState.getValue(FACING_PRIMARY);
        for (int i = 0; i < 6; i++) {
            if (superState.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                return superState.setValue(FACING_PRIMARY, direction).setValue(FACING_SECONDARY, getSecondaryDirection(direction, superState));
            }
            direction = MiscUtil.nextDirection(direction);
        }
        return superState;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.getAbilities().mayBuild) {
            Direction direction = MiscUtil.nextDirection(state.getValue(FACING_PRIMARY));
            for (int i = 0; i < 5; i++) {
                if (state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                    level.setBlock(pos, state.setValue(FACING_PRIMARY, direction).setValue(FACING_SECONDARY, getSecondaryDirection(direction, state)), 3);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(null, pos, ClassicPipes.PIPE_ADJUST_SOUND, SoundSource.BLOCKS);
                    }
                    return InteractionResult.SUCCESS;
                }
                direction = MiscUtil.nextDirection(direction);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            this.checkPoweredState(level, pos, state);
        }
    }

    private void checkPoweredState(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(ENABLED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(ENABLED, false), 2);
        } else if (!state.getValue(ENABLED) && level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(ENABLED, true), 2);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean b) {
        this.checkPoweredState(level, pos, state);
    }

}
