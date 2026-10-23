package nl.mikaaah.alchemicallaser;

import java.util.*;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import nl.mikaaah.alchemicallaser.recipe.LaserRecipe;

public class LaserBlockEntity extends KineticBlockEntity {
    public final ItemStackHandler lensInventory = new ItemStackHandler(1) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return !AlchemicalLaser.lensName(stack).equals("none"); }
        @Override protected void onContentsChanged(int slot) {
            cachedInput = ItemStack.EMPTY; cached = null; resetCycle();
            if (level != null && !level.isClientSide) { setChanged(); sendData(); }
        }
    };
    public enum Phase { IDLE, READY, CHARGING, PROCESSING, COMPLETING }
    public Phase phase = Phase.IDLE;
    public float progress;
    private int charge;
    private RecipeHolder<LaserRecipe> cached;
    private ItemStack cachedInput = ItemStack.EMPTY;
    private ItemStack cycleInput = ItemStack.EMPTY;
    private TransportedItemStack target;
    private long lastCallback = -100, lastAdvance = -100, nextLookup;
    private Object soundInstance;
    private final Map<UUID, LooseCycle> looseCycles = new HashMap<>();
    private static final class LooseCycle {
        final RecipeHolder<LaserRecipe> recipe;
        final ItemStack input;
        int charge;
        float progress;
        LooseCycle(RecipeHolder<LaserRecipe> recipe, ItemStack input) { this.recipe = recipe; this.input = input.copy(); }
    }
    public LaserBlockEntity(BlockPos pos, BlockState state) { super(AlchemicalLaser.LASER_ENTITY.get(), pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new BeltProcessingBehaviour(this).whenItemEnters(this::process).whileItemHeld(this::process));
    }
    @Override public float calculateStressApplied() { return lastStressApplied = LaserConfig.STRESS.get().floatValue(); }
    public ItemStack lens() { return lensInventory.getStackInSlot(0); }
    public ItemStack exchangeLens(ItemStack replacement) {
        ItemStack previous = lens().copy(); lensInventory.setStackInSlot(0, replacement);
        sound("lens_install", .55f, 1f); return previous;
    }
    public boolean hasPower() { return Math.abs(getSpeed()) >= LaserConfig.MIN_RPM.get() && !level.hasNeighborSignal(worldPosition); }
    public boolean isBeamActive() { return phase == Phase.PROCESSING && hasPower(); }
    private void setPhase(Phase state) {
        if (phase == state) return;
        phase = state; sendData();
    }
    public void resetCycle() {
        if (level != null && !level.isClientSide && (phase == Phase.CHARGING || phase == Phase.PROCESSING)) sound("shutdown", .25f, lensPitch());
        target = null; cycleInput = ItemStack.EMPTY; progress = 0; charge = 0; looseCycles.clear();
        if (phase != Phase.IDLE) { phase = Phase.IDLE; if (level != null && !level.isClientSide) sendData(); }
    }
    private RecipeHolder<LaserRecipe> recipe(ItemStack input) {
        boolean sameInput = ItemStack.isSameItemSameComponents(input, cachedInput) && input.getCount() == cachedInput.getCount();
        if (sameInput && cached != null && level.getRecipeManager().byKey(cached.id()).orElse(null) == cached
            && cached.value().matchesInput(input, lens())) return cached;
        if (sameInput && cached == null && level.getGameTime() < nextLookup) return null;
        cachedInput = input.copy(); nextLookup = level.getGameTime() + 20;
        cached = level.getRecipeManager().getAllRecipesFor(AlchemicalLaser.LASER_RECIPE.get()).stream()
            .filter(r -> r.value().matchesInput(input, lens())).sorted(Comparator.comparing(r -> r.id().toString())).findFirst().orElse(null);
        return cached;
    }
    private ProcessingResult process(TransportedItemStack stack, TransportedItemStackHandlerBehaviour handler) {
        if (level == null || level.isClientSide) return ProcessingResult.PASS;
        lastCallback = level.getGameTime();
        var previousRecipe = cached;
        var found = recipe(stack.stack);
        if (target != null && previousRecipe != found) resetCycle();
        if (lens().isEmpty() || found == null) { resetCycle(); return ProcessingResult.PASS; }
        if (!looseCycles.isEmpty()) resetCycle();
        if (target != null && target != stack) resetCycle();
        if (target == null || !ItemStack.matches(cycleInput, stack.stack)) {
            target = stack; cycleInput = stack.stack.copy(); progress = 0; charge = 0;
            setPhase(Phase.READY);
        }
        if (!hasPower() || Math.abs(getSpeed()) < found.value().minimumRpm()) {
            // Release on power loss. The unconsumed stack can travel onward or restart on a Depot.
            resetCycle(); return ProcessingResult.PASS;
        }
        if (lastAdvance == level.getGameTime()) return ProcessingResult.HOLD;
        lastAdvance = level.getGameTime();
        if (charge < LaserConfig.CHARGE_TICKS.get()) {
            if (charge == 0) { sound("activation", .35f, .9f); sound("charge", .45f, lensPitch()); }
            setPhase(Phase.CHARGING); charge++;
            return ProcessingResult.HOLD;
        }
        if (phase != Phase.PROCESSING) { setPhase(Phase.PROCESSING); sound("fire", .45f, lensPitch()); }
        progress += Math.min(Math.abs(getSpeed()), LaserConfig.MAX_RPM.get()) / 32f;
        if (level.getGameTime() % 8 == 0) sendData();
        if (progress < found.value().duration()) return ProcessingResult.HOLD;
        // Commit every complete recipe quantity in the held stack, in one cycle.
        var batch = found.value().rollBatch(stack.stack, level.random);
        List<TransportedItemStack> results = new ArrayList<>();
        for (ItemStack out : batch.outputs()) {
            while (!out.isEmpty()) {
                TransportedItemStack t = stack.copy(); t.stack = out.split(Math.min(out.getCount(), out.getMaxStackSize()));
                t.locked = false; t.clearFanProcessingData(); results.add(t);
            }
        }
        TransportedItemStack remainder = stack.copy(); remainder.stack.shrink(batch.consumed());
        remainder.locked = false;
        if (remainder.stack.isEmpty()) handler.handleProcessingOnItem(stack, TransportedResult.convertTo(results));
        else handler.handleProcessingOnItem(stack, TransportedResult.convertToAndLeaveHeld(results, remainder));
        sound("complete", .4f, lensPitch()); setPhase(Phase.COMPLETING);
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
        target = null; cycleInput = ItemStack.EMPTY; progress = 0; charge = 0;
        setChanged();
        return ProcessingResult.PASS;
    }
    public float lensPitch() {
        LensDefinition definition=lens().getItemHolder().getData(LensDefinition.TYPE);
        if(definition!=null)return definition.pitch();
        return switch (AlchemicalLaser.lensName(lens())) { case "growth" -> 1.15f; case "convergence" -> .9f;
            case "splitting" -> 1.3f; case "power" -> .7f; case "eclipse" -> .6f; default -> 1f; }; }
    private void sound(String name, float volume, float pitch) {
        if (level != null && !level.isClientSide) level.playSound(null, worldPosition, AlchemicalLaser.AUDIO.get(name).get(),
            SoundSource.BLOCKS, volume * LaserConfig.SOUND_VOLUME.get().floatValue(), pitch);
    }
    @Override public void tick() {
        super.tick();
        if (level.isClientSide) {
            nl.mikaaah.alchemicallaser.client.LaserEffects.tick(this);
            return;
        }
        if (!hasPower() || lens().isEmpty()) { resetCycle(); return; }
        if (target != null && level.getGameTime() - lastCallback <= 2) return;
        if (target != null) resetCycle();
        tickLooseItems();
    }
    private void tickLooseItems() {
        // The hanging lantern occupies the upper part of this air gap. Never fire through an obstruction.
        BlockPos gap = worldPosition.below();
        if (!level.getBlockState(gap).getCollisionShape(level, gap).isEmpty()) { resetCycle(); return; }
        if (looseCycles.isEmpty() && level.getGameTime() % 5 != 0) {
            if (level.getGameTime() - lastCallback > 2 && phase != Phase.IDLE) resetCycle();
            return;
        }
        double x = worldPosition.getX(), y = worldPosition.getY(), z = worldPosition.getZ();
        var items = level.getEntitiesOfClass(ItemEntity.class, new AABB(x+.2,y-1.25,z+.2,x+.8,y-.35,z+.8),
            entity -> entity.isAlive() && !entity.getItem().isEmpty());
        Set<UUID> present = new HashSet<>();
        boolean processing = false, charging = false, completed = false;
        for (ItemEntity item : items) {
            var input = item.getItem();
            var found = recipe(input);
            if (found == null || Math.abs(getSpeed()) < found.value().minimumRpm()) continue;
            UUID id = item.getUUID(); present.add(id);
            LooseCycle cycle = looseCycles.get(id);
            if (cycle == null || cycle.recipe != found || !ItemStack.matches(cycle.input, input)) {
                cycle = new LooseCycle(found, input); looseCycles.put(id, cycle);
            }
            if (cycle.charge < LaserConfig.CHARGE_TICKS.get()) {
                cycle.charge++; charging = true; continue;
            }
            cycle.progress += Math.min(Math.abs(getSpeed()), LaserConfig.MAX_RPM.get()) / 32f;
            if (cycle.progress < found.value().duration()) { processing = true; continue; }
            var batch = found.value().rollBatch(input, level.random);
            List<ItemEntity> outputs = new ArrayList<>();
            boolean spawned = true;
            for (ItemStack out : batch.outputs()) {
                var entity = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), out);
                entity.setDeltaMovement(item.getDeltaMovement()); entity.setDefaultPickUpDelay();
                if (!level.addFreshEntity(entity)) { spawned = false; break; }
                outputs.add(entity);
            }
            if (!spawned) { outputs.forEach(ItemEntity::discard); continue; }
            if (batch.consumed() == input.getCount()) item.discard();
            else item.setItem(input.copyWithCount(input.getCount() - batch.consumed()));
            looseCycles.remove(id); completed = true;
        }
        looseCycles.keySet().retainAll(present);
        if (completed) {
            sound("complete", .4f, lensPitch()); level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0); setChanged();
        }
        if (processing) {
            if (phase != Phase.PROCESSING) sound("fire", .45f, lensPitch());
            setPhase(Phase.PROCESSING);
        } else if (charging) {
            if (phase != Phase.CHARGING) { sound("activation", .35f, .9f); sound("charge", .45f, lensPitch()); }
            setPhase(Phase.CHARGING);
        } else if (completed) setPhase(Phase.COMPLETING);
        else if (level.getGameTime() - lastCallback > 2) resetCycle();
    }
    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Lens", lensInventory.serializeNBT(registries));
        if (clientPacket) { tag.putInt("Phase", phase.ordinal()); tag.putFloat("Progress", progress); }
    }
    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        lensInventory.deserializeNBT(registries, tag.getCompound("Lens"));
        if (clientPacket) { phase = Phase.values()[Math.clamp(tag.getInt("Phase"), 0, Phase.values().length - 1)]; progress = tag.getFloat("Progress"); }
    }
    @Override public void destroy() {
        if (level != null && !level.isClientSide && !lens().isEmpty()) {
            Block.popResource(level, worldPosition, lens().copy()); lensInventory.setStackInSlot(0, ItemStack.EMPTY);
        }
        super.destroy();
    }
    @Override protected AABB createRenderBoundingBox() { return new AABB(worldPosition).expandTowards(0, -2, 0); }
    @Override public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        super.addToGoggleTooltip(tooltip, sneaking);
        tooltip.add(Component.translatable("goggles.alchemicallaser.lens", lens().isEmpty() ? Component.translatable("goggles.alchemicallaser.none") : lens().getHoverName()));
        tooltip.add(Component.translatable("goggles.alchemicallaser.state", Component.translatable("state.alchemicallaser." + phase.name().toLowerCase(Locale.ROOT))));
        tooltip.add(Component.translatable("goggles.alchemicallaser.minimum", LaserConfig.MIN_RPM.get()));
        tooltip.add(Component.translatable("goggles.alchemicallaser.redstone", level.hasNeighborSignal(worldPosition) ? "OFF" : "ON"));
        return true;
    }
    public Object getSoundInstance() { return soundInstance; }
    public void setSoundInstance(Object sound) { soundInstance = sound; }
    @Override public boolean triggerEvent(int event, int data) {
        if(event==1) {
            if(level!=null&&level.isClientSide) nl.mikaaah.alchemicallaser.client.LaserEffects.complete(this);
            return true;
        }
        return super.triggerEvent(event,data);
    }
}
