package ru.myffy.smoothsneak;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

final class SmoothSneakGuiOpener {
    private static boolean openRequested;

    SmoothSneakGuiOpener() {
    }

    static void requestOpen() {
        openRequested = true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !openRequested) {
            return;
        }

        openRequested = false;
        Minecraft.getMinecraft().displayGuiScreen(new SmoothSneakConfigScreen(null));
    }
}
