package nl.mikaaah.alchemicallaser.client;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Exercises the real wrapper against Create's model dimensions without a GL context. */
public final class ControllerModelCheck {
    private static BakedModel model(List<BakedQuad> quads) {
        return (BakedModel)Proxy.newProxyInstance(BakedModel.class.getClassLoader(),new Class<?>[]{BakedModel.class},
            (proxy,method,args) -> switch(method.getName()) {
                case "getQuads" -> args[1]==null ? quads : List.of();
                case "applyTransform" -> proxy;
                default -> throw new UnsupportedOperationException(method.getName());
            });
    }
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        var stream=ControllerModelCheck.class.getResourceAsStream("/assets/create/models/block/rotation_speed_controller/block.json");
        check(stream!=null,"Missing Create model");
        var json=JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
        for(var axis:List.of(Direction.Axis.Z,Direction.Axis.X)) {
            List<BakedQuad> quads=new ArrayList<>();
            for(var entry:json.getAsJsonArray("elements")) {
                var e=entry.getAsJsonObject();var lo=e.getAsJsonArray("from");var hi=e.getAsJsonArray("to");
                for(String face:e.getAsJsonObject("faces").keySet()) {
                    float x=lo.get(0).getAsFloat()/16,y=lo.get(1).getAsFloat()/16,z=lo.get(2).getAsFloat()/16;
                    float X=hi.get(0).getAsFloat()/16,Y=hi.get(1).getAsFloat()/16,Z=hi.get(2).getAsFloat()/16;
                    float[][] points=switch(face) {
                        case "up" -> new float[][]{{x,Y,z},{X,Y,z},{X,Y,Z},{x,Y,Z}};
                        case "down" -> new float[][]{{x,y,z},{X,y,z},{X,y,Z},{x,y,Z}};
                        case "north" -> new float[][]{{x,y,z},{X,y,z},{X,Y,z},{x,Y,z}};
                        case "south" -> new float[][]{{x,y,Z},{X,y,Z},{X,Y,Z},{x,Y,Z}};
                        case "west" -> new float[][]{{x,y,z},{x,y,Z},{x,Y,Z},{x,Y,z}};
                        default -> new float[][]{{X,y,z},{X,y,Z},{X,Y,Z},{X,Y,z}};
                    };
                    int[] vertices=new int[32];
                    for(int i=0;i<4;i++) {
                        float[] p=points[i];
                        if(axis==Direction.Axis.X) p=new float[]{1-p[2],p[1],p[0]};
                        for(int c=0;c<3;c++) vertices[i*8+c]=Float.floatToRawIntBits(p[c]);
                    }
                    quads.add(new BakedQuad(vertices,-1,Direction.UP,null,true));
                }
            }
            var body=quads.getFirst();var original=model(List.of(body));
            var wrapped=new ControllerTopModel(original,model(quads),axis);
            var combined=wrapped.getQuads(null,null,RandomSource.create(42));
            check(combined.size()>10&&combined.size()<quads.size(),"Expected only controller rails, not complete controller");
            check(combined.getFirst()==body,"Lantern geometry lost");
            check(wrapped.getQuads(null,Direction.UP,RandomSource.create(42)).isEmpty(),"General quads duplicated in culled pass");
            check(wrapped.getQuads(null,null,RandomSource.create(42),ModelData.EMPTY,null).size()==combined.size(),"Chunk render pass lost top rails");
            check(wrapped.getRenderPasses(null,false).getFirst()==wrapped,"Inventory render lost wrapper");
            check(wrapped.applyTransform(ItemDisplayContext.GUI,new PoseStack(),false)==wrapped,"GUI transform lost top rails");
            check(wrapped.applyTransform(ItemDisplayContext.FIXED,new PoseStack(),false)==wrapped,"Fixed transform lost top rails");
            System.out.println("Controller model check passed for axis "+axis+": "+(combined.size()-1)+" native rail quads, lantern retained, GUI and chunk passes retained.");
        }
    }
}
