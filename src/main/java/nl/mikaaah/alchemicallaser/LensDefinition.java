package nl.mikaaah.alchemicallaser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
public record LensDefinition(int color, float pitch) {
    public static final Codec<LensDefinition> CODEC=RecordCodecBuilder.create(i -> i.group(
        Codec.intRange(0,0xffffff).fieldOf("color").forGetter(LensDefinition::color),
        Codec.floatRange(.5f,2f).optionalFieldOf("pitch",1f).forGetter(LensDefinition::pitch)
    ).apply(i,LensDefinition::new));
    public static final DataMapType<Item,LensDefinition> TYPE=DataMapType.builder(
        AlchemicalLaser.id("laser_lenses"),Registries.ITEM,CODEC).synced(CODEC,false).build();
}
