package nl.mikaaah.alchemicallaser;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** The controller-style friction drive: one horizontal large cog directly above. */
public final class LaserDrive {
    private LaserDrive() {}
    public static boolean connects(KineticBlockEntity first, KineticBlockEntity second) {
        if (first instanceof LaserBlockEntity) return accepts(first, second);
        if (second instanceof LaserBlockEntity) return accepts(second, first);
        return false;
    }
    private static boolean accepts(KineticBlockEntity laser, KineticBlockEntity cog) {
        if (!cog.getBlockPos().equals(laser.getBlockPos().above())) return false;
        BlockState state = cog.getBlockState();
        if (!ICogWheel.isLargeCog(state)) return false;
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        return axis != Direction.Axis.Y && axis != ((LaserBlock)laser.getBlockState().getBlock()).getRotationAxis(laser.getBlockState());
    }
}
