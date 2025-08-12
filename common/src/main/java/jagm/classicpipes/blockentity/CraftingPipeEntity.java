package jagm.classicpipes.blockentity;

import com.mojang.serialization.Codec;
import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.NetworkedPipeBlock;
import jagm.classicpipes.inventory.container.FilterContainer;
import jagm.classicpipes.inventory.menu.CraftingPipeMenu;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CraftingPipeEntity extends NetworkedPipeEntity implements MenuProvider {

    private final FilterContainer filter;
    private final Direction[] slotDirections;
    private final NonNullList<ItemStack> heldItems;

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
        this.heldItems = NonNullList.withSize(9, ItemStack.EMPTY);
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
    public void eject(ServerLevel level, BlockPos pos, ItemInPipe item) {
        List<Integer> matchingSlots = new ArrayList<>();
        for (int slot = 0; slot < 9; slot++) {
            if (ItemStack.isSameItemSameComponents(this.filter.getItem(slot), item.getStack())) {
                matchingSlots.add(slot);
            }
        }
        ItemStack stack = item.getStack().copy();
        while (!stack.isEmpty()) {
            int minSlot = matchingSlots.getFirst();
            int minCount = this.heldItems.get(minSlot).getCount();
            for (int slot : matchingSlots) {
                int slotCount = this.heldItems.get(slot).getCount();
                if (slotCount < minCount) {
                    minSlot = slot;
                    minCount = slotCount;
                }
            }
            this.heldItems.set(minSlot, stack.copyWithCount(minCount + 1));
            stack.shrink(1);
        }
        boolean readyToCraft = true;
        while (readyToCraft) {
            for (int slot = 0; slot < 9; slot++) {
                if (this.heldItems.get(slot).getCount() < this.filter.getItem(slot).getCount()) {
                    readyToCraft = false;
                }
            }
            if (readyToCraft) {
                for (int slot = 0; slot < 9; slot++) {
                    ItemStack ingredient = this.filter.getItem(slot);
                    if (!ingredient.isEmpty()) {
                        this.heldItems.get(slot).shrink(ingredient.getCount());
                        this.queued.add(new ItemInPipe(
                                ingredient.copy(),
                                ItemInPipe.DEFAULT_SPEED,
                                ItemInPipe.HALFWAY,
                                Direction.DOWN,
                                this.slotDirections[slot],
                                false,
                                (short) 0
                        ));
                    }
                }
            }
        }
    }

    public ItemStack getResult() {
        return this.filter.getItem(9);
    }

    public List<ItemStack> getIngredients() {
        List<ItemStack> ingredients = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack ingredient = this.filter.getItem(i);
            if (!ingredient.isEmpty()) {
                ingredients.add(this.filter.getItem(i));
            }
        }
        return ingredients;
    }

    public void dropHeldItems(ServerLevel serverLevel, BlockPos pos) {
        for (ItemStack stack : this.heldItems) {
            if (!stack.isEmpty()) {
                ItemEntity droppedItem = new ItemEntity(serverLevel, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, stack);
                droppedItem.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(droppedItem);
            }
        }
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
        this.heldItems.clear();
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
        ValueInput.TypedInputList<ItemStackWithSlot> heldItemList = valueInput.listOrEmpty("held_items", ItemStackWithSlot.CODEC);
        for (ItemStackWithSlot slotStack : heldItemList) {
            if (slotStack.slot() >= 0 && slotStack.slot() < 9) {
                this.heldItems.set(slotStack.slot(), slotStack.stack());
            }
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
        ValueOutput.TypedOutputList<ItemStackWithSlot> heldItemList = valueOutput.list("held_items", ItemStackWithSlot.CODEC);
        for (int slot = 0; slot < this.heldItems.size(); slot++) {
            ItemStack stack = this.heldItems.get(slot);
            if (!stack.isEmpty()) {
                heldItemList.add(new ItemStackWithSlot(slot, stack));
            }
        }
    }

}
