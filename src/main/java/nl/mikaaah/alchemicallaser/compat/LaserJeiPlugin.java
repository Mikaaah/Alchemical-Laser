package nl.mikaaah.alchemicallaser.compat;

import java.util.List;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;
import nl.mikaaah.alchemicallaser.recipe.LaserRecipe;

@JeiPlugin
public class LaserJeiPlugin implements IModPlugin {
    public static final RecipeType<LaserRecipe> TYPE = new RecipeType<>(AlchemicalLaser.id("laser_processing"), LaserRecipe.class);
    @Override public ResourceLocation getPluginUid() { return AlchemicalLaser.id("jei"); }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) { registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper())); }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level != null) registration.addRecipes(TYPE, Minecraft.getInstance().level.getRecipeManager()
            .getAllRecipesFor(AlchemicalLaser.LASER_RECIPE.get()).stream().map(r -> r.value()).toList());
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) { registration.addRecipeCatalyst(AlchemicalLaser.EMITTER_ITEM.toStack(), TYPE); }
    private static class Category implements IRecipeCategory<LaserRecipe> {
        final IDrawable background, icon;
        Category(IGuiHelper gui) { background=gui.createBlankDrawable(164,78); icon=gui.createDrawableIngredient(VanillaTypes.ITEM_STACK,AlchemicalLaser.EMITTER_ITEM.toStack()); }
        @Override public RecipeType<LaserRecipe> getRecipeType() { return TYPE; }
        @Override public Component getTitle() { return Component.translatable("recipe.alchemicallaser.laser_processing"); }
        @Override public IDrawable getBackground() { return background; }
        @Override public IDrawable getIcon() { return icon; }
        @Override public void setRecipe(IRecipeLayoutBuilder builder,LaserRecipe recipe,IFocusGroup focuses) {
            List<ItemStack> inputs=java.util.Arrays.stream(recipe.ingredient().getItems()).map(s -> s.copyWithCount(recipe.inputCount())).toList();
            builder.addSlot(RecipeIngredientRole.INPUT,10,26).addItemStacks(inputs);
            builder.addSlot(RecipeIngredientRole.CATALYST,57,4).addItemStack(new ItemStack(BuiltInRegistries.ITEM.get(recipe.lens())));
            builder.addSlot(RecipeIngredientRole.OUTPUT,104,26).addItemStack(recipe.result());
            recipe.secondary().ifPresent(s -> builder.addSlot(RecipeIngredientRole.OUTPUT,132,26).addItemStack(s)
                .addTooltipCallback((view,tooltip) -> tooltip.add(Component.translatable("recipe.alchemicallaser.chance", Math.round(recipe.secondaryChance()*100)))));
        }
        @Override public void draw(LaserRecipe recipe,IRecipeSlotsView slots,GuiGraphics graphics,double mouseX,double mouseY) {
            var font=Minecraft.getInstance().font;
            graphics.drawString(font,"→",62,31,0x725335,false);
            graphics.drawString(font,Component.translatable("recipe.alchemicallaser.duration",recipe.duration()),3,57,0x555555,false);
            graphics.drawString(font,Component.translatable("recipe.alchemicallaser.minimum",recipe.minimumRpm()),3,68,0x555555,false);
        }
    }
}
