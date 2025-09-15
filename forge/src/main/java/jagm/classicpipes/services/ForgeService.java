package jagm.classicpipes.services;

import jagm.classicpipes.blockentity.FluidPipeEntity;
import jagm.classicpipes.blockentity.ItemPipeEntity;
import jagm.classicpipes.client.renderer.FluidRenderInfo;
import jagm.classicpipes.network.ForgePacketHandler;
import jagm.classicpipes.util.FluidInPipe;
import jagm.classicpipes.util.ItemInPipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class ForgeService implements LoaderService {

    @Override
    public <B extends BlockEntity> BlockEntityType<B> createBlockEntityType(BiFunction<BlockPos, BlockState, B> blockEntitySupplier, Block... validBlocks) {
        return new BlockEntityType<>(blockEntitySupplier::apply, Set.of(validBlocks));
    }

    @Override
    public <M extends AbstractContainerMenu, D> MenuType<M> createMenuType(TriFunction<Integer, Inventory, D, M> menuSupplier, StreamCodec<RegistryFriendlyByteBuf, D> codec) {
        return IForgeMenuType.create((id, inventory, buffer) -> menuSupplier.apply(id, inventory, codec.decode(new RegistryFriendlyByteBuf(buffer, inventory.player.registryAccess()))));
    }

    @Override
    public <D> void openMenu(ServerPlayer player, MenuProvider menuProvider, D payload, StreamCodec<RegistryFriendlyByteBuf, D> codec) {
        player.openMenu(menuProvider, buffer -> codec.encode(new RegistryFriendlyByteBuf(buffer, player.registryAccess()), payload));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ForgePacketHandler.sendToServer(payload);
    }

    @Override
    public void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
        ForgePacketHandler.sendToClient(player, payload);
    }

    @Override
    public boolean canAccessContainer(Level level, BlockPos containerPos, Direction face) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof ItemPipeEntity) {
            return false;
        }
        if (blockEntity != null) {
            Optional<IItemHandler> itemHandlerOptional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, face).resolve();
            if (itemHandlerOptional.isPresent()) {
                return itemHandlerOptional.get().getSlots() > 0;
            }
        }
        // Forge transfer API does not wrap sided containers without block entities, e.g. composters, so they must be handled separately.
        BlockState state = level.getBlockState(containerPos);
        if (state.getBlock() instanceof WorldlyContainerHolder containerHolder) {
            return containerHolder.getContainer(state, level, containerPos).getSlotsForFace(face).length > 0;
        }
        return false;
    }

    @Override
    public boolean handleItemInsertion(ItemPipeEntity pipe, ServerLevel level, BlockPos pipePos, BlockState pipeState, ItemInPipe item) {
        BlockPos containerPos = pipePos.relative(item.getTargetDirection());
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof ItemPipeEntity nextPipe) {
            item.resetProgress(item.getTargetDirection().getOpposite());
            nextPipe.insertPipeItem(level, item);
            level.sendBlockUpdated(containerPos, nextPipe.getBlockState(), nextPipe.getBlockState(), 2);
            return true;
        }
        Direction face = item.getTargetDirection().getOpposite();
        BlockState state = level.getBlockState(containerPos);
        if (blockEntity != null) {
            Optional<IItemHandler> itemHandlerOptional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, face).resolve();
            if (itemHandlerOptional.isPresent()) {
                IItemHandler itemHandler = itemHandlerOptional.get();
                ItemStack stack = item.getStack();
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    stack = itemHandler.insertItem(slot, stack, false);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
                item.setStack(stack);
            }
        }
        // Forge transfer API does not wrap sided containers without block entities, e.g. composters, so they must be handled separately.
        if (state.getBlock() instanceof WorldlyContainerHolder containerHolder) {
            WorldlyContainer container = containerHolder.getContainer(state, level, containerPos);
            ItemStack stack = HopperBlockEntity.addItem(null, container, item.getStack(), face);
            if (stack.isEmpty()) {
                return true;
            }
            item.setStack(stack);
        }
        item.resetProgress(item.getTargetDirection());
        pipe.routeItem(pipeState, item);
        return false;
    }

    @Override
    public boolean handleItemExtraction(ItemPipeEntity pipe, BlockState pipeState, ServerLevel level, BlockPos containerPos, Direction face, int amount) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof ItemPipeEntity) {
            return false;
        }
        if (blockEntity != null) {
            Optional<IItemHandler> itemHandlerOptional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, face).resolve();
            if (itemHandlerOptional.isPresent()) {
                IItemHandler itemHandler = itemHandlerOptional.get();
                for (int slot = itemHandler.getSlots() - 1; slot >= 0; slot--) {
                    ItemStack stack = itemHandler.extractItem(slot, amount, false);
                    if (!stack.isEmpty()) {
                        pipe.setItem(face.getOpposite().get3DDataValue(), stack);
                        return true;
                    }
                }
            }
        }
        // Forge transfer API does not wrap worldly containers without block entities, e.g. composters, so they must be handled separately.
        BlockState state = level.getBlockState(containerPos);
        if (state.getBlock() instanceof WorldlyContainerHolder containerHolder) {
            WorldlyContainer container = containerHolder.getContainer(state, level, containerPos);
            int[] slots = container.getSlotsForFace(face);
            for (int i = slots.length - 1; i >= 0; i--) {
                int slot = slots[i];
                ItemStack stack = container.getItem(slot);
                if (!stack.isEmpty() && container.canTakeItemThroughFace(slot, stack, face)) {
                    int count = stack.getCount();
                    if (HopperBlockEntity.addItem(container, pipe, container.removeItem(slot, amount), face.getOpposite()).isEmpty()) {
                        container.setChanged();
                        return true;
                    } else {
                        stack.setCount(count);
                        if (count == 1) {
                            container.setItem(slot, stack);
                        }
                    }
                }
            }
        }
        return false;
    }

    public List<ItemStack> getContainerItems(ServerLevel level, BlockPos pos, Direction face) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            Optional<IItemHandler> itemHandlerOptional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, face).resolve();
            if (itemHandlerOptional.isPresent()) {
                IItemHandler itemHandler = itemHandlerOptional.get();
                List<ItemStack> stacks = new ArrayList<>();
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    ItemStack slotStack = itemHandler.getStackInSlot(slot);
                    if (slotStack.isEmpty()) {
                        continue;
                    }
                    boolean matched = false;
                    for (ItemStack stack : stacks) {
                        if (ItemStack.isSameItemSameComponents(stack, slotStack)) {
                            stack.setCount(stack.getCount() + slotStack.getCount());
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        stacks.add(slotStack.copy());
                    }
                }
                return stacks;
            }
        }
        return List.of();
    }

    @Override
    public boolean extractSpecificItem(ItemPipeEntity pipe, ServerLevel level, BlockPos containerPos, Direction face, ItemStack stack) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity != null) {
            ItemStack target = stack.copy();
            Optional<IItemHandler> itemHandlerOptional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, face).resolve();
            if (itemHandlerOptional.isPresent()) {
                IItemHandler itemHandler = itemHandlerOptional.get();
                for (int slot = itemHandler.getSlots() - 1; slot >= 0; slot--) {
                    if (ItemStack.isSameItemSameComponents(stack, itemHandler.getStackInSlot(slot))) {
                        ItemStack extracted = itemHandler.extractItem(slot, target.getCount(), false);
                        if (!extracted.isEmpty()) {
                            target.shrink(extracted.getCount());
                            pipe.setItem(face.getOpposite().get3DDataValue(), extracted);
                            if (target.isEmpty()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getModName(String modId) {
        return ModList.get().getModContainerById(modId).map(ModContainer::getModInfo).map(IModInfo::getDisplayName).orElse(modId);
    }

    @Override
    public boolean handleFluidInsertion(FluidPipeEntity pipe, ServerLevel level, BlockPos pipePos, BlockState pipeState, Fluid fluid, FluidInPipe fluidPacket) {
        BlockPos containerPos = pipePos.relative(fluidPacket.getTargetDirection());
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof FluidPipeEntity nextPipe) {
            if (nextPipe.emptyOrMatches(fluid)) {
                nextPipe.setFluid(fluid);
                boolean bounced = false;
                int amountToPass = Math.min(fluidPacket.getAmount(), nextPipe.remainingCapacity());
                if (amountToPass == fluidPacket.getAmount()) {
                    fluidPacket.resetProgress(fluidPacket.getTargetDirection().getOpposite());
                    nextPipe.insertFluidPacket(level, fluidPacket);
                } else {
                    bounced = true;
                    FluidInPipe newPacket = fluidPacket.copyWithAmount(amountToPass);
                    newPacket.resetProgress(fluidPacket.getTargetDirection().getOpposite());
                    nextPipe.insertFluidPacket(level, newPacket);
                    fluidPacket.setAmount(fluidPacket.getAmount() - amountToPass);
                    fluidPacket.resetProgress(fluidPacket.getTargetDirection());
                    pipe.routePacket(pipeState, fluidPacket);
                }
                level.sendBlockUpdated(containerPos, nextPipe.getBlockState(), nextPipe.getBlockState(), 2);
                return !bounced;
            }
        } else if (blockEntity != null) {
            Direction face = fluidPacket.getTargetDirection().getOpposite();
            Optional<IFluidHandler> fluidHandlerOptional = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, face).resolve();
            if (fluidHandlerOptional.isPresent()) {
                IFluidHandler fluidHandler = fluidHandlerOptional.get();
                int amountFilled = fluidHandler.fill(new FluidStack(fluid, fluidPacket.getAmount()), IFluidHandler.FluidAction.EXECUTE);
                if (amountFilled >= fluidPacket.getAmount()) {
                    return true;
                }
                fluidPacket.setAmount(fluidPacket.getAmount() - amountFilled);
            }
        }
        fluidPacket.resetProgress(fluidPacket.getTargetDirection());
        pipe.routePacket(pipeState, fluidPacket);
        return false;
    }

    @Override
    public boolean canAccessFluidContainer(Level level, BlockPos containerPos, Direction face) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof FluidPipeEntity) {
            return false;
        } else if (blockEntity != null) {
            Optional<IFluidHandler> fluidHandlerOptional = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, face).resolve();
            if (fluidHandlerOptional.isPresent()) {
                IFluidHandler fluidHandler = fluidHandlerOptional.get();
                return fluidHandler.getTanks() > 0;
            }
        }
        return false;
    }

    @Override
    public boolean handleFluidExtraction(FluidPipeEntity pipe, BlockState pipeState, ServerLevel level, BlockPos containerPos, Direction face, int amount) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity instanceof FluidPipeEntity || pipe.totalAmount() >= FluidPipeEntity.CAPACITY) {
            return false;
        } else if (blockEntity != null) {
            Optional<IFluidHandler> fluidHandlerOptional = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, face).resolve();
            if (fluidHandlerOptional.isPresent()) {
                IFluidHandler fluidHandler = fluidHandlerOptional.get();
                FluidStack drainedStack = pipe.isEmpty() ? fluidHandler.drain(Math.min(amount, pipe.remainingCapacity()), IFluidHandler.FluidAction.EXECUTE) : fluidHandler.drain(new FluidStack(pipe.getFluid(), amount), IFluidHandler.FluidAction.EXECUTE);
                if (!drainedStack.isEmpty()) {
                    pipe.setFluid(drainedStack.getFluid());
                    pipe.insertFluidPacket(level, new FluidInPipe(drainedStack.getAmount(), pipe.getTargetSpeed(), (short) 0, face.getOpposite(), face.getOpposite(), (short) 0));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public FluidRenderInfo getFluidRenderInfo(FluidState fluidState, BlockAndTintGetter level, BlockPos pos) {
        IClientFluidTypeExtensions fluidInfo = IClientFluidTypeExtensions.of(fluidState);
        int tint = fluidInfo.getTintColor(fluidState, level, pos);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidInfo.getStillTexture(fluidState, level, pos));
        return new FluidRenderInfo(tint, sprite);
    }

}
