# ReAnimated 1.2

The big one this update is the **close animation** — menus and containers finally animate out instead of just blinking away. On top of that there's a bunch of new config options, a couple of fixes, and — finally — some real performance work.

---

## 🇬🇧 English

### Close animation
Whatever a screen does when it opens, it now does in reverse when you close it. Slide it up on open, it slides back down on close; grow it from the center, it shrinks back. Works everywhere — inventory, furnace, crafting table, chests, the pause menu, all of it — and for every way you can close a screen (Escape, the close button, the inventory key). If you close something before it finished opening, the reverse just picks up from where it was instead of snapping. There's a toggle for it in the settings if you'd rather keep instant closing.

### New settings
- **Animation speed, in ticks.** One slider that controls how fast everything plays (20 ticks = 1 second). If the inventory animation felt like too much when you're opening it constantly, turn this down — the default is quicker now too.
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

### Анимация закрытия
Теперь экран закрывается тем же движением, что и открывался, только в обратную сторону. Выехал снизу — уезжает вниз; вырос из центра — сжимается обратно. Работает везде — инвентарь, печка, верстак, сундуки, меню паузы — и при любом способе закрытия (Escape, кнопка, клавиша инвентаря). Если закрыть до конца открытия, обратный ход начнётся с текущего места, без рывка. В настройках есть переключатель, если хочется оставить мгновенное закрытие.

### Новые настройки
- **Скорость анимации в тиках.** Один ползунок на скорость всего (20 тиков = 1 секунда). Если анимация инвентаря казалась чрезмерной — просто убавь; по умолчанию она теперь и так быстрее.
- **Анимировать: Весь интерфейс / Только ванильные.** «Только ванильные» не трогает экраны из модов и анимирует лишь ванильные меню. Пригодится, если GUI какого-то мода плохо дружит с движением.
- Кнопка **«Протестированные моды»** в настройках — открывает список модов, которые точно хорошо работают с ReAnimated.

### Исправления
- Размытый фон больше не уезжает вместе с панелью при открытии контейнера — стоит на месте, как и должен, на всех версиях.
- Чат полностью не трогается, так что конфликта с модами на анимацию чата больше нет.

### И наконец: ОПТИМИЗАЦИЯ МОДА! 🚀
Вот чем я доволен больше всего. Раньше мод выполнял свою работу с отрисовкой каждый кадр, пока экран открыт — постоянно дёргал матрицы туда-сюда, даже когда вообще ничего не двигалось. А экран висит открытым, ничего не делая, бóльшую часть времени.

Теперь — нет. Когда никакая анимация не проигрывается, ReAnimated не делает **ни одной** лишней операции отрисовки: он просто отходит в сторону и включается только на те доли секунды, пока что-то реально едет или масштабируется. То есть просто открытое меню теперь не стоит тебе вообще ничего. Легче, чище и совершенно незаметно по фризам и FPS.

---

### Загрузчики и версии / Loaders & versions
- Fabric / Quilt: 1.21.1 – 1.21.11, 26.1, 26.2 (нужен Fabric API / requires Fabric API).
- Отдельный файл под каждую версию — бери подходящий к своей игре. Настройки лежат в `config/reanimated.json`.
- **NeoForge:** версия 1.2 для 1.21.1 – 26.2 выйдет на этой неделе. / **NeoForge:** the 1.2 build for 1.21.1 – 26.2 lands this week.

*mod by @pycodder — https://modrinth.com/user/pycodder*
