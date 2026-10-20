package nl.mikaaah.alchemicallaser.compat;

import java.util.*;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.api.render.EmiTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;
import nl.mikaaah.alchemicallaser.recipe.LaserRecipe;

@EmiEntrypoint
public class LaserEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(AlchemicalLaser.id("laser_processing"), EmiStack.of(AlchemicalLaser.EMITTER_ITEM.get()));
    @Override public void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY); registry.addWorkstation(CATEGORY,EmiStack.of(AlchemicalLaser.EMITTER_ITEM.get()));
        for (var holder:registry.getRecipeManager().getAllRecipesFor(AlchemicalLaser.LASER_RECIPE.get())) registry.addRecipe(new Display(holder.id(),holder.value()));
    }
    private static final class Display extends BasicEmiRecipe {
        final LaserRecipe recipe;
        Display(ResourceLocation id,LaserRecipe recipe) {
            super(CATEGORY,id,164,78);this.recipe=recipe;
            inputs=List.of(EmiIngredient.of(recipe.ingredient(),recipe.inputCount()));
            catalysts=List.of(EmiStack.of(new ItemStack(BuiltInRegistries.ITEM.get(recipe.lens()))));
            var out=new ArrayList<EmiStack>();out.add(EmiStack.of(recipe.result()));
            recipe.secondary().ifPresent(s -> out.add(EmiStack.of(s).setChance(recipe.secondaryChance())));outputs=out;
        }
        @Override public void addWidgets(WidgetHolder widgets) {
            widgets.addSlot(inputs.get(0),10,26);
            widgets.addSlot(catalysts.get(0),57,4);
            widgets.addTexture(EmiTexture.EMPTY_ARROW,56,29);
            widgets.addSlot(outputs.get(0),104,26).recipeContext(this);
            if(outputs.size()>1) widgets.addSlot(outputs.get(1),132,26).recipeContext(this);
            widgets.addText(Component.translatable("recipe.alchemicallaser.duration",recipe.duration()),3,57,0xaaaaaa,false);
            widgets.addText(Component.translatable("recipe.alchemicallaser.minimum",recipe.minimumRpm()),3,68,0xaaaaaa,false);
        }
    }
}
