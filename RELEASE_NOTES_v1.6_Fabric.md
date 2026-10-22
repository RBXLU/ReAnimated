# ReAnimated 1.6 — Fabric / Quilt

Everything here comes from player feedback on 1.5.0: the settings screen was cramped and partly unreachable, lists sat still while everything around them moved, the player model in the inventory waited for the panel to catch up, and clipping got in the way of other mods' GUIs.

---

## 🇬🇧 English

### ⚙️ The settings screen is readable again

On **26.1 / 26.2** every option lived in one fixed two-column grid about 605 px tall. Anything below the bottom of your screen was simply unreachable — the screen had no scrolling — and five buttons pinned to the corners overlapped the first rows.

- **Sections are now tabs.** One section at a time: *General · Menu · Pause · Containers · Cursor · Logo · Tabs*. The tallest one is four rows, so everything fits even at 320×240.
- The corner buttons are gone; **Export / Import / Studio / Profile Editor** now live inside the **General** tab.

On **1.21.1 – 1.21.11** the list already scrolled, but the same five corner buttons overlapped the option rows at GUI scale 3–4, where the screen gets narrower than 558 px. They've moved into a **Tools** section at the top of the scrolling list.

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

### 🧩 Clipping no longer breaks other mods' GUIs

On **1.21.1 – 1.21.3** the mod shifted every clipping rectangle by whatever was in the drawing matrix at that moment — including transforms other mods had just applied. Those mods write clipping in their own coordinates and count on the vanilla behaviour of those versions, where the matrix is ignored, so their content was clipped away.

That's why **items disappeared while scrolling the creative inventory with Smooth Scrolling installed**, and why the block list in Rechiseled used to render empty. Clipping now moves with ReAnimated's own animation only, and never with someone else's transform. On 1.21.4 and newer the game does this itself, so those versions were never affected.

### 🖼️ Overlays from other UI mods travel with the screen

Mods like **EMI, JEI and REI** draw their item panel from a hook on the same method ReAnimated wraps. Which hook ran first was up to chance, and ours usually won — so the panel stood still while the screen behind it slid into place.

ReAnimated's wrapper now has a higher mixin priority, which makes it the outermost one: everything other mods add to a screen is drawn inside the animation and moves with it.

### 🧍 The inventory player model keeps up

Since 1.21.6 the game draws the player model in your inventory — the one that follows your cursor — outside the interface transform, with raw screen coordinates, while its clipping rectangle *did* follow the animation. The model stood still and got cut in half while the panel travelled towards it.

Its rectangle and size now go through the same transform as the panel, so it rides along and scales with presets like *From background*. Affects **1.21.6 – 1.21.11, 26.1 and 26.2**; on 1.21.1 – 1.21.5 the model was already drawn through the interface transform.

### Versions

There's a **separate download for each Minecraft version** — GUI rendering changed a fair amount across 1.21.x and again in 26.x. File names look like `reanimated-<Minecraft version>+1.6.jar`.

| | |
|---|---|
| Minecraft | 1.21.1 – 1.21.11, 26.1.2, 26.2 |
| Loader | **Fabric** or **Quilt**, plus **[Fabric API](https://modrinth.com/mod/fabric-api)** |

Client-side only — not required on servers, and players without it won't notice anything different. Your config carries over untouched: `listsEnabled` and `profileLists` appear with their defaults on first launch.

Everything from 1.5.0 is unchanged: button press, the pause menu's own settings, config sharing through the clipboard, and the background dim fade.

### 💬 What's planned in future updates:
- 1. Add support for most mods
- 2. Performance update
- 3. Bug fixes.

---

## 🇷🇺 Русский

Всё, что здесь есть, пришло из отзывов игроков на 1.5.0: экран настроек оказался тесным и частично недоступным, списки стояли на месте, пока всё вокруг ехало, модель игрока в инвентаре дожидалась панель, а обрезка мешала интерфейсам других модов.

### ⚙️ Экран настроек снова читается

На **26.1 / 26.2** все опции лежали одной жёсткой сеткой в два столбца высотой около 605 px. Всё, что не помещалось в высоту экрана, было просто недоступно — прокрутки у экрана не было, — а пять кнопок по углам наезжали на первые ряды.

- **Секции стали вкладками.** По одной за раз: *Общее · Меню · Пауза · Контейнеры · Курсор · Логотип · Вкладки*. Самая высокая — четыре ряда, помещается даже в 320×240.
- Угловых кнопок больше нет: **Копировать / Вставить / Студия / Редактор профиля** переехали во вкладку **Общее**.

На **1.21.1 – 1.21.11** список и раньше прокручивался, но те же пять кнопок по углам наезжали на строки при GUI scale 3–4, где ширина экрана падает ниже 558 px. Теперь они — секция **Инструменты** в начале самого списка.

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

### 🧩 Обрезка больше не ломает чужие интерфейсы

На **1.21.1 – 1.21.3** мод сдвигал любой прямоугольник обрезки на всё, что оказалось в матрице отрисовки, — включая трансформации, наложенные другими модами. А они задают обрезку в своих координатах и рассчитывают на ванильное поведение этих версий, где матрица не учитывается: их содержимое уезжало под обрезку и пропадало.

Отсюда и **пропадающие предметы при прокрутке креативного инвентаря с модом Smooth Scrolling**, и пустой список блоков в Rechiseled. Теперь обрезка едет только на собственную анимацию ReAnimated и никогда — на чужую. С 1.21.4 это делает сама игра, поэтому те версии никогда не были затронуты.

### 🖼️ Оверлеи других UI-модов едут вместе с экраном

**EMI, JEI, REI** и подобные рисуют свою панель предметов из хука на том же методе, который оборачивает ReAnimated. Чей хук отработает первым — было делом случая, и обычно выигрывал наш: панель стояла на месте, пока экран за ней выезжал.

Теперь у нашей обёртки выше приоритет миксина, и она стала самой внешней: всё, что моды дорисовывают к экрану, попадает внутрь анимации и едет вместе с ним.

### 🧍 Модель игрока в инвентаре не отстаёт

С 1.21.6 игра рисует модель игрока — ту, что следит за курсором, — мимо трансформации интерфейса, по сырым экранным координатам, а прямоугольник обрезки при этом за анимацией следовал. Модель стояла на месте и обрезалась наполовину, пока панель до неё ехала.

Теперь прямоугольник и размер модели проходят через ту же матрицу, что и панель: она едет вместе с ней и масштабируется на пресетах вроде «Из фона». Касается **1.21.6 – 1.21.11, 26.1 и 26.2**; на 1.21.1 – 1.21.5 модель и так рисовалась через трансформацию интерфейса.

### Версии

Для **каждой версии Minecraft — свой файл**: отрисовка интерфейса заметно менялась внутри 1.21.x и ещё раз в 26.x. Имя файла выглядит как `reanimated-<версия Minecraft>+1.6.jar`.

| | |
|---|---|
| Minecraft | 1.21.1 – 1.21.11, 26.1.2, 26.2 |
| Загрузчик | **Fabric** или **Quilt** плюс **[Fabric API](https://modrinth.com/mod/fabric-api)** |

Мод клиентский: на сервере не нужен, игроки без него ничего не заметят. Конфиг переносится как есть — `listsEnabled` и `profileLists` при первом запуске получают значения по умолчанию.

Всё из 1.5.0 на месте: нажатие кнопок, свои настройки меню паузы, обмен конфигом через буфер и плавное затемнение фона.

### 💬 Что запланировано в следующих обновлениях:
- 1. Добавление поддержки кучи модов
- 2. Улучшение производительности
- 3. Фикс багов
