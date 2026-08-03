# ReAnimated
[![Available for Fabric](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg)](https://fabricmc.net/)
[![Requires FabricAPI](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3.3.1/assets/cozy/requires/fabric-api_vector.svg)](https://modrinth.com/mod/fabric-api)
[![Available for Quilt](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/quilt_vector.svg)](https://quiltmc.org/)
[![Available for NeoForge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/neoforge_vector.svg)](https://neoforged.net/)
[![Available on Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/mod/re-animated)
[![Available on CurseForge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg)](https://www.curseforge.com/minecraft/mc-mods/reanimated)

**Smooth animations for the entire Minecraft UI.** Menus, buttons, containers and the inventory slide, scale and follow your cursor — without changing how the game plays.

Vanilla menus just pop in and out with no transition, and after a while that started to bug me. So I made ReAnimated — a client-side mod that adds smooth **open and close** animations to the whole UI. It doesn't touch gameplay in any way, it's purely visual, and it's fine to run on any server.

![icon](https://cdn.modrinth.com/data/cached_images/18ae1c5f86365aa33567ff4a18d1095a2d94bb5a.png)

## ✨ Features

Open a menu — the main menu, your inventory, a chest, a furnace, doesn't matter — and the panel eases into place instead of snapping. Close it and the same motion plays in reverse.

- **Menus open (and close) smoothly** — titles **and** buttons slide up together from below, then reverse on close. Works on the main menu, Singleplayer, Multiplayer, Settings and every other screen automatically.
- **Containers slide up** — furnaces, chests, barrels, the inventory and any other container panel ease in from the bottom.
- **Animated logo, two styles** — the *Minecraft* logo either **grows in** with a soft bounce, or assembles **letter by letter** in a cascade. Pick the one you like.
- **Tabs slide out** — the category tabs in **Advancements** and the **Creative** inventory slide out from behind the panel, one after another: top tabs rise from below, bottom tabs drop from above.
- **Following slot highlight** — the inventory slot highlight glides after your cursor instead of snapping between slots.
- **Button hover scaling** — buttons gently grow when hovered and ease back when you move away.

### Presets & your own animations
Every screen animates through a preset you choose:

- **Default** — slides up from the bottom
- **From background** — grows out of the center with a little bounce
- **From foreground** — flies in and settles down to normal size
- **None** — turns the movement off but keeps the rest

Want something specific? Open the **Animation Studio** or the **Profile Editor** built into the settings — live sliders, ready-made starting points, an easing-curve graph and a live preview, so you can dial in your own open/close animation.

## ⚙️ Configuration

Open **Options → Video Settings → `ReAnimated settings`**, or through **Mod Menu** if you have it. (Using Sodium / VulkanMod / Iris? The button shows up in their settings screen too.)

The settings are grouped into sections — **General · Menu screens · Containers · Cursor · Logo · Tabs** — and every animation can be tuned independently:

- **Enable / disable** each animation
- **Duration** (how fast) and **slide distance / depth**
- **Trajectory / easing** — *Linear*, *Out Cubic*, *Out Back (bounce)*, *Out Expo* and more
- Cascade delay & direction, hover scale amount & speed, slot-highlight speed, logo style, and more

Settings are saved to `config/reanimated.json` and apply instantly.

## 📦 Requirements

- **Fabric** or **Quilt**, plus **[Fabric API](https://modrinth.com/mod/fabric-api)**
- **Fabric Loader** 0.16+
- Minecraft **1.21.1 – 1.21.11**, and the newer **26.1 / 26.2** builds

Client-side only — not required on servers, and players without it won't notice anything different. Compatible with **Sodium** and other rendering mods (ReAnimated only touches GUI classes).

### Supported versions
There's a separate download for each Minecraft version. The GUI rendering changed a fair amount across 1.21.x and again in 26.x, so every version has its own build made against it — grab the one that matches your game exactly.

**NeoForge:** builds exist, but they currently sit at **1.2.1** — the Fabric line is further ahead, so the logo cascade, the tab animation and the sectioned settings aren't in the NeoForge files yet.

> Each file is pinned to one Minecraft version — pick the one that matches your game. The mod version looks like `<Minecraft version>+<mod version>`, e.g. `1.21.1+1.4.0`.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/N1N7225QLU)

---

Made with ❤️ by **[@pycodder](https://modrinth.com/user/pycodder)**

<details>
<summary>🇷🇺 Описание на русском</summary>

**Плавные анимации для всего интерфейса Minecraft.** Меню, кнопки, контейнеры и инвентарь плавно выезжают, увеличиваются и следуют за курсором — не влияя на игровой процесс.

Ванильные меню просто появляются и исчезают без всякого перехода, и меня это со временем начало раздражать. Поэтому я сделал ReAnimated — клиентский мод, который добавляет плавные анимации **открытия и закрытия** для всего интерфейса. На геймплей никак не влияет, чисто визуально, и спокойно работает на любом сервере.

### Возможности

Открываете меню — главное меню, инвентарь, сундук, печку, что угодно — и панель плавно въезжает на место, а не появляется рывком. Закрываете — то же движение проигрывается в обратную сторону.

- **Меню открываются и закрываются плавно** — заголовки **и** кнопки выезжают снизу вместе, на закрытии — в обратную сторону. Работает на всех экранах автоматически.
- **Контейнеры выезжают** — печка, сундук, инвентарь и любой другой контейнер плавно появляются снизу.
- **Анимация логотипа, два стиля** — логотип *Minecraft* либо **вырастает** с мягким отскоком, либо собирается **по буквам** каскадом. Выбираешь сам.
- **Вкладки выезжают** — вкладки категорий в **достижениях** и **креативном** инвентаре выезжают из-за панели по очереди: верхние поднимаются снизу, нижние опускаются сверху.
- **Подсветка слота** плавно следует за курсором по инвентарю.
- **Увеличение кнопок при наведении** — кнопки мягко растут под курсором и возвращаются обратно.

### Пресеты и свои анимации
Каждый экран анимируется выбранным пресетом:

- **Default** — выезжает снизу
- **From background** — вырастает из центра с лёгким отскоком
- **From foreground** — влетает и уменьшается до нормального размера
- **None** — движение выключено, остальное работает

Нужно что-то своё? В настройках есть **Студия анимаций** и **Редактор профиля** — живые ползунки, готовые пресеты, график easing и живое превью, чтобы собрать свою анимацию открытия/закрытия.

### Настройки

Настройки → Настройки графики → «Настройки ReAnimated», или через **Mod Menu**. (Sodium / VulkanMod / Iris? Кнопка появится и в их настройках.) Настройки разбиты на секции — **Общее · Экраны меню · Контейнеры · Курсор · Логотип · Вкладки** — у каждой анимации свой переключатель, ползунки длительности, дистанции/глубины и траектории. Всё сохраняется в `config/reanimated.json` и применяется сразу.

### Поддержка

Fabric / Quilt + Fabric API, Minecraft **1.21.1 – 1.21.11**, а также сборки **26.1 / 26.2**. Для каждой версии — отдельная сборка (рендеринг интерфейса заметно менялся между версиями), бери ровно под свою игру.

**NeoForge:** сборки есть, но пока на версии **1.2.1** — Fabric-линейка ушла вперёд, так что каскада логотипа, анимации вкладок и секций в настройках в NeoForge-файлах ещё нет.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/N1N7225QLU)

</details>
=======
A client-side mod for **Minecraft 1.21.1 (Fabric)** that adds smooth animations to the entire game interface. Pure cosmetics — not required on servers, doesn't affect gameplay.

## What gets animated

| # | Where | What it does |
|---|-------|-------------|
| 1 | Main menu | The "Minecraft" logo smoothly "grows in" with a slight bounce; buttons emerge from depth with a fade-in and upward shift, one after another. |
| 2 | Singleplayer | All buttons and the world list are animated (shared button + list system). |
| 3 | Multiplayer | Saved servers smoothly slide up one by one. |
| 4 | Options | All buttons and settings rows are animated. |
| 5 | Furnaces / chests / any container | The whole interface smoothly slides up when opened. |
| 6 | Inventory | The slot highlight (gray fill) smoothly follows the cursor; the inventory itself slides up. |
| 7 | Any button | On hover it smoothly scales up a little, and smoothly returns when the cursor leaves. |

Animations apply to **all** Minecraft screens and buttons automatically — not just the ones listed — because the mod hooks into the base interface classes (`Screen`, `ClickableWidget`, `HandledScreen`, `EntryListWidget`, `LogoDrawer`) rather than each screen individually.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for 1.21.1.
2. Place the following into your `mods` folder:
   - `reanimated-1.0.0.jar` (from `build/libs/`)
   - [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.1
3. Launch the game with the Fabric 1.21.1 profile.

## Building from source

Building requires **JDK 21** (Minecraft 1.21.1 specifically requires it, and Fabric Loom requires Gradle itself to run on Java 21, not just the toolchain).

```bash
# Linux/macOS
JAVA_HOME=/path/to/jdk-21 ./gradlew build

# Windows
set JAVA_HOME=C:\path\to\jdk-21
gradlew.bat build
```

The built mod will appear at `build/libs/reanimated-1.0.0.jar`.

To run a test client straight from the project:

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew runClient
```

## Tuning animation speed

All durations, distances, and speeds are consolidated into a single file — [`Anim.java`](src/main/java/com/pycodder/reanimated/anim/Anim.java). Change the constants (e.g. `WIDGET_HOVER_SCALE`, `CONTAINER_SLIDE`, `LOGO_DURATION`) and rebuild.

## Structure

```
src/main/java/com/pycodder/reanimated/
├── ReAnimatedClient.java         — entry point
├── anim/
│   ├── Anim.java                 — shared parameters and per-frame state
│   └── Easing.java               — easing functions
└── mixin/
    ├── ScreenMixin.java          — screen-open moment (items 1–4)
    ├── ClickableWidgetMixin.java — button appearance + scaling (items 1–4, 7)
    ├── HandledScreenMixin.java   — container slide-in + slot highlight (items 5, 6)
    ├── LogoDrawerMixin.java      — logo animation (item 1)
    └── EntryListWidgetMixin.java — list-entry slide-in (items 3, 4)
```
 main
