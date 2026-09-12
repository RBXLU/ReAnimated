package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.ReAnimatedClient;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.ScreenNav;
import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Shadow public Screen screen;

    @Unique private String reanimated$closeName = null;
    @Unique private boolean reanimated$closeNameResolved = false;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void reanimated$onSetScreen(Screen screen, CallbackInfo ci) {
        if (Anim.bypassClose) return;

        if (screen != null) {
            reanimated$onOpenScreen(screen, ci);
            return;
        }

        ScreenNav.clear();

        if (Anim.isClosing()) {
            Anim.pendingScreen = null;
            Anim.clearEnterDir();
            ci.cancel();
            return;
        }

        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.closeAnimationEnabled) return;

        Screen current = this.screen;
        if (current == null) return;
        if (!reanimated$safeToDefer(current)) return;

        boolean container = current instanceof AbstractContainerScreen;
        Anim.currentIsPause = Anim.isPauseScreen(current);
        boolean presetActive = Anim.presetLayerActive(container);
        boolean profileActive = c.profile.enabled;
        if (!presetActive && !profileActive) return;

        Anim.beginClose(container);
        ci.cancel();
    }

    @Unique
    private void reanimated$onOpenScreen(Screen next, CallbackInfo ci) {
        if (Anim.isClosing()) {
            if (Anim.pendingScreen != null) {
                Anim.pendingScreen = next;
                ci.cancel();
            } else {
                Anim.finishClose();
            }
            return;
        }

        if (!Anim.transitionLayerActive()) return;

        Screen current = this.screen;
        if (current == null || current == next) return;
        if (current instanceof AbstractContainerScreen || next instanceof AbstractContainerScreen) return;
        if (Anim.isVolatileScreen(current) || Anim.isVolatileScreen(next)) return;
        if (!Anim.shouldAnimate(current) || !Anim.shouldAnimate(next)) return;
        if (!reanimated$safeToDefer(current)) return;

        Anim.currentIsPause = Anim.isPauseScreen(current);
        Anim.currentOverride = ScreenRules.get(current);

        int forward = ScreenNav.accept(current, next);
        Anim.beginTransition(next, -forward);
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
