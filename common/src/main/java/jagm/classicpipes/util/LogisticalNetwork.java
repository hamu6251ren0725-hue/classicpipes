package jagm.classicpipes.util;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.LogisticalPipeEntity;
import jagm.classicpipes.blockentity.ProviderPipeEntity;
import jagm.classicpipes.blockentity.RoutingPipeEntity;
import jagm.classicpipes.inventory.menu.RequestMenu;
import jagm.classicpipes.network.ClientBoundItemListPayload;
import jagm.classicpipes.services.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogisticalNetwork implements MenuProvider {

    private static final Pattern MOD_LOOKUP = Pattern.compile("@\\S+");
    private static final Pattern TAG_LOOKUP = Pattern.compile("#\\S+");

    private final BlockPos pos;
    private final Set<RoutingPipeEntity> routingPipes;
    private final Set<RoutingPipeEntity> defaultRoutes;
    private final Set<ProviderPipeEntity> providerPipes;
    private final Set<LogisticalPipeEntity> otherPipes;

    public LogisticalNetwork(BlockPos pos, LogisticalPipeEntity... pipes) {
        this.routingPipes = new HashSet<>();
        this.defaultRoutes = new HashSet<>();
        this.providerPipes = new HashSet<>();
        this.otherPipes = new HashSet<>();
        for (LogisticalPipeEntity pipe : pipes) {
            this.addPipe(pipe);
        }
        this.pos = pos;
    }

    public void merge(ServerLevel level, LogisticalNetwork otherNetwork) {
        otherNetwork.getAllPipes().forEach(pipe -> {
            this.addPipe(pipe);
            pipe.setLogisticalNetwork(this, level, pipe.getBlockPos(), pipe.getBlockState());
            pipe.setController(false);
        });
    }

    public Set<LogisticalPipeEntity> getAllPipes() {
        Set<LogisticalPipeEntity> allPipes = new HashSet<>();
        allPipes.addAll(this.routingPipes);
        allPipes.addAll(this.providerPipes);
        allPipes.addAll(this.otherPipes);
        return allPipes;
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

    public void destroy(ServerLevel level) {
        this.getAllPipes().forEach(pipe -> pipe.disconnect(level));
    }

    public void addPipe(LogisticalPipeEntity pipe) {
        if (pipe instanceof RoutingPipeEntity routingPipe) {
            this.routingPipes.add(routingPipe);
            if (routingPipe.isDefaultRoute()) {
                this.defaultRoutes.add(routingPipe);
            }
        } else if (pipe instanceof ProviderPipeEntity providerPipe) {
            this.providerPipes.add(providerPipe);
        } else {
            this.otherPipes.add(pipe);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container." + ClassicPipes.MOD_ID + ".request");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RequestMenu(id, this.requestItemList("", 0));
    }

    public ClientBoundItemListPayload requestItemList(String search, int page) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ProviderPipeEntity providerPipe : this.providerPipes) {
            for (ItemStack stack : providerPipe.getCache()) {
                if (itemMatchesSearch(stack, search)) {
                    boolean alreadyThere = false;
                    for (ItemStack inStack : stacks) {
                        if (ItemStack.isSameItemSameComponents(stack, inStack)) {
                            inStack.grow(stack.getCount());
                            alreadyThere = true;
                            break;
                        }
                    }
                    if (!alreadyThere) {
                        stacks.add(stack);
                    }
                }
            }
        }
        stacks.sort(Comparator.comparing(stack -> stack.getItem().getName().getString()));
        int maxPage = stacks.size() / 72;
        page = Math.min(page, maxPage);
        int start = page * 72;
        int end = Math.min(start + 72, stacks.size());
        return new ClientBoundItemListPayload(stacks.subList(start, end), maxPage);
    }

    private static boolean itemMatchesSearch(ItemStack stack, String search) {
        Matcher modMatcher = MOD_LOOKUP.matcher(search);
        if (modMatcher.find()) {
            String match = modMatcher.group();
            search = search.replace(match, "");
            String searchedMod = normalise(match.replaceFirst("@", ""));
            String itemModID = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().split(":")[0];
            String itemModName = normalise(Services.LOADER_SERVICE.getModName(itemModID));
            if (!normalise(itemModID).contains(searchedMod) && !itemModName.contains(searchedMod)) {
                return false;
            }
        }
        Matcher tagMatcher = TAG_LOOKUP.matcher(search);
        while (tagMatcher.find()) {
            boolean foundTag = false;
            String match = tagMatcher.group();
            search = search.replace(match, "");
            for (TagKey<Item> tag : stack.getTags().toList()) {
                String searchedTag = normalise(match.replaceFirst("#", ""));
                String itemTag = normalise(tag.location().toString());
                if (itemTag.contains(searchedTag)) {
                    foundTag = true;
                    break;
                }
            }
            if (!foundTag) {
                return false;
            }
        }
        search = normalise(search);
        String itemName = normalise(stack.getItemName().getString());
        return itemName.contains(search);
    }

    private static String normalise(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

}
