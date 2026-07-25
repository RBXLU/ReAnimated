# ReAnimated 1.4.0 — the logo spells itself in, and tabs slide out from behind the panel

Two new animations, a tidier settings screen, and a fix for button text that refused to fade away.

---

## 🇬🇧 English

### 🔤 Logo letter cascade — a new logo style
The "Minecraft" title logo can now come together **one letter at a time**: each letter rises into place on its own cascade beat, left to right.

- A new **Logo style** switch: **Grow** (the original animation — the whole logo scales in with a bounce) or **Letters** (the new per-letter cascade).
- Both styles stay fully tunable — duration, cascade delay, offset, direction, easing.

### 📑 Tabs slide out from behind the panel — Advancements + Creative
Open the **Advancements** screen or the **Creative** inventory and the category tabs now **slide out from behind the panel, one after another**.

- **Top tabs rise up from below, bottom tabs drop down from above** — each emerging from behind its own edge of the panel.
- While a tab is still behind the panel it stays hidden, so it really looks like it slides out — no icons poking through the frame.
- Configurable: on/off, duration, cascade delay, **depth (offset)**, easing.

### ⚙️ Settings, now in sections
The settings screen was one long list; it's now grouped under headings — **General · Menu screens · Containers · Cursor · Logo · Tabs** — so finding a knob is quick. On every supported version.

### 🩹 Fixes
- **Button text now fades out with its button.** As a button faded to nearly invisible, its label used to snap back to fully solid — leaving text floating on screen with no button under it. (Minecraft itself forces near-transparent text back to opaque; ReAnimated now stops drawing a widget once it's that faint.)
- **Mod Menu button on Minecraft 26.1 / 26.2.** The mod had no Mod Menu integration on those versions at all — the settings button is there now.

### Notes
- Upgrading from an older config? Your saved tab settings keep their old values — bump **Tabs → offset Y** in settings (or delete `config/reanimated.json`) to get the new deeper default.
- Everything else from 1.3 is still here: the **Animation Studio** and **Profile Editor** for building your own open/close animation, presets, close animations, hover scaling and the following slot highlight.

---

## 🇷🇺 Русский

### 🔤 Побуквенный каскад логотипа — новый стиль
Логотип «Minecraft» теперь может собираться **по одной букве**: каждая буква поднимается на место по своему шагу каскада, слева направо.

- Новый переключатель **стиля логотипа**: **Рост** (родная анимация — логотип целиком вырастает с отскоком) или **Буквы** (новый побуквенный каскад).
- Оба стиля полностью настраиваются — длительность, задержка каскада, сдвиг, направление, траектория.

### 📑 Вкладки выезжают из-за панели — достижения + креатив
Открой экран **достижений** или **креативный** инвентарь — вкладки категорий теперь **выезжают из-за панели по очереди**.

- **Верхние вкладки поднимаются снизу, нижние — опускаются сверху**, каждая появляется из-за своего края панели.
- Пока вкладка ещё за панелью — её не видно, поэтому она действительно «выезжает из-под панели», без торчащих из рамки иконок.
- Настраивается: вкл/выкл, длительность, задержка каскада, **глубина (сдвиг)**, траектория.

### ⚙️ Настройки — теперь по секциям
Раньше настройки были одним длинным списком; теперь всё под заголовками — **Общее · Экраны меню · Контейнеры · Курсор · Логотип · Вкладки** — нужное находится сразу. На всех поддерживаемых версиях.

### 🩹 Исправления
- **Текст кнопки теперь гаснет вместе с кнопкой.** Когда кнопка догорала почти до невидимости, её надпись скачком становилась полностью непрозрачной — и на экране висел текст без кнопки под ним. (Сам Minecraft принудительно делает почти прозрачный текст непрозрачным; теперь ReAnimated просто не рисует настолько погасший виджет.)
- **Кнопка в Mod Menu на Minecraft 26.1 / 26.2.** На этих версиях интеграции с Mod Menu не было вообще — теперь кнопка настроек на месте.

### Заметки
- Обновляешься со старого конфига? Сохранённые настройки вкладок держат прежние значения — подними **Вкладки → сдвиг Y** (или удали `config/reanimated.json`), чтобы получить новую глубину по умолчанию.
- Всё остальное из 1.3 на месте: **Студия анимаций** и **Редактор профиля** для своей анимации открытия/закрытия, пресеты, анимации закрытия, увеличение под курсором и подсветка слота, следующая за курсором.

---

### Загрузчики и версии / Loaders & versions
- **Fabric / Quilt**, Minecraft **1.21.1 – 1.21.11, 26.1, 26.2** (нужен Fabric API). / **Fabric / Quilt**, Minecraft **1.21.1 – 1.21.11, 26.1, 26.2** (requires Fabric API).
- Версия мода — `<версия Minecraft>+1.4.0`, например `1.21.1+1.4.0`. / Mod version is `<Minecraft version>+1.4.0`, e.g. `1.21.1+1.4.0`.
- Конфиг от 1.3 подхватывается как есть. / Your 1.3 config carries over as-is.

### Благодарности / Credits
- Побуквенный каскад логотипа (идея + текстуры букв) и идея каскада вкладок — из мода **EaseGUI** (Weyne1), **LGPL-3.0**. Полная атрибуция — в `CREDITS.md` и папке `licenses/`. / The logo letter cascade (idea + letter textures) and the tab-cascade idea come from **EaseGUI** (Weyne1), **LGPL-3.0**. Full attribution is in `CREDITS.md` and the `licenses/` folder.

*mod by @pycodder — https://modrinth.com/user/pycodder*
