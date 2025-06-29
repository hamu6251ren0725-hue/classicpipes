package jagm.classicpipes.block;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.blockentity.LapisPipeEntity;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
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

public class LapisPipeBlock extends AbstractPipeBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;

    public LapisPipeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.DOWN).setValue(ATTACHED, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LapisPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == ClassicPipes.LAPIS_PIPE_ENTITY ? LapisPipeEntity::tick : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ATTACHED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            for (Direction direction : Direction.values()) {
                if (state.getValue(PROPERTY_BY_DIRECTION.get(direction)) && isContainerAt(context.getLevel(), context.getClickedPos().relative(direction), direction)) {
                    return state.setValue(FACING, direction).setValue(ATTACHED, true);
                }
            }
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockState superState = super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
        Direction d = superState.getValue(FACING);
        for (int i = 0; i < 6; i++) {
            if (superState.getValue(PROPERTY_BY_DIRECTION.get(d)) && isContainerAt((Level) level, pos.relative(d), d)) {
                return superState.setValue(FACING, d).setValue(ATTACHED, true);
            }
            d = MiscUtil.nextDirection(d);
        }
        return superState.setValue(ATTACHED, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.getAbilities().mayBuild) {
            Direction direction = MiscUtil.nextDirection(state.getValue(FACING));
            for (int i = 0; i < 5; i++) {
                if (state.getValue(PROPERTY_BY_DIRECTION.get(direction)) && isContainerAt(level, pos.relative(direction), direction)) {
                    BlockState newState = state.setValue(FACING, direction).setValue(ATTACHED, true);
                    level.setBlock(pos, newState, 3);
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

    private static boolean isContainerAt(Level level, BlockPos pos, Direction direction) {
        Container container = getBlockContainer(level, pos);
        if (container != null && !(container instanceof AbstractPipeEntity)) {
            if (container instanceof WorldlyContainer worldlyContainer) {
                return worldlyContainer.getSlotsForFace(direction.getOpposite()).length > 0;
            } else {
                return true;
            }
        }
        return false;
    }

}
