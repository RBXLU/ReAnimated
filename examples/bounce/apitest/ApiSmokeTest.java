import com.pycodder.reanimated.api.*;

import java.util.List;

public class ApiSmokeTest {

    private static int failed = 0;

    static void check(String what, boolean ok) {
        System.out.printf("  %-58s %s%n", what, ok ? "ok" : "ПРОВАЛ");
        if (!ok) failed++;
    }

    static final class Bounce implements CustomPreset {
        public String id() { return "reanimated_bounce:jump"; }
        public String displayName() { return "Jump"; }
        public PresetMotion motionAt(float p) {
            float landed = outBounce(p);
            return new PresetMotion(0f, -(1f - landed) * 56f, 1f);
        }
        static float outBounce(float t) {
            float n1 = 7.5625f, d1 = 2.75f;
            if (t < 1f / d1) return n1 * t * t;
            if (t < 2f / d1) { t -= 1.5f / d1; return n1 * t * t + 0.75f; }
            if (t < 2.5f / d1) { t -= 2.25f / d1; return n1 * t * t + 0.9375f; }
            t -= 2.625f / d1; return n1 * t * t + 0.984375f;
        }
    }

    public static void main(String[] args) {
        System.out.println("Версия API: " + ReAnimatedApi.API_VERSION);

        System.out.println("\n[1] Регистрация анимации");
        Bounce bounce = new Bounce();
        ReAnimatedApi.registerPreset(bounce);
        check("анимация видна по идентификатору", ReAnimatedApi.preset("reanimated_bounce:jump") == bounce);
        check("анимация попала в список", ReAnimatedApi.presets().size() == 1);
        ReAnimatedApi.registerPreset(bounce);
        check("повторная регистрация не задваивает", ReAnimatedApi.presets().size() == 1);
        ReAnimatedApi.registerPreset(null);
        check("пресет без идентификатора не ломает список", ReAnimatedApi.presets().size() == 1);

        System.out.println("\n[2] Кривая прыжка");
        PresetMotion start = ReAnimatedApi.motionOf(bounce, 0f);
        PresetMotion end = ReAnimatedApi.motionOf(bounce, 1f);
        check("в начале экран смещён вверх", start.offsetY < -50f);
        check("в конце экран ровно на месте", end.isIdentity());
        float prev = Float.NEGATIVE_INFINITY;
        boolean hops = false;
        for (int i = 1; i <= 100; i++) {
            float y = ReAnimatedApi.motionOf(bounce, i / 100f).offsetY;
            if (y < prev - 0.01f) hops = true;
            prev = y;
        }
        check("экран подпрыгивает, а не просто съезжает", hops);

        System.out.println("\n[3] Изоляция чужого кода");
        CustomPreset broken = new CustomPreset() {
            public String id() { return "broken:preset"; }
            public String displayName() { return "Broken"; }
            public PresetMotion motionAt(float progress) { throw new IllegalStateException("нарочно"); }
        };
        check("исключение из пресета гасится", ReAnimatedApi.motionOf(broken, 0.5f).isIdentity());
        check("null из пресета не роняет", ReAnimatedApi.motionOf(new CustomPreset() {
            public String id() { return "null:preset"; }
            public String displayName() { return "Null"; }
            public PresetMotion motionAt(float progress) { return null; }
        }, 0.5f).isIdentity());

        System.out.println("\n[4] Снятие с регистрации (кнопка «выключить»)");
        ReAnimatedApi.unregisterPreset("reanimated_bounce:jump");
        check("анимация пропала из списка", ReAnimatedApi.presets().isEmpty());
        check("по идентификатору больше не находится", ReAnimatedApi.preset("reanimated_bounce:jump") == null);
        ReAnimatedApi.unregisterPreset("reanimated_bounce:jump");
        check("повторное снятие безопасно", ReAnimatedApi.presets().isEmpty());
        ReAnimatedApi.registerPreset(bounce);
        check("включается обратно", ReAnimatedApi.presets().size() == 1);

        System.out.println("\n[5] Правила «анимировать ли экран»");
        check("без правил спрашивать нечего", !ReAnimatedApi.hasFilters());
        ReAnimatedApi.registerFilter(screen -> { throw new RuntimeException("нарочно"); });
        ReAnimatedApi.registerFilter(screen -> "fragile".equals(screen)
                ? AnimationFilter.Result.SKIP : AnimationFilter.Result.DEFAULT);
        check("правила появились", ReAnimatedApi.hasFilters());
        check("падающее правило пропускается, следующее отвечает",
                ReAnimatedApi.askFilters("fragile") == AnimationFilter.Result.SKIP);
        check("посторонний экран правилами не тронут",
                ReAnimatedApi.askFilters("other") == AnimationFilter.Result.DEFAULT);

        System.out.println("\n[6] События");
        final String[] seen = new String[3];
        check("без слушателей событий не собираем", !ReAnimatedApi.hasListeners());
        ReAnimatedApi.addListener(new AnimationListener() {
            public void onOpen(Object screen) { throw new RuntimeException("нарочно"); }
        });
        ReAnimatedApi.addListener(new AnimationListener() {
            public void onOpen(Object screen) { seen[0] = String.valueOf(screen); }
            public void onClose(Object screen, Object next) { seen[1] = screen + "->" + next; }
            public void onFinish(Object screen) { seen[2] = String.valueOf(screen); }
        });
        ReAnimatedApi.fireOpen("title");
        ReAnimatedApi.fireClose("title", "options");
        ReAnimatedApi.fireFinish("title");
        check("падающий слушатель не мешает остальным", "title".equals(seen[0]));
        check("закрытие сообщает, куда уходим", "title->options".equals(seen[1]));
        check("завершение доходит", "title".equals(seen[2]));

        System.out.println("\n[7] Кнопки в настройках");
        final boolean[] toggled = {false};
        SettingsEntry entry = new SettingsEntry() {
            public String id() { return "reanimated_bounce:toggle"; }
            public String label() { return toggled[0] ? "Bounce: OFF" : "Bounce: ON"; }
            public void open(Object parent) { toggled[0] = !toggled[0]; }
        };
        ReAnimatedApi.registerSettingsEntry(entry);
        ReAnimatedApi.registerSettingsEntry(entry);
        check("кнопка не задваивается", ReAnimatedApi.settingsEntries().size() == 1);
        check("надпись показывает состояние", "Bounce: ON".equals(entry.label()));
        ReAnimatedApi.openEntry(entry, null);
        check("нажатие переключило состояние", toggled[0]);
        check("надпись обновилась", "Bounce: OFF".equals(ReAnimatedApi.settingsEntries().get(0).label()));
        ReAnimatedApi.openEntry(new SettingsEntry() {
            public String id() { return "broken:entry"; }
            public String label() { return "Broken"; }
            public void open(Object parent) { throw new RuntimeException("нарочно"); }
        }, null);
        check("падающая кнопка не роняет игру", true);

        System.out.println("\n[8] Состояние интерфейса");
        check("без поставщика состояние «покой»", !ReAnimatedApi.state().isAnimating());
        ReAnimatedApi.setStateSupplier(() -> new AnimationState(true, false, true, 0.5f, 12f, -3f, 0.95f));
        AnimationState st = ReAnimatedApi.state();
        check("состояние доезжает до расширения",
                st.isAnimating() && st.isTransitioning() && st.offsetX() == 12f && st.scale() == 0.95f);
        ReAnimatedApi.setStateSupplier(() -> { throw new RuntimeException("нарочно"); });
        check("падающий поставщик даёт «покой»", !ReAnimatedApi.state().isAnimating());

        System.out.println();
        if (failed == 0) {
            System.out.println("ВСЁ ПРОШЛО");
        } else {
            System.out.println("ПРОВАЛОВ: " + failed);
            System.exit(1);
        }
    }
}
