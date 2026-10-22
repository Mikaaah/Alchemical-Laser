package nl.mikaaah.alchemicallaser.mixin;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import nl.mikaaah.alchemicallaser.LaserDrive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RotationPropagator.class, remap = false)
public abstract class RotationPropagatorMixin {
    @Inject(method = "getRotationSpeedModifier", at = @At("HEAD"), cancellable = true, remap = false)
    private static void alchemicallaser$topCogDrive(KineticBlockEntity from, KineticBlockEntity to,
            CallbackInfoReturnable<Float> result) {
        if (LaserDrive.connects(from, to)) result.setReturnValue(1f);
    }
}
