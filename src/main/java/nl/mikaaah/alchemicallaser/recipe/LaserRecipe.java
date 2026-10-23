package nl.mikaaah.alchemicallaser.recipe;

import java.util.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;

public record LaserRecipe(Ingredient ingredient, int inputCount, ResourceLocation lens, ItemStack result,
        int duration, int minimumRpm, Optional<ItemStack> secondary, float secondaryChance, boolean copyComponents)
        implements Recipe<SingleRecipeInput> {
    public static final MapCodec<LaserRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(LaserRecipe::ingredient),
        Codec.intRange(1, 64).optionalFieldOf("input_count", 1).forGetter(LaserRecipe::inputCount),
        ResourceLocation.CODEC.fieldOf("lens").forGetter(LaserRecipe::lens),
        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(LaserRecipe::result),
        Codec.intRange(1, 72000).optionalFieldOf("duration", 100).forGetter(LaserRecipe::duration),
        Codec.intRange(1, 4096).optionalFieldOf("minimum_rpm", 16).forGetter(LaserRecipe::minimumRpm),
        ItemStack.STRICT_CODEC.optionalFieldOf("secondary").forGetter(LaserRecipe::secondary),
        Codec.floatRange(0, 1).optionalFieldOf("secondary_chance", 1f).forGetter(LaserRecipe::secondaryChance),
        Codec.BOOL.optionalFieldOf("copy_components", false).forGetter(LaserRecipe::copyComponents)
    ).apply(i, LaserRecipe::new));
    public boolean matchesInput(ItemStack input, ItemStack installedLens) {
        return input.getCount() >= inputCount && ingredient.test(input)
            && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(installedLens.getItem()).equals(lens);
    }
    @Override public boolean matches(SingleRecipeInput input, Level level) {
        return input.item().getCount() >= inputCount && ingredient.test(input.item());
    }
    public ItemStack makeResult(ItemStack input) {
        ItemStack out = result.copy();
        if (copyComponents) {
            out.applyComponents(input.getComponents());
            out.applyComponents(result.getComponentsPatch());
        }
        return out;
    }
    public List<ItemStack> roll(ItemStack input, RandomSource random) {
        List<ItemStack> outputs = new ArrayList<>(); outputs.add(makeResult(input));
        if (secondary.isPresent() && random.nextFloat() < secondaryChance) outputs.add(secondary.get().copy());
        return outputs;
    }
    public record Batch(int consumed, List<ItemStack> outputs) {}
    public Batch rollBatch(ItemStack input, RandomSource random) {
        int operations = input.getCount() / inputCount;
        List<ItemStack> outputs = new ArrayList<>();
        for (int operation = 0; operation < operations; operation++) {
            for (ItemStack rolled : roll(input, random)) {
                for (ItemStack existing : outputs) {
                    if (rolled.isEmpty()) break;
                    if (!ItemStack.isSameItemSameComponents(existing, rolled)) continue;
                    int moved = Math.min(rolled.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(moved); rolled.shrink(moved);
                }
                while (!rolled.isEmpty()) outputs.add(rolled.split(Math.min(rolled.getCount(), rolled.getMaxStackSize())));
            }
        }
        return new Batch(operations * inputCount, outputs);
    }
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return makeResult(input.item()); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }
    @Override public RecipeSerializer<?> getSerializer() { return AlchemicalLaser.LASER_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return AlchemicalLaser.LASER_RECIPE.get(); }
    @Override public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, ingredient); }
    @Override public boolean isSpecial() { return true; }
    public static final class Serializer implements RecipeSerializer<LaserRecipe> {
        private static final StreamCodec<RegistryFriendlyByteBuf, LaserRecipe> STREAM = StreamCodec.of(
            (buf, r) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.ingredient);
                buf.writeVarInt(r.inputCount); buf.writeResourceLocation(r.lens);
                ItemStack.STREAM_CODEC.encode(buf, r.result); buf.writeVarInt(r.duration); buf.writeVarInt(r.minimumRpm);
                buf.writeBoolean(r.secondary.isPresent()); r.secondary.ifPresent(s -> ItemStack.STREAM_CODEC.encode(buf, s));
                buf.writeFloat(r.secondaryChance); buf.writeBoolean(r.copyComponents);
            }, buf -> new LaserRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), buf.readVarInt(),
                buf.readResourceLocation(), ItemStack.STREAM_CODEC.decode(buf), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean() ? Optional.of(ItemStack.STREAM_CODEC.decode(buf)) : Optional.empty(), buf.readFloat(), buf.readBoolean()));
        @Override public MapCodec<LaserRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, LaserRecipe> streamCodec() { return STREAM; }
    }
}
