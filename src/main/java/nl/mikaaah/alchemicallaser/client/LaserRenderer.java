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
        // Native controller-style horizontal friction roller, driven by the cog above.
        renderRotatingKineticBlock(be, shaft(getRotationAxisOf(be)), pose, buffer.getBuffer(RenderType.solid()), light);
        float time = be.getLevel().getGameTime() + partial;
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
        int rgb = AlchemicalLaser.lensColor(be.lens());
        VertexConsumer vertex = buffer.getBuffer(RenderType.beaconBeam(AlchemicalLaser.id("textures/effect/beam.png"), true));
        float pulse = 1 + .12f * (float)Math.sin(time * 1.7);
        tube(pose, vertex, .065f * pulse, -.325f, -1.04f, rgb, 70, time);
        tube(pose, vertex, .026f * pulse, -.325f, -1.04f, rgb, 220, time);
        tube(pose, vertex, .009f, -.325f, -1.04f, 0xffffff, 255, time);
    }
    private static void tube(PoseStack pose, VertexConsumer out, float radius, float top, float bottom, int color, int alpha, float time) {
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        float v = -time * .075f;
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4, n = (i + 1) * Math.PI / 4;
            float x1 = .5f + (float)Math.cos(a) * radius, z1 = .5f + (float)Math.sin(a) * radius;
            float x2 = .5f + (float)Math.cos(n) * radius, z2 = .5f + (float)Math.sin(n) * radius;
            vertex(out, pose, x1, top, z1, 0, v, r,g,b,alpha); vertex(out, pose, x1, bottom, z1, 0, v+2,r,g,b,alpha);
            vertex(out, pose, x2, bottom,z2,1,v+2,r,g,b,alpha); vertex(out,pose,x2,top,z2,1,v,r,g,b,alpha);
        }
    }
    private static void vertex(VertexConsumer out, PoseStack pose, float x,float y,float z,float u,float v,int r,int g,int b,int a) {
        out.addVertex(pose.last().pose(),x,y,z).setColor(r,g,b,a).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(),0,1,0);
    }
    @Override public int getViewDistance() { return 64; }
}
