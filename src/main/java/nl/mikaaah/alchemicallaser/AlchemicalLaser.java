package nl.mikaaah.alchemicallaser;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.*;
import nl.mikaaah.alchemicallaser.recipe.LaserRecipe;

@Mod(AlchemicalLaser.ID)
public final class AlchemicalLaser {
    public static final String ID = "alchemicallaser";
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ID, path); }
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final DeferredBlock<LaserBlock> EMITTER = BLOCKS.register("laser_emitter", () -> new LaserBlock(
        BlockBehaviour.Properties.of().strength(3.5f).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK).noOcclusion()));
    public static final DeferredItem<BlockItem> EMITTER_ITEM = ITEMS.registerSimpleBlockItem(EMITTER);
    public static final Map<String, DeferredItem<Item>> LENSES = new LinkedHashMap<>();
    static { for (String color : List.of("growth", "convergence", "splitting", "power", "eclipse"))
        LENSES.put(color, ITEMS.registerSimpleItem(color + "_lens", new Item.Properties().stacksTo(1))); }
    public static final Supplier<BlockEntityType<LaserBlockEntity>> LASER_ENTITY = ENTITIES.register("laser_emitter",
        () -> BlockEntityType.Builder.of(LaserBlockEntity::new, EMITTER.get()).build(null));
    public static final Supplier<RecipeType<LaserRecipe>> LASER_RECIPE = TYPES.register("laser_processing", () -> new RecipeType<>() {
        @Override public String toString() { return ID + ":laser_processing"; }
    });
    public static final Supplier<RecipeSerializer<LaserRecipe>> LASER_SERIALIZER = SERIALIZERS.register("laser_processing", LaserRecipe.Serializer::new);
    public static final Map<String, Supplier<SoundEvent>> AUDIO = new LinkedHashMap<>();
    static { for (String name : List.of("lens_install", "activation", "charge", "fire", "beam", "complete", "shutdown"))
        AUDIO.put(name, SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id(name)))); }
    static { TABS.register("main", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.alchemicallaser"))
        .icon(() -> EMITTER_ITEM.toStack()).displayItems((parameters, output) -> {
            output.accept(EMITTER_ITEM.get()); LENSES.values().forEach(lens -> output.accept(lens.get()));
        }).build()); }
    public AlchemicalLaser(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus); ITEMS.register(bus); ENTITIES.register(bus); TYPES.register(bus);
        SERIALIZERS.register(bus); SOUNDS.register(bus); TABS.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, LaserConfig.SPEC);
        bus.addListener(this::capabilities);
        bus.addListener((net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent event) -> event.register(LensDefinition.TYPE));
    }
    private void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, LASER_ENTITY.get(), (be, side) -> be.lensInventory);
    }
    public static String lensName(ItemStack stack) {
        for (var entry : LENSES.entrySet()) if (stack.is(entry.getValue().get())) return entry.getKey();
        return !stack.isEmpty() && stack.getItemHolder().getData(LensDefinition.TYPE) != null
            ? net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() : "none";
    }
    public static int lensColor(ItemStack stack) {
        LensDefinition definition=stack.getItemHolder().getData(LensDefinition.TYPE);
        if(definition!=null)return definition.color();
        return switch (lensName(stack)) { case "growth" -> 0x86c65b;
            case "convergence" -> 0xf0b65d; case "splitting" -> 0x65d6d1; case "power" -> 0xe77e47;
            case "eclipse" -> 0xa286dc; default -> 0x666b70; };
    }
}
