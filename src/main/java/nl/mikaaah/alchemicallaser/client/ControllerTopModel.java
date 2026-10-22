package nl.mikaaah.alchemicallaser.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

/** References Create's already-baked top rails; no Create model or texture is bundled. */
public final class ControllerTopModel extends BakedModelWrapper<BakedModel> {
    private final List<List<BakedQuad>> rails = new ArrayList<>();
    public ControllerTopModel(BakedModel lantern, BakedModel controller, Direction.Axis rollerAxis) {
        super(lantern);
        int crossCoordinate = rollerAxis == Direction.Axis.Z ? 0 : 2;
        for (int side = 0; side < 7; side++) {
            Direction face = side == 6 ? null : Direction.values()[side];
            rails.add(controller.getQuads(null, face, RandomSource.create(42)).stream()
                .filter(quad -> isTopRail(quad, crossCoordinate)).toList());
        }
    }
    private static boolean isTopRail(BakedQuad quad, int crossCoordinate) {
        int[] vertices = quad.getVertices(); int stride = vertices.length / 4;
        boolean left = true, right = true;
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * stride;
            if (Float.intBitsToFloat(vertices[offset + 1]) < .5f - .0001f) return false;
            float cross = Float.intBitsToFloat(vertices[offset + crossCoordinate]);
            left &= cross <= .3751f; right &= cross >= .6249f;
        }
        return left || right;
    }
    private List<BakedQuad> withRails(List<BakedQuad> body, Direction side) {
        var result = new ArrayList<>(body);
        result.addAll(rails.get(side == null ? 6 : side.ordinal()));
        return result;
    }
    @Override public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        return withRails(originalModel.getQuads(state, side, random), side);
    }
    @Override public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random,
            ModelData data, RenderType type) {
        return withRails(originalModel.getQuads(state, side, random, data, type), side);
    }
    @Override public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) { return List.of(this); }
    @Override public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
        originalModel.applyTransform(context, pose, leftHand);
        return this;
    }
}
