# ReAnimated API

Небольшой API для других модов: добавить свою анимацию появления, подсказать моду,
что конкретный экран трогать не надо, узнать, куда прямо сейчас сдвинут интерфейс,
и повесить свою кнопку в настройки.

Пакет `com.pycodder.reanimated.api` не тянет за собой ни классов игры, ни классов
загрузчика: одни и те же вызовы работают на Fabric, Quilt и NeoForge и на всех
поддерживаемых версиях. Регистрировать можно в любой момент — списки читаются при
отрисовке.

Расширения изолированы: исключение из чужого кода логируется и гасится, а не роняет
игру и не ломает анимацию остальных экранов.

---

## Подключение

ReAnimated не публикуется в maven, поэтому проще всего положить jar нужной версии в
`libs/` и объявить его зависимостью только на время компиляции — в рантайме API
приходит из самого мода:

```gradle
dependencies {
    // Fabric
    modCompileOnly files("libs/reanimated-1.21.11+1.7.jar")
    // NeoForge
    // compileOnly files("libs/reanimated-1.6.jar")
}
```

Мод не обязателен: обращения к API стоит спрятать за проверкой, что класс загружен
(`FabricLoader.getInstance().isModLoaded("reanimated")`, `ModList.get().isLoaded(...)`
или обычный `try { Class.forName(...) } catch (Throwable ignored) {}`).

`ReAnimatedApi.API_VERSION` — версия самого API, растёт только при добавлении в него
нового.

## Своя анимация появления

Мод описывает не кадры, а положение экрана в произвольный момент: сдвиг в пикселях
и масштаб относительно центра. Прогресс приходит линейным — кривая является частью
самой анимации, так что расширение полностью управляет характером движения.

```java
ReAnimatedApi.registerPreset(new CustomPreset() {
    @Override public String id() { return "mymod:swoop"; }
    @Override public String displayName() { return "Swoop"; }

    @Override public PresetMotion motionAt(float progress) {
        float e = 1f - (1f - progress) * (1f - progress);   // ease-out
        return new PresetMotion((1f - e) * 40f, 0f, 0.9f + 0.1f * e);
    }
});
```

Убрать её обратно — `ReAnimatedApi.unregisterPreset("mymod:swoop")`: анимация пропадёт
из списка, а экраны, где её выбрали, вернутся к обычному пресету. Это нужно расширению,
которое умеет выключаться.

Анимация появляется в настройках мода строкой **«Анимация расширения»**. Выбор
хранится в конфиге как `uiPreset: "CUSTOM"` плюс `customPresetId: "mymod:swoop"`,
поэтому переживает временное отсутствие мода: пока его нет, экраны открываются
пресетом по умолчанию, вернётся — вернётся и анимация.

Закрытие мод проигрывает сам, отматывая ту же кривую назад. Взаимодействие с
каскадом кнопок и переходами между экранами тоже берёт на себя он.

## Правило «анимировать ли этот экран»

Нужно там, где ReAnimated не может знать деталей чужого экрана: экран сам рисует
свою анимацию, держит кадровый буфер, ломается от смещённой матрицы или, наоборот,
хочет анимироваться, хотя игрок отключил анимацию модовых экранов.

```java
ReAnimatedApi.registerFilter(screen -> screen instanceof MyFragileScreen
        ? AnimationFilter.Result.SKIP
        : AnimationFilter.Result.DEFAULT);
```

Правила спрашиваются в порядке регистрации, первый определённый ответ побеждает.
Настройка конкретного экрана, сделанная игроком, сильнее любого правила.

## Где сейчас интерфейс

Чтобы двигать свой оверлей вместе с экраном, а не рядом с ним:

```java
AnimationState s = ReAnimatedApi.state();
if (s.isAnimating()) {
    matrices.translate(s.offsetX(), s.offsetY());
    // s.scale(), s.progress(), s.isClosing(), s.isTransitioning()
}
```

Снимок собирается только по запросу: пока никто не спрашивает, лишних расчётов нет.

## События

```java
ReAnimatedApi.addListener(new AnimationListener() {
    @Override public void onOpen(Object screen) { }
    @Override public void onClose(Object screen, Object next) { }
    @Override public void onFinish(Object screen) { }
});
```

Экран приходит как `Object`: у мода одна кодовая база на разные версии игры и разные
маппинги, где тип `Screen` называется по-разному. Приводите через `instanceof`.
Вызовы происходят в потоке отрисовки — тяжёлую работу из них лучше не делать.

## Своя кнопка в настройках

```java
ReAnimatedApi.registerSettingsEntry(new SettingsEntry() {
    @Override public String id() { return "mymod:tweaks"; }
    @Override public String label() { return "MyMod tweaks..."; }
    @Override public void open(Object parent) {
        MinecraftClient.getInstance().setScreen(new MyScreen((Screen) parent));
    }
});
```

Кнопка появляется секцией **«Расширения»** в экране настроек мода. Надпись —
обычный текст, а не ключ перевода: расширение переводит себя само.

Кнопка не обязана открывать экран: если `open` просто что-то переключает, экран настроек
пересобирается сразу после нажатия, поэтому надпись вида `"Bounce: ON"` / `"Bounce: OFF"`
обновляется на месте. Рабочий пример такой кнопки — в [`examples/bounce`](examples/bounce).

---

Готовый пример расширения (своя анимация + кнопка её выключения) лежит в
[`examples/bounce`](examples/bounce) — там же прогон API без игры.

Вопросы и пожелания к API — в issues репозитория.
