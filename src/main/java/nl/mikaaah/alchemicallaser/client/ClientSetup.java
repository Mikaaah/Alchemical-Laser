package nl.mikaaah.alchemicallaser.client;

import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlock;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;
import nl.mikaaah.alchemicallaser.ponder.LaserPonderPlugin;

@EventBusSubscriber(modid = AlchemicalLaser.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent public static void models(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        for (var state : AlchemicalLaser.EMITTER.get().getStateDefinition().getPossibleStates()) {
            var axis = AlchemicalLaser.EMITTER.get().getRotationAxis(state);
            var controllerState = AllBlocks.ROTATION_SPEED_CONTROLLER.getDefaultState().setValue(SpeedControllerBlock.HORIZONTAL_AXIS, axis);
            var controller = models.get(BlockModelShaper.stateToModelLocation(controllerState));
            var key = BlockModelShaper.stateToModelLocation(state);
            if (controller != null && models.containsKey(key)) models.put(key, new ControllerTopModel(models.get(key), controller, axis));
        }
        var itemKey = new ModelResourceLocation(AlchemicalLaser.id("laser_emitter"), "inventory");
        var controller = models.get(BlockModelShaper.stateToModelLocation(AllBlocks.ROTATION_SPEED_CONTROLLER.getDefaultState()
            .setValue(SpeedControllerBlock.HORIZONTAL_AXIS, Direction.Axis.Z)));
        if (controller != null && models.containsKey(itemKey)) models.put(itemKey, new ControllerTopModel(models.get(itemKey), controller, Direction.Axis.Z));
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AlchemicalLaser.LASER_ENTITY.get(), LaserRenderer::new);
    }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PonderIndex.addPlugin(new LaserPonderPlugin()));
    }
}
