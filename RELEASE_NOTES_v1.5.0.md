# ReAnimated 1.5.0 — buttons press in, the pause menu gets its own timing, and settings travel between players

Four additions: a press animation for every button, a separate set of knobs for the Esc menu, one-click sharing of your whole config, and background dimming that fades in with the panel instead of snapping on.

---

## 🇬🇧 English

### 🖱️ Buttons press in
Click a button — or hit Enter on the one you've selected — and it now **dips inward and springs back**, instead of only playing a sound. It rides on top of the hover scaling, so a button under the cursor stays enlarged and presses down from there.

- Configurable: **on/off**, **depth** (how far it sinks) and **duration**.
- Fires only when the widget actually takes the click — a dead click on a disabled button leaves it alone.
- Works on every widget you can press: ordinary buttons, cycling options and **sliders** alike.
- **Text fields are left out on purpose** — of the press *and* of the hover scaling. The scaling is purely visual, while the game still counts clicks, the caret and text selection at the unscaled position, so a scaled-up field drifts under the mouse and hitting the right character becomes guesswork.

### ⏸️ The pause menu has its own settings
Esc is the screen you open most often, and the timing that suits a settings menu feels sluggish there. The pause menu now has **its own section**:

- **on/off**, **speed (ticks)**, **slide distance** and **trajectory** — independent of the general menu settings;
- **its own preset**, or **Same as general** if you'd rather it follow the main one.

Out of the box it's set a touch faster than the rest of the UI (4 ticks against 6).

### 📋 Export and import your settings
Two new buttons in the settings screen:

- **Export** — puts your entire configuration into the clipboard as a single line;
- **Import** — reads it back and applies it immediately.

Handy for carrying your setup to another instance, or for sharing an animation you tuned in the Studio with someone else. The format is the same as `config/reanimated.json`, so pasting the file's contents works too. Anything unreadable is rejected and your current settings stay untouched.

### 🌘 Background dimming fades in
Vanilla switches the dimming behind a screen on instantly, so the panel used to slide in over a background that had already snapped dark. Now that dimming **fades in along the same curve as the screen** and fades back out on close.

- Visible in-game (inventory, chests, the pause menu) — on the main menu there's a panorama behind the screen, not a dimmed world.
- While no animation is playing, Minecraft draws the dimming itself exactly as before — mods that change it are unaffected.
- Can be switched off in **General**.

### Notes
- Your existing config carries over; the new options appear with their defaults.
- Everything from 1.4 is still here: logo letter cascade, tab animations, the Animation Studio and Profile Editor, presets, close animations, hover scaling and the following slot highlight.
- Buttons not growing on hover? Check **Cursor → Hover — scale amount**: at 0 there is nothing to scale, and the setting carries over from your old config.
- Available for **Minecraft 1.21.1 – 1.21.11, 26.1 and 26.2**, on both **Fabric/Quilt** and **NeoForge** — the NeoForge builds are level with the Fabric line again.

### 💬 What planned in future updates:
- 1. Add support for most mods
- 2. Performance update
- 3. Bug fixes.

---

## 🇷🇺 Русский

### 🖱️ Кнопки вдавливаются
Нажмите кнопку мышью — или Enter на выбранной — и она теперь **проседает и упруго возвращается**, а не только щёлкает звуком. Эффект складывается с увеличением под курсором: наведённая кнопка остаётся увеличенной и проседает уже из этого состояния.

- Настраивается: **вкл/выкл**, **глубина** и **длительность**.
- Срабатывает, только если виджет действительно принял клик — «мёртвый» клик по неактивной кнопке ничего не запускает.
- Работает на всём, что можно нажать: обычные кнопки, переключатели и **ползунки**.
- **Поля ввода намеренно исключены** — и из нажатия, и из увеличения под курсором. Масштаб чисто визуальный, а клики, курсор и выделение текста игра по-прежнему считает по исходным координатам: увеличенное поле «плывёт» под мышью, и попасть в нужный символ становится нечем.

### ⏸️ У меню паузы свои настройки
Esc открывают чаще любого другого экрана, и длительность, уместная для меню настроек, там ощущается затянутой. Теперь у меню паузы **своя секция**:

- **вкл/выкл**, **скорость (тики)**, **дистанция выезда** и **траектория** — независимо от общих настроек меню;
- **свой пресет** или вариант **«Как общий»**, если хочется, чтобы он следовал за основным.

По умолчанию оно немного быстрее остального интерфейса (4 тика против 6).

### 📋 Экспорт и импорт настроек
Две новые кнопки в экране настроек:

- **Копировать** — копирует всю конфигурацию в буфер обмена одной строкой в json структуре;
- **Вставить** — читает её обратно и применяет сразу.

Удобно перенести настройку в другую сборку или поделиться анимацией, собранной в Студии. Формат тот же, что у `config/reanimated.json`, так что содержимое файла тоже подойдёт. Нечитаемая строка отклоняется, текущие настройки при этом не трогаются.

### 🌘 Затемнение фона появляется плавно
Ваниль включает затемнение за экраном мгновенно — панель выезжала на фоне, который уже «щёлкнул» тёмным. Теперь затемнение **набирается по той же кривой, что и экран**, а на закрытии гаснет обратно.

- Видно в игре (инвентарь, сундуки, меню паузы) — на главном меню за экраном панорама, а не затемнённый мир.
- Пока анимация не идёт, затемнение рисует сам Minecraft ровно как раньше — моды, которые его меняют, не затрагиваются.
- Отключается в секции **Общее**.

### 💬 Что запланировано в следующих обновлениях:
- 1. Добавление поддержки кучи модов
- 2. Улучшение производительности
- 3. Фикс багов
