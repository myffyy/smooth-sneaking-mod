package ru.myffy.smoothsneak;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;

final class SmoothLayerBipedArmor extends LayerBipedArmor {
    SmoothLayerBipedArmor(RendererLivingEntity<?> renderer) {
        super(renderer);
    }

    @Override
    protected void initArmor() {
        this.modelLeggings = new SmoothModelBiped(0.5F);
        this.modelArmor = new SmoothModelBiped(1.0F);
    }
}
