package jagm.classicpipes.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.TransportPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
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

public abstract class TransportPipeBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST, Direction.UP, UP, Direction.DOWN, DOWN)));

    private final Function<BlockState, VoxelShape> shapes;

    public TransportPipeBlock(BlockBehaviour.Properties properties) {
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
        float apothem = 8.0F;
        Map<Direction, VoxelShape> map = Shapes.rotateAll(Block.boxZ(apothem, 0.0, 8.0));
        this.shapes = this.getShapeForEachState((state) -> {
            for (Map.Entry<Direction, BooleanProperty> entry : PROPERTY_BY_DIRECTION.entrySet()) {
                if (state.getValue(entry.getValue())) {
                    return Shapes.or(map.get(entry.getKey()), Block.cube(apothem));
                }
            }
            return Block.cube(apothem);
        });
    }

    protected boolean canConnect(Container container){
        if(container != null){
            if(container instanceof TransportPipeEntity pipeEntity){
                return this.canConnectToPipe(pipeEntity);
            }
            return true;
        }
        return false;
    }

    protected boolean canConnectToPipe(TransportPipeEntity pipeEntity){
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .trySetValue(NORTH, this.canConnect(HopperBlockEntity.getContainerAt(context.getLevel(), context.getClickedPos().north())))
                .trySetValue(EAST, this.canConnect(HopperBlockEntity.getContainerAt(context.getLevel(), context.getClickedPos().east())))
                .trySetValue(SOUTH, this.canConnect(HopperBlockEntity.getContainerAt(context.getLevel(), context.getClickedPos().south())))
                .trySetValue(WEST, this.canConnect(HopperBlockEntity.getContainerAt(context.getLevel(), context.getClickedPos().west())))
                .trySetValue(UP, this.canConnect(HopperBlockEntity.getContainerAt(context.getLevel(), context.getClickedPos().above())))
                .trySetValue(DOWN, this.canConnect(HopperBlockEntity.getContainerAt(context.getLevel(), context.getClickedPos().below())))
                .trySetValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(direction), this.canConnect(HopperBlockEntity.getContainerAt((Level) level, neighborPos)));
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

}
