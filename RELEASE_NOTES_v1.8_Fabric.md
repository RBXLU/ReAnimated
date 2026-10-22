# ReAnimated 1.8

Every screen can now be set up on its own, there is a PvP mode, and the mod animates the hotbar, tabs and the small controls inside menus.

---

## 🇬🇧 English

### Every screen your way
Each screen can now have its own settings, not just a preset and speed:
- distance and easing of the opening animation;
- closing animation, background dim and transitions, on or off for this screen only;
- its own button cascade, made in the profile editor with a live preview.

Everything starts at "As general", so nothing changes until you touch it.

Press **F7** on any screen to open its settings right there (you can rebind it in Controls). You can also set one rule for all screens of a mod; a screen's own settings win over it.

### PvP mode
A new switch at the top of the settings. With it on, the inventory, chests and other containers, and the death screen open and close instantly. The slot highlight and the hotbar frame snap into place instead of sliding, and tooltips and the recipe book appear right away. Other menus stay animated.

### Hotbar
- When you scroll or press a number key, the selection frame slides over to the new slot instead of jumping there.
- The item name above the hotbar rises up a little and fades in when you switch items.

Both can be turned off separately in the settings, and the frame speed is adjustable.

### Tabs
On screens with vanilla tabs (world creation, statistics and so on), the new tab slides in from the side you switched towards. Going back slides it in from the other side. Duration, distance and curve are adjustable.

### Sliders, checkboxes, text fields
- When you click somewhere on a slider, the handle slides to that spot instead of jumping. While you drag it, it follows the cursor exactly, with no lag.
- A checkbox gives a little bounce when you tick or untick it.
- A text field's frame lights up smoothly when you click into it, and fades back out when you click away.

### Fixed
- In the normal (non-dev) game on 1.21.x, the mod sometimes didn't recognize the pause menu, the chat, and loading screens, and handled them like regular menus. They're recognized properly again.
- The title screen logo animation plays when the logo actually shows up. At game start it used to finish under the loading screen, so you never saw it.
- On 1.21.8 and newer, a menu you go back to (for example with "Done" in Options) slides in again instead of popping into place, and the next menu you open no longer comes in from the side for no reason.
- The game log no longer shows the "Invalid path in mod resource-pack reanimated" error at startup.
- The smooth slot highlight no longer draws a second white square on top of the game's own highlight. You see exactly the highlight of the game or your resource pack (OreUI-style frames included), and that highlight is what glides between slots.

### Versions
- Fabric / Quilt with Fabric API: 1.21.1 – 1.21.11, 26.1.2, 26.2, 26.3
- Client-side only. Your config carries over; the new animations are on by default.

---

## 🇷🇺 Русский

### Каждый экран по-своему
У каждого экрана теперь свои настройки, а не только пресет и скорость:
- дистанция и траектория появления;
- анимация закрытия, затемнение фона и переходы: включить или выключить только для этого экрана;
- свой каскад кнопок, который настраивается в редакторе профиля с живым превью.

Всё по умолчанию стоит «Как в общих», так что ничего не меняется, пока вы сами не поменяете.

Нажмите **F7** на любом экране, и откроются его настройки (клавишу можно сменить в «Управлении»). Ещё можно задать одно правило сразу на все экраны мода; настройки отдельного экрана сильнее.

### PvP-режим
Новый переключатель в самом верху настроек. Когда он включён, инвентарь, сундуки и другие контейнеры, а также экран смерти открываются и закрываются мгновенно. Подсветка слота и рамка хотбара сразу встают на место, а не доезжают, подсказки и книга рецептов появляются без задержки. Остальные меню анимируются как обычно.

### Хотбар
- Когда вы крутите колёсико или жмёте цифру, рамка выбранного слота переезжает к новому слоту, а не перескакивает.
- Название предмета над хотбаром при смене немного приподнимается и проявляется.

И то и другое выключается в настройках отдельно, скорость рамки настраивается.

### Вкладки
На экранах с ванильными вкладками (создание мира, статистика и другие) новая вкладка въезжает с той стороны, куда вы переключились. При возврате назад она въезжает с другой стороны. Длительность, дистанция и траектория настраиваются.

### Ползунки, чекбоксы, поля ввода
- Если кликнуть в любое место ползунка, ручка доезжает туда, а не перескакивает. Когда тащите её мышью, она идёт ровно за курсором, без отставания.
- Чекбокс слегка отскакивает, когда его ставите или снимаете.
- Рамка поля ввода плавно загорается, когда в него кликаете, и плавно гаснет, когда уходите из него.

### Исправлено
- В обычной (не dev) игре на 1.21.x мод иногда не узнавал меню паузы, чат и экраны загрузки и обрабатывал их как обычные меню. Теперь он снова их узнаёт.
- Анимация логотипа на главном экране играет, когда логотип действительно появляется. При запуске игры она раньше доигрывала под экраном загрузки, и её не было видно.
- На 1.21.8 и новее меню, на которое вы возвращаетесь (например, кнопкой «Готово» в настройках), снова въезжает, а не встаёт на место рывком. Следующее открытое меню больше не въезжает сбоку без причины.
- В логе игры при запуске больше нет ошибки «Invalid path in mod resource-pack reanimated».
- Плавная подсветка слота больше не рисует второй белый квадрат поверх подсветки игры. Видна ровно подсветка игры или ресурспака (в том числе рамки в стиле OreUI), и между слотами плавно переезжает именно она.

### Версии
- Fabric / Quilt с Fabric API: 1.21.1 – 1.21.11, 26.1.2, 26.2, 26.3
- Мод только клиентский. Конфиг переносится, новые анимации включены по умолчанию.
