package nl.mikaaah.alchemicallaser;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class LaserConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MIN_RPM, MAX_RPM, CHARGE_TICKS;
    public static final ModConfigSpec.DoubleValue STRESS, SOUND_VOLUME;
    static {
        var b = new ModConfigSpec.Builder();
        MIN_RPM = b.comment("Minimum speed needed to start a recipe.").defineInRange("minimumRpm", 16, 1, 256);
        MAX_RPM = b.comment("Speed cap for duration scaling. Recipe time is measured at 32 RPM.").defineInRange("maximumEffectiveRpm", 256, 16, 4096);
        CHARGE_TICKS = b.comment("Fixed charging period before each batch.").defineInRange("chargeTicks", 12, 1, 200);
        STRESS = b.comment("Constant stress impact per RPM while connected, matching Create network accounting.").defineInRange("stressImpact", 8.0, 0.0, 1024.0);
        SOUND_VOLUME = b.comment("Volume multiplier for all laser sounds.").defineInRange("soundVolume", 0.65, 0.0, 2.0);
        SPEC = b.build();
    }
}
