package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Поиск настроек конкретного экрана и список экранов, которые игрок уже открывал.
 *
 * Ключ — полное имя класса экрана: оно уникально и переживает переименования в
 * настройках. Правило на все экраны мода хранится в той же карте под ключом
 * {@code mod:<id>}; у экрана приоритет выше, чем у его мода.
 *
 * Список «виденных» экранов живёт только в памяти и не пишется в конфиг: в сборке с
 * сотней модов он бы разросся на пустом месте, а нужен он ровно затем, чтобы в
 * редакторе было из чего выбирать. Экраны с уже заданными настройками показываются всегда.
 */
public final class ScreenRules {
    private ScreenRules() {}

    /** Префикс ключа правила на все экраны мода. */
    public static final String MOD_PREFIX = "mod:";

    /** Сколько имён держим — чтобы список не рос бесконечно в больших сборках. */
    private static final int MAX_SEEN = 200;

    /** Имена классов в обычной сборке Fabric 1.21.x: {@code class_490} и т. п. */
    private static final Pattern OBFUSCATED = Pattern.compile("(class|C)_\\d+");

    /** Экраны, открытые за текущий запуск игры. Свежие — в конце. */
    private static final Set<String> SEEN = new LinkedHashSet<>();
    /** Заголовки виденных экранов: по имени класса в обычной сборке Fabric экран не узнать. */
    private static final Map<String, String> TITLES = new HashMap<>();
    /** Мод виденного экрана (ключ правила мода), если его удалось определить. */
    private static final Map<String, String> SCREEN_MOD = new HashMap<>();
    /** Названия модов по ключу правила. */
    private static final Map<String, String> MOD_NAMES = new HashMap<>();

    /** По классу экрана находит мод, которому он принадлежит. Задаёт загрузчик. */
    public interface ModLookup {
        /** {id, название} или null, если класс не из мода. */
        String[] find(Class<?> cls);
    }

    private static volatile ModLookup lookup = null;

    public static void setModLookup(ModLookup l) {
        lookup = l;
    }

    private static final String[] NO_MOD = new String[0];

    private static final ClassValue<String[]> MOD_OF = new ClassValue<>() {
        @Override
        protected String[] computeValue(Class<?> type) {
            ModLookup l = lookup;
            if (l == null || type.getName().startsWith("net.minecraft.")) return NO_MOD;
            try {
                String[] r = l.find(type);
                if (r == null || r.length < 2 || r[0] == null) return NO_MOD;
                // Свои экраны настроек мод не анимирует как чужой мод, а игра — не мод.
                if ("minecraft".equals(r[0]) || "reanimated".equals(r[0])) return NO_MOD;
                return r;
            } catch (Throwable t) {
                return NO_MOD;
            }
        }
    };

    /** Ключ правила мода для экрана или null. */
    public static String modKey(Object screen) {
        if (screen == null) return null;
        String[] m = MOD_OF.get(screen.getClass());
        return m.length == 0 ? null : MOD_PREFIX + m[0];
    }

    public static boolean isModKey(String key) {
        return key != null && key.startsWith(MOD_PREFIX);
    }

    /** Запоминает экран, чтобы он появился в списке редактора. */
    public static void remember(Object screen) {
        remember(screen, null);
    }

    /** То же, с заголовком экрана — им экран подписывается в списке. */
    public static void remember(Object screen, String title) {
        if (screen == null) return;
        String key = key(screen);
        // Экраны настроек самого ReAnimated в списке только мешали бы, а экраны загрузки
        // и подключения живут доли секунды и не анимируются — настраивать там нечего.
        if (key.startsWith("com.pycodder.reanimated.")) return;
        ScreenFlags flags = ScreenFlags.of(screen);
        if (flags != null && flags.transient_) return;
        String mod = modKey(screen);
        synchronized (SEEN) {
            // Повторное добавление поднимает экран наверх списка «недавних».
            SEEN.remove(key);
            SEEN.add(key);
            while (SEEN.size() > MAX_SEEN) {
                String old = SEEN.iterator().next();
                SEEN.remove(old);
                TITLES.remove(old);
                SCREEN_MOD.remove(old);
            }
            if (title != null && !title.isBlank()) TITLES.put(key, title.strip());
            if (mod != null) {
                SCREEN_MOD.put(key, mod);
                MOD_NAMES.put(mod, MOD_OF.get(screen.getClass())[1]);
            }
        }
    }

    /** Ключ экрана в конфиге. */
    public static String key(Object screen) {
        return screen.getClass().getName();
    }

    /** Короткое имя класса или null, если оно ничего не скажет игроку. */
    private static String className(String key) {
        int dot = key.lastIndexOf('.');
        String name = (dot < 0 ? key : key.substring(dot + 1)).replace('$', '.');
        int inner = name.lastIndexOf('.');
        String last = inner < 0 ? name : name.substring(inner + 1);
        return OBFUSCATED.matcher(last).matches() || last.matches("\\d+") ? null : name;
    }

    /** Имя для списка настроек: заголовок экрана и, где оно понятное, имя класса. */
    public static String displayName(String key) {
        if (isModKey(key)) {
            String name;
            synchronized (SEEN) {
                name = MOD_NAMES.get(key);
            }
            if (name == null || name.isEmpty()) {
                ScreenOverride o = ReAnimatedConfig.get().screenOverrides.get(key);
                name = o != null && o.name != null && !o.name.isEmpty() ? o.name : key.substring(MOD_PREFIX.length());
            }
            return name;
        }
        String title;
        synchronized (SEEN) {
            title = TITLES.get(key);
        }
        if (title == null) {
            ScreenOverride o = ReAnimatedConfig.get().screenOverrides.get(key);
            if (o != null && o.name != null && !o.name.isEmpty()) title = o.name;
        }
        String cls = className(key);
        if (title == null || title.isEmpty()) return cls != null ? cls : key.substring(key.lastIndexOf('.') + 1);
        if (cls == null || cls.equalsIgnoreCase(title.replace(" ", ""))) return title;
        return title + " (" + cls + ")";
    }

    /** Экран из мода (не {@code net.minecraft.*}) — в списке помечается отдельно. */
    public static boolean isModded(String key) {
        return !isModKey(key) && !key.startsWith("net.minecraft.");
    }

    /** Ключ правила мода для экрана с этим ключом, если мод известен. */
    public static String modOfKey(String key) {
        synchronized (SEEN) {
            return SCREEN_MOD.get(key);
        }
    }

    /**
     * Действующие настройки этого экрана — его собственные поверх правила его мода —
     * или null, если ни для экрана, ни для мода ничего не задано.
     */
    public static ScreenOverride get(Object screen) {
        if (screen == null) return null;
        Map<String, ScreenOverride> map = ReAnimatedConfig.get().screenOverrides;
        if (map == null || map.isEmpty()) return null;
        ScreenOverride own = map.get(key(screen));
        String mod = modKey(screen);
        return ScreenOverride.merge(own, mod == null ? null : map.get(mod));
    }

    /** Настройки по ключу, создавая запись при первом изменении. */
    public static ScreenOverride getOrCreate(String key) {
        Map<String, ScreenOverride> map = ReAnimatedConfig.get().screenOverrides;
        ScreenOverride o = map.get(key);
        if (o == null) {
            o = new ScreenOverride();
            map.put(key, o);
        }
        // Имя сохраняем вместе с правилом: в следующем запуске экран могут ещё не
        // открыть, а в списке он должен называться так же.
        String name;
        synchronized (SEEN) {
            name = isModKey(key) ? MOD_NAMES.get(key) : TITLES.get(key);
        }
        if (name != null && !name.isEmpty()) o.name = name;
        return o;
    }

    /** Убирает записи, которые ничего не меняют — чтобы конфиг не копил пустые объекты. */
    public static void prune() {
        ReAnimatedConfig.get().screenOverrides.entrySet().removeIf(e ->
            e.getValue() == null || e.getValue().isDefault());
    }

    /** Есть ли у экрана или мода с этим ключом свои настройки. */
    public static boolean customized(String key) {
        ScreenOverride o = ReAnimatedConfig.get().screenOverrides.get(key);
        return o != null && !o.isDefault();
    }

    /**
     * Экраны для редактора: сначала с уже заданными настройками, затем недавно
     * открытые (свежие сверху).
     */
    public static List<String> listForEditor() {
        List<String> out = new ArrayList<>();
        for (String key : ReAnimatedConfig.get().screenOverrides.keySet()) {
            if (!isModKey(key)) out.add(key);
        }
        List<String> seen;
        synchronized (SEEN) {
            seen = new ArrayList<>(SEEN);
        }
        for (int i = seen.size() - 1; i >= 0; i--) {
            String key = seen.get(i);
            if (!out.contains(key)) out.add(key);
        }
        return out;
    }

    /** Моды для редактора: с заданными правилами и те, чьи экраны открывали. */
    public static List<String> modsForEditor() {
        List<String> out = new ArrayList<>();
        for (String key : ReAnimatedConfig.get().screenOverrides.keySet()) {
            if (isModKey(key)) out.add(key);
        }
        List<String> seen;
        synchronized (SEEN) {
            seen = new ArrayList<>(SEEN);
            for (int i = seen.size() - 1; i >= 0; i--) {
                String mod = SCREEN_MOD.get(seen.get(i));
                if (mod != null && !out.contains(mod)) out.add(mod);
            }
        }
        return out;
    }
}
