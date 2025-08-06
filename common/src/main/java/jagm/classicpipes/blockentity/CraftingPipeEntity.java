package jagm.classicpipes.blockentity;

import com.mojang.serialization.Codec;
import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.NetworkedPipeBlock;
import jagm.classicpipes.inventory.container.FilterContainer;
import jagm.classicpipes.inventory.menu.CraftingPipeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Arrays;

public class CraftingPipeEntity extends NetworkedPipeEntity implements MenuProvider {

    private final FilterContainer filter;
    private final Direction[] slotDirections;

    public CraftingPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.CRAFTING_PIPE_ENTITY, pos, state);
        this.filter = new FilterContainer(this, 10, true);
        this.slotDirections = new Direction[10];
        Direction defaultDirection = Direction.DOWN;
        for (Direction validDirection : Direction.values()) {
            if (state.getValue(NetworkedPipeBlock.PROPERTY_BY_DIRECTION.get(validDirection)).equals(NetworkedPipeBlock.ConnectionState.UNLINKED)) {
                defaultDirection = validDirection;
                break;
            }
        }
        Arrays.fill(this.slotDirections, defaultDirection);
    }

    public Direction[] getSlotDirections() {
        return this.slotDirections;
    }

    public void setSlotDirection(int slot, Direction direction) {
        this.slotDirections[slot] = direction;
    }

    @Override
    public void update(ServerLevel level, BlockState state, BlockPos pos, Direction direction, boolean wasConnected) {
        Direction defaultDirection = Direction.DOWN;
        for (Direction validDirection : Direction.values()) {
            if (state.getValue(NetworkedPipeBlock.PROPERTY_BY_DIRECTION.get(validDirection)).equals(NetworkedPipeBlock.ConnectionState.UNLINKED)) {
                defaultDirection = validDirection;
                break;
            }
        }
        for (int i = 0; i < this.slotDirections.length; i++) {
            if (!state.getValue(NetworkedPipeBlock.PROPERTY_BY_DIRECTION.get(this.slotDirections[i])).equals(NetworkedPipeBlock.ConnectionState.UNLINKED)) {
                this.slotDirections[i] = defaultDirection;
            }
        }
        super.update(level, state, pos, direction, wasConnected);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container." + ClassicPipes.MOD_ID + ".crafting_pipe");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CraftingPipeMenu(id, playerInventory, this.filter, this.slotDirections, this.getBlockPos());
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        this.filter.clearContent();
        super.loadAdditional(valueInput);
        ValueInput.TypedInputList<Byte> directionsByteList = valueInput.listOrEmpty("slot_directions", Codec.BYTE);
        int i = 0;
        for (byte directionByte : directionsByteList) {
            if (i >= 10) {
                break;
            }
            this.slotDirections[i] = Direction.from3DDataValue(directionByte);
            i++;
        }
        ValueInput.TypedInputList<ItemStackWithSlot> filterList = valueInput.listOrEmpty("filter", ItemStackWithSlot.CODEC);
        for (ItemStackWithSlot slotStack : filterList) {
            this.filter.setItem(slotStack.slot(), slotStack.stack());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ValueOutput.TypedOutputList<Byte> directionsByteList = valueOutput.list("slot_directions", Codec.BYTE);
        for (Direction direction : this.slotDirections) {
            directionsByteList.add(direction == null ? (byte) 0 : (byte) direction.get3DDataValue());
        }
        ValueOutput.TypedOutputList<ItemStackWithSlot> filterList = valueOutput.list("filter", ItemStackWithSlot.CODEC);
        for (int slot = 0; slot < this.filter.getContainerSize(); slot++) {
            ItemStack stack = this.filter.getItem(slot);
            if (!stack.isEmpty()) {
                filterList.add(new ItemStackWithSlot(slot, stack));
            }
        }
    }

}
