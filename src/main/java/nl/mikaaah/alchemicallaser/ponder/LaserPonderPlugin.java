package nl.mikaaah.alchemicallaser.ponder;

import net.createmod.ponder.api.registration.*;
import net.minecraft.resources.ResourceLocation;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;

public class LaserPonderPlugin implements PonderPlugin {
    private static final ResourceLocation TAG = AlchemicalLaser.id("laser_processing");
    @Override public String getModId() { return AlchemicalLaser.ID; }
    @Override public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        var scenes = helper.forComponents(AlchemicalLaser.id("laser_emitter"), AlchemicalLaser.id("red_lens"),
            AlchemicalLaser.id("blue_lens"), AlchemicalLaser.id("purple_lens"), AlchemicalLaser.id("white_lens"));
        scenes.addStoryBoard("laser_workshop", LaserScenes::setup, TAG);
        scenes.addStoryBoard("laser_workshop", LaserScenes::power, TAG);
        scenes.addStoryBoard("laser_workshop", LaserScenes::processing, TAG);
        scenes.addStoryBoard("laser_workshop", LaserScenes::lenses, TAG);
        scenes.addStoryBoard("laser_workshop", LaserScenes::automation, TAG);
        scenes.addStoryBoard("laser_workshop", LaserScenes::redstone, TAG);
    }
    @Override public void onPonderLevelRestore(net.createmod.ponder.api.level.PonderLevel level) {
        com.simibubi.create.foundation.ponder.PonderWorldBlockEntityFix.fixControllerBlockEntities(level);
    }
    @Override public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(TAG).addToIndex().item(AlchemicalLaser.EMITTER_ITEM.get(), true, false)
            .title("Alchemical Lasers").description("Kinetic processing with interchangeable optical lenses").register();
        var tag = helper.addToTag(TAG);
        for (String item : new String[]{"laser_emitter", "red_lens", "blue_lens", "purple_lens", "white_lens"}) tag.add(AlchemicalLaser.id(item));
    }
}
