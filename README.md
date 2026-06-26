# ReAnimated

**Smooth animations for the entire Minecraft UI.** Menus, buttons, containers and the
inventory now slide, scale and follow your cursor — without changing how the game plays.
Pure client‑side eye‑candy, fully configurable, safe on any server.

![icon]([assets/reanimated/icon.png](https://cdn.modrinth.com/data/cached_images/18ae1c5f86365aa33567ff4a18d1095a2d94bb5a.png))

## ✨ Features

- **Menus open smoothly** — titles **and** buttons slide up together from below (no more "levitating" text). Works on the main menu, Singleplayer, Multiplayer, Settings and every other screen automatically.
- **Animated logo** — the *Minecraft* logo grows in with a subtle bounce on the title screen.
- **Containers slide up** — furnaces, chests, barrels, the inventory and any other container panel smoothly slide in from the bottom, while the blurred background stays perfectly still.
- **Following slot highlight** — the slot highlight glides smoothly across the inventory as you move the cursor, instead of snapping.
- **Button hover scaling** — buttons gently grow when hovered and ease back when you move away.
- **Server list** appears from the bottom as the Multiplayer screen slides in.

## ⚙️ Configuration

Open **Options → Video Settings → `ReAnimated settings`**.

Every animation can be tuned independently:

- **Enable / disable** each animation
- **Duration** (how fast)
- **Slide distance**
- **Trajectory / easing** — *Linear*, *Out Cubic*, *Out Back (bounce)*, *Out Expo*
- Hover scale amount & speed, slot‑highlight speed, and more

Settings are saved to `config/reanimated.json` and apply instantly.

## 📦 Requirements

- Minecraft **1.21.1**
- **Fabric Loader** 0.16+
- **[Fabric API](https://modrinth.com/mod/fabric-api)**

Client‑side only — not required on servers. Compatible with **Sodium** and other
rendering mods (ReAnimated only touches GUI classes, never world rendering).

## 🧩 Compatibility notes

- Animations affect *opening* screens. A close/out transition isn't included yet (planned).

---

Made with ❤️ by **[@pycodder](https://modrinth.com/user/pycodder)**

<details>
<summary>🇷🇺 Описание на русском</summary>

**Плавные анимации для всего интерфейса Minecraft.** Меню, кнопки, контейнеры и
инвентарь теперь плавно выезжают, увеличиваются и следуют за курсором — не влияя на
игровой процесс. Чисто клиентский мод, полностью настраиваемый, безопасен на любом сервере.

**Возможности:**
- Меню открываются плавно — заголовки **и** кнопки выезжают снизу **вместе**.
- Логотип *Minecraft* красиво вырастает с лёгким отскоком.
- Контейнеры (печь, сундук, инвентарь и др.) плавно выезжают снизу, а размытый фон стоит на месте.
- Подсветка слота плавно следует за курсором по инвентарю.
- Кнопки мягко увеличиваются при наведении.

**Настройки:** Настройки → Настройки графики → «Настройки ReAnimated». Для каждой
анимации: вкл/выкл, длительность, дистанция, траектория и другое.

**Требуется:** Minecraft 1.21.1, Fabric Loader, Fabric API. Только клиент.
</details>
