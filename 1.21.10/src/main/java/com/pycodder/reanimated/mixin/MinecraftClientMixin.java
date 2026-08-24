package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.ReAnimatedClient;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.UiPreset;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Deferred screen close: instead of an immediate {@code setScreen(null)} the screen stays current briefly and plays the open animation in reverse. */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow public Screen currentScreen;

    @Unique private String reanimated$closeName = null;
    @Unique private boolean reanimated$closeNameResolved = false;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void reanimated$onSetScreen(Screen screen, CallbackInfo ci) {
        if (Anim.bypassClose) return;

        if (screen != null) {
            if (Anim.isClosing()) Anim.finishClose();
            return;
        }

        if (Anim.isClosing()) {
            ci.cancel();
            return;
        }

        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.closeAnimationEnabled) return;

        Screen current = this.currentScreen;
        if (current == null) return;
        if (!reanimated$safeToDefer(current)) return;

        boolean container = current instanceof HandledScreen;
        boolean presetActive = (container ? c.containerEnabled : c.screenOpenEnabled)
                && c.uiPreset != UiPreset.NONE;
        boolean profileActive = c.profile.enabled;
        if (!presetActive && !profileActive) return;

        Anim.beginClose(container);
        ci.cancel();
    }

    @Unique
    private boolean reanimated$safeToDefer(Screen screen) {
        String name = reanimated$closeMethodName();
        if (name == null) return false;
        try {
            Class<?> owner = screen.getClass().getMethod(name).getDeclaringClass();
            return owner.getName().startsWith("net.minecraft.");
        } catch (Throwable t) {
            return false;
        }
    }

    @Unique
    private String reanimated$closeMethodName() {
        if (!reanimated$closeNameResolved) {
            reanimated$closeNameResolved = true;
            for (String candidate : new String[] {"method_25419", "close", "onClose"}) {
                try {
                    Screen.class.getMethod(candidate);
                    reanimated$closeName = candidate;
                    break;
                } catch (NoSuchMethodException ignored) {
                }
            }
            if (reanimated$closeName == null) {
                ReAnimatedClient.LOGGER.warn(
                    "[ReAnimated] Screen.close() not found in this mapping environment — close animation disabled");
            }
        }
        return reanimated$closeName;
    }
}
