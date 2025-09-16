package jagm.classicpipes.compat;
/*
import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.menu.RecipePipeMenu;
import jagm.classicpipes.network.ForgePacketHandler;
import jagm.classicpipes.network.ServerBoundTransferRecipe;
import jagm.classicpipes.util.MiscUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JeiPlugin*/
public class ForgeJEIPlugin /*implements IModPlugin*/ {
/*
    @Override
    public ResourceLocation getPluginUid() {
        return MiscUtil.resourceLocation("jei_plugin");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addUniversalRecipeTransferHandler(new IUniversalRecipeTransferHandler<RecipePipeMenu>() {

            @Override
            public Class<? extends RecipePipeMenu> getContainerClass() {
                return RecipePipeMenu.class;
            }

            @Override
            public Optional<MenuType<RecipePipeMenu>> getMenuType() {
                return Optional.of(ClassicPipes.RECIPE_PIPE_MENU);
            }

            @Override
            public IRecipeTransferError transferRecipe(RecipePipeMenu recipePipeMenu, Object recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
                if (doTransfer) {
                    List<IRecipeSlotView> inputs = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
                    List<ItemStack> recipeToSend = new ArrayList<>();
                    List<Integer> slotsToSend = new ArrayList<>();
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = inputs.size() > i ? inputs.get(i).getDisplayedItemStack().orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
                        if (!stack.isEmpty()) {
                            recipeToSend.add(stack);
                            slotsToSend.add(i);
                        }
                        recipePipeMenu.getSlot(i).set(stack);
                    }
                    List<IRecipeSlotView> outputs = recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT);
                    ItemStack stack = outputs.getFirst().getDisplayedItemStack().orElse(ItemStack.EMPTY);
                    if (!stack.isEmpty()) {
                        recipeToSend.add(stack);
                        slotsToSend.add(9);
                    }
                    recipePipeMenu.getSlot(9).set(stack);
                    ForgePacketHandler.sendToServer(new ServerBoundTransferRecipe(recipeToSend, slotsToSend));
                }
                return null;
            }

        });
    }
*/
}
