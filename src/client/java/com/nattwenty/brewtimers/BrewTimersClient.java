package com.nattwenty.brewtimers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.nattwenty.brewtimers.Utils.worldSpaceToScreenSpace;


public class BrewTimersClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of("brewtimers", "before_chat"), BrewTimersClient::render);
        KeyBinding keybinding_brewtimers_timer = KeyBindingHelper.registerKeyBinding(new KeyBinding("Timer GUI", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, KeyBinding.Category.MISC));


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            HashMap<BlockPos, BlockTimer> hm = BlockTimerManager.getInstance().checkTimers();
            if (!hm.isEmpty()) {
                hm.forEach((pos, blockTimer) -> {
                    if (!blockTimer.isElapsed()) {
                        client.getToastManager().add(SystemToast.create(
                                client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Brew Timers"), Text.of("Timer \"" + blockTimer.getTimerName() + "\" is finished!")
                        ));
                    }
                    assert client.world != null;
                    client.world.playSoundClient(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.MASTER, 1.0F, 1.0F, false);
                    client.world.addParticleClient(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0, 0);

                    blockTimer.setTimerUTC(Instant.now().getEpochSecond() + 8);
                });
            }

            while (keybinding_brewtimers_timer.wasPressed()) {
                assert client.getCameraEntity() != null;
                HitResult hit = client.getCameraEntity().raycast(4.5, 0, false);
                if (hit instanceof BlockHitResult blockHit) {

                    BlockPos pos = blockHit.getBlockPos();
                    if (BlockTimerManager.getInstance().containsKey(pos)) {
                        BlockTimerManager.getInstance().removeTimer(pos);
                        client.getToastManager().add(SystemToast.create(
                                client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Brew Timers"), Text.of("Timer removed.")
                        ));
                    }
                    else {
                        client.send(() -> client.setScreen(new BlockTimerScreen(pos)));
                    }
                }
            }
        });
	}

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d OFFSET = new Vec3d(0.0, 0.75, 0.0);
        AtomicInteger timers_added = new AtomicInteger();

        HashMap<BlockPos, BlockTimer> brew_timers = BlockTimerManager.getInstance().getBrewTimers();

        brew_timers.forEach((pos, blockTimer) -> {

            Vec3d text_pos = pos.toCenterPos().add(OFFSET);
            Vector3f forward_V = client.gameRenderer.getCamera().getRotation().transform(new Vector3f(0f,0f,-1f)).normalize();

            assert client.getCameraEntity() != null;
            Vec3d eye_pos = client.getCameraEntity().getEyePos();
            double distance = eye_pos.distanceTo(text_pos);

            Vec3d relative = text_pos.subtract(eye_pos);
            Vec3d forward = new Vec3d(forward_V.x, forward_V.y, forward_V.z);

            Vec3d screenSpace = worldSpaceToScreenSpace(text_pos);
            float opacity = (float) Math.clamp((1d - (distance - 3) / 6d), 0d, 1d);
            int color = ColorHelper.lerp(opacity, 0x00FFFFFF, 0xFFFFFFFF);

            double timer_length = blockTimer.getTimerUTC() - blockTimer.getStartUTC();
            double elapsed = blockTimer.getTimerUTC() - Instant.now().getEpochSecond();
            double completed = elapsed / Math.max(timer_length, 0.01);
            int progress_color = ColorHelper.lerp((float) completed, 0xFFFF0000, 0xFF00FF00);

            int timer_color;
            String timer;
            int time_width;

            String text = blockTimer.getTimerName();
            int width = client.textRenderer.getWidth(text);

            if (blockTimer.isElapsed()) {
                timer = "⚠";
                time_width = client.textRenderer.getWidth(timer);
                timer_color = 0xFFFF0000;
            }
            else {
                timer = StringUtils.repeat('|', (int) Math.clamp(Math.ceil((20 * completed)), 0, 20));
                time_width = client.textRenderer.getWidth(timer);
                timer_color = progress_color;
            }
            //Draw in world
            if (distance <= 12 && forward.dotProduct(relative) >= 0) { //If its close enough and on screen
                context.drawText(client.textRenderer, timer, (int) screenSpace.getX() - time_width / 2, (int) screenSpace.getY() - 10, ColorHelper.lerp(opacity, 0x00FFFFFF, timer_color), true);
                context.drawText(client.textRenderer, text, (int) screenSpace.getX() - width / 2, (int) screenSpace.getY(), color, true);
            }
            //Draw on screen
            if (timers_added.get() < 11) {
                if (!blockTimer.isElapsed()) {
                    context.fill(8, 8 + timers_added.get() * 30, client.textRenderer.getWidth("||||||||||||||||||||")+12, timers_added.get() * 30 + 20, 0x77000000);
                }
                context.drawText(client.textRenderer, timer, 10, 10 + timers_added.get() * 30, timer_color, true);
                context.drawText(client.textRenderer, text, 10, 22 + timers_added.get() * 30, 0xFFFFFFFF, true);
            }

            timers_added.set(timers_added.get() + 1);
        });
    }
}