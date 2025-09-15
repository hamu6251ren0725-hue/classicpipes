package jagm.classicpipes.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public abstract class BooleanDirectionsPipeBlock extends PipeBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST, Direction.UP, UP, Direction.DOWN, DOWN)));

    public BooleanDirectionsPipeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
        );
    }

    @Override
    public boolean isPipeConnected(BlockState state, Direction direction) {
        return state.getValue(PROPERTY_BY_DIRECTION.get(direction));
    }

    @Override
    public BlockState setPipeConnected(BlockState state, Direction direction, boolean connected) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(direction), connected);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState superState = super.getStateForPlacement(context);
        if (superState != null) {
            return superState
                    .trySetValue(NORTH, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.NORTH))
                    .trySetValue(EAST, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.EAST))
                    .trySetValue(SOUTH, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.SOUTH))
                    .trySetValue(WEST, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.WEST))
                    .trySetValue(UP, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.UP))
                    .trySetValue(DOWN, this.canConnect(context.getLevel(), context.getClickedPos(), Direction.DOWN));
        }
        return this.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

}
