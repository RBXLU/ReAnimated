# ReAnimated 1.4.1 HOTFIX — crash fix for Minecraft 26.2

A single crash fix. **Minecraft 26.2 only** — no other version is affected, and nothing else changed.

---

## 🇬🇧 English

### 💥 Fixed: crash when opening the Creative inventory / Advancements

On 26.2 the game could crash while rendering with:

```
java.lang.IllegalArgumentException: Scissor size must be >0, was 308x0
```

**What happened.** The new tab animation hides a tab while it's still behind the panel by clipping it to the strip of screen outside the panel. If that strip has no height — the panel sits flush against the top or bottom edge of the screen, which happens at large GUI scales or in small windows — the clip rectangle came out zero-height. Older Minecraft versions quietly ignored such a rectangle; 26.2 validates it and throws, taking the game down.

**The fix.** The strip is measured first, and if there's no room for it, that tab simply isn't animated for that frame — no clipping, no crash. Everything else about the tab animation is unchanged.

If you're on 26.2, replace the `26.2+1.4.0` file with this one. Your config carries over untouched.

---

## 🇷🇺 Русский

### 💥 Исправлено: краш при открытии креативного инвентаря / достижений

На 26.2 игра могла падать во время отрисовки с ошибкой:

```
java.lang.IllegalArgumentException: Scissor size must be >0, was 308x0
```

**Что происходило.** Новая анимация вкладок прячет вкладку, пока та ещё за панелью, обрезая её по полосе экрана за краем панели. Если у этой полосы нет высоты — панель прижата к верхнему или нижнему краю экрана, а так бывает при крупном масштабе интерфейса или в маленьком окне — прямоугольник обрезки получался нулевой высоты. Старые версии Minecraft такой прямоугольник молча игнорировали, а 26.2 его проверяет и падает вместе с игрой.

**Как починено.** Полоса теперь измеряется заранее, и если места для неё нет — вкладка в этом кадре просто не анимируется: ни обрезки, ни краша. Всё остальное в анимации вкладок осталось прежним.

Если ты на 26.2 — замени файл `26.2+1.4.0` этим. Конфиг подхватится как есть.

---

### Загрузчики и версии / Loaders & versions
- **Fabric / Quilt**, Minecraft **26.2** (нужен Fabric API). / **Fabric / Quilt**, Minecraft **26.2** (requires Fabric API).
- Версия мода — `26.2+1.4.1HOTFIX`. / Mod version is `26.2+1.4.1HOTFIX`.
- Остальные версии остаются на **1.4.0** — они этим багом не затронуты. / Every other version stays on **1.4.0** — they are not affected by this bug.

*mod by @pycodder — https://modrinth.com/user/pycodder*
