package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.blockentity.NetworkedPipeEntity;
import jagm.classicpipes.inventory.container.RequestMenuContainer;
import jagm.classicpipes.network.ClientBoundItemListPayload;
import jagm.classicpipes.network.ServerBoundSortingModePayload;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import jagm.classicpipes.util.SortingMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestMenu extends AbstractContainerMenu {

    private static final Pattern MOD_LOOKUP = Pattern.compile("@\\S+");
    private static final Pattern TAG_LOOKUP = Pattern.compile("#\\S+");

    private List<ItemStack> networkItems;
    private final Container toDisplay;
    public final NonNullList<Slot> displaySlots = NonNullList.create();
    private String search;
    private int page;
    private int maxPage;
    private SortingMode sortingMode;
    private final BlockPos networkPos;
    private final BlockPos requestPos;
    private final BlockEntity controllerPipe;
    private final BlockEntity requestPipe;

    public RequestMenu(int id, Inventory inventory, ClientBoundItemListPayload payload) {
        super(ClassicPipes.REQUEST_MENU, id);
        this.networkItems = payload.networkItems();
        this.networkPos = payload.networkPos();
        this.requestPos = payload.requestPos();
        this.sortingMode = payload.sortingMode();
        this.networkItems.sort(this.sortingMode.getComparator());
        this.toDisplay = new RequestMenuContainer();
        Level level = inventory.player.level();
        this.controllerPipe = level.getBlockEntity(this.networkPos);
        this.requestPipe = level.getBlockEntity(this.requestPos);
        this.search = "";
        this.page = 0;
        this.maxPage = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 9; j++) {
                displaySlots.add(new FilterSlot(this.toDisplay, i * 9 + j, 8 + j * 18, 36 + i * 18));
            }
        }
        this.updateSearch();
    }

    public void update(List<ItemStack> networkItems) {
        this.networkItems = networkItems;
        this.update();
    }

    public void update() {
        this.networkItems.sort(this.sortingMode.getComparator());
        this.updateSearch();
    }

    public void updateSearch() {
        this.toDisplay.clearContent();
        int display = this.toDisplay.getContainerSize();
        List<ItemStack> matchingItems = new ArrayList<>();
        for (ItemStack stack : this.networkItems) {
            if (this.search.isEmpty() || itemMatchesSearch(stack, this.search)) {
                matchingItems.add(stack);
            }
        }
        this.maxPage = matchingItems.size() / display;
        if (this.page > this.maxPage) {
            this.page = this.maxPage;
        }
        int index = 0;
        for (ItemStack stack : matchingItems) {
            if (index >= (this.page + 1) * display) {
                break;
            }
            if (index >= this.page * display) {
                this.toDisplay.setItem(index % display, stack);
            }
            index++;
        }
    }

    public int getMaxPage() {
        return this.maxPage;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.controllerPipe instanceof NetworkedPipeEntity controller && this.requestPipe instanceof NetworkedPipeEntity requester) {
            return player.level().getBlockEntity(controller.getBlockPos()) == controller && Container.stillValidBlockEntity(requester, player) && controller.isController() && controller.getLogisticalNetwork() == requester.getLogisticalNetwork();
        }
        return false;
    }

    public String getSearch() {
        return this.search;
    }

    public SortingMode getSortingMode() {
        return this.sortingMode;
    }

    public void setSortingMode(SortingMode sortingMode) {
        this.sortingMode = sortingMode;
        Services.LOADER_SERVICE.sendToServer(new ServerBoundSortingModePayload(sortingMode));
        this.networkItems.sort(this.sortingMode.getComparator());
        this.updateSearch();
    }

    public void setSearch(String search) {
        this.search = search;
        this.updateSearch();
    }

    public void changePage(int increment) {
        this.page += increment;
        if (this.page < 0) {
            this.page = 0;
        } else if (this.page > this.maxPage) {
            this.page = this.maxPage;
        }
        this.updateSearch();
    }

    public int getPage() {
        return this.page;
    }

    public BlockPos getNetworkPos() {
        return this.networkPos;
    }

    public BlockPos getRequestPos() {
        return this.requestPos;
    }

    public void removeStack(ItemStack stack) {
        this.networkItems.remove(stack);
    }

    private static boolean itemMatchesSearch(ItemStack stack, String search) {
        Matcher modMatcher = MOD_LOOKUP.matcher(search);
        if (modMatcher.find()) {
            String match = modMatcher.group();
            search = search.replace(match, "");
            String searchedMod = normalise(match.replaceFirst("@", ""));
            String itemModID = MiscUtil.modFromItem(stack);
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
