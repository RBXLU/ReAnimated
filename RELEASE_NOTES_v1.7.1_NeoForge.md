# ReAnimated 1.7.1 for NeoForge

NeoForge was one release behind Fabric. This update brings it up to date: everything from 1.7 plus the 1.7.1 fixes. From now on both loaders use the same version number.

---

## 🇬🇧 English

### Everything from 1.7
- **Screen transitions.** When you go from one menu to another, the old screen slides out and the new one comes in from the other side. Going back plays it in reverse.
- **Recipe book** grows out from under the panel instead of popping in.
- **Tooltips** grow in from the cursor, including tooltips from other mods.
- **Toasts** (advancements, recipes and so on) slide in along your chosen curve.
- **Per-screen settings.** Any screen can have its own preset, its own speed, or no animation at all.
- **Four more presets** (from top, from left, from right, pop) and **three more curves** (In-Out Cubic, Out Elastic, Out Bounce).
- **An API for other mods** to add their own animations or tell ReAnimated to leave a screen alone.

### Closing no longer hangs
Closing a menu could leave it sitting there for about half a second after the animation before it vanished. Now the panel starts moving right away and the screen closes the moment the animation ends. Screens the mod doesn't animate (chat, for example) close instantly.

### Lighter
The mod does less work per frame with a screen open, so the FPS hit with menus and the inventory open should be smaller.

### Cleaner settings
The first tab, **General**, now has what you actually change: preset, speed, close animation and background dimming. Everything finer lives in its own tab.

### Works with Blur+
With Blur+ the blur now fades out together with the closing menu instead of after it, and the dark flash at the end is gone.

### Versions
- NeoForge: 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11
- Client-side only. Your config carries over as is.
- The settings page inside Sodium is still Fabric-only. On NeoForge, open the settings from the mods list or the button on the video settings screen.

---

## 🇷🇺 Русский

NeoForge отставал от Fabric на один релиз. Это обновление его догоняет: всё из 1.7 плюс исправления 1.7.1. Теперь у обоих загрузчиков один номер версии.

### Всё из 1.7
- **Переходы между экранами.** Когда переходишь из одного меню в другое, старый экран уезжает, а новый приезжает с другой стороны. При возврате всё играет в обратную сторону.
- **Книга рецептов** вырастает из-под панели, а не появляется рывком.
- **Подсказки** вырастают из-под курсора, в том числе подсказки из других модов.
- **Уведомления** (достижения, рецепты и прочее) выезжают по выбранной кривой.
- **Настройки отдельных экранов.** Любому экрану можно дать свой пресет, свою скорость или отключить анимацию совсем.
- **Ещё четыре пресета** (сверху, слева, справа, pop) и **три кривые** (In-Out Cubic, Out Elastic, Out Bounce).
- **API для других модов**: можно добавить свою анимацию или попросить ReAnimated не трогать экран.

### Закрытие больше не зависает
После закрытия меню оно могло ещё примерно полсекунды висеть после анимации, а потом резко пропадать. Теперь панель начинает уезжать сразу, и экран закрывается ровно тогда, когда заканчивается анимация. Экраны, которые мод не анимирует (например, чат), закрываются сразу.

### Легче
Пока открыт экран, мод делает меньше работы за кадр, так что с открытыми меню и инвентарём FPS должен проседать меньше.

### Настройки стали проще
На первой вкладке, **Общее**, теперь то, что реально меняют: пресет, скорость, анимация закрытия и затемнение фона. Всё подробное лежит на своих вкладках.

### Совместимость с Blur+
С Blur+ размытие теперь гаснет вместе с закрывающимся меню, а не после него, и тёмной вспышки в конце больше нет.

### Версии
- NeoForge: 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11
- Мод только клиентский. Конфиг переносится как есть.
- Страница настроек внутри Sodium по-прежнему только на Fabric. На NeoForge настройки открываются из списка модов или кнопкой на экране настроек графики.
