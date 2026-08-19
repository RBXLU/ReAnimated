# ReAnimated 1.5 — NeoForge

The NeoForge line jumps straight from 1.4.1 to 1.5: it picks up everything the Fabric builds got in 1.5.0 (which never shipped for NeoForge) plus everything added since from player feedback.

---

## 🇬🇧 English

### 🖱️ Buttons press in
Click a button — or hit Enter on the one you've selected — and it now **dips inward and springs back**, instead of only playing a sound. It rides on top of the hover scaling, so a button under the cursor stays enlarged and presses down from there.

- Configurable: **on/off**, **depth** and **duration**.
- Fires only when the widget actually takes the click — a dead click on a disabled button leaves it alone.
- Works on ordinary buttons, cycling options and **sliders** alike.
- **Text fields are left out on purpose** — of the press *and* of the hover scaling. The scaling is purely visual, while the game still counts clicks and the caret at the unscaled position, so a scaled-up field drifts under the mouse.

### ⏸️ The pause menu has its own settings
Esc is the screen you open most often, and the timing that suits a settings menu feels sluggish there. The pause menu now has **its own tab**: on/off, speed, slide distance and trajectory, plus **its own preset** or *Same as general* if you'd rather it follow the main one. Out of the box it's a touch faster than the rest of the UI (4 ticks against 6).

### 📋 Export and import your settings
Two buttons in the **General** tab: **Export** puts your whole configuration into the clipboard as a single line, **Import** reads it back and applies it immediately. Handy for carrying a setup to another instance or sharing an animation you tuned in the Studio. The format matches `config/reanimated.json`, so pasting the file's contents works too; anything unreadable is rejected and your current settings stay untouched.

### 🌘 Background dimming fades in
Vanilla switches the dimming behind a screen on instantly, so the panel used to slide in over a background that had already snapped dark. Now that dimming **fades in along the same curve as the screen** and fades back out on close. Visible in-game (inventory, chests, the pause menu); while no animation is playing, Minecraft draws the dimming itself exactly as before, so mods that change it are unaffected. Can be switched off in **General**.

### 📜 Lists animate with the screen
Frames with rows in them — servers, worlds, resource packs, data packs, mod lists — used to be deliberately frozen in place while the rest of the screen slid in. That was a workaround for an old clipping problem, and it is no longer needed.

- The list travels with the screen like everything else.
- **Rows arrive one after another**, top to bottom, sliding up out of the list frame.
- Can be turned off in **General → Animate lists**.
- Fine-tuning (duration, row delay, distance, easing) lives in `config/reanimated.json` under `profileLists`.

### ⚙️ Settings screen fixes
- The **seventh tab no longer sits on top of the first row of options**. Seven tabs need three rows, but the options started at a fixed height that only allowed for two.
- **"Tested mods" is no longer cut off** mid-word — the link buttons are now as wide as their label instead of a fixed 116 px.
- **The screen now fits the window it is given.** The layout was hard-wired: three columns of tabs, two columns of rows 20 px tall and 158 px wide. At 427×240 the **General** section — eleven rows — did not fit at all: the bottom row slid under the **Done** button and off the edge of the screen, and most labels were cut down to scrolling text. The number of tab columns, the row grid and the column width are now picked to match the window, so a whole section always sits between the tabs and **Done**, down to 320×240. The block of options is centred in the space it has, a half-filled row of tabs is centred on its own, and rows may now be up to 240 px wide instead of 158, so far fewer labels have to scroll.

### 🧭 The mod's button stays off Sodium's screen

On the video settings screen ReAnimated adds a **ReAnimated settings** button and hunts for a free corner to put it in. That hunt only ever counted vanilla widgets — and Sodium's search box, page list and buttons are not vanilla widgets, they are Sodium's own. The screen looked empty, so the button went to the top-left corner, straight on top of Sodium's search field and version panel. **Iris, Embeddium and VulkanMod** are built the same way.

The check now measures every widget on the screen, whichever mod it belongs to, and the button shrinks to the gap it actually has — or stays away entirely when there is none, in which case the settings are still one click away through the mods list. Verified against Sodium 0.6.13 and 0.8.12 / 0.8.13 on 1.21.1, 1.21.3, 1.21.5 and 1.21.11. The vanilla video settings screen is untouched — the button sits exactly where it always did.

### 🧩 Clipping no longer breaks other mods' GUIs
On **1.21.1 and 1.21.3** the mod shifted every clipping rectangle by whatever was in the drawing matrix at that moment — including transforms other mods had just applied. Those mods write clipping in their own coordinates and count on the vanilla behaviour of those versions, where the matrix is ignored, so their content was clipped away: items disappeared while scrolling the creative inventory with **Smooth Scrolling** installed, and the block list in **Rechiseled** rendered empty. Clipping now moves with ReAnimated's own animation only, never with someone else's. On 1.21.4 and newer the game does this itself, so those versions were never affected.

### 🖼️ Overlays from other UI mods travel with the screen
Mods like **EMI, JEI and REI** draw their item panel from a hook on the same method ReAnimated wraps. Which hook ran first was up to chance, and ours usually won — so the panel stood still while the screen behind it slid into place. ReAnimated's wrapper now has a higher mixin priority, which makes it the outermost one: everything other mods add to a screen is drawn inside the animation and moves with it.

### 🧍 The inventory player model keeps up
Since 1.21.6 the game draws the player model in your inventory — the one that follows your cursor — outside the interface transform, with raw screen coordinates, while its clipping rectangle *did* follow the animation. The model stood still and got cut in half while the panel travelled towards it. Its rectangle and size now go through the same transform as the panel, so it rides along and scales with presets like *From background*. Affects **1.21.8, 1.21.10 and 1.21.11**; on 1.21.1 – 1.21.5 the model was already drawn through the interface transform.

### Versions

Jars are `reanimated-1.5-MC<version>-neoforge.jar`, built against Mojang official mappings — drop straight into `mods/`.

| Minecraft | NeoForge |
|---|---|
| 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11 | ✅ |
| 1.21.2, 1.21.6, 1.21.7, 1.21.9 | — NeoForge only ever put out beta builds there; Fabric only |
| 26.1, 26.2 | — no NeoForge build of the mod yet; Fabric only |

Your config carries over untouched — new settings get sensible defaults on first launch.

One thing the Fabric builds have and these don't: a settings page **inside Sodium's own screen**. It is built on Sodium's config API through a Fabric entry point; the NeoForge side needs a separate registration and hasn't been done yet. Everything else is identical between the two loaders.

### 💬 What's planned in future updates:
- 1. Add support for most mods
- 2. Performance update
- 3. Bug fixes.

---

## 🇷🇺 Русский

Ветка NeoForge шагает сразу с 1.4.1 на 1.5: в неё входит всё, что Fabric получил в 1.5.0 (для NeoForge та версия так и не выходила), плюс всё, что добавилось после — по отзывам игроков.

### 🖱️ Кнопки вдавливаются
Нажмите кнопку мышью — или Enter на выбранной — и она теперь **проседает и упруго возвращается**, а не только щёлкает звуком. Эффект складывается с увеличением под курсором: наведённая кнопка остаётся увеличенной и проседает уже из этого состояния.

- Настраивается: **вкл/выкл**, **глубина** и **длительность**.
- Срабатывает, только если виджет действительно принял клик — «мёртвый» клик по неактивной кнопке ничего не запускает.
- Работает на обычных кнопках, переключателях и **ползунках**.
- **Поля ввода намеренно исключены** — и из нажатия, и из увеличения под курсором: масштаб чисто визуальный, а клики и курсор игра считает по исходным координатам, поэтому увеличенное поле «плывёт» под мышью.

### ⏸️ У меню паузы свои настройки
Esc открывают чаще любого другого экрана, и длительность, уместная для меню настроек, там ощущается затянутой. Теперь у меню паузы **своя вкладка**: вкл/выкл, скорость, дистанция выезда и траектория, плюс **свой пресет** или вариант «Как общий». По умолчанию оно немного быстрее остального интерфейса (4 тика против 6).

### 📋 Экспорт и импорт настроек
Две кнопки во вкладке **Общее**: **Копировать** кладёт всю конфигурацию в буфер обмена одной строкой, **Вставить** читает её обратно и применяет сразу. Удобно перенести настройку в другую сборку или поделиться анимацией, собранной в Студии. Формат тот же, что у `config/reanimated.json`, так что содержимое файла тоже подойдёт; нечитаемая строка отклоняется, текущие настройки не трогаются.

### 🌘 Затемнение фона появляется плавно
Ваниль включает затемнение за экраном мгновенно — панель выезжала на фоне, который уже «щёлкнул» тёмным. Теперь затемнение **набирается по той же кривой, что и экран**, а на закрытии гаснет обратно. Видно в игре (инвентарь, сундуки, меню паузы); пока анимация не идёт, затемнение рисует сам Minecraft ровно как раньше, и моды, которые его меняют, не затрагиваются. Отключается в секции **Общее**.

### 📜 Списки едут вместе с экраном
Фреймы со строками — сервера, миры, ресурспаки, наборы данных, списки модов — намеренно стояли на месте, пока остальной экран выезжал. Это был обход старой проблемы с обрезкой, и он больше не нужен.

- Список едет вместе с экраном, как и всё остальное.
- **Строки появляются по очереди** сверху вниз, выезжая из рамки списка.
- Отключается в **Общее → Анимация списков**.
- Тонкая настройка (длительность, задержка между строками, дистанция, траектория) — в `config/reanimated.json`, раздел `profileLists`.

### ⚙️ Починки экрана настроек
- **Седьмая вкладка больше не лежит на первой строке опций.** Семь вкладок занимают три ряда, а строки настроек начинались с фиксированной высоты, рассчитанной на два.
- **«Протестированные моды» больше не обрезается** посреди слова — кнопки-ссылки теперь по ширине надписи, а не фиксированные 116 px.
- **Экран подстраивается под размер окна.** Раскладка была зашита намертво: три колонки вкладок, два столбца строк высотой 20 px и шириной 158 px. На экране 427×240 секция **Общее** — одиннадцать строк — не помещалась вовсе: нижний ряд уезжал под кнопку **Готово** и за край экрана, а большинство подписей превращалось в бегущую строку. Теперь число колонок вкладок, сетка строк и ширина столбца подбираются под окно, и секция целиком всегда укладывается между вкладками и **Готово** — вплоть до 320×240. Блок настроек центрируется в отведённой полосе, неполный ряд вкладок центрируется отдельно, а строки стали шире — до 240 px вместо 158, так что прокручивать приходится куда меньше подписей.

### 🧭 Кнопка мода больше не ложится на экран Sodium

На экране настроек графики ReAnimated добавляет кнопку **Настройки ReAnimated** и ищет для неё свободный угол. Поиск учитывал только ванильные виджеты — а строка поиска, список страниц и кнопки Sodium ванильными виджетами не являются, они свои собственные. Экран выглядел пустым, и кнопка вставала в левый верхний угол, прямо поверх поля поиска и панели версии Sodium. **Iris, Embeddium и VulkanMod** устроены так же.

Теперь проверка меряет каждый виджет на экране, какому бы моду он ни принадлежал, а кнопка ужимается под реально свободный зазор — или не рисуется вовсе, если места нет; настройки в этом случае по-прежнему в одном клике через список модов. Проверено с Sodium 0.6.13 и 0.8.12 / 0.8.13 на 1.21.1, 1.21.3, 1.21.5 и 1.21.11. Ванильный экран настроек графики не затронут — кнопка там на прежнем месте.

### 🧩 Обрезка больше не ломает чужие интерфейсы
На **1.21.1 и 1.21.3** мод сдвигал любой прямоугольник обрезки на всё, что оказалось в матрице отрисовки, — включая трансформации других модов. А они задают обрезку в своих координатах и рассчитывают на ванильное поведение этих версий, где матрица не учитывается: их содержимое уезжало под обрезку и пропадало. Отсюда пропадающие предметы при прокрутке креативного инвентаря с модом **Smooth Scrolling** и пустой список блоков в **Rechiseled**. Теперь обрезка едет только на собственную анимацию ReAnimated и никогда — на чужую. С 1.21.4 это делает сама игра, поэтому те версии не были затронуты.

### 🖼️ Оверлеи других UI-модов едут вместе с экраном
**EMI, JEI, REI** и подобные рисуют свою панель предметов из хука на том же методе, который оборачивает ReAnimated. Чей хук отработает первым — было делом случая, и обычно выигрывал наш: панель стояла на месте, пока экран за ней выезжал. Теперь у нашей обёртки выше приоритет миксина, и она стала самой внешней: всё, что моды дорисовывают к экрану, попадает внутрь анимации и едет вместе с ним.

### 🧍 Модель игрока в инвентаре не отстаёт
С 1.21.6 игра рисует модель игрока — ту, что следит за курсором, — мимо трансформации интерфейса, по сырым экранным координатам, а прямоугольник обрезки при этом за анимацией следовал. Модель стояла на месте и обрезалась наполовину, пока панель до неё ехала. Теперь прямоугольник и размер модели проходят через ту же матрицу, что и панель: она едет вместе с ней и масштабируется на пресетах вроде «Из фона». Касается **1.21.8, 1.21.10 и 1.21.11**; на 1.21.1 – 1.21.5 модель и так рисовалась через трансформацию интерфейса.

### Версии

Файлы называются `reanimated-1.5-MC<версия>-neoforge.jar`, собраны под официальные маппинги Mojang — кладите прямо в `mods/`.

| Minecraft | NeoForge |
|---|---|
| 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11 | ✅ |
| 1.21.2, 1.21.6, 1.21.7, 1.21.9 | — NeoForge там выпускал только бета-сборки; только Fabric |
| 26.1, 26.2 | — сборки мода под NeoForge пока нет; только Fabric |

Конфиг переносится как есть — новые настройки при первом запуске получают значения по умолчанию.

Одно отличие от Fabric-сборок: там есть страница настроек **внутри экрана Sodium**. Она сделана на config API Sodium через точку входа Fabric, для NeoForge нужна отдельная регистрация — её пока нет. Всё остальное у обоих загрузчиков одинаково.

### 💬 Что запланировано в следующих обновлениях:
- 1. Добавление поддержки кучи модов
- 2. Улучшение производительности
- 3. Фикс багов
