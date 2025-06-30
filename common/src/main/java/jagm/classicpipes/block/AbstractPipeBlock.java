package jagm.classicpipes.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.services.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractPipeBlock extends TransparentBlock implements SimpleWaterloggedBlock, EntityBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST, Direction.UP, UP, Direction.DOWN, DOWN)));

    private final Function<BlockState, VoxelShape> shapes;

    public AbstractPipeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(WATERLOGGED, false)
        );
        Map<Direction, VoxelShape> map = Shapes.rotateAll(Block.boxZ(8.0F, 0.0F, 8.0F));
        this.shapes = this.getShapeForEachState(state -> {
            VoxelShape shape = Block.cube(8.0F);
            for (Map.Entry<Direction, BooleanProperty> entry : PROPERTY_BY_DIRECTION.entrySet()) {
                if (state.getValue(entry.getValue())) {
                    shape = Shapes.or(map.get(entry.getKey()), shape);
                }
            }
            return shape;
        });
    }

    protected boolean canConnect(Level level, BlockPos pipePos, Direction direction) {
        BlockPos neighbourPos = pipePos.relative(direction);
        if (level.getBlockState(neighbourPos).getBlock() instanceof AbstractPipeBlock pipeBlock) {
            return this.canConnectToPipe(pipeBlock);
        }
        return Services.BLOCK_ENTITY_HELPER.canAccessContainer(level, neighbourPos, direction.getOpposite());
    }

    protected boolean canConnectToPipe(AbstractPipeBlock pipeBlock){
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .trySetValue(NORTH, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.NORTH))
                .trySetValue(EAST, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.EAST))
                .trySetValue(SOUTH, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.SOUTH))
                .trySetValue(WEST, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.WEST))
                .trySetValue(UP, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.UP))
                .trySetValue(DOWN, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.DOWN))
                .trySetValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pipePos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pipePos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        boolean wasConnected = state.getValue(PROPERTY_BY_DIRECTION.get(direction));
        boolean willConnect = this.canConnect((Level) level, pipePos, direction);
        BlockState newState = state.setValue(PROPERTY_BY_DIRECTION.get(direction), willConnect);
        if (wasConnected != willConnect && level.getBlockEntity(pipePos) instanceof AbstractPipeEntity pipe && level instanceof ServerLevel serverLevel) {
            pipe.update(serverLevel, newState, pipePos, direction, wasConnected);
        }
        return newState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.apply(state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            if (blockEntity instanceof AbstractPipeEntity pipe) {
                pipe.dropItems(serverLevel, pos);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractPipeEntity pipe) {
            return Math.min(15, pipe.getContents().size());
        }
        return 0;
    }

}
