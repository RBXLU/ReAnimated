# Third-party credits

## EaseGUI — logo letter cascade

ReAnimated's **per-letter logo cascade** (the `Letters` logo style) is based on the
logo animator from **EaseGUI** by **Weyne1**:

- Project: https://github.com/Weyne1/EaseGUI
- License: **GNU Lesser General Public License v3.0 (LGPL-3.0)**

What is used from EaseGUI:

- The **idea and approach** of splitting the Minecraft title logo into nine per-letter
  full-canvas textures and animating each letter on its own cascade timeline. The code in
  `com.pycodder.reanimated.anim.LogoLetters` and `mixin.LogoDrawerMixin` was re-written from
  scratch against ReAnimated's own animation systems (`AnimProfile`, `Easing`, `UiTransform`),
  but the concept is EaseGUI's.
- The nine **letter textures** (`assets/reanimated/textures/gui/title/letters/{m,i,n,e,c,r,a,f,t}.png`)
  are taken from EaseGUI and redistributed under the LGPL-3.0.

The full license texts are included in [`licenses/EaseGUI-LGPL-3.0.txt`](licenses/EaseGUI-LGPL-3.0.txt)
and [`licenses/GPL-3.0.txt`](licenses/GPL-3.0.txt) (the LGPL is an additional grant on top of the GPL).

The rest of ReAnimated remains under its own license; this notice covers only the EaseGUI-derived
logo-cascade feature and its textures.
