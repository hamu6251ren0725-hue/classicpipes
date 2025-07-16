package jagm.classicpipes.services;

import io.netty.buffer.ByteBuf;
import jagm.classicpipes.blockentity.AbstractPipeEntity;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;

public interface BlockEntityHelper {

    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> blockEntitySupplier, Block... validBlocks);

    <T extends AbstractContainerMenu, D> MenuType<T> createMenuType(TriFunction<Integer, Inventory, D, T> menuSupplier, StreamCodec<ByteBuf, D> codec);

    <D> void openMenu(ServerPlayer player, MenuProvider menuProvider, D payload, StreamCodec<ByteBuf, D> codec);

    boolean canAccessContainer(Level level, BlockPos containerPos, Direction face);

    boolean handleItemInsertion(AbstractPipeEntity pipe, ServerLevel level, BlockPos pipePos, BlockState pipeState, ItemInPipe item);

    boolean handleItemExtraction(AbstractPipeEntity pipe, BlockState pipeState, ServerLevel level, BlockPos containerPos, Direction face, int amount);

}
