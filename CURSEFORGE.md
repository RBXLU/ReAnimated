# ReAnimated

[![Available for Fabric](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg)](https://fabricmc.net/)
[![Available for Quilt](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/quilt_vector.svg)](https://quiltmc.org/)
[![Available for NeoForge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/neoforge_vector.svg)](https://neoforged.net/)
[![Requires FabricAPI](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3.3.1/assets/cozy/requires/fabric-api_vector.svg)](https://modrinth.com/mod/fabric-api)
[![Available on Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/mod/re-animated)

**Smooth animations for the entire Minecraft UI.** Menus, buttons, containers and the inventory slide, scale and follow your cursor — without changing how the game plays.

Vanilla menus just pop in and out with no transition, and after a while that started to bug me. So I made ReAnimated — a client-side mod that adds smooth **open and close** animations to the whole UI. It doesn't touch gameplay in any way, it's purely visual, and it's fine to run on any server.

## ✨ Features

Open a menu — the main menu, your inventory, a chest, a furnace, doesn't matter — and the panel eases into place instead of snapping. Close it and the same motion plays in reverse.

- **Menus open (and close) smoothly** — titles **and** buttons slide up together from below, then reverse on close. Works on the main menu, Singleplayer, Multiplayer, Settings and every other screen automatically.
- **Containers slide up** — furnaces, chests, barrels, the inventory and any other container panel ease in from the bottom.
- **Animated logo, two styles** — the *Minecraft* logo either **grows in** with a soft bounce, or assembles **letter by letter** in a cascade.
- **Tabs slide out** — the category tabs in **Advancements** and the **Creative** inventory slide out from behind the panel, one after another: top tabs rise from below, bottom tabs drop from above.
- **Buttons press in** — clicking a button (or pressing Enter on it) dips it inward and springs it back. Sliders and cycling options too.
- **Button hover scaling** — buttons gently grow when hovered and ease back when you move away.
- **Background dimming fades in** — the dim behind a screen builds up along with the panel instead of snapping on.
- **Following slot highlight** — the inventory slot highlight glides after your cursor instead of snapping between slots.

### Presets & your own animations

Every screen animates through a preset you choose:

- **Default** — slides up from the bottom
- **From background** — grows out of the center with a little bounce
- **From foreground** — flies in and settles down to normal size
- **None** — turns the movement off but keeps the rest

Want something specific? Open the **Animation Studio** or the **Profile Editor** built into the settings — live sliders, ready-made starting points, an easing-curve graph and a live preview, so you can dial in your own open/close animation.

## ⚙️ Configuration

Open **Options → Video Settings → `ReAnimated settings`**, or through **Mod Menu** if you have it. (Using Sodium / VulkanMod / Iris? The button shows up in their settings screen too.)

The settings are grouped into sections — **General · Menu screens · Pause menu · Containers · Cursor · Logo · Tabs**:

- **The pause menu has its own settings** — speed, distance, trajectory and preset, separate from other menus
- **Export / Import** — copy your whole configuration to the clipboard and paste it into another instance, or share it with someone else
- **Enable / disable** each animation
- **Duration** (how fast) and **slide distance / depth**
- **Trajectory / easing** — *Linear*, *Out Cubic*, *Out Back (bounce)*, *Out Expo* and more
- Cascade delay & direction, hover scale amount & speed, press depth, slot-highlight speed, logo style, and more

Settings are saved to `config/reanimated.json` and apply instantly.

## 📦 Requirements

- **Fabric** or **Quilt**, plus **[Fabric API](https://modrinth.com/mod/fabric-api)** — or **NeoForge**
- Minecraft **1.21.1 – 1.21.11**, and the newer **26.1 / 26.2** builds

Client-side only — not required on servers, and players without it won't notice anything different. Compatible with **Sodium** and other rendering mods (ReAnimated only touches GUI classes).

### Which file do I need?

There's a **separate download for each Minecraft version** — the GUI rendering changed a fair amount across 1.21.x and again in 26.x, so every version has its own build made against it. Grab the one that matches your game exactly; file names look like `reanimated-<Minecraft version>+<mod version>.jar`, e.g. `reanimated-1.21.1+1.5.0.jar`.

NeoForge builds are kept level with the Fabric line — same version number, same features.

---

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/N1N7225QLU)

---

<details>
<summary>🇷🇺 Описание на русском</summary>

**Плавные анимации для всего интерфейса Minecraft.** Меню, кнопки, контейнеры и инвентарь плавно выезжают, увеличиваются и следуют за курсором — не влияя на игровой процесс.

Ванильные меню просто появляются и исчезают без всякого перехода, и меня это со временем начало раздражать. Поэтому я сделал ReAnimated — клиентский мод, который добавляет плавные анимации **открытия и закрытия** для всего интерфейса. На геймплей никак не влияет, чисто визуально, и спокойно работает на любом сервере.

### Возможности

- **Меню открываются и закрываются плавно** — заголовки **и** кнопки выезжают снизу вместе, на закрытии — в обратную сторону. Работает на всех экранах автоматически.
- **Контейнеры выезжают** — печка, сундук, инвентарь и любой другой контейнер плавно появляются снизу.
- **Анимированный логотип, два стиля** — логотип *Minecraft* либо **вырастает** с мягким отскоком, либо собирается **по буквам**.
- **Вкладки выезжают из-за панели** — в достижениях и креативном инвентаре, по очереди: верхние поднимаются снизу, нижние опускаются сверху.
- **Кнопки вдавливаются** — нажатие мышью или Enter'ом проседает кнопку и упруго возвращает её. Работает и на ползунках.
- **Увеличение под курсором** — кнопки мягко растут при наведении и плавно возвращаются.
- **Плавное затемнение фона** — затемнение за экраном набирается вместе с панелью, а не рывком.
- **Догоняющая подсветка слота** — подсветка в инвентаре плавно следует за курсором.

### Пресеты и свои анимации

Каждый экран появляется по выбранному пресету: **Обычный** (выезд снизу), **Из фона** (вырастает из центра с отскоком), **С переднего плана** (влетает и уменьшается) или **Без анимации**. Хочется своего — в настройках есть **Студия анимаций** и **Редактор профиля**: живые ползунки, готовые заготовки, график кривой и предпросмотр.

### Настройки

Настройки → Настройки графики → «Настройки ReAnimated», или через **Mod Menu**. (Sodium / VulkanMod / Iris? Кнопка появится и в их экране настроек.)

Разбиты на секции — **Общее · Экраны меню · Меню паузы · Контейнеры · Курсор · Логотип · Вкладки**. У меню паузы свой набор настроек (скорость, дистанция, траектория, пресет), а кнопки **Копировать/Вставить** переносят всю конфигурацию через буфер обмена — удобно перенести настройку в другую сборку или поделиться ей. Всё сохраняется в `config/reanimated.json` и применяется сразу.

### Требования

**Fabric** или **Quilt** плюс **[Fabric API](https://modrinth.com/mod/fabric-api)** — либо **NeoForge**. Minecraft **1.21.1 – 1.21.11**, а также сборки **26.1 / 26.2**.

Мод клиентский: на сервере не нужен, игроки без него ничего не заметят. Совместим с Sodium и другими рендер-модами — ReAnimated трогает только классы интерфейса.

### Какой файл скачивать

Для **каждой версии Minecraft — свой файл**: отрисовка интерфейса заметно менялась внутри 1.21.x и ещё раз в 26.x, поэтому под каждую версию собирается отдельная сборка. Берите ту, что совпадает с вашей игрой: имя файла выглядит как `reanimated-<версия Minecraft>+<версия мода>.jar`, например `reanimated-1.21.1+1.5.0.jar`.

</details>

---

Made with ❤️ by **[@pycodder](https://modrinth.com/user/pycodder)**
