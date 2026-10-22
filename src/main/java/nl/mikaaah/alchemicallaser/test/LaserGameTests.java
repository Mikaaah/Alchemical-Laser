package nl.mikaaah.alchemicallaser.test;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import nl.mikaaah.alchemicallaser.*;

@GameTestHolder(AlchemicalLaser.ID)
@PrefixGameTestTemplate(false)
public class LaserGameTests {
    private static final BlockPos EMITTER = new BlockPos(2,3,2), DEPOT = new BlockPos(2,1,2), MOTOR = new BlockPos(2,3,3);
    private static LaserBlockEntity setup(GameTestHelper h, String lens, int count) {
        h.setBlock(DEPOT,AllBlocks.DEPOT.getDefaultState());
        h.setBlock(EMITTER,AlchemicalLaser.EMITTER.get().defaultBlockState());
        h.setBlock(MOTOR,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING,Direction.NORTH));
        var motor=(CreativeMotorBlockEntity)h.getBlockEntity(MOTOR);motor.generatedSpeed.setValue(32);motor.updateGeneratedRotation();
        var be=(LaserBlockEntity)h.getBlockEntity(EMITTER);be.exchangeLens(AlchemicalLaser.LENSES.get(lens).toStack());
        var depot=(DepotBlockEntity)h.getBlockEntity(DEPOT);
        depot.getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).setCenteredHeldItem(
            new com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack(new ItemStack(Items.SAND,count)));
        return be;
    }
    @GameTest(template="empty",timeoutTicks=220)
    public static void convertsSandOnDepot(GameTestHelper h) {
        setup(h,"red",1);
        h.succeedWhen(() -> {
            var stack=((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).getHeldItemStack();
            h.assertTrue(stack.is(Items.GLASS)&&stack.getCount()==1,"Expected exactly one glass from one sand");
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void wrongLensDoesNotConsume(GameTestHelper h) {
        setup(h,"blue",3);
        h.runAfterDelay(60,() -> {
            var stack=((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).getHeldItemStack();
            h.assertTrue(stack.is(Items.SAND)&&stack.getCount()==3,"Wrong lens must leave input untouched");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=160)
    public static void redstoneDoesNotConsume(GameTestHelper h) {
        setup(h,"red",2);h.setBlock(EMITTER.east(),Blocks.REDSTONE_BLOCK);
        h.runAfterDelay(120,() -> {
            var stack=((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).getHeldItemStack();
            h.assertTrue(stack.is(Items.SAND)&&stack.getCount()==2,"Redstone-disabled emitter consumed items");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=250)
    public static void removingRedstoneRestartsDepot(GameTestHelper h) {
        setup(h,"red",1);h.setBlock(EMITTER.east(),Blocks.REDSTONE_BLOCK);
        h.runAfterDelay(45,() -> h.setBlock(EMITTER.east(),Blocks.AIR));
        h.succeedWhen(() -> {
            var stack=((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).getHeldItemStack();
            h.assertTrue(stack.is(Items.GLASS),"Depot did not restart after redstone was removed");
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void interruptedCycleRetainsInput(GameTestHelper h) {
        setup(h,"red",2);
        h.runAfterDelay(45,() -> h.setBlock(MOTOR,Blocks.AIR));
        h.runAfterDelay(125,() -> {
            var stack=((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).getHeldItemStack();
            h.assertTrue(stack.is(Items.SAND)&&stack.getCount()==2,"Interrupted cycle changed or lost input");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void lensSwapReturnsOriginal(GameTestHelper h) {
        var be=setup(h,"red",1);
        ItemStack returned=be.exchangeLens(AlchemicalLaser.LENSES.get("purple").toStack());
        h.assertTrue(returned.is(AlchemicalLaser.LENSES.get("red").get())&&returned.getCount()==1,"Lens exchange did not return exactly one original lens");
        h.assertTrue(be.lens().is(AlchemicalLaser.LENSES.get("purple").get()),"New lens was not installed");
        h.assertTrue(be.exchangeLens(ItemStack.EMPTY).getCount()==1&&be.lens().isEmpty(),"Removing the lens duplicated or lost it");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=500)
    public static void stackedInputsAreConserved(GameTestHelper h) {
        setup(h,"red",3);
        h.succeedWhen(() -> {
            var handler=h.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,h.absolutePos(DEPOT),Direction.DOWN);
            h.assertTrue(handler!=null,"Depot has no item capability");
            int sand=0,glass=0;
            for(int slot=0;slot<handler.getSlots();slot++) {
                var stack=handler.getStackInSlot(slot);
                if(stack.is(Items.SAND))sand+=stack.getCount();
                if(stack.is(Items.GLASS))glass+=stack.getCount();
            }
            h.assertTrue(sand==0&&glass==3,"Three sand must produce exactly three glass, including buffered output");
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void recipeQuantitiesAndComponents(GameTestHelper h) {
        var input=new ItemStack(Items.AMETHYST_SHARD,3);
        input.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Retained name"));
        var output=new ItemStack(Items.QUARTZ,2);
        var recipe=new nl.mikaaah.alchemicallaser.recipe.LaserRecipe(net.minecraft.world.item.crafting.Ingredient.of(Items.AMETHYST_SHARD),
            2,AlchemicalLaser.id("purple_lens"),output,40,16,java.util.Optional.of(new ItemStack(Items.REDSTONE)),1f,true);
        h.assertTrue(recipe.matchesInput(input,AlchemicalLaser.LENSES.get("purple").toStack()),"Counted input did not match");
        h.assertTrue(!recipe.matchesInput(input.copyWithCount(1),AlchemicalLaser.LENSES.get("purple").toStack()),"Insufficient quantity matched");
        var results=recipe.roll(input,h.getLevel().random);
        h.assertTrue(results.size()==2&&results.get(0).is(Items.QUARTZ)&&results.get(0).getCount()==2,"Recipe output/guaranteed secondary is incorrect");
        h.assertTrue(results.get(0).getHoverName().getString().equals("Retained name"),"Input components were not retained");
        h.assertTrue(input.getCount()==3,"Recipe simulation mutated its input");
        var ops=h.getLevel().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var encoded=nl.mikaaah.alchemicallaser.recipe.LaserRecipe.CODEC.codec().encodeStart(ops,recipe).getOrThrow();
        var decoded=nl.mikaaah.alchemicallaser.recipe.LaserRecipe.CODEC.codec().parse(ops,encoded).getOrThrow();
        h.assertTrue(decoded.inputCount()==2&&decoded.secondary().isPresent()&&decoded.copyComponents(),"Recipe codec roundtrip lost fields");h.succeed();
    }
    private static void beltTest(GameTestHelper h,int rpm) {
        h.setBlock(EMITTER,AlchemicalLaser.EMITTER.get().defaultBlockState());
        h.setBlock(MOTOR,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING,Direction.NORTH));
        var power=(CreativeMotorBlockEntity)h.getBlockEntity(MOTOR);power.generatedSpeed.setValue(32);power.updateGeneratedRotation();
        ((LaserBlockEntity)h.getBlockEntity(EMITTER)).exchangeLens(AlchemicalLaser.LENSES.get("red").toStack());
        BlockPos start=new BlockPos(0,1,2),end=new BlockPos(4,1,2),drive=new BlockPos(0,1,3);
        var shaft=AllBlocks.SHAFT.getDefaultState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,Direction.Axis.Z);
        h.setBlock(start,shaft);h.setBlock(end,shaft);
        com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem.createBelts(h.getLevel(),h.absolutePos(start),h.absolutePos(end));
        h.setBlock(drive,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING,Direction.NORTH));
        var motor=(CreativeMotorBlockEntity)h.getBlockEntity(drive);motor.generatedSpeed.setValue(rpm);motor.updateGeneratedRotation();
        h.runAfterDelay(20,() -> {
            var belt=(com.simibubi.create.content.kinetics.belt.BeltBlockEntity)h.getBlockEntity(DEPOT);
            belt.getBehaviour(com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour.TYPE)
                .handleInsertion(new ItemStack(Items.SAND),Direction.UP,false);
        });
        h.succeedWhen(() -> {
            var belt=(com.simibubi.create.content.kinetics.belt.BeltBlockEntity)h.getBlockEntity(start);
            var controller=belt.getControllerBE();h.assertTrue(controller!=null,"Belt controller missing");
            int glass=0,sand=0;
            for(var item:controller.getInventory().getTransportedItems()) {
                if(item.stack.is(Items.GLASS))glass+=item.stack.getCount();
                if(item.stack.is(Items.SAND))sand+=item.stack.getCount();
            }
            h.assertTrue(glass==1&&sand==0,"Expected exactly one converted glass moving on the belt");
        });
    }
    @GameTest(template="empty",timeoutTicks=260)
    public static void beltProcessesForward(GameTestHelper h) { beltTest(h,32); }
    @GameTest(template="empty",timeoutTicks=260)
    public static void beltProcessesReverse(GameTestHelper h) { beltTest(h,-32); }
    private static void speedTest(GameTestHelper h,int rpm) {
        setup(h,"red",1);
        var motor=(CreativeMotorBlockEntity)h.getBlockEntity(MOTOR);motor.generatedSpeed.setValue(rpm);motor.updateGeneratedRotation();
        h.succeedWhen(() -> {
            var stack=((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).getHeldItemStack();
            h.assertTrue(stack.is(Items.GLASS)&&stack.getCount()==1,"Expected one glass at "+rpm+" RPM");
        });
    }
    @GameTest(template="empty",timeoutTicks=300)
    public static void processesAt16Rpm(GameTestHelper h) { speedTest(h,16); }
    @GameTest(template="empty",timeoutTicks=180)
    public static void processesAt64Rpm(GameTestHelper h) { speedTest(h,64); }
    @GameTest(template="empty",timeoutTicks=140)
    public static void processesAt128Rpm(GameTestHelper h) { speedTest(h,128); }
    @GameTest(template="empty",timeoutTicks=100)
    public static void processesAt256Rpm(GameTestHelper h) { speedTest(h,256); }
    @GameTest(template="empty",timeoutTicks=100)
    public static void belowMinimumSpeedDoesNotConsume(GameTestHelper h) {
        setup(h,"red",2);
        var motor=(CreativeMotorBlockEntity)h.getBlockEntity(MOTOR);motor.generatedSpeed.setValue(8);motor.updateGeneratedRotation();
        h.runAfterDelay(60,() -> {
            var stack=((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).getHeldItemStack();
            h.assertTrue(stack.is(Items.SAND)&&stack.getCount()==2,"Below-minimum speed changed the input");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void acceptsOnlyAlignedSideShafts(GameTestHelper h) {
        var be=setup(h,"red",1);
        var block=AlchemicalLaser.EMITTER.get();
        var state=h.getBlockState(EMITTER);
        h.assertTrue(block.getRotationAxis(state)==Direction.Axis.Z,"Drive shaft must rotate horizontally");
        for(Direction face:Direction.values()) {
            h.assertTrue(block.hasShaftTowards(h.getLevel(),h.absolutePos(EMITTER),state,face)==(face.getAxis()==Direction.Axis.Z),
                "Unexpected shaft connection on "+face);
        }
        h.runAfterDelay(20,() -> {
            h.assertTrue(Math.abs(be.getSpeed())==32,"Side shaft did not drive emitter"); h.succeed();
        });
    }
    private static void specialistTest(GameTestHelper h,String lens,Item input,int count,Item output,int amount,int rpm) {
        setup(h,lens,0);
        var motor=(CreativeMotorBlockEntity)h.getBlockEntity(MOTOR);
        motor.generatedSpeed.setValue(rpm);motor.updateGeneratedRotation();
        ((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE)
            .setCenteredHeldItem(new com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack(new ItemStack(input,count)));
        h.succeedWhen(() -> {
            var stack=((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).getHeldItemStack();
            h.assertTrue(stack.is(output)&&stack.getCount()==amount,"Wrong "+lens+" lens result");
            var be=(LaserBlockEntity)h.getBlockEntity(EMITTER);
            h.assertTrue(be.lens().is(AlchemicalLaser.LENSES.get(lens).get())&&be.lens().getCount()==1,"Lens was consumed");
        });
    }
    @GameTest(template="empty",timeoutTicks=250)
    public static void growthLensProcessesSeeds(GameTestHelper h) { specialistTest(h,"growth",Items.WHEAT_SEEDS,1,Items.WHEAT,1,32); }
    @GameTest(template="empty",timeoutTicks=250)
    public static void convergenceConsumesFourShards(GameTestHelper h) { specialistTest(h,"convergence",Items.AMETHYST_SHARD,4,Items.AMETHYST_BLOCK,1,32); }
    @GameTest(template="empty",timeoutTicks=250)
    public static void splittingReturnsFourShards(GameTestHelper h) { specialistTest(h,"splitting",Items.AMETHYST_BLOCK,1,Items.AMETHYST_SHARD,4,32); }
    @GameTest(template="empty",timeoutTicks=250)
    public static void powerLensProcessesObsidian(GameTestHelper h) { specialistTest(h,"power",Items.OBSIDIAN,1,Items.CRYING_OBSIDIAN,1,64); }
    @GameTest(template="empty",timeoutTicks=250)
    public static void eclipseLensProcessesSculk(GameTestHelper h) { specialistTest(h,"eclipse",Items.SCULK,1,Items.ECHO_SHARD,1,128); }
    @GameTest(template="empty",timeoutTicks=250)
    public static void convergenceRequiresFullInput(GameTestHelper h) {
        setup(h,"convergence",0);
        ((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE)
            .setCenteredHeldItem(new com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack(new ItemStack(Items.AMETHYST_SHARD,3)));
        h.runAfterDelay(220,() -> {
            var stack=((DepotBlockEntity)h.getBlockEntity(DEPOT)).getBehaviour(com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE).getHeldItemStack();
            h.assertTrue(stack.is(Items.AMETHYST_SHARD)&&stack.getCount()==3,"Incomplete convergence input was consumed");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=120)
    public static void verticalShaftCannotDriveLaser(GameTestHelper h) {
        setup(h,"red",1);h.setBlock(MOTOR,Blocks.AIR);
        var above=EMITTER.above();
        h.setBlock(above,AllBlocks.SHAFT.getDefaultState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,Direction.Axis.Y));
        h.setBlock(above.above(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING,Direction.DOWN));
        var motor=(CreativeMotorBlockEntity)h.getBlockEntity(above.above());motor.generatedSpeed.setValue(32);motor.updateGeneratedRotation();
        h.runAfterDelay(50,() -> {
            h.assertTrue(((LaserBlockEntity)h.getBlockEntity(EMITTER)).getSpeed()==0,"Vertical shaft drove laser");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=120)
    public static void overheadCogCannotDriveLaser(GameTestHelper h) {
        setup(h,"red",1);h.setBlock(MOTOR,Blocks.AIR);
        var above=EMITTER.above();
        h.setBlock(above,AllBlocks.LARGE_COGWHEEL.getDefaultState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,Direction.Axis.X));
        h.setBlock(above.east(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING,Direction.WEST));
        var motor=(CreativeMotorBlockEntity)h.getBlockEntity(above.east());motor.generatedSpeed.setValue(32);motor.updateGeneratedRotation();
        h.runAfterDelay(50,() -> {
            h.assertTrue(((LaserBlockEntity)h.getBlockEntity(EMITTER)).getSpeed()==0,"Removed overhead cog drive is still active");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=120)
    public static void acceptsSideShaftOnRotatedLaser(GameTestHelper h) {
        setup(h,"red",1);h.setBlock(MOTOR,Blocks.AIR);
        h.setBlock(EMITTER,AlchemicalLaser.EMITTER.get().defaultBlockState().setValue(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING,Direction.EAST));
        h.setBlock(EMITTER.east(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING,Direction.WEST));
        var motor=(CreativeMotorBlockEntity)h.getBlockEntity(EMITTER.east());motor.generatedSpeed.setValue(-32);motor.updateGeneratedRotation();
        h.runAfterDelay(50,() -> {
            h.assertTrue(Math.abs(((LaserBlockEntity)h.getBlockEntity(EMITTER)).getSpeed())==32,"Rotated emitter did not accept side shaft");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=120)
    public static void acceptsOppositeSideShaft(GameTestHelper h) {
        setup(h,"red",1);h.setBlock(MOTOR,Blocks.AIR);
        h.setBlock(EMITTER.north(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING,Direction.SOUTH));
        var motor=(CreativeMotorBlockEntity)h.getBlockEntity(EMITTER.north());motor.generatedSpeed.setValue(-32);motor.updateGeneratedRotation();
        h.runAfterDelay(50,() -> {
            h.assertTrue(Math.abs(((LaserBlockEntity)h.getBlockEntity(EMITTER)).getSpeed())==32,"Opposite shaft end did not accept power");h.succeed();
        });
    }
}
