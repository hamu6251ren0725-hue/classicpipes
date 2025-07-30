package jagm.classicpipes.util;

import jagm.classicpipes.blockentity.LogisticalPipeEntity;
import jagm.classicpipes.blockentity.ProviderPipeEntity;
import jagm.classicpipes.blockentity.RoutingPipeEntity;
import jagm.classicpipes.inventory.menu.RequestMenu;
import jagm.classicpipes.network.ClientBoundItemListPayload;
import jagm.classicpipes.services.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class LogisticalNetwork {

    private static final byte DEFAULT_COOLDOWN = 40; // 2 seconds between client updates.

    private final BlockPos pos;
    private final Set<RoutingPipeEntity> routingPipes;
    private final Set<RoutingPipeEntity> defaultRoutes;
    private final Set<ProviderPipeEntity> providerPipes;
    private SortingMode sortingMode;
    private boolean cacheChanged;
    private byte cacheCooldown;
    private final List<RequestedItem> requestedItems;

    public LogisticalNetwork(BlockPos pos, SortingMode sortingMode) {
        this.routingPipes = new HashSet<>();
        this.defaultRoutes = new HashSet<>();
        this.providerPipes = new HashSet<>();
        this.sortingMode = sortingMode;
        this.pos = pos;
        this.cacheChanged = false;
        this.cacheCooldown = (byte) 0;
        this.requestedItems = new ArrayList<>();
    }

    public LogisticalNetwork(BlockPos pos) {
        this(pos, SortingMode.AMOUNT_DESCENDING);
    }

    public List<ItemStack> request(ServerLevel level, ItemStack stack, BlockPos requestPos, Player player) {
        ItemStack originalStack = stack.copy();
        Map<ProviderPipeEntity, Integer> extractionSchedule = new HashMap<>();
        List<ItemStack> missingItems = new ArrayList<>();
        for (ProviderPipeEntity providerPipe : this.providerPipes) {
            if (stack.isEmpty()) {
                break;
            }
            for (ItemStack cacheStack : providerPipe.getCache()) {
                if (ItemStack.isSameItemSameComponents(stack, cacheStack)) {
                    int amount = Math.min(stack.getCount(), cacheStack.getCount());
                    extractionSchedule.put(providerPipe, amount);
                    stack.shrink(amount);
                    break;
                }
            }
        }
        if (stack.isEmpty()) {
            RequestedItem requestedItem = new RequestedItem(originalStack, requestPos, player.getName().getString());
            this.requestedItems.add(requestedItem);
            for (ProviderPipeEntity providerPipe : extractionSchedule.keySet()) {
                ItemStack toExtract = originalStack.copyWithCount(extractionSchedule.get(providerPipe));
                if (!providerPipe.extractItem(level, toExtract)) {
                    missingItems.add(toExtract);
                    break;
                }
            }
            if (!missingItems.isEmpty()) {
                this.requestedItems.remove(requestedItem);
            }
        } else {
            missingItems.add(stack);
        }
        return missingItems;
    }

    public void tick(ServerLevel level) {
        if (this.cacheChanged && this.cacheCooldown <= 0) {
            List<ServerPlayer> playerList = level.getPlayers(player -> player.containerMenu instanceof RequestMenu menu && menu.getNetworkPos().equals(this.getPos()));
            if (!playerList.isEmpty()) {
                ClientBoundItemListPayload toSend = this.requestItemList(BlockPos.ZERO);
                for (ServerPlayer player : playerList) {
                    Services.LOADER_SERVICE.sendToClient(player, toSend);
                }
            }
            this.cacheChanged = false;
            this.cacheCooldown = DEFAULT_COOLDOWN;
        } else if (this.cacheCooldown > 0) {
            this.cacheCooldown--;
        }
        this.getRequestedItems().removeIf(RequestedItem::timedOut);
    }

    public Set<RoutingPipeEntity> getRoutingPipes() {
        return this.routingPipes;
    }

    public Set<RoutingPipeEntity> getDefaultRoutes() {
        return this.defaultRoutes;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void addPipe(LogisticalPipeEntity pipe) {
        if (pipe instanceof RoutingPipeEntity routingPipe) {
            this.routingPipes.add(routingPipe);
            if (routingPipe.isDefaultRoute()) {
                this.defaultRoutes.add(routingPipe);
            }
        } else if (pipe instanceof ProviderPipeEntity providerPipe) {
            this.providerPipes.add(providerPipe);
        }
    }

    public void removePipe(LogisticalPipeEntity pipe) {
        if (pipe instanceof RoutingPipeEntity routingPipe) {
            this.routingPipes.remove(routingPipe);
            if (routingPipe.isDefaultRoute()) {
                this.defaultRoutes.remove(routingPipe);
            }
        } else if (pipe instanceof ProviderPipeEntity providerPipe) {
            this.providerPipes.remove(providerPipe);
        }
    }

    public ClientBoundItemListPayload requestItemList(BlockPos requestPos) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ProviderPipeEntity providerPipe : this.providerPipes) {
            for (ItemStack stack : providerPipe.getCache()) {
                boolean alreadyThere = false;
                for (ItemStack inStack : stacks) {
                    if (ItemStack.isSameItemSameComponents(stack, inStack)) {
                        inStack.grow(stack.getCount());
                        if (inStack.getCount() < 0) {
                            inStack.setCount(Integer.MAX_VALUE);
                        }
                        alreadyThere = true;
                        break;
                    }
                }
                if (!alreadyThere) {
                    stacks.add(stack.copy());
                }
            }
        }
        return new ClientBoundItemListPayload(stacks, this.sortingMode, this.pos, requestPos);
    }

    public void cacheUpdated() {
        this.cacheChanged = true;
    }

    public void setSortingMode(SortingMode sortingMode) {
        this.sortingMode = sortingMode;
    }

    public SortingMode getSortingMode() {
        return this.sortingMode;
    }

    public List<RequestedItem> getRequestedItems() {
        return this.requestedItems;
    }

    public void removeRequestedItem(RequestedItem requestedItem) {
        this.requestedItems.remove(requestedItem);
    }

    public void addRequestedItem(RequestedItem requestedItem) {
        this.requestedItems.add(requestedItem);
    }
}
