# ReAnimated 1.6 — Fabric / Quilt

Everything here comes from player feedback on 1.5.0: the settings screen was cramped and partly unreachable, lists sat still while everything around them moved, the player model in the inventory waited for the panel to catch up, and clipping got in the way of other mods' GUIs.

---

## 🇬🇧 English

### ⚙️ The settings screen is readable again

On **26.1 / 26.2** every option lived in one fixed two-column grid about 605 px tall. Anything below the bottom of your screen was simply unreachable — the screen had no scrolling — and five buttons pinned to the corners overlapped the first rows.

- **Sections are now tabs.** One section at a time: *General · Menu · Pause · Containers · Cursor · Logo · Tabs*. The tallest one is four rows, so everything fits even at 320×240.
- The corner buttons are gone; **Export / Import / Studio / Profile Editor** now live inside the **General** tab.

On **1.21.1 – 1.21.11** the list already scrolled, but the same five corner buttons overlapped the option rows at GUI scale 3–4, where the screen gets narrower than 558 px. They've moved into a **Tools** section at the top of the scrolling list.

On the same versions the **Animate** row also read `Animate: Animate: All UI` — the button prefixes the option name itself, and the mod was adding it a second time. It now reads `Animate: All UI`.

### 📜 Lists animate with the screen

Frames with rows in them — **the mod list in Mod Menu**, servers, worlds, resource packs, data packs — used to be deliberately frozen in place while the rest of the screen slid in. That was a workaround for an old clipping problem, and it is no longer needed.

- The list travels with the screen like everything else.
- **Rows arrive one after another**, top to bottom, sliding up out of the list frame.
- Can be turned off in **General → Animate lists** (the old frozen behaviour comes back).
- Fine-tuning (duration, row delay, distance, easing) lives in `config/reanimated.json` under `profileLists`.

### 🧪 Settings inside Sodium

If you run **Sodium 0.8 or newer**, ReAnimated now appears as its own entry in Sodium's settings screen — pages in the list on the left, options drawn with Sodium's own controls, covered by its search and applied with its Apply button. The floating button over Sodium's screen disappears on its own once the page is there.

- Pages mirror the mod's sections: *General · Menu · Pause · Containers · Cursor · Logo · Tabs*.
- Animation Studio, Profile Editor and the mod's own settings screen open straight from the *Tools* group.
- Sodium has no fractional sliders, so scales are shown as percentages and durations in milliseconds — the config file keeps them as they were.
- Available on **1.21.1, 1.21.11, 26.1 and 26.2** — the only versions Sodium 0.8+ exists for. On every other version, and on older Sodium, the usual button stays.

### 🧭 The mod's button stays off other mods' screens

The video settings screen gets a **ReAnimated settings** button, and the mod was supposed to look for a free corner to put it in. It never really looked: the button always went to the top-left corner.

With **Sodium 0.8+** that never showed, because the settings already live as a page inside Sodium. But **Sodium 0.6** — the only branch that exists for 1.21.2 – 1.21.9 — has no config API, so the page cannot be built and the button does appear. It landed straight on Sodium's `General / Quality / Performance / Advanced` tab row, right on top of the labels. **Iris, VulkanMod and Embeddium** replace that screen the same way.

The spot is now chosen from the widgets that are actually on the screen, and the button shrinks to the gap it has. Bounds come from the widget itself where it exposes them, otherwise from `getNavigationFocus()`. Sodium 0.6 offers neither — its widgets implement only `isMouseOver` — so as a last resort the candidate rectangle is probed point by point. When nothing fits, no button is drawn at all and the settings stay one click away through the mods list.

The vanilla video settings screen is untouched: the button sits exactly where it always did.

### 🧩 Clipping no longer breaks other mods' GUIs

On **1.21.1 – 1.21.3** the mod shifted every clipping rectangle by whatever was in the drawing matrix at that moment — including transforms other mods had just applied. Those mods write clipping in their own coordinates and count on the vanilla behaviour of those versions, where the matrix is ignored, so their content was clipped away.

That's why **items disappeared while scrolling the creative inventory with Smooth Scrolling installed**, and why the block list in Rechiseled used to render empty. Clipping now moves with ReAnimated's own animation only, and never with someone else's transform. On 1.21.4 and newer the game does this itself, so those versions were never affected.

### 🖼️ Overlays from other UI mods travel with the screen

Mods like **EMI, JEI and REI** draw their item panel from a hook on the same method ReAnimated wraps. Which hook ran first was up to chance, and ours usually won — so the panel stood still while the screen behind it slid into place.

ReAnimated's wrapper now has a higher mixin priority, which makes it the outermost one: everything other mods add to a screen is drawn inside the animation and moves with it.

### 🧍 The inventory player model keeps up

Since 1.21.6 the game draws the player model in your inventory — the one that follows your cursor — outside the interface transform, with raw screen coordinates, while its clipping rectangle *did* follow the animation. The model stood still and got cut in half while the panel travelled towards it.

Its rectangle and size now go through the same transform as the panel, so it rides along and scales with presets like *From background*. Affects **1.21.6 – 1.21.11, 26.1 and 26.2**; on 1.21.1 – 1.21.5 the model was already drawn through the interface transform.

### 🤝 Support for the mods you actually run

1.6 was checked against a full modpack rather than a bare client. On **Fabric 1.21.1, 1.21.11 and 26.2** every screen the mod touches was opened with **Sodium, Sodium Extra, Reese's Sodium Options, Xaero's World Map, Simple Voice Chat, FancyMenu, Essential, Better F3, Better Advancements, Puzzle, Polymorph** and **Mod Menu** installed at once.

Nothing in the logs points at a conflict with ReAnimated, and a screen travels as one piece: Sodium's panel with its search and page list, Mod Menu's list together with its rows, and the vanilla menus. FancyMenu's toolbar stays put, because it is drawn as an overlay above the screen rather than inside it. What that pass did turn up is fixed below.

### 🐛 Fixed in this release

- Options no longer hide below the bottom of the screen on **26.1 / 26.2**, and the corner buttons no longer sit on the first rows.
- The **Animate** row read `Animate: Animate: All UI`; it now reads `Animate: All UI`.
- The mod's button no longer lands on the tab row of **Sodium 0.6, Iris, VulkanMod and Embeddium**.
- Clipping no longer eats other mods' GUI content on **1.21.1 – 1.21.3** — the cause behind the vanishing items with **Smooth Scrolling** and the empty block list in **Rechiseled**.
- The item panel of **EMI, JEI and REI** no longer stands still while the screen slides in.
- The inventory player model no longer lags behind the panel on **1.21.6+**.

### Versions

There's a **separate download for each Minecraft version** — GUI rendering changed a fair amount across 1.21.x and again in 26.x. File names look like `reanimated-<Minecraft version>+1.6.jar`.

| | |
|---|---|
| Minecraft | 1.21.1 – 1.21.11, 26.1.2, 26.2 |
| Loader | **Fabric** or **Quilt**, plus **[Fabric API](https://modrinth.com/mod/fabric-api)** |

Client-side only — not required on servers, and players without it won't notice anything different. Your config carries over untouched: `listsEnabled` and `profileLists` appear with their defaults on first launch.

Everything from 1.5.0 is unchanged: button press, the pause menu's own settings, config sharing through the clipboard, and the background dim fade.

### 💬 What's planned in future updates:
- Performance update

---

## 🇷🇺 Русский

Всё, что здесь есть, пришло из отзывов игроков на 1.5.0: экран настроек оказался тесным и частично недоступным, списки стояли на месте, пока всё вокруг ехало, модель игрока в инвентаре дожидалась панель, а обрезка мешала интерфейсам других модов.

### ⚙️ Экран настроек снова читается

На **26.1 / 26.2** все опции лежали одной жёсткой сеткой в два столбца высотой около 605 px. Всё, что не помещалось в высоту экрана, было просто недоступно — прокрутки у экрана не было, — а пять кнопок по углам наезжали на первые ряды.

- **Секции стали вкладками.** По одной за раз: *Общее · Меню · Пауза · Контейнеры · Курсор · Логотип · Вкладки*. Самая высокая — четыре ряда, помещается даже в 320×240.
- Угловых кнопок больше нет: **Копировать / Вставить / Студия / Редактор профиля** переехали во вкладку **Общее**.

На **1.21.1 – 1.21.11** список и раньше прокручивался, но те же пять кнопок по углам наезжали на строки при GUI scale 3–4, где ширина экрана падает ниже 558 px. Теперь они — секция **Инструменты** в начале самого списка.

Там же строка **Анимировать** показывала `Анимировать: Анимировать: Весь интерфейс` — кнопка сама подставляет имя опции, а мод добавлял его второй раз. Теперь там `Анимировать: Весь интерфейс`.

### 📜 Списки едут вместе с экраном

Фреймы со строками — **список модов в Mod Menu**, сервера, миры, ресурспаки, наборы данных — намеренно стояли на месте, пока остальной экран выезжал. Это был обход старой проблемы с обрезкой, и он больше не нужен.

- Список едет вместе с экраном, как и всё остальное.
- **Строки появляются по очереди** сверху вниз, выезжая из рамки списка.
- Отключается в **Общее → Анимация списков** (вернётся прежнее поведение).
- Тонкая настройка (длительность, задержка между строками, дистанция, траектория) — в `config/reanimated.json`, раздел `profileLists`.

### 🧪 Настройки внутри Sodium

На **Sodium 0.8 и новее** ReAnimated появляется отдельным пунктом в экране Sodium — страницы в списке слева, элементы управления его же, вместе с его поиском и кнопкой «Применить». Отдельная кнопка поверх его экрана убирается сама, когда страница есть.

- Страницы повторяют секции мода: *Общее · Меню · Пауза · Контейнеры · Курсор · Логотип · Вкладки*.
- Студия анимаций, редактор профиля и полный экран настроек открываются прямо из группы *Инструменты*.
- Дробных ползунков у Sodium нет, поэтому масштабы показаны в процентах, а длительности в миллисекундах — в файле конфига всё осталось как было.
- Доступно на **1.21.1, 1.21.11, 26.1 и 26.2** — только для этих версий существует Sodium 0.8+. На остальных версиях и на старом Sodium остаётся привычная кнопка.

### 🧭 Кнопка мода больше не ложится на чужие экраны

На экране настроек графики появляется кнопка **Настройки ReAnimated**, и мод должен был искать под неё свободный угол. На деле не искал: кнопка всегда вставала в левый верхний угол.

С **Sodium 0.8+** это не было видно — там кнопка не показывается вовсе, потому что настройки уже лежат страницей внутри Sodium. Но у **Sodium 0.6** — единственной ветки, существующей для 1.21.2 – 1.21.9, — config API нет, страницу собрать не из чего, и кнопка появляется. Она ложилась прямо на ряд вкладок `General / Quality / Performance / Advanced`, поверх подписей. **Iris, VulkanMod и Embeddium** подменяют этот экран точно так же.

Теперь место выбирается по виджетам, которые реально есть на экране, а ширина кнопки ужимается под свободный зазор. Границы берутся у самого виджета, если он их отдаёт, иначе — через `getNavigationFocus()`. Sodium 0.6 не отдаёт ни того, ни другого: его виджеты реализуют только `isMouseOver`, — поэтому в последнюю очередь прямоугольник-кандидат проверяется по точкам. Если не помещается ничего, кнопка не рисуется вовсе, а настройки остаются в одном клике через список модов.

Ванильный экран настроек графики не затронут — кнопка там на прежнем месте.

### 🧩 Обрезка больше не ломает чужие интерфейсы

На **1.21.1 – 1.21.3** мод сдвигал любой прямоугольник обрезки на всё, что оказалось в матрице отрисовки, — включая трансформации, наложенные другими модами. А они задают обрезку в своих координатах и рассчитывают на ванильное поведение этих версий, где матрица не учитывается: их содержимое уезжало под обрезку и пропадало.

Отсюда и **пропадающие предметы при прокрутке креативного инвентаря с модом Smooth Scrolling**, и пустой список блоков в Rechiseled. Теперь обрезка едет только на собственную анимацию ReAnimated и никогда — на чужую. С 1.21.4 это делает сама игра, поэтому те версии никогда не были затронуты.

### 🖼️ Оверлеи других UI-модов едут вместе с экраном

**EMI, JEI, REI** и подобные рисуют свою панель предметов из хука на том же методе, который оборачивает ReAnimated. Чей хук отработает первым — было делом случая, и обычно выигрывал наш: панель стояла на месте, пока экран за ней выезжал.

Теперь у нашей обёртки выше приоритет миксина, и она стала самой внешней: всё, что моды дорисовывают к экрану, попадает внутрь анимации и едет вместе с ним.

### 🧍 Модель игрока в инвентаре не отстаёт

С 1.21.6 игра рисует модель игрока — ту, что следит за курсором, — мимо трансформации интерфейса, по сырым экранным координатам, а прямоугольник обрезки при этом за анимацией следовал. Модель стояла на месте и обрезалась наполовину, пока панель до неё ехала.

Теперь прямоугольник и размер модели проходят через ту же матрицу, что и панель: она едет вместе с ней и масштабируется на пресетах вроде «Из фона». Касается **1.21.6 – 1.21.11, 26.1 и 26.2**; на 1.21.1 – 1.21.5 модель и так рисовалась через трансформацию интерфейса.

### 🤝 Поддержка модов, с которыми реально играют

1.6 проверялась не на голом клиенте, а на полноценной сборке. На **Fabric 1.21.1, 1.21.11 и 26.2** каждый экран, которого касается мод, открывался с одновременно установленными **Sodium, Sodium Extra, Reese's Sodium Options, Xaero's World Map, Simple Voice Chat, FancyMenu, Essential, Better F3, Better Advancements, Puzzle, Polymorph** и **Mod Menu**.

В логах нет ни одного конфликта с ReAnimated, а экран едет единым целым: панель Sodium с её поиском и списком страниц, список Mod Menu вместе со строками, ванильные меню. Тулбар FancyMenu остаётся на месте — он рисуется оверлеем поверх экрана, а не внутри него. То, что этот прогон всё-таки выявил, исправлено ниже.

### 🐛 Что исправлено в этой версии

- На **26.1 / 26.2** опции больше не прячутся за нижним краем экрана, а угловые кнопки не лежат на первых рядах.
- Строка **Анимировать** показывала `Анимировать: Анимировать: Весь интерфейс` — теперь `Анимировать: Весь интерфейс`.
- Кнопка мода больше не ложится на ряд вкладок **Sodium 0.6, Iris, VulkanMod и Embeddium**.
- Обрезка больше не съедает содержимое чужих интерфейсов на **1.21.1 – 1.21.3** — это и была причина пропадающих предметов со **Smooth Scrolling** и пустого списка блоков в **Rechiseled**.
- Панель предметов **EMI, JEI и REI** больше не стоит на месте, пока экран выезжает.
- Модель игрока в инвентаре больше не отстаёт от панели на **1.21.6+**.

### Версии

Для **каждой версии Minecraft — свой файл**: отрисовка интерфейса заметно менялась внутри 1.21.x и ещё раз в 26.x. Имя файла выглядит как `reanimated-<версия Minecraft>+1.6.jar`.

| | |
|---|---|
| Minecraft | 1.21.1 – 1.21.11, 26.1.2, 26.2 |
| Загрузчик | **Fabric** или **Quilt** плюс **[Fabric API](https://modrinth.com/mod/fabric-api)** |

Мод клиентский: на сервере не нужен, игроки без него ничего не заметят. Конфиг переносится как есть — `listsEnabled` и `profileLists` при первом запуске получают значения по умолчанию.

Всё из 1.5.0 на месте: нажатие кнопок, свои настройки меню паузы, обмен конфигом через буфер и плавное затемнение фона.

### 💬 Что запланировано в следующих обновлениях:
- Улучшение производительности
