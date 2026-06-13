package ru.myffy.smoothsneak;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

final class SmoothSneakConfig {
    static final float DEFAULT_SPRING_STIFFNESS = 0.336F;
    static final float DEFAULT_SPRING_DAMPING = 0.76F;
    static final float DEFAULT_MAX_VELOCITY = 0.408F;

    static final float MIN_SPRING_STIFFNESS = 0.10F;
    static final float MAX_SPRING_STIFFNESS = 0.50F;
    static final float MIN_SPRING_DAMPING = 0.50F;
    static final float MAX_SPRING_DAMPING = 0.90F;
    static final float MIN_MAX_VELOCITY = 0.15F;
    static final float MAX_MAX_VELOCITY = 0.60F;

    static float springStiffness = DEFAULT_SPRING_STIFFNESS;
    static float springDamping = DEFAULT_SPRING_DAMPING;
    static float maxVelocity = DEFAULT_MAX_VELOCITY;

    private static Configuration configuration;

    private SmoothSneakConfig() {
    }

    static void load(File file) {
        configuration = new Configuration(file);
        syncFromFile();
    }

    static void syncFromFile() {
        if (configuration == null) {
            return;
        }

        String category = "animation";
        springStiffness = configuration.getFloat(
                "springStiffness",
                category,
                DEFAULT_SPRING_STIFFNESS,
                MIN_SPRING_STIFFNESS,
                MAX_SPRING_STIFFNESS,
                "Spring acceleration. Recommended range: 0.10-0.50. Default: 0.336."
        );
        springDamping = configuration.getFloat(
                "springDamping",
                category,
                DEFAULT_SPRING_DAMPING,
                MIN_SPRING_DAMPING,
                MAX_SPRING_DAMPING,
                "Velocity retention/damping. Recommended range: 0.50-0.90. Default: 0.76."
        );
        maxVelocity = configuration.getFloat(
                "maxVelocity",
                category,
                DEFAULT_MAX_VELOCITY,
                MIN_MAX_VELOCITY,
                MAX_MAX_VELOCITY,
                "Maximum animation speed per tick. Recommended range: 0.15-0.60. Default: 0.408."
        );

        saveIfChanged();
    }

    static void saveValues(float stiffness, float damping, float velocity) {
        springStiffness = clamp(stiffness, MIN_SPRING_STIFFNESS, MAX_SPRING_STIFFNESS);
        springDamping = clamp(damping, MIN_SPRING_DAMPING, MAX_SPRING_DAMPING);
        maxVelocity = clamp(velocity, MIN_MAX_VELOCITY, MAX_MAX_VELOCITY);

        if (configuration != null) {
            String category = "animation";
            configuration.get(category, "springStiffness", DEFAULT_SPRING_STIFFNESS).set(springStiffness);
            configuration.get(category, "springDamping", DEFAULT_SPRING_DAMPING).set(springDamping);
            configuration.get(category, "maxVelocity", DEFAULT_MAX_VELOCITY).set(maxVelocity);
            configuration.save();
        }
    }

    static void resetDefaults() {
        saveValues(DEFAULT_SPRING_STIFFNESS, DEFAULT_SPRING_DAMPING, DEFAULT_MAX_VELOCITY);
    }

    private static void saveIfChanged() {
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
