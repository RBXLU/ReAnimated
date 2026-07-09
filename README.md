# ReAnimated

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
