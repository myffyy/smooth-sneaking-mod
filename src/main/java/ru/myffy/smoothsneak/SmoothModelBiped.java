package ru.myffy.smoothsneak;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

final class SmoothModelBiped extends ModelBiped {
    private final PartPose standing = new PartPose();
    private final PartPose sneaking = new PartPose();

    SmoothModelBiped(float modelSize) {
        super(modelSize);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
            return;
        }

        boolean vanillaSneak = this.isSneak;

        this.isSneak = false;
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        standing.capture(this);

        this.isSneak = true;
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        sneaking.capture(this);

        float progress = SmoothSneakRenderer.getPoseProgress((EntityPlayer) entity);
        standing.applyInterpolated(this, sneaking, progress);
        this.isSneak = vanillaSneak;
    }

    private static final class PartPose {
        private final Transform head = new Transform();
        private final Transform headwear = new Transform();
        private final Transform body = new Transform();
        private final Transform rightArm = new Transform();
        private final Transform leftArm = new Transform();
        private final Transform rightLeg = new Transform();
        private final Transform leftLeg = new Transform();

        private void capture(ModelBiped model) {
            head.capture(model.bipedHead);
            headwear.capture(model.bipedHeadwear);
            body.capture(model.bipedBody);
            rightArm.capture(model.bipedRightArm);
            leftArm.capture(model.bipedLeftArm);
            rightLeg.capture(model.bipedRightLeg);
            leftLeg.capture(model.bipedLeftLeg);
        }

        private void applyInterpolated(ModelBiped model, PartPose target, float progress) {
            head.apply(model.bipedHead, target.head, progress);
            headwear.apply(model.bipedHeadwear, target.headwear, progress);
            body.apply(model.bipedBody, target.body, progress);
            rightArm.apply(model.bipedRightArm, target.rightArm, progress);
            leftArm.apply(model.bipedLeftArm, target.leftArm, progress);
            rightLeg.apply(model.bipedRightLeg, target.rightLeg, progress);
            leftLeg.apply(model.bipedLeftLeg, target.leftLeg, progress);
        }
    }

    private static final class Transform {
        private float rotationPointX;
        private float rotationPointY;
        private float rotationPointZ;
        private float rotateAngleX;
        private float rotateAngleY;
        private float rotateAngleZ;

        private void capture(ModelRenderer part) {
            rotationPointX = part.rotationPointX;
            rotationPointY = part.rotationPointY;
            rotationPointZ = part.rotationPointZ;
            rotateAngleX = part.rotateAngleX;
            rotateAngleY = part.rotateAngleY;
            rotateAngleZ = part.rotateAngleZ;
        }

        private void apply(ModelRenderer part, Transform target, float progress) {
            part.rotationPointX = lerp(rotationPointX, target.rotationPointX, progress);
            part.rotationPointY = lerp(rotationPointY, target.rotationPointY, progress);
            part.rotationPointZ = lerp(rotationPointZ, target.rotationPointZ, progress);
            part.rotateAngleX = lerp(rotateAngleX, target.rotateAngleX, progress);
            part.rotateAngleY = lerp(rotateAngleY, target.rotateAngleY, progress);
            part.rotateAngleZ = lerp(rotateAngleZ, target.rotateAngleZ, progress);
        }

        private float lerp(float from, float to, float progress) {
            return from + (to - from) * progress;
        }
    }
}
