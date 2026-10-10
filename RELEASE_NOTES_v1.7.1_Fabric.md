# ReAnimated 1.7.1

Mostly a fix-up release. Closing screens feels right again, the mod is lighter, the settings are less of a wall of sliders, and there's a build for 26.3.

---

## 🇬🇧 English

### Closing no longer hangs
Someone reported that after closing a menu it would just sit there for about half a second after the animation and then vanish all at once. Fair complaint, and there were actually two things behind it:

- The close animation barely moved at the start and did almost all of its movement in the last few frames, so it looked frozen.
- The close waited for the slowest part of the screen, and with the button cascade on, that could add a lot of dead time on screens with many buttons.

Both are fixed. The panel starts moving right away, everything finishes together, and the screen closes the moment the animation ends. Screens the mod doesn't animate at all (chat, for example) now close instantly instead of lingering for a moment.

### Lighter
With a screen open the mod was doing the same work several times per frame. Now it works it out once per frame and reuses it. You should see less of an FPS hit with menus and the inventory open.

### Cleaner settings
The settings list had grown to around sixty sliders. Now the things you actually change come first: preset, speed, close animation, background dimming, and a simple on/off list of what to animate. All the fine-tuning is still there, just folded away under **Show all settings** at the bottom. On 26.x the settings are split into tabs with **General** first.

### Works with Smooth Scrolling
With Smooth Scrolling installed, the slot highlight in the creative inventory was drawn in the wrong spot while scrolling, so it looked like items were in the wrong slots. The highlight now scrolls along with the items.

### Works with Blur+
With Blur+ the blurred background stayed at full strength while the menu closed and only started fading afterwards, with a short dark flash at the end. Now the blur fades out together with the menu and there's no flash.

### Minecraft 26.3
New build for 26.3. It's a fresh version and its renderer changed quite a bit, so if something looks off (especially with Sodium or Iris), please let me know.

### Versions
- Fabric / Quilt with Fabric API: 1.21.1 – 1.21.11, 26.1.2, 26.2, 26.3
- Client-side only. Your config carries over as is.

---

## 🇷🇺 Русский

### Закрытие больше не зависает
Мне написали, что после закрытия меню оно ещё примерно полсекунды висит после анимации, а потом резко пропадает. Так и было, и причин оказалось две:

- Анимация закрытия почти не двигалась в начале и делала почти всё движение в последние кадры, поэтому казалось, что всё застыло.
- Закрытие ждало самую медленную часть экрана, и с каскадом кнопок на экранах с большим количеством кнопок набегала заметная пауза.

Оба исправлены. Панель начинает уезжать сразу, всё заканчивается одновременно, и экран закрывается ровно тогда, когда заканчивается анимация. Экраны, которые мод не анимирует (например, чат), теперь закрываются сразу, а не висят лишний момент.

### Легче
Пока открыт экран, мод делал одну и ту же работу по несколько раз за кадр. Теперь он считает всё один раз за кадр. С открытыми меню и инвентарём FPS должен проседать меньше.

### Настройки стали проще
Настроек набралось около шестидесяти ползунков. Теперь сверху то, что реально меняют: пресет, скорость, анимация закрытия, затемнение фона и простой список того, что анимировать. Всё остальное никуда не делось, оно спрятано за переключателем **Показывать все настройки** внизу. На 26.x настройки разбиты на вкладки, и первая из них **Общее**.

### Совместимость со Smooth Scrolling
Со Smooth Scrolling при прокрутке креативного инвентаря подсветка слота рисовалась не там, и казалось, что предметы лежат не в тех слотах. Теперь подсветка прокручивается вместе с предметами.

### Совместимость с Blur+
С Blur+ размытый фон держался в полную силу, пока меню закрывалось, начинал гаснуть только после этого, и в конце была короткая тёмная вспышка. Теперь размытие гаснет вместе с меню, и вспышки нет.

### Minecraft 26.3
Новая сборка под 26.3. Версия свежая, и рендер в ней сильно поменялся, так что если что-то выглядит не так (особенно с Sodium или Iris), напишите мне.

### Версии
- Fabric / Quilt с Fabric API: 1.21.1 – 1.21.11, 26.1.2, 26.2, 26.3
- Мод только клиентский. Конфиг переносится как есть.
