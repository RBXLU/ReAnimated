# ReAnimated 1.2 — NeoForge

This is the big one for NeoForge: the port finally catches up with the Fabric build and then some. You get the brand-new **close animation**, all the **UI appearance presets**, a pile of new config options, a couple of fixes, and — finally — real performance work.

---

## 🇬🇧 English

### UI presets (new on NeoForge)
The whole appearance system from the Fabric build is here now. Pick how screens come in from the settings:
- **Default** — slides up from the bottom.
- **From background** — grows out of the center with a little bounce.
- **From foreground** — flies in shrinking down to size.
- **No animation** — instant, if you want it off.

The blurred background always stays put no matter which preset you pick — only the panel and its contents move.

### Close animation
Whatever a screen does when it opens, it now does in reverse when you close it. Slide it up on open, it slides back down on close; grow it from the center, it shrinks back. Works everywhere — inventory, furnace, crafting table, chests, the pause menu, all of it — and for every way you can close a screen (Escape, the close button, the inventory key). If you close something before it finished opening, the reverse just picks up from where it was instead of snapping. There's a toggle for it in the settings if you'd rather keep instant closing.

### New settings
- **Animation speed, in ticks.** One slider that controls how fast everything plays (20 ticks = 1 second). If the inventory animation felt like too much when you're opening it constantly, turn this down — the default is quick.
- **Animate: All UI / Vanilla only.** "Vanilla only" leaves modded screens alone and only animates the game's own menus. Handy if a mod's GUI doesn't play nice with the movement.
- **Tested mods** button in the settings — opens the list of mods that are confirmed to work well with ReAnimated.

### Fixes
- The blurred background no longer drifts along with the panel when you open a container. It stays put like it should now, on every version.
- Chat is left alone completely, so it won't fight with chat-animation mods anymore.

### And finally: THE MOD IS OPTIMIZED! 🚀
This is the one I'm most happy about. Up until now the mod did its rendering work every single frame a screen was open — pushing and popping matrices constantly, even when absolutely nothing was moving. And a screen sits open, doing nothing, most of the time.

Now it doesn't. When there's no animation actually playing, ReAnimated does **zero** extra rendering work — it steps out of the way entirely and only kicks in for the fraction of a second something is actually sliding or scaling. So a menu just sitting there open costs you nothing now. Lighter, cleaner, and it should be completely invisible in your frame times.

---

## 🇷🇺 Русский

### Пресеты появления UI (новое на NeoForge)
Вся система внешнего вида из Fabric-сборки теперь здесь. Выбери в настройках, как экраны появляются:
- **Default** — выезжает снизу.
- **From background** — вырастает из центра с лёгким отскоком.
- **From foreground** — влетает, уменьшаясь до нормального размера.
- **No animation** — мгновенно, если хочется выключить.

Размытый фон при любом пресете стоит на месте — двигается только панель и её содержимое.

### Анимация закрытия
Теперь экран закрывается тем же движением, что и открывался, только в обратную сторону. Выехал снизу — уезжает вниз; вырос из центра — сжимается обратно. Работает везде — инвентарь, печка, верстак, сундуки, меню паузы — и при любом способе закрытия (Escape, кнопка, клавиша инвентаря). Если закрыть до конца открытия, обратный ход начнётся с текущего места, без рывка. В настройках есть переключатель, если хочется оставить мгновенное закрытие.

### Новые настройки
- **Скорость анимации в тиках.** Один ползунок на скорость всего (20 тиков = 1 секунда). Если анимация инвентаря казалась чрезмерной — просто убавь; по умолчанию она и так быстрая.
- **Анимировать: Весь интерфейс / Только ванильные.** «Только ванильные» не трогает экраны из модов и анимирует лишь ванильные меню. Пригодится, если GUI какого-то мода плохо дружит с движением.
- Кнопка **«Протестированные моды»** в настройках — открывает список модов, которые точно хорошо работают с ReAnimated.

### Исправления
- Размытый фон больше не уезжает вместе с панелью при открытии контейнера — стоит на месте, как и должен, на всех версиях.
- Чат полностью не трогается, так что конфликта с модами на анимацию чата больше нет.

### И наконец: ОПТИМИЗАЦИЯ МОДА! 🚀
Вот чем я доволен больше всего. Раньше мод выполнял свою работу с отрисовкой каждый кадр, пока экран открыт — постоянно дёргал матрицы туда-сюда, даже когда вообще ничего не двигалось. А экран висит открытым, ничего не делая, бóльшую часть времени.

Теперь — нет. Когда никакая анимация не проигрывается, ReAnimated не делает **ни одной** лишней операции отрисовки: он просто отходит в сторону и включается только на те доли секунды, пока что-то реально едет или масштабируется. То есть просто открытое меню теперь не стоит тебе вообще ничего. Легче, чище и совершенно незаметно по фризам и FPS.

---

### Загрузчики и версии / Loader & versions
- **NeoForge:** 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11.
- NeoForge не выпускался для 1.21.2 / 1.21.6 / 1.21.7 / 1.21.9 и для снапшотов 26.x — там доступна только сборка под Fabric / Quilt. / NeoForge was never released for 1.21.2 / 1.21.6 / 1.21.7 / 1.21.9 or the 26.x snapshots — use the Fabric / Quilt build there.
- Отдельный файл под каждую версию — бери подходящий к своей игре. Настройки лежат в `config/reanimated.json`. / One file per version — grab the one matching your game. Settings live in `config/reanimated.json`.

*mod by @pycodder — https://modrinth.com/user/pycodder*
