package ru.myffy.smoothsneak;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;

final class SmoothRenderPlayer extends RenderPlayer {
    SmoothRenderPlayer(RenderManager renderManager, boolean slimArms) {
        super(renderManager, slimArms);

        this.mainModel = new SmoothModelPlayer(0.0F, slimArms);
        this.layerRenderers.clear();
        this.addLayer(new SmoothLayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerDeadmau5Head(this));
        this.addLayer(new LayerCape(this));
        this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
    }

    @Override
    public void renderRightArm(AbstractClientPlayer clientPlayer) {
        renderArmWithoutSmoothSneak(clientPlayer, true);
    }

    @Override
    public void renderLeftArm(AbstractClientPlayer clientPlayer) {
        renderArmWithoutSmoothSneak(clientPlayer, false);
    }

    private void renderArmWithoutSmoothSneak(AbstractClientPlayer clientPlayer, boolean rightArm) {
        SmoothModelPlayer model = (SmoothModelPlayer) this.getMainModel();
        model.setSmoothSneakEnabled(false);
        try {
            if (rightArm) {
                super.renderRightArm(clientPlayer);
            } else {
                super.renderLeftArm(clientPlayer);
            }
        } finally {
            model.setSmoothSneakEnabled(true);
        }
    }
}
