package nl.mikaaah.alchemicallaser.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.*;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import nl.mikaaah.alchemicallaser.*;

public class LaserScenes {
    private static final BlockPos LASER = new BlockPos(2,3,3), BELT = new BlockPos(2,1,3), SECOND = new BlockPos(4,3,3);
    private static CreateSceneBuilder base(SceneBuilder builder, SceneBuildingUtil util, String key, String title) {
        var scene = new CreateSceneBuilder(builder);
        scene.title(key, title); scene.configureBasePlate(0,0,7); scene.scaleSceneView(.85f);
        scene.world().showSection(util.select().layer(0),Direction.UP);
        scene.world().showSection(util.select().fromTo(0,1,3,6,1,3),Direction.NORTH);
        scene.world().showSection(util.select().position(LASER),Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(LASER),32);
        scene.idle(15); return scene;
    }
    private static void text(CreateSceneBuilder scene, SceneBuildingUtil util, String text, BlockPos target) {
        scene.overlay().showText(70).text(text).pointAt(util.vector().centerOf(target)).placeNearTarget().attachKeyFrame(); scene.idle(80);
    }
    private static void lens(CreateSceneBuilder scene, String color, BlockPos at) {
        scene.world().modifyBlockEntity(at,LaserBlockEntity.class,be -> be.exchangeLens(AlchemicalLaser.LENSES.get(color).toStack()));
    }
    private static void phase(CreateSceneBuilder scene, BlockPos at, LaserBlockEntity.Phase phase) {
        scene.world().modifyBlockEntity(at,LaserBlockEntity.class,be -> be.phase=phase);
    }
    public static void setup(SceneBuilder b, SceneBuildingUtil u) {
        var s=base(b,u,"laser_setup","Setting up an Alchemical Laser");
        text(s,u,"Place the emitter above a horizontal belt, leaving one air block between them, just like a Mechanical Press.",LASER);
        s.overlay().showOutline(PonderPalette.GREEN, LASER,u.select().position(LASER.below()),60);s.idle(65);
        s.world().setBlock(BELT,AllBlocks.DEPOT.getDefaultState(),true);
        text(s,u,"A Depot works at the same height. Funnels and hoppers can supply and extract items.",BELT);
        s.world().showSection(u.select().position(LASER.south()),Direction.NORTH);
        text(s,u,"Connect a shaft to either exposed end of the horizontal axle. Use a wrench to rotate the emitter and align the shaft.",LASER.south());
        s.markAsFinished();
    }
    public static void power(SceneBuilder b, SceneBuildingUtil u) {
        var s=base(b,u,"laser_power","Power and processing speed");
        s.world().showSection(u.select().position(LASER.south()),Direction.NORTH);
        s.world().setKineticSpeed(u.select().position(LASER).add(u.select().position(LASER.south())),8);
        text(s,u,"The default minimum speed is 16 RPM. Individual recipes can require a higher speed.",LASER);
        s.world().setKineticSpeed(u.select().position(LASER).add(u.select().position(LASER.south())),32);
        lens(s,"red",LASER);
        text(s,u,"Recipe durations are measured at 32 RPM. The laser always draws stress from the network while connected.",LASER);
        s.world().setKineticSpeed(u.select().position(LASER).add(u.select().position(LASER.south())),128);
        phase(s,LASER,LaserBlockEntity.Phase.PROCESSING);
        text(s,u,"Higher speed shortens processing, up to the configured effective speed cap. Charging still takes a short fixed time.",LASER);
        phase(s,LASER,LaserBlockEntity.Phase.IDLE);s.markAsFinished();
    }
    public static void processing(SceneBuilder b, SceneBuildingUtil u) {
        var s=base(b,u,"laser_belt_processing","Processing items on a belt");lens(s,"red",LASER);
        text(s,u,"With a red lens installed, sand can be transformed into glass. Only matching recipes hold an item.",LASER);
        var item=s.world().createItemOnBelt(BELT,Direction.WEST,new ItemStack(Items.SAND));
        s.world().stallBeltItem(item,true);phase(s,LASER,LaserBlockEntity.Phase.CHARGING);s.idle(20);
        phase(s,LASER,LaserBlockEntity.Phase.PROCESSING);
        text(s,u,"The item stops under the emitter while it charges and fires. Input items are consumed only when the cycle completes.",BELT);
        s.world().changeBeltItemTo(item,new ItemStack(Items.GLASS));phase(s,LASER,LaserBlockEntity.Phase.COMPLETING);s.idle(8);
        phase(s,LASER,LaserBlockEntity.Phase.IDLE);s.world().stallBeltItem(item,false);s.idle(35);
        text(s,u,"The output continues along the belt. Stacks are processed one recipe quantity per cycle; leftovers are retained.",BELT);
        s.markAsFinished();
    }
    public static void lenses(SceneBuilder b, SceneBuildingUtil u) {
        var s=base(b,u,"laser_lenses","Changing optical lenses");
        text(s,u,"Right-click with a lens to install it or exchange the installed lens. Lenses are reusable.",LASER);
        for(String color:new String[]{"red","blue","purple","white","growth","convergence","splitting","power","eclipse"}) {
            s.overlay().showControls(u.vector().centerOf(LASER),Pointing.DOWN,30).rightClick().withItem(AlchemicalLaser.LENSES.get(color).toStack());
            lens(s,color,LASER);phase(s,LASER,LaserBlockEntity.Phase.PROCESSING);s.idle(35);phase(s,LASER,LaserBlockEntity.Phase.IDLE);
        }
        text(s,u,"Nine reusable lenses select different recipes: four colored optics plus Growth, Convergence, Splitting, Power and Eclipse.",LASER);
        s.world().modifyBlockEntity(LASER,LaserBlockEntity.class,be -> be.exchangeLens(ItemStack.EMPTY));
        text(s,u,"Sneak-right-click with an empty hand to remove the lens. Item-handler automation can also insert and extract lenses.",LASER);
        s.markAsFinished();
    }
    public static void automation(SceneBuilder b, SceneBuildingUtil u) {
        var s=base(b,u,"laser_automation","Chaining laser transformations");
        s.world().showSection(u.select().position(SECOND).add(u.select().position(SECOND.south())),Direction.DOWN);
        lens(s,"purple",LASER);lens(s,"white",SECOND);
        text(s,u,"Use multiple emitters with different lenses to build a sequential processing line.",LASER);
        var item=s.world().createItemOnBelt(BELT,Direction.WEST,new ItemStack(Items.AMETHYST_SHARD));
        s.world().stallBeltItem(item,true);phase(s,LASER,LaserBlockEntity.Phase.PROCESSING);s.idle(45);
        s.world().changeBeltItemTo(item,new ItemStack(Items.QUARTZ));phase(s,LASER,LaserBlockEntity.Phase.IDLE);
        s.world().stallBeltItem(item,false);s.idle(25);
        s.world().removeItemsFromBelt(new BlockPos(4,1,3));
        var second=s.world().createItemOnBelt(new BlockPos(4,1,3),Direction.WEST,new ItemStack(Items.QUARTZ));
        s.world().stallBeltItem(second,true);phase(s,SECOND,LaserBlockEntity.Phase.PROCESSING);
        text(s,u,"Purple transforms amethyst into quartz. White then transforms quartz into prismarine crystals.",SECOND);
        s.world().changeBeltItemTo(second,new ItemStack(Items.PRISMARINE_CRYSTALS));phase(s,SECOND,LaserBlockEntity.Phase.IDLE);
        s.world().stallBeltItem(second,false);s.idle(30);
        text(s,u,"Datapacks and KubeJS can replace these examples with your own production chains.",SECOND);s.markAsFinished();
    }
    public static void redstone(SceneBuilder b, SceneBuildingUtil u) {
        var s=base(b,u,"laser_redstone","Controlling the emitter with redstone");lens(s,"red",LASER);
        phase(s,LASER,LaserBlockEntity.Phase.PROCESSING);
        text(s,u,"An unpowered emitter runs automatically whenever its lens, input and speed match a recipe.",LASER);
        s.world().setBlock(LASER.east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),true);
        s.world().showSection(u.select().position(LASER.east()),Direction.DOWN);phase(s,LASER,LaserBlockEntity.Phase.IDLE);
        text(s,u,"A powered redstone signal disables the emitter. An interrupted cycle leaves its input unchanged and releases the belt item.",LASER.east());
        s.world().setBlock(LASER.east(),Blocks.AIR.defaultBlockState(),true);
        phase(s,LASER,LaserBlockEntity.Phase.CHARGING);s.idle(20);phase(s,LASER,LaserBlockEntity.Phase.PROCESSING);
        text(s,u,"Removing the signal allows new cycles to start. A Depot can restart processing when power returns.",LASER);
        phase(s,LASER,LaserBlockEntity.Phase.IDLE);s.markAsFinished();
    }
}
