package com.raishxn.gtna.client.renderer.machine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.godforge.ForgeOfGodsMachine;

import java.util.HashMap;
import java.util.Map;

/**
 * GTNH {@code GT_MACHINES_GOD_FORGE_LOOP}: the 22 second loop played at the controller while the star is active
 * (GTNH restarts it every {@code SOUND_LOOP_LENGTH} ticks; here it simply loops). Muffled controllers stay silent.
 */
@OnlyIn(Dist.CLIENT)
public final class ForgeOfGodsSound extends AbstractTickableSoundInstance {

    private static final SoundEvent LOOP = SoundEvent.createVariableRangeEvent(GTNACORE.id("godforge.loop"));
    private static final Map<BlockPos, ForgeOfGodsSound> PLAYING = new HashMap<>();

    private final ForgeOfGodsMachine machine;

    private ForgeOfGodsSound(ForgeOfGodsMachine machine) {
        super(LOOP, SoundSource.BLOCKS, RandomSource.create());
        this.machine = machine;
        BlockPos pos = machine.getPos();
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
    }

    /** Called from the renderer each frame the star is drawn. */
    public static void ensurePlaying(ForgeOfGodsMachine machine) {
        if (machine.isMuffled()) return;
        BlockPos pos = machine.getPos().immutable();
        ForgeOfGodsSound sound = PLAYING.get(pos);
        if (sound != null && !sound.isStopped() && sound.machine == machine) return;
        sound = new ForgeOfGodsSound(machine);
        PLAYING.put(pos, sound);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    @Override
    public void tick() {
        if (machine.isInValid() || !machine.isRenderActive() || machine.isMuffled() ||
                machine.getLevel() != Minecraft.getInstance().level) {
            PLAYING.remove(machine.getPos(), this);
            stop();
        }
    }
}
