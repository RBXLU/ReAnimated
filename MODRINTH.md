<p align="center">
<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/hero.png" alt="ReAnimated — smooth animations for the entire Minecraft UI" width="100%">
</p>

<p align="center">
<a href="https://fabricmc.net/"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg" alt="Fabric"></a>
<a href="https://quiltmc.org/"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/quilt_vector.svg" alt="Quilt"></a>
<a href="https://neoforged.net/"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/neoforge_vector.svg" alt="NeoForge"></a>
<a href="https://modrinth.com/mod/fabric-api"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3.3.1/assets/cozy/requires/fabric-api_vector.svg" alt="Requires Fabric API"></a>
</p>

<p align="center">
<a href="https://www.curseforge.com/minecraft/mc-mods/reanimated"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg" alt="Available on CurseForge"></a>
</p>

> **Client-side only.** Not required on servers, and players without it won't notice anything different. Nothing about gameplay changes — this is purely how the interface moves.


<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/divider.png" alt="" width="100%">

<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/header_about.png" alt="About" width="100%">

Vanilla menus pop in and out with no transition. **ReAnimated** gives the whole interface a sense of motion: menus, containers, the inventory, tooltips and notifications ease into place instead of snapping, and screens hand over to each other instead of blinking.

Everything is optional, everything is tunable, and if you want something the presets don't cover, you can build your own animation in the built-in **Animation Studio** — or add one from your own mod through the **extension API** below.

<p align="center">
<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/motion.png" alt="A screen slides, fades and settles into place" width="100%">
</p>


<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/divider.png" alt="" width="100%">

<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/header_features.png" alt="Features" width="100%">

**Screens**
- **Menus open and close smoothly** — titles *and* buttons arrive together, and the same motion plays in reverse on close. Every screen, automatically.
- **Screens hand over to each other** — open Settings from the main menu and the old screen slides out while the new one arrives from the other side; going back plays it in reverse. Slide, zoom, or both.
- **Containers slide up** — chests, furnaces, barrels and the inventory ease in from the bottom.
- **Background dimming fades in** along with the panel instead of snapping on.

**Details that add up**
- **Tooltips grow in** out of the cursor — including tooltips added by other mods.
- **Toasts slide on your curve** — advancements, recipes and system messages keep vanilla's timing but travel along the trajectory you pick.
- **Buttons scale on hover** and **dip in when pressed** — mouse or keyboard, sliders and cycling options too.
- **The slot highlight follows your cursor** across the inventory instead of jumping between slots.
- **Lists animate** — the mod list, servers, worlds and resource packs travel with the screen, and their rows arrive one after another.
- **Animated logo, two styles** — the *Minecraft* logo grows in with a soft bounce, or assembles letter by letter.
- **Tabs slide out** from behind the panel in Advancements and the Creative inventory.


<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/divider.png" alt="" width="100%">

<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/header_presets.png" alt="Presets" width="100%">

Every screen enters through a preset you choose:

| Preset | What it does |
|---|---|
| **Default** | Slides up from the bottom |
| **Slide down / left / right** | The same entrance from the other three sides |
| **From background** | Grows out of the centre with a little bounce |
| **From foreground** | Flies in and settles down to normal size |
| **Pop** | Jumps out of a point with a strong overshoot |
| **None** | Turns the movement off but keeps everything else |

Each preset runs along a **trajectory**: *Linear, Out Cubic, Out Back, Out Expo, In-Out Cubic, Out Elastic, Out Bounce*.

Want something of your own? The **Animation Studio** and the **Profile Editor** live inside the settings — live sliders, ready-made starting points, an easing-curve graph and a live preview.


<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/divider.png" alt="" width="100%">

<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/header_settings.png" alt="Settings" width="100%">

Open **Options → Video Settings → `ReAnimated settings`**, or through **Mod Menu**.

- **Sections:** *General · Menu screens · Pause menu · Screen transitions · Containers · Cursor · Tooltips & toasts · Logo · Tabs*
- **Per-screen settings** — give one screen its own preset or speed, or switch its animation off entirely
- **The pause menu has its own settings** — it is opened the most, so it gets its own speed and preset
- **Export / Import** — your whole configuration as one line in the clipboard, ready to share
- Everything is saved to `config/reanimated.json` and applies instantly

> **Running Sodium 0.8+?** ReAnimated gets its own entry inside Sodium's settings screen — real pages in the list on the left, drawn with Sodium's own controls and covered by its search. On older Sodium, VulkanMod or Iris a button appears in their screen instead.


<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/divider.png" alt="" width="100%">

<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/header_api.png" alt="Extension API" width="100%">

Other mods can plug into ReAnimated: **add their own opening animation**, **tell it to leave a screen alone** (or to animate it anyway), **read where the interface is right now** so their overlay travels with the screen, and **put their own button** into the settings.

```java
ReAnimatedApi.registerPreset(new CustomPreset() {
    public String id() { return "mymod:swoop"; }
    public String displayName() { return "Swoop"; }
    public PresetMotion motionAt(float progress) {
        float e = 1f - (1f - progress) * (1f - progress);
        return new PresetMotion((1f - e) * 40f, 0f, 0.9f + 0.1f * e);
    }
});
```

The API pulls in neither game nor loader classes, so the same calls work on Fabric, Quilt and NeoForge across every supported version, and extensions are isolated — an exception from someone else's code is logged, not fatal. Full guide: [`API.md`](https://github.com/RBXLU/devReAnimated/blob/main/API.md).


<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/divider.png" alt="" width="100%">

<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/header_install.png" alt="Install" width="100%">

1. Install **Fabric** (or Quilt / NeoForge) for your Minecraft version
2. Drop **ReAnimated** and **[Fabric API](https://modrinth.com/mod/fabric-api)** into `mods/`
3. Launch the game — that's it, the defaults are already tuned


<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/divider.png" alt="" width="100%">

<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/header_versions.png" alt="Versions" width="100%">

There's a **separate download for each Minecraft version** — GUI rendering changed a fair amount across 1.21.x and again in 26.x, so every version has its own build made against it. Grab the one that matches your game exactly.

| | |
|---|---|
| **Minecraft** | 1.21.1 – 1.21.11, 26.1.2, 26.2 |
| **Loaders** | Fabric · Quilt · NeoForge |
| **File name** | `reanimated-<Minecraft version>+<mod version>.jar`, e.g. `reanimated-1.21.11+1.7.jar` |

NeoForge builds carry the same features; their version numbers run one release behind, because 1.5.0 never shipped for NeoForge — Fabric **1.7** matches NeoForge **1.6**.


<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/divider.png" alt="" width="100%">

<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/header_credits.png" alt="Credits" width="100%">

Made with ❤️ by **[@pycodder](https://modrinth.com/user/pycodder)**

The **letter-by-letter logo cascade** (idea + letter textures) and the idea of the **tab cascade** come from [**EaseGUI**](https://github.com/Weyne1/EaseGUI) by Weyne1, under **LGPL-3.0**. Full attribution lives in `CREDITS.md` and the `licenses/` folder.


<img src="https://raw.githubusercontent.com/RBXLU/devReAnimated/main/docs/images/divider.png" alt="" width="100%">


<details>
<summary><b>🇷🇺 Описание на русском</b></summary>

<br>

> **Мод клиентский.** На сервере не нужен, игроки без него ничего не заметят. Геймплей не меняется — меняется только то, как двигается интерфейс.

### О моде

Ванильные меню появляются и исчезают рывком. **ReAnimated** возвращает интерфейсу движение: меню, контейнеры, инвентарь, подсказки и уведомления плавно встают на место, а экраны передают друг другу эстафету вместо мгновенной подмены.

Всё можно выключить и всё можно настроить, а если встроенных пресетов мало — своя анимация собирается в **Студии анимаций** прямо в игре или добавляется из своего мода через **API расширений** ниже.

### Возможности

**Экраны**
- **Меню открываются и закрываются плавно** — заголовки и кнопки выезжают вместе, на закрытии то же движение играет в обратную сторону. На всех экранах автоматически.
- **Экраны передают друг другу эстафету** — открываете настройки из главного меню, и старый экран уезжает в сторону, а новый приходит с противоположной; на возврате — зеркально. Сдвиг, масштаб или всё сразу.
- **Контейнеры выезжают** — сундуки, печки, бочки и инвентарь плавно появляются снизу.
- **Затемнение фона** набирается вместе с панелью, а не включается рывком.

**Мелочи, из которых складывается ощущение**
- **Подсказки вырастают** из-под курсора — в том числе подсказки из других модов.
- **Уведомления выезжают по вашей кривой** — достижения, рецепты и системные сообщения сохраняют ванильные тайминги, но двигаются по выбранной траектории.
- **Кнопки растут под курсором** и **вдавливаются при нажатии** — и мышью, и с клавиатуры, включая ползунки.
- **Подсветка слота следует за курсором**, а не перепрыгивает между слотами.
- **Списки анимируются** — моды, сервера, миры и ресурспаки едут вместе с экраном, строки появляются по очереди.
- **Анимация логотипа, два стиля** — вырастает целиком или собирается по буквам.
- **Вкладки выезжают** из-за панели в достижениях и креативном инвентаре.

### Пресеты

| Пресет | Что делает |
|---|---|
| **Default** | Выезжает снизу |
| **Выезд сверху / слева / справа** | То же появление с трёх других сторон |
| **From background** | Вырастает из центра с лёгким отскоком |
| **From foreground** | Влетает и уменьшается до нормального размера |
| **Pop** | Выскакивает из точки с сильным отскоком |
| **None** | Движение выключено, остальное работает |

Траектории: *Linear, Out Cubic, Out Back, Out Expo, In-Out Cubic, Out Elastic, Out Bounce*.

### Настройки

Настройки → Настройки графики → «Настройки ReAnimated», или через **Mod Menu**.

- **Секции:** *Общее · Экраны меню · Меню паузы · Переходы · Контейнеры · Курсор · Подсказки и уведомления · Логотип · Вкладки*
- **Настройки отдельных экранов** — свой пресет, своя скорость или выключенная анимация для конкретного меню
- **У меню паузы свои настройки** — его открывают чаще всего, поэтому у него отдельные скорость и пресет
- **Копировать / Вставить** — вся конфигурация одной строкой через буфер обмена
- Всё сохраняется в `config/reanimated.json` и применяется сразу

> **Стоит Sodium 0.8+?** ReAnimated появится отдельным пунктом внутри экрана Sodium — своими страницами в списке слева, его же элементами управления и с его поиском. На старом Sodium, VulkanMod или Iris вместо этого добавляется кнопка в их экран.

### API расширений

Другие моды могут добавить свою анимацию появления, подсказать не трогать конкретный экран (или, наоборот, анимировать его), узнать текущий сдвиг интерфейса и повесить свою кнопку в настройки. Пакет не тянет за собой классов игры и загрузчика, поэтому один и тот же код работает на Fabric, Quilt и NeoForge. Руководство — в [`API.md`](https://github.com/RBXLU/devReAnimated/blob/main/API.md).

### Версии

Для каждой версии Minecraft — свой файл: отрисовка интерфейса заметно менялась внутри 1.21.x и ещё раз в 26.x.

| | |
|---|---|
| **Minecraft** | 1.21.1 – 1.21.11, 26.1.2, 26.2 |
| **Загрузчики** | Fabric · Quilt · NeoForge |
| **Имя файла** | `reanimated-<версия Minecraft>+<версия мода>.jar`, например `reanimated-1.21.11+1.7.jar` |

Сборки NeoForge отстают на один номер: версия 1.5.0 для них не выходила, поэтому Fabric **1.7** соответствует NeoForge **1.6**.

### Благодарности

**Побуквенный каскад логотипа** (идея и текстуры букв) и идея **каскада вкладок** — из мода [**EaseGUI**](https://github.com/Weyne1/EaseGUI) (Weyne1), под лицензией **LGPL-3.0**. Полная атрибуция — в `CREDITS.md` и папке `licenses/`.

</details>

<!--
  Сюда просятся живые кадры из игры — их можно снять только в игре:
  1. GIF: главное меню -> настройки -> назад (виден переход между экранами)
  2. GIF: открытие сундука/инвентаря (выезд панели + подсветка слота за курсором)
  3. GIF: подсказка предмета и всплывающее достижение
  4. Скриншот экрана настроек мода и страницы внутри Sodium
  Класть в docs/images/ и вставлять так же, как баннеры выше.
-->
