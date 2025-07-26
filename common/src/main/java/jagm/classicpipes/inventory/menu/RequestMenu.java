package jagm.classicpipes.inventory.menu;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.container.RequestMenuContainer;
import jagm.classicpipes.network.ClientBoundItemListPayload;
import jagm.classicpipes.services.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestMenu extends AbstractContainerMenu {

    private static final Pattern MOD_LOOKUP = Pattern.compile("@\\S+");
    private static final Pattern TAG_LOOKUP = Pattern.compile("#\\S+");
    private static final Comparator<ItemStack> SORT_BY_NAME = Comparator.comparing(stack -> stack.getItem().getName().getString());

    private List<ItemStack> networkItems;
    private Container toDisplay;
    private String search;
    private int page;

    public RequestMenu(int id, ClientBoundItemListPayload payload) {
        super(ClassicPipes.REQUEST_MENU, id);
        this.networkItems = payload.networkItems();
        this.toDisplay = new RequestMenuContainer();
        this.search = "";
        this.page = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new FilterSlot(this.toDisplay, i * 9 + j, 8 + j * 18, 36 + i * 18));
            }
        }
        this.updateSearch();
    }

    public void update(List<ItemStack> networkItems) {
        this.networkItems = networkItems;
        this.updateSearch();
    }

    public void updateSearch() {
        this.toDisplay.clearContent();
        int index = 0;
        int display = this.toDisplay.getContainerSize();
        if (this.networkItems.size() < (this.page + 1) * display) {
            this.page = Math.min(this.networkItems.size() / display, this.page);
        }
        for (ItemStack stack : this.networkItems) {
            if (itemMatchesSearch(stack, this.search)) {
                if (index >= (this.page + 1) * display) {
                    break;
                }
                if (index >= this.page * display) {
                    this.toDisplay.setItem(index % display, stack);
                }
                index++;
            }
        }
    }

    @Override
    public void clicked(int index, int button, ClickType clickType, Player player) {
        if (index >= 0 && index < this.toDisplay.getContainerSize()) {
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) {
                // TODO open request subscreen
            } else if (clickType == ClickType.SWAP) {
                // TODO quick-request items
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public String getSearch() {
        return this.search;
    }

    public void setSearch(String search) {
        this.search = search;
        this.updateSearch();
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
