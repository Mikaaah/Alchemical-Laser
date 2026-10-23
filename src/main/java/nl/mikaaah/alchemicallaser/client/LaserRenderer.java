package nl.mikaaah.alchemicallaser.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import nl.mikaaah.alchemicallaser.*;

public class LaserRenderer extends KineticBlockEntityRenderer<LaserBlockEntity> {
    public LaserRenderer(BlockEntityRendererProvider.Context context) { super(context); }
    @Override protected void renderSafe(LaserBlockEntity be, float partial, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        // Horizontal drive shaft accepts ordinary side connections at either end.
        renderRotatingKineticBlock(be, shaft(getRotationAxisOf(be)), pose, buffer.getBuffer(RenderType.solid()), light);
        if (!be.lens().isEmpty()) {
            pose.pushPose(); pose.translate(.5, -.31, .5);
            // FIXED displays use the lens's horizontal mounting transform, flush inside the nozzle.
            // Datapack lenses can still be ordinary generated item models, as in 0.1.0.
            if (AlchemicalLaser.LENSES.values().stream().noneMatch(item -> be.lens().is(item.get())))
                pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.scale(.46f, .46f, .46f);
            Minecraft.getInstance().getItemRenderer().renderStatic(be.lens(), ItemDisplayContext.FIXED,
                be.phase == LaserBlockEntity.Phase.IDLE ? light : LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, buffer, be.getLevel(), 0);
            pose.popPose();
        }
        if (!be.isBeamActive()) return;
        int color = 0xff000000 | AlchemicalLaser.lensColor(be.lens());
        // Reuse Minecraft's animated beacon core, halo and native beacon texture.
        pose.pushPose();
        pose.translate(1, -.325, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.scale(1, .715f, 1);
        net.minecraft.client.renderer.blockentity.BeaconRenderer.renderBeaconBeam(pose, buffer,
            net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png"),
            partial, 1f, be.getLevel().getGameTime(), 0, 1, color, .10f, .14f);
        pose.popPose();
    }
    @Override public int getViewDistance() { return 64; }
}
