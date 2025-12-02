package com.nattwenty.brewtimers;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.time.Instant;

public class BlockTimerScreen extends Screen{
    private static final int ELEMENT_HEIGHT = 20;
    private static final int ELEMENT_SPACING = 10;
    private static final int BUTTON_WIDTH = 100;
    private BlockPos pos;

    public BlockTimerScreen(BlockPos pos) {
        super(Text.of(BrewTimers.MOD_ID + " TimerScreen"));
        this.pos = pos;
    }

    @Override
    public void init() {
        initToastBar();
        initClearButton();
        initCloseButton();
    }

    private void initCloseButton() {
        Text closeButtonTitle = Text.of("Close");
        int closeButtonX = width - BUTTON_WIDTH - ELEMENT_SPACING;
        int closeButtonY = height - ELEMENT_HEIGHT - ELEMENT_SPACING;
        ButtonWidget closeButton = ButtonWidget.builder(
                closeButtonTitle, button -> client.setScreen(null)
        ).dimensions(closeButtonX,closeButtonY,BUTTON_WIDTH,ELEMENT_HEIGHT).build();

        addDrawableChild(closeButton);
    }

    private void initToastBar() {
        int toastBarY = ELEMENT_SPACING;
        int timeBarY = toastBarY + ELEMENT_HEIGHT + ELEMENT_SPACING;
        int timeBarWidth = 35;

        Text toastButtonTitle = Text.of("Set Timer");
        int toastButtonX = width - 2 * (ELEMENT_SPACING + BUTTON_WIDTH);
        int toastButtonY = height - ELEMENT_HEIGHT - ELEMENT_SPACING;

        Text textFieldMessage = Text.of("Timer");
        int textFieldX = ELEMENT_SPACING;
        int textFieldWidth = BUTTON_WIDTH * 2;

        TextFieldWidget nameField = new TextFieldWidget(textRenderer, textFieldX, toastBarY, textFieldWidth, ELEMENT_HEIGHT, textFieldMessage);
        nameField.setText("Timer Name");

        TextFieldWidget timeHField = new TextFieldWidget(textRenderer, textFieldX, timeBarY, timeBarWidth, ELEMENT_HEIGHT, Text.of("0"));
        timeHField.setSuggestion("H");
        timeHField.setText("0");
        TextFieldWidget timeMField = new TextFieldWidget(textRenderer, textFieldX + timeBarWidth + ELEMENT_SPACING, timeBarY, timeBarWidth, ELEMENT_HEIGHT, Text.of("0"));
        timeMField.setSuggestion("M");
        timeMField.setText("0");
        TextFieldWidget timeSField = new TextFieldWidget(textRenderer, textFieldX + 2 * (timeBarWidth + ELEMENT_SPACING), timeBarY, timeBarWidth, ELEMENT_HEIGHT, Text.of("0"));
        timeSField.setSuggestion("S");
        timeSField.setText("0");

        ButtonWidget toastButton = ButtonWidget.builder(
                toastButtonTitle, button -> {
                    int H = 0;
                    int M = 0;
                    int S = 0;

                    if (!timeHField.getText().isEmpty()) {
                        try {
                            H = Integer.parseInt(timeHField.getText());
                        } catch (NumberFormatException ignored) {}
                    }

                    if (!timeMField.getText().isEmpty()) {
                        try {
                            M = Integer.parseInt(timeMField.getText());
                        }  catch (NumberFormatException ignored) {}
                    }

                    if  (!timeSField.getText().isEmpty()) {
                        try {
                            S = Integer.parseInt(timeSField.getText());
                        }   catch (NumberFormatException ignored) {}
                    }

                    if (nameField.getText().isEmpty()) {
                        assert client != null;
                        client.getToastManager().add(SystemToast.create(
                                client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Brew Manager"), Text.of("You must set a timer name.")
                        ));
                    }
                    else {
                        Long UTC = Instant.now().getEpochSecond() + H * 360L + M * 60L + S;
                        BlockTimerManager manager = BlockTimerManager.getInstance();
                        manager.addTimer(pos, new BlockTimer(nameField.getText(), UTC));
                        assert client != null;
                        client.getToastManager().add(SystemToast.create(
                                client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Brew Manager"), Text.of("Timer added!")
                        ));
                        client.setScreen(null);
                    }
                }
        ).dimensions(toastButtonX, toastButtonY, BUTTON_WIDTH, ELEMENT_HEIGHT).build();

        addDrawableChild(toastButton);
        addDrawableChild(nameField);
        addDrawableChild(timeHField);
        addDrawableChild(timeMField);
        addDrawableChild(timeSField);
    }
    private void initClearButton() {
        Text clearButtonTitle = Text.of("Clear Timers");
        int closeButtonX = width - 3 * (BUTTON_WIDTH + ELEMENT_SPACING);
        int closeButtonY = height - ELEMENT_HEIGHT - ELEMENT_SPACING;
        assert client != null;
        ButtonWidget closeButton = ButtonWidget.builder(
                clearButtonTitle, button -> {
                    BlockTimerManager.getInstance().clear();
                    client.getToastManager().add(SystemToast.create(
                            client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Brew Manager"), Text.of("Timers cleared.")
                    ));
                }
        ).dimensions(closeButtonX,closeButtonY,BUTTON_WIDTH,ELEMENT_HEIGHT).build();

        addDrawableChild(closeButton);
    }
}
