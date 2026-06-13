package ru.myffy.smoothsneak;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(
        modid = SmoothSneakClientMod.MODID,
        name = SmoothSneakClientMod.NAME,
        version = SmoothSneakClientMod.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.9]"
)
@SideOnly(Side.CLIENT)
public final class SmoothSneakClientMod {
    public static final String MODID = "smoothsneakclient";
    public static final String NAME = "Smooth Sneak Client";
    public static final String VERSION = "1.1.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SmoothSneakConfig.load(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        installSmoothPlayerRenderers();
        ClientCommandHandler.instance.registerCommand(new SmoothSneakCommand());
        MinecraftForge.EVENT_BUS.register(new SmoothSneakRenderer());
        MinecraftForge.EVENT_BUS.register(new SmoothSneakGuiOpener());
    }

    @SuppressWarnings("unchecked")
    private void installSmoothPlayerRenderers() {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        SmoothRenderPlayer defaultRenderer = new SmoothRenderPlayer(renderManager, false);
        SmoothRenderPlayer slimRenderer = new SmoothRenderPlayer(renderManager, true);

        try {
            Field skinMapField = ReflectionHelper.findField(RenderManager.class, "skinMap", "field_178636_l");
            Map<String, RenderPlayer> skinMap = (Map<String, RenderPlayer>) skinMapField.get(renderManager);
            skinMap.put("default", defaultRenderer);
            skinMap.put("slim", slimRenderer);

            Field playerRendererField = ReflectionHelper.findField(RenderManager.class, "playerRenderer", "field_178637_m");
            playerRendererField.set(renderManager, defaultRenderer);
        } catch (Throwable ignored) {
            // Keep the mod non-fatal on clients that wrap or replace RenderManager internals.
        }
    }
}
