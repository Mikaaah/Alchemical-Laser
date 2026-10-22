package nl.mikaaah.alchemicallaser.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;
import nl.mikaaah.alchemicallaser.*;

public final class LaserEffects {
    public static void tick(LaserBlockEntity be) {
        var level = be.getLevel(); var pos = be.getBlockPos();
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.distanceToSqr(pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5) > 4096) return;
        if (!be.isVirtual() && be.isBeamActive() && !(be.getSoundInstance() instanceof BeamSound sound && !sound.isStopped())) {
            var sound = new BeamSound(be); be.setSoundInstance(sound); mc.getSoundManager().play(sound);
        }
        boolean charging = be.phase == LaserBlockEntity.Phase.CHARGING;
        boolean completing = be.phase == LaserBlockEntity.Phase.COMPLETING;
        if (!be.isBeamActive() && !charging && !completing) return;
        if (level.getGameTime() % (completing ? 2 : 3) != 0) return;
        int rgb = AlchemicalLaser.lensColor(be.lens());
        var dust = new DustParticleOptions(new Vector3f((rgb>>16&255)/255f, (rgb>>8&255)/255f, (rgb&255)/255f), .65f);
        double x = pos.getX()+.5, y = pos.getY(), z = pos.getZ()+.5;
        if (charging) {
            y -= .375;
            for (int i=0;i<2;i++) {
                double a = level.random.nextDouble()*Math.PI*2, dx = Math.cos(a)*.35, dz = Math.sin(a)*.35;
                level.addParticle(ParticleTypes.ENCHANT,x+dx,y+.15,z+dz,-dx*.8,0,-dz*.8);
                level.addParticle(dust,x+dx*.5,y+.18,z+dz*.5,0,.01,0);
            }
        } else {
            for (int i=0;i<(completing?5:2);i++) level.addParticle(dust,x+(level.random.nextDouble()-.5)*.2,y-1.02,
                z+(level.random.nextDouble()-.5)*.2,0,.025,0);
            level.addParticle(ParticleTypes.ELECTRIC_SPARK,x,y-1.02,z,(level.random.nextDouble()-.5)*.06,.04,(level.random.nextDouble()-.5)*.06);
        }
    }
    public static void complete(LaserBlockEntity be) {
        var level=be.getLevel();var p=be.getBlockPos();int rgb=AlchemicalLaser.lensColor(be.lens());
        var dust=new DustParticleOptions(new Vector3f((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f),.9f);
        for(int i=0;i<8;i++) {
            double a=i*Math.PI/4,dx=Math.cos(a),dz=Math.sin(a);
            level.addParticle(dust,p.getX()+.5+dx*.12,p.getY()-1.02,p.getZ()+.5+dz*.12,dx*.04,.04,dz*.04);
        }
        level.addParticle(ParticleTypes.END_ROD,p.getX()+.5,p.getY()-1,p.getZ()+.5,0,.025,0);
    }
    private static final class BeamSound extends AbstractTickableSoundInstance {
        private final LaserBlockEntity be;
        BeamSound(LaserBlockEntity be) {
            super(AlchemicalLaser.AUDIO.get("beam").get(), SoundSource.BLOCKS, RandomSource.create());
            this.be=be; looping=true; delay=0; relative=false; attenuation=SoundInstance.Attenuation.LINEAR;
            x=be.getBlockPos().getX()+.5; y=be.getBlockPos().getY()+.4; z=be.getBlockPos().getZ()+.5;
        }
        @Override public void tick() {
            var mc=Minecraft.getInstance();
            if (be.isRemoved() || be.getLevel()!=mc.level || !be.isBeamActive() || mc.player==null
                || mc.player.distanceToSqr(x,y,z)>4096) { stop(); return; }
            volume=.18f*LaserConfig.SOUND_VOLUME.get().floatValue();
            pitch=be.lensPitch()*(.9f+Math.min(Math.abs(be.getSpeed()),256)/1024f);
        }
        @Override public boolean canStartSilent() { return true; }
    }
}
