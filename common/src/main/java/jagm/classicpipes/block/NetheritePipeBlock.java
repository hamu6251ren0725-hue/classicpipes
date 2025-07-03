package jagm.classicpipes.block;

import com.google.common.collect.Maps;
import com.ibm.icu.impl.locale.XCldrStub;
import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.LogisticalPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public class NetheritePipeBlock extends AbstractPipeBlock {

    public static final BooleanProperty LINKED_NORTH = BooleanProperty.create("linked_north");
    public static final BooleanProperty LINKED_EAST = BooleanProperty.create("linked_east");
    public static final BooleanProperty LINKED_SOUTH = BooleanProperty.create("linked_south");
    public static final BooleanProperty LINKED_WEST = BooleanProperty.create("linked_west");
    public static final BooleanProperty LINKED_UP = BooleanProperty.create("linked_up");
    public static final BooleanProperty LINKED_DOWN = BooleanProperty.create("linked_down");
    public static final Map<Direction, BooleanProperty> LINKED_PROPERTY_BY_DIRECTION = XCldrStub.ImmutableMap.copyOf(Maps.newEnumMap(Map.of(Direction.NORTH, LINKED_NORTH, Direction.EAST, LINKED_EAST, Direction.SOUTH, LINKED_SOUTH, Direction.WEST, LINKED_WEST, Direction.UP, LINKED_UP, Direction.DOWN, LINKED_DOWN)));

    public NetheritePipeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(LINKED_NORTH, false)
                .setValue(LINKED_EAST, false)
                .setValue(LINKED_SOUTH, false)
                .setValue(LINKED_WEST, false)
                .setValue(LINKED_UP, false)
                .setValue(LINKED_DOWN, false)
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogisticalPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.LOGISTICAL_PIPE_ENTITY ? LogisticalPipeEntity::tick : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LINKED_NORTH, LINKED_EAST, LINKED_SOUTH, LINKED_WEST, LINKED_UP, LINKED_DOWN);
    }

}
