package jagm.classicpipes.compat;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.client.screen.DiamondFluidPipeScreen;
import jagm.classicpipes.client.screen.FilterScreen;
import jagm.classicpipes.inventory.container.Filter;
import jagm.classicpipes.inventory.menu.RecipePipeMenu;
import jagm.classicpipes.network.ServerBoundSetFilterPayload;
import jagm.classicpipes.network.ServerBoundTransferRecipePayload;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public class FabricJEIPlugin implements IModPlugin {

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
                    Services.LOADER_SERVICE.sendToServer(new ServerBoundTransferRecipePayload(recipeToSend, slotsToSend));
                }
                return null;
            }

        });
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(FilterScreen.class, new IGhostIngredientHandler<>() {

            @Override
            public <I> List<Target<I>> getTargetsTyped(FilterScreen filterScreen, ITypedIngredient<I> ingredient, boolean doStart) {
                ItemStack stack = ingredient.getItemStack().orElse(ItemStack.EMPTY);
                if (!doStart || stack.isEmpty()) {
                    return List.of();
                }
                Fluid fluid = Services.LOADER_SERVICE.getFluidFromStack(stack);
                if (filterScreen instanceof DiamondFluidPipeScreen) {
                    if (fluid != null && !fluid.isSame(Fluids.EMPTY)) {
                        stack = new ItemStack(fluid.getBucket());
                        if (stack.isEmpty()) {
                            return List.of();
                        }
                    } else {
                        return List.of();
                    }
                }
                List<Target<I>> validSlots = new ArrayList<>();
                for (int i = 0; i < filterScreen.filterSlots(); i++) {
                    Slot slot = filterScreen.getMenu().getSlot(i);
                    if (slot.container instanceof Filter) {
                        ItemStack finalStack = stack;
                        validSlots.add(new Target<>() {

                            @Override
                            public Rect2i getArea() {
                                return new Rect2i(filterScreen.filterScreenLeft() + slot.x, filterScreen.filterScreenTop() + slot.y, 16, 16);
                            }

                            @Override
                            public void accept(I ingredient) {
                                slot.set(finalStack);
                                Services.LOADER_SERVICE.sendToServer(new ServerBoundSetFilterPayload(slot.index, finalStack));
                            }

                        });
                    }
                }
                return validSlots;
            }

            @Override
            public void onComplete() {}

        });
    }

}
