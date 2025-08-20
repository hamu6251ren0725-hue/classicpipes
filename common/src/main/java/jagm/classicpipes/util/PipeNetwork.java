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

import java.util.*;

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
    private final Map<ProviderPipeEntity, List<ItemStack>> takenFromCache;

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
        this.takenFromCache = new HashMap<>();
    }

    public PipeNetwork(BlockPos pos) {
        this(pos, SortingMode.AMOUNT_DESCENDING);
    }

    private void enqueue(ItemStack stack, int amount, BlockPos requestPos, String playerName, ProviderPipeEntity providerPipe) {
        RequestedItem requestedItem = new RequestedItem(stack.copyWithCount(amount), requestPos, playerName);
        boolean matched = false;
        for (Tuple<ProviderPipeEntity, RequestedItem> tuple : this.queue) {
            if (tuple.a() == providerPipe && requestedItem.matches(tuple.b())) {
                matched = true;
                tuple.b().getStack().grow(amount);
                break;
            }
        }
        if (!matched) {
            this.queue.add(new Tuple<>(providerPipe, requestedItem));
        }
    }

    private MissingItem queueRequest(ItemStack stack, BlockPos requestPos, Player player) {
        MissingItem missingItem = new MissingItem(stack.copy());
        String playerName = player != null ? player.getName().getString() : "";
        for (ProviderPipeEntity providerPipe : this.providerPipes) {
            if (missingItem.isEmpty()) {
                break;
            }
            List<ItemStack> alreadyTaken = this.takenFromCache.containsKey(providerPipe) ? this.takenFromCache.get(providerPipe) : new ArrayList<>();
            for (ItemStack cacheStack : providerPipe.getCache()) {
                if (ItemStack.isSameItemSameComponents(stack, cacheStack)) {
                    int cacheCount = cacheStack.getCount();
                    int takenIndex = -1;
                    for (int i = 0; i < alreadyTaken.size(); i++) {
                        ItemStack takenStack = alreadyTaken.get(i);
                        if (ItemStack.isSameItemSameComponents(cacheStack, takenStack)) {
                            cacheCount -= takenStack.getCount();
                            takenIndex = i;
                            break;
                        }
                    }
                    int amount = Math.min(missingItem.getCount(), cacheCount);
                    if (amount > 0) {
                        this.enqueue(stack, amount, requestPos, playerName, providerPipe);
                        missingItem.shrink(amount);
                        if (takenIndex >= 0) {
                            ItemStack takenStack = alreadyTaken.get(takenIndex);
                            alreadyTaken.set(takenIndex, takenStack.copyWithCount(takenStack.getCount() + amount));
                        } else {
                            alreadyTaken.add(cacheStack.copyWithCount(amount));
                        }
                        this.takenFromCache.put(providerPipe, alreadyTaken);
                    }
                    break;
                }
            }
        }
        if (!missingItem.isEmpty()) {
            // TODO deal with looping recipes
            for (CraftingPipeEntity craftingPipe : this.craftingPipes) {
                ItemStack result = craftingPipe.getResult();
                if (ItemStack.isSameItemSameComponents(result, stack)) {
                    int requiredCrafts = Math.ceilDiv(missingItem.getCount(), result.getCount());
                    List<ItemStack> ingredients = craftingPipe.getIngredients();
                    for (int i = 0; i < requiredCrafts; i++) {
                        boolean canCraft = true;
                        for (ItemStack ingredient : ingredients) {
                            MissingItem missingForCraft = this.queueRequest(ingredient, craftingPipe.getBlockPos(), player);
                            if (!missingForCraft.isEmpty()) {
                                missingItem.addMissingIngredient(missingForCraft);
                                canCraft = false;
                            }
                        }
                        if (canCraft) {
                            missingItem.shrink(result.getCount());
                            this.enqueue(stack, result.getCount(), requestPos, playerName, null);
                        }
                    }
                }
            }
        }
        return missingItem;
    }

    public void request(ServerLevel level, ItemStack stack, BlockPos requestPos, Player player, boolean partialRequest) {
        MissingItem missingItem = this.queueRequest(stack.copy(), requestPos, player);
        if (missingItem.isEmpty()) {
            boolean cancelled = false;
            for (Tuple<ProviderPipeEntity, RequestedItem> tuple : this.queue) {
                this.addRequestedItem(tuple.b());
                if (tuple.a() != null && !tuple.a().extractItem(level, tuple.b().getStack())) {
                    cancelled = true;
                    if (!partialRequest && player != null) {
                        player.displayClientMessage(Component.translatable("chat." + ClassicPipes.MOD_ID + ".could_not_extract", stack.getCount(), stack.getItemName(), tuple.a().getBlockPos()), false);
                    }
                    break;
                }
            }
            if (cancelled) {
                this.queue.forEach(tuple -> this.removeRequestedItem(tuple.b()));
            }
        } else if (partialRequest && missingItem.getCount() < stack.getCount()) {
            this.queue.clear();
            this.takenFromCache.clear();
            this.request(level, stack.copyWithCount(stack.getCount() - missingItem.getCount()), requestPos, player, false);
            return;
        } else if (player != null) {
            player.displayClientMessage(Component.translatable("chat." + ClassicPipes.MOD_ID + ".missing_item.a", stack.getCount(), stack.getItemName()), false);
            for (ItemStack missing : missingItem.getBaseItems(new ArrayList<>())) {
                player.displayClientMessage(Component.translatable("chat." + ClassicPipes.MOD_ID + ".missing_item.b", missing.getCount(), missing.getItemName()), false);
            }
        }
        this.queue.clear();
        this.takenFromCache.clear();
        /*List<RequestedItem> toAdd = new ArrayList<>();
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
        this.takenFromCache.clear();
        for (RequestedItem requestedItem : this.requestedItems) {
            ClassicPipes.LOGGER.info("Requested {}x {} to {}", requestedItem.getAmountRemaining(), requestedItem.getStack().getItemName().getString(), requestedItem.getDestination());
        }*/
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
