package ru.myffy.smoothsneak;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Smooths only the rendered third-person crouch offset. Gameplay state, packets,
 * hitboxes, movement and first-person rendering are left untouched.
 */
final class SmoothSneakRenderer {
    private static final float VANILLA_SNEAK_RENDER_OFFSET = 0.125F;
    private static final int STALE_TICKS = 200;

    private static final Map<UUID, SneakState> states = new HashMap<UUID, SneakState>();

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.entityPlayer;

        if (!shouldSmooth(player)) {
            return;
        }

        SneakState state = getState(player, event.partialRenderTick);
        float progress = easeInOut(state.renderProgress);

        /*
         * RenderPlayer in 1.8.9 already drops sneaking non-local players by
         * 0.125 blocks. Add the missing part while entering sneak, and cancel
         * the stale vanilla drop while leaving sneak, producing a visual-only
         * smooth transition.
         */
        float vanillaProgress = isVanillaSneakOffsetApplied(player) ? 1.0F : 0.0F;
        float correction = (vanillaProgress - progress) * VANILLA_SNEAK_RENDER_OFFSET;
        GlStateManager.translate(0.0F, correction, 0.0F);
        state.appliedCorrection = correction;
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        SneakState state = states.get(event.entityPlayer.getUniqueID());
        if (state != null && state.appliedCorrection != 0.0F) {
            GlStateManager.translate(0.0F, -state.appliedCorrection, 0.0F);
            state.appliedCorrection = 0.0F;
        }
    }

    @SubscribeEvent
    public void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld == null) {
            states.clear();
            return;
        }

        Iterator<Map.Entry<UUID, SneakState>> iterator = states.entrySet().iterator();
        while (iterator.hasNext()) {
            SneakState state = iterator.next().getValue();
            if (minecraft.theWorld.getTotalWorldTime() - state.lastSeenTick > STALE_TICKS) {
                iterator.remove();
            }
        }
    }

    private boolean shouldSmooth(EntityPlayer player) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld == null || player == null || player.isInvisible()) {
            return false;
        }

        if (player == minecraft.thePlayer && minecraft.gameSettings.thirdPersonView == 0) {
            return false;
        }

        return player instanceof AbstractClientPlayer;
    }

    private boolean isVanillaSneakOffsetApplied(EntityPlayer player) {
        return player.isSneaking() && !(player instanceof EntityPlayerSP);
    }

    static float getPoseProgress(EntityPlayer player) {
        SneakState state = states.get(player.getUniqueID());
        if (state == null) {
            return player.isSneaking() ? 1.0F : 0.0F;
        }

        return easeInOut(state.renderProgress);
    }

    private static float easeInOut(float progress) {
        return progress * progress * (3.0F - 2.0F * progress);
    }

    private SneakState getState(EntityPlayer player, float partialTicks) {
        UUID uuid = player.getUniqueID();
        SneakState state = states.get(uuid);
        if (state == null) {
            float initial = player.isSneaking() ? 1.0F : 0.0F;
            state = new SneakState(initial);
            states.put(uuid, state);
        }

        long worldTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
        state.update(player.isSneaking() ? 1.0F : 0.0F, worldTime);
        state.renderProgress = state.getInterpolatedProgress(partialTicks);
        return state;
    }

    private static final class SneakState {
        private float previousProgress;
        private float currentProgress;
        private float velocity;
        private long lastUpdatedTick = Long.MIN_VALUE;
        private long lastSeenTick;
        private float appliedCorrection;
        private float renderProgress;

        private SneakState(float initialProgress) {
            this.previousProgress = initialProgress;
            this.currentProgress = initialProgress;
            this.renderProgress = initialProgress;
        }

        private void update(float target, long worldTime) {
            lastSeenTick = worldTime;
            if (lastUpdatedTick == worldTime) {
                return;
            }

            previousProgress = currentProgress;
            velocity += (target - currentProgress) * SmoothSneakConfig.springStiffness;
            velocity *= SmoothSneakConfig.springDamping;
            velocity = clamp(velocity, -SmoothSneakConfig.maxVelocity, SmoothSneakConfig.maxVelocity);
            currentProgress += velocity;

            if (Math.abs(target - currentProgress) < 0.001F) {
                currentProgress = target;
                velocity = 0.0F;
            }

            if (currentProgress < 0.0F) {
                currentProgress = 0.0F;
                velocity = 0.0F;
            } else if (currentProgress > 1.0F) {
                currentProgress = 1.0F;
                velocity = 0.0F;
            }

            lastUpdatedTick = worldTime;
        }

        private float getInterpolatedProgress(float partialTicks) {
            return previousProgress + (currentProgress - previousProgress) * partialTicks;
        }

        private float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
