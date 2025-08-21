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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
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
    private boolean waitingForCraft;

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
    public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        super.tickServer(level, pos, state);
        BlockPos crafterPos = pos.relative(this.slotDirections[9]);
        if (this.waitingForCraft && this.isEmpty() && level.getBlockEntity(crafterPos) instanceof CrafterBlockEntity crafter) {
            level.scheduleTick(crafterPos, crafter.getBlockState().getBlock(), 0);
            level.playSound(null, crafterPos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS);
        } else if (!this.queued.isEmpty()) {
            this.addQueuedItems(level, false);
        }
    }

    @Override
    public void update(ServerLevel level, BlockState state, BlockPos pos, Direction direction, boolean wasConnected) {
        super.update(level, state, pos, direction, wasConnected);
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
    }

    @Override
    public void eject(ServerLevel level, BlockPos pos, ItemInPipe item) {
        List<Integer> matchingSlots = new ArrayList<>();
        for (int slot = 0; slot < 9; slot++) {
            if (ItemStack.isSameItemSameComponents(this.filter.getItem(slot), item.getStack())) {
                matchingSlots.add(slot);
            }
        }
        if (!matchingSlots.isEmpty()) {
            ItemStack stack = item.getStack().copy();
            while (!stack.isEmpty()) {
                int minSlot = matchingSlots.getFirst();
                int minAmount = this.heldItems.get(minSlot).getCount();
                for (int slot : matchingSlots) {
                    int slotAmount = this.heldItems.get(slot).getCount();
                    if (slotAmount < minAmount) {
                        minSlot = slot;
                        minAmount = slotAmount;
                    }
                }
                this.heldItems.set(minSlot, stack.copyWithCount(this.heldItems.get(minSlot).getCount() + 1));
                stack.shrink(1);
            }
        } else {
            super.eject(level, pos, item);
        }
        attemptCraft();
        this.setChanged();
    }

    private void attemptCraft() {
        ClassicPipes.LOGGER.info("Attempting craft.");
        if (!this.waitingForCraft) {
            ClassicPipes.LOGGER.info("Not already awaiting craft.");
            boolean readyToCraft = true;
            for (int slot = 0; slot < 9; slot++) {
                ClassicPipes.LOGGER.info("Slot {}: {}x {} for {}x {}", slot, this.heldItems.get(slot).getCount(), this.heldItems.get(slot).getItemName().getString(), this.filter.getItem(slot).getCount(), this.filter.getItem(slot).getItemName().getString());
                if (this.heldItems.get(slot).getCount() < this.filter.getItem(slot).getCount()) {
                    readyToCraft = false;
                }
            }
            if (readyToCraft) {
                ClassicPipes.LOGGER.info("Ready to craft.");
                for (int slot = 0; slot < 9; slot++) {
                    ItemStack ingredient = this.filter.getItem(slot);
                    if (!ingredient.isEmpty()) {
                        ClassicPipes.LOGGER.info("Queueing ingredient.");
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
                this.waitingForCraft = true;
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

    public NonNullList<ItemStack> getHeldItems() {
        return heldItems;
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
    public void insertPipeItem(Level level, ItemInPipe item) {
        ItemStack stack = item.getStack();
        if (!stack.isEmpty() && this.waitingForCraft && item.getFromDirection().equals(this.slotDirections[9]) && ItemStack.isSameItemSameComponents(this.getResult(), stack)) {
            this.waitingForCraft = false;
            attemptCraft();
        }
        super.insertPipeItem(level, item);
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
        this.waitingForCraft = valueInput.getBooleanOr("waiting_for_craft", false);
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
        valueOutput.putBoolean("waiting_for_craft", this.waitingForCraft);
    }

}
