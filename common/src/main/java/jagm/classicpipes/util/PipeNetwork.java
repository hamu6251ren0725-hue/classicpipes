package jagm.classicpipes.util;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.*;
import jagm.classicpipes.inventory.menu.RequestMenu;
import jagm.classicpipes.network.ClientBoundItemListPayload;
import jagm.classicpipes.services.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PipeNetwork {

    private static final byte DEFAULT_COOLDOWN = 40; // 2 seconds between client updates.

    private final BlockPos pos;
    private final Set<RoutingPipeEntity> routingPipes;
    private final Set<RoutingPipeEntity> defaultRoutes;
    private final Set<ProviderPipeEntity> providerPipes;
    private final Set<StockingPipeEntity> stockingPipes;
    private final Set<MatchingPipeEntity> matchingPipes;
    private final Set<CraftingPipeEntity> craftingPipes;
    private SortingMode sortingMode;
    private boolean cacheChanged;
    private byte cacheCooldown;
    private final List<RequestedItem> requestedItems;
    private final List<Tuple<ProviderPipeEntity, RequestedItem>> queue;

    public PipeNetwork(BlockPos pos, SortingMode sortingMode) {
        this.routingPipes = new HashSet<>();
        this.defaultRoutes = new HashSet<>();
        this.providerPipes = new HashSet<>();
        this.stockingPipes = new HashSet<>();
        this.matchingPipes = new HashSet<>();
        this.craftingPipes = new HashSet<>();
        this.sortingMode = sortingMode;
        this.pos = pos;
        this.cacheChanged = false;
        this.cacheCooldown = (byte) 0;
        this.requestedItems = new ArrayList<>();
        this.queue = new ArrayList<>();
    }

    public PipeNetwork(BlockPos pos) {
        this(pos, SortingMode.AMOUNT_DESCENDING);
    }

    private MissingItem queueRequest(ItemStack stack, BlockPos requestPos, Player player) {
        MissingItem missingItem = new MissingItem(stack);
        String playerName = player != null ? player.getName().getString() : "";
        for (ProviderPipeEntity providerPipe : this.providerPipes) {
            if (missingItem.isEmpty()) {
                break;
            }
            for (ItemStack cacheStack : providerPipe.getCache()) {
                if (ItemStack.isSameItemSameComponents(stack, cacheStack)) {
                    int amount = Math.min(missingItem.getCount(), cacheStack.getCount());
                    this.queue.add(new Tuple<>(providerPipe, new RequestedItem(stack.copyWithCount(amount), requestPos, playerName)));
                    missingItem.shrink(amount);
                    break;
                }
            }
        }
        if (!missingItem.isEmpty()) {
            for (CraftingPipeEntity craftingPipe : this.craftingPipes) {
                ItemStack result = craftingPipe.getResult();
                if (ItemStack.isSameItemSameComponents(result, stack)) {
                    int requiredCrafts = Math.ceilDiv(missingItem.getCount(), result.getCount());
                    List<ItemStack> ingredients = craftingPipe.getIngredients();
                    for (ItemStack ingredient : ingredients) {
                        MissingItem missingForCraft = this.queueRequest(ingredient.copyWithCount(ingredient.getCount() * requiredCrafts), craftingPipe.getBlockPos(), player);
                        if (!missingForCraft.isEmpty()) {
                            missingItem.addMissingIngredient(missingForCraft);
                        }
                    }
                    if (!missingItem.hasMissingIngredients()) {
                        this.queue.add(new Tuple<>(null, new RequestedItem(stack.copyWithCount(missingItem.getCount()), requestPos, playerName)));
                        missingItem.shrink(missingItem.getCount());
                    }
                }
            }
        }
        return missingItem;
    }

    public void request(ServerLevel level, ItemStack stack, BlockPos requestPos, Player player, boolean partialRequest) {
        MissingItem missingItem = this.queueRequest(stack.copy(), requestPos, player);
        List<RequestedItem> toAdd = new ArrayList<>();
        if (missingItem.isEmpty()) {
            for (Tuple<ProviderPipeEntity, RequestedItem> tuple : this.queue) {
                if (tuple.a() != null) {
                    if (!tuple.a().extractItem(level, tuple.b().getStack())) {
                        toAdd.clear();
                        break;
                    }
                }
                toAdd.add(tuple.b());
            }
        } else if (partialRequest) {
            int available = stack.getCount() - missingItem.getCount();
            this.queue.clear();
            missingItem = this.queueRequest(stack.copyWithCount(available), requestPos, player);
            if (missingItem.isEmpty()) {
                for (Tuple<ProviderPipeEntity, RequestedItem> tuple : this.queue) {
                    if (tuple.a() != null) {
                        if (!tuple.a().extractItem(level, tuple.b().getStack())) {
                            toAdd.clear();
                            break;
                        }
                    }
                    toAdd.add(tuple.b());
                }
            }
        }
        if (!missingItem.isEmpty() && player != null) {
            // TODO deal with looping recipes
            for (ItemStack missing : missingItem.getBaseItems(new ArrayList<>())) {
                player.displayClientMessage(Component.translatable("chat." + ClassicPipes.MOD_ID + ".missing_item", missing.getCount(), missing.getItem().getName()), false);
            }
        }
        for (RequestedItem requestedItem : toAdd) {
            boolean matched = false;
            for (RequestedItem alreadyThere : this.requestedItems) {
                if (requestedItem.matches(alreadyThere)) {
                    alreadyThere.getStack().grow(requestedItem.getAmountRemaining());
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                this.requestedItems.add(requestedItem);
            }
        }
        this.queue.clear();
    }

    public void tick(ServerLevel level) {
        if (this.cacheChanged && this.cacheCooldown <= 0) {
            List<ServerPlayer> playerList = level.getPlayers(player -> player.containerMenu instanceof RequestMenu menu && menu.getNetworkPos().equals(this.getPos()));
            if (!playerList.isEmpty()) {
                ClientBoundItemListPayload toSend = this.requestItemList(this.pos);
                for (ServerPlayer player : playerList) {
                    Services.LOADER_SERVICE.sendToClient(player, toSend);
                }
            }
            for (StockingPipeEntity stockingPipe : this.stockingPipes) {
                if (stockingPipe.isActiveStocking()) {
                    stockingPipe.tryRequests(level);
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

    public Set<StockingPipeEntity> getStockingPipes() {
        return this.stockingPipes;
    }

    public Set<MatchingPipeEntity> getMatchingPipes() {
        return this.matchingPipes;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void addPipe(NetworkedPipeEntity pipe) {
        if (pipe instanceof RoutingPipeEntity routingPipe) {
            this.routingPipes.add(routingPipe);
            if (routingPipe.isDefaultRoute()) {
                this.defaultRoutes.add(routingPipe);
            }
        } else if (pipe instanceof ProviderPipeEntity providerPipe) {
            this.providerPipes.add(providerPipe);
        } else if (pipe instanceof StockingPipeEntity stockingPipe) {
            this.stockingPipes.add(stockingPipe);
        } else if (pipe instanceof MatchingPipeEntity matchingPipe) {
            this.matchingPipes.add(matchingPipe);
        } else if (pipe instanceof CraftingPipeEntity craftingPipe) {
            this.craftingPipes.add(craftingPipe);
        }
    }

    public void removePipe(NetworkedPipeEntity pipe) {
        if (pipe instanceof RoutingPipeEntity routingPipe) {
            this.routingPipes.remove(routingPipe);
            if (routingPipe.isDefaultRoute()) {
                this.defaultRoutes.remove(routingPipe);
            }
        } else if (pipe instanceof ProviderPipeEntity providerPipe) {
            this.providerPipes.remove(providerPipe);
        } else if (pipe instanceof StockingPipeEntity stockingPipe) {
            this.stockingPipes.remove(stockingPipe);
        } else if (pipe instanceof MatchingPipeEntity matchingPipe) {
            this.matchingPipes.remove(matchingPipe);
        } else if (pipe instanceof CraftingPipeEntity craftingPipe) {
            this.craftingPipes.remove(craftingPipe);
        }
        this.requestedItems.removeIf(requestedItem -> requestedItem.getDestination().equals(pipe.getBlockPos()));
    }

    public ClientBoundItemListPayload requestItemList(BlockPos requestPos) {
        List<ItemStack> existingItems = new ArrayList<>();
        for (ProviderPipeEntity providerPipe : this.providerPipes) {
            for (ItemStack stack : providerPipe.getCache()) {
                boolean alreadyThere = false;
                for (ItemStack inStack : existingItems) {
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
                    existingItems.add(stack.copy());
                }
            }
        }
        List<ItemStack> craftableItems = new ArrayList<>();
        for (CraftingPipeEntity craftingPipe : this.craftingPipes) {
            ItemStack result = craftingPipe.getResult();
            if (!result.isEmpty()) {
                craftableItems.add(result.copyWithCount(1));
            }
        }
        return new ClientBoundItemListPayload(existingItems, craftableItems, this.sortingMode, this.pos, requestPos);
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
