package ru.myffy.smoothsneak;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

final class SmoothSneakConfigScreen extends GuiScreen {
    private static final int DONE = 0;
    private static final int RESET = 1;

    private final GuiScreen parent;
    private SettingControl stiffness;
    private SettingControl damping;
    private SettingControl velocity;

    SmoothSneakConfigScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        int center = this.width / 2;
        int top = Math.max(52, this.height / 2 - 82);
        this.buttonList.clear();

        stiffness = new SettingControl(
                "Spring Stiffness (recommended 0.10-0.50, default 0.336)",
                center - 150,
                top,
                SmoothSneakConfig.MIN_SPRING_STIFFNESS,
                SmoothSneakConfig.MAX_SPRING_STIFFNESS,
                SmoothSneakConfig.springStiffness
        );
        damping = new SettingControl(
                "Spring Damping (recommended 0.50-0.90, default 0.76)",
                center - 150,
                top + 48,
                SmoothSneakConfig.MIN_SPRING_DAMPING,
                SmoothSneakConfig.MAX_SPRING_DAMPING,
                SmoothSneakConfig.springDamping
        );
        velocity = new SettingControl(
                "Max Velocity (recommended 0.15-0.60, default 0.408)",
                center - 150,
                top + 96,
                SmoothSneakConfig.MIN_MAX_VELOCITY,
                SmoothSneakConfig.MAX_MAX_VELOCITY,
                SmoothSneakConfig.maxVelocity
        );

        this.buttonList.add(new GuiButton(RESET, center - 154, top + 152, 150, 20, "Reset Defaults"));
        this.buttonList.add(new GuiButton(DONE, center + 4, top + 152, 150, 20, "Done"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Smooth Sneak Client", this.width / 2, 18, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Use /sneaking to open this menu.", this.width / 2, 31, 0xA0A0A0);
        stiffness.draw(mouseX, mouseY);
        damping.draw(mouseX, mouseY);
        velocity.draw(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        stiffness.update();
        damping.update();
        velocity.update();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        stiffness.mouseClicked(mouseX, mouseY, mouseButton);
        damping.mouseClicked(mouseX, mouseY, mouseButton);
        velocity.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        saveCurrentValues();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        stiffness.mouseDragged(mouseX);
        damping.mouseDragged(mouseX);
        velocity.mouseDragged(mouseX);
        saveCurrentValues();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        stiffness.mouseReleased();
        damping.mouseReleased();
        velocity.mouseReleased();
        super.mouseReleased(mouseX, mouseY, state);
        saveCurrentValues();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (stiffness.keyTyped(typedChar, keyCode) || damping.keyTyped(typedChar, keyCode) || velocity.keyTyped(typedChar, keyCode)) {
            saveCurrentValues();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == DONE) {
            saveCurrentValues();
            this.mc.displayGuiScreen(parent);
        } else if (button.id == RESET) {
            SmoothSneakConfig.resetDefaults();
            stiffness.setValue(SmoothSneakConfig.springStiffness);
            damping.setValue(SmoothSneakConfig.springDamping);
            velocity.setValue(SmoothSneakConfig.maxVelocity);
        }
    }

    @Override
    public void onGuiClosed() {
        saveCurrentValues();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void saveCurrentValues() {
        SmoothSneakConfig.saveValues(stiffness.getValue(), damping.getValue(), velocity.getValue());
    }

    private final class SettingControl {
        private final DecimalFormat decimalFormat = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.US));
        private final String label;
        private final int x;
        private final int y;
        private final int sliderWidth = 212;
        private final int sliderHeight = 10;
        private final int textWidth = 68;
        private final float min;
        private final float max;
        private final GuiTextField textField;
        private float value;
        private boolean dragging;

        private SettingControl(String label, int x, int y, float min, float max, float initialValue) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.min = min;
            this.max = max;
            this.value = initialValue;
            this.textField = new GuiTextField(0, fontRendererObj, x + sliderWidth + 20, y + 17, textWidth, 18);
            this.textField.setMaxStringLength(8);
            this.textField.setText(format(value));
        }

        private void draw(int mouseX, int mouseY) {
            drawString(fontRendererObj, label, x, y, 0xFFFFFF);

            int sliderY = y + 22;
            drawRect(x, sliderY, x + sliderWidth, sliderY + sliderHeight, 0xFF202020);
            drawRect(x + 1, sliderY + 1, x + sliderWidth - 1, sliderY + sliderHeight - 1, 0xFF606060);

            int knobX = x + Math.round(getNormalizedValue() * sliderWidth);
            int knobColor = isMouseOverSlider(mouseX, mouseY) || dragging ? 0xFFE0E0E0 : 0xFFB0B0B0;
            drawRect(knobX - 3, sliderY - 3, knobX + 4, sliderY + sliderHeight + 3, knobColor);
            textField.drawTextBox();
        }

        private void update() {
            textField.updateCursorCounter();
        }

        private void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            textField.mouseClicked(mouseX, mouseY, mouseButton);
            if (mouseButton == 0 && isMouseOverSlider(mouseX, mouseY)) {
                dragging = true;
                updateFromMouse(mouseX);
                textField.setText(format(value));
            }
        }

        private void mouseDragged(int mouseX) {
            if (dragging) {
                updateFromMouse(mouseX);
                textField.setText(format(value));
            }
        }

        private void mouseReleased() {
            dragging = false;
            applyTextValue();
        }

        private boolean keyTyped(char typedChar, int keyCode) {
            if (!textField.isFocused()) {
                return false;
            }

            boolean handled = textField.textboxKeyTyped(typedChar, keyCode);
            applyTextValue();
            return handled;
        }

        private float getValue() {
            applyTextValue();
            return value;
        }

        private void setValue(float value) {
            this.value = SmoothSneakConfig.clamp(value, min, max);
            this.textField.setText(format(this.value));
        }

        private void updateFromMouse(int mouseX) {
            float normalized = SmoothSneakConfig.clamp((mouseX - x) / (float) sliderWidth, 0.0F, 1.0F);
            value = min + (max - min) * normalized;
        }

        private void applyTextValue() {
            try {
                value = SmoothSneakConfig.clamp(Float.parseFloat(textField.getText()), min, max);
            } catch (NumberFormatException ignored) {
                // Keep the last valid value while the user is typing.
            }
        }

        private float getNormalizedValue() {
            return (value - min) / (max - min);
        }

        private boolean isMouseOverSlider(int mouseX, int mouseY) {
            int sliderY = y + 22;
            return mouseX >= x && mouseX <= x + sliderWidth && mouseY >= sliderY - 4 && mouseY <= sliderY + sliderHeight + 4;
        }

        private String format(float value) {
            return decimalFormat.format(value);
        }
    }
}
