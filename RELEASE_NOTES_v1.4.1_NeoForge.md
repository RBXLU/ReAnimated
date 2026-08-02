# ReAnimated 1.4.1 — NeoForge

---

## 🇬🇧 English

### What's new since NeoForge 1.2.1

**Animation Studio** — a screen where you build your own open/close animation from scratch:
live sliders instead of text fields, ready-made presets (slide up/down, fly in from the side,
zoom in/out, fade, pop), a visual graph of the easing curve with a running marker, and **two**
previews one above the other — how a screen opens and how it closes (closing is an exact reverse
of opening).

**Profile Editor** — the same animation model with numeric entry and a three-element live preview.

**Layered animations.** The UI preset moves the whole screen; the profile/Studio cascades the
buttons on top of it. They add up instead of replacing each other, and closing plays both layers
back in reverse, each at its own pace.

**Logo letter cascade.** The "Minecraft" logo can now fly in letter by letter (nine letters,
`M I N E C R A F T`), with its own duration, offset, delay, direction and easing. The classic
"grow" style is still there and stays the default. *Letter idea and textures come from EaseGUI
(Weyne1), LGPLv3 — see CREDITS.md.*

**Tab cascade.** Advancement tabs and creative-inventory tabs slide out from behind the panel one
by one. Top-row tabs rise from under the top edge, bottom-row tabs drop from under the bottom edge;
until a tab has cleared the panel it stays clipped, so nothing pops in mid-air.

**Sectioned settings.** The settings screen is split into six tabs — General, Menu, Containers,
Cursor, Logo, Tabs — so every option fits on screen without scrolling, even at 320×240.
The Profile Editor and Animation Studio open from the General tab.

**Settings are now always reachable — including with Sodium.** Two independent ways in:

* **Mods list → ReAnimated → Config.** Works no matter what else is installed; this is the NeoForge
  equivalent of the ModMenu button on Fabric.
* **A button on the video settings screen.** It now also appears on screens *replaced* by other mods
  (Sodium, VulkanMod, Embeddium, Rubidium, Iris) — previously only the vanilla screen was
  recognised, so with Sodium installed the button simply never showed up.

  Its position is no longer fixed. The mod checks the top-left corner first — free on the vanilla
  screen, so nothing moves there — and falls back through the other corners until it finds a spot
  where it doesn't overlap anything. On Sodium the old fixed corner landed straight on top of the
  tab bar. If every candidate is taken, no button is drawn at all: the mods list still gets you in,
  and that beats a label smeared over someone else's text.

**Text no longer flashes at low opacity.** When a button faded below ~1.5% opacity, vanilla treated
its text colour as "almost transparent" and drew it fully opaque — a bright flash on an invisible
button. Such a widget is now simply not drawn.

**Clipping fixes (1.4.1).** On 1.21.1 and 1.21.3 the mod still shifts the clip rectangle along with
the animation, because Minecraft does not do it there. From **1.21.4** Minecraft does it itself, so
the workaround is gone — with it in place the rectangle was transformed **twice**. Among the
NeoForge builds that actually broke GUIs on **1.21.8, 1.21.10 and 1.21.11**: mods that draw inside
their own shifted coordinate system (SuperMartijn642's Core Lib, and Rechiseled which is built on
it) came up with an empty list. On 1.21.4 and 1.21.5 Core Lib clips through a different path and
was never hit — there the change is a correctness fix with no visible symptom.

### Versions

Jars are `reanimated-1.4.1-MC<version>-neoforge.jar`, built against Mojang official mappings —
drop straight into `mods/`.

| Minecraft | NeoForge |
|---|---|
| 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11 | ✅ |
| 1.21.2, 1.21.6, 1.21.7, 1.21.9 | — NeoForge only ever put out beta builds there; Fabric only |
| 26.1, 26.2 | — no NeoForge build of the mod yet; Fabric only |

Your config carries over untouched — new settings get sensible defaults on first launch.

---

## 🇷🇺 Русский

### Что нового с NeoForge 1.2.1

**Студия анимаций** — экран, где анимация открытия/закрытия собирается с нуля: живые ползунки
вместо полей ввода, готовые заготовки (выезд вверх/вниз, влёт сбоку, приближение/отдаление,
проявление, «поп»), наглядный график кривой сглаживания с бегущим маркером и **два** превью одно
под другим — как экран открывается и как закрывается (закрытие — точный реверс открытия).

**Редактор профиля** — та же модель анимации, но числами, с превью из трёх элементов.

**Слоёные анимации.** Пресет двигает экран целиком, профиль/Студия каскадит кнопки поверх него.
Они складываются, а не исключают друг друга; при закрытии оба слоя отматываются назад, каждый в
своём темпе.

**Побуквенный каскад логотипа.** Логотип «Minecraft» умеет влетать по буквам (девять букв,
`M I N E C R A F T`) — со своей длительностью, сдвигом, задержкой, направлением и сглаживанием.
Классический стиль «вырастание» никуда не делся и остаётся по умолчанию. *Идея и текстуры букв —
из мода EaseGUI (Weyne1), LGPLv3, см. CREDITS.md.*

**Каскад вкладок.** Вкладки достижений и креативного инвентаря выезжают из-за плашки по очереди.
Верхний ряд всплывает из-под верхнего края, нижний — опускается из-под нижнего; пока вкладка не
вышла из-за панели, она обрезается, так что ничего не возникает в воздухе.

**Настройки по секциям.** Экран настроек разбит на шесть вкладок — Общее, Меню, Контейнеры,
Курсор, Логотип, Вкладки — и всё помещается без прокрутки даже на 320×240. Редактор профиля и
Студия открываются из вкладки «Общее».

**В настройки теперь можно попасть всегда — в том числе с Sodium.** Два независимых пути:

* **Список модов → ReAnimated → «Настроить».** Работает при любом наборе модов; это аналог кнопки
  ModMenu на Fabric.
* **Кнопка на экране настроек графики.** Теперь она появляется и на экранах, *подменённых* другими
  модами (Sodium, VulkanMod, Embeddium, Rubidium, Iris) — раньше мод узнавал только ванильный
  экран, поэтому с Sodium кнопки просто не было.

  Место больше не зашито в координатах. Сначала проверяется левый верхний угол — на ванильном
  экране он свободен, так что там ничего не сдвинулось, — а дальше перебираются остальные углы,
  пока не найдётся место без пересечений с чужими виджетами. У Sodium прежний фиксированный угол
  ложился ровно на панель вкладок. Если свободного места нет вообще, кнопка не рисуется: попасть в
  настройки всё равно можно из списка модов, и это лучше, чем надпись поверх чужого текста.

**Текст больше не вспыхивает на низкой прозрачности.** Когда кнопка гасла ниже ~1.5%, ваниль
считала цвет её текста «почти прозрачным» и рисовала надпись полностью непрозрачной — яркая
вспышка на невидимой кнопке. Теперь такой виджет просто не рисуется.

**Починена обрезка (1.4.1).** На 1.21.1 и 1.21.3 мод по-прежнему двигает прямоугольник обрезки
вместе с анимацией, потому что Minecraft там этого не делает. С **1.21.4** Minecraft справляется
сам, и обходной приём убран: вместе с ванильным он преобразовывал прямоугольник **дважды**. Из
NeoForge-сборок GUI ломались на **1.21.8, 1.21.10 и 1.21.11**: у модов, которые рисуют в
собственной сдвинутой системе координат (Core Lib от SuperMartijn642 и построенный на нём
Rechiseled), список выходил пустым. На 1.21.4 и 1.21.5 Core Lib обрезает другим путём и под
раздачу не попадал — там это правка «по-честному», без видимых симптомов.

### Версии

Файлы называются `reanimated-1.4.1-MC<версия>-neoforge.jar`, собраны против официальных маппингов
Mojang — кладутся в `mods/` как есть.

| Minecraft | NeoForge |
|---|---|
| 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11 | ✅ |
| 1.21.2, 1.21.6, 1.21.7, 1.21.9 | — у NeoForge там были только beta-сборки; только Fabric |
| 26.1, 26.2 | — NeoForge-сборки мода пока нет; только Fabric |

Конфиг подхватывается как есть — новые настройки получают разумные значения при первом запуске.

---

*mod by @pycodder — https://modrinth.com/user/pycodder*
