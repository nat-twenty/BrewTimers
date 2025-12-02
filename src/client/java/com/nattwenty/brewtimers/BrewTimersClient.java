package com.nattwenty.brewtimers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.util.HashMap;


public class BrewTimersClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
        KeyBinding keybinding_brewtimers_timer = KeyBindingHelper.registerKeyBinding(new KeyBinding("Timer GUI", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, KeyBinding.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            HashMap<BlockPos, BlockTimer> hm = BlockTimerManager.getInstance().checkTimers();
            if (!hm.isEmpty()) {
                hm.forEach((pos, blockTimer) -> {
                    client.getToastManager().add(SystemToast.create(
                            client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Brew Timers"), Text.of("Timer \"" + blockTimer.getTimerName() + "\" is finished!")
                    ));
                    assert client.world != null;
                    client.world.playSoundClient(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.MASTER, 1.0F, 1.0F, false);
                    client.world.addParticleClient(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0, 0);

                    blockTimer.setTimerUTC(Instant.now().getEpochSecond() + 10);
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
}