package com.pycodder.reanimated.compat;

import com.pycodder.reanimated.anim.ScreenRules;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;

import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;

/**
 * Какому моду принадлежит класс экрана — для правил «на все экраны мода».
 *
 * Пока NeoForge грузил моды модулями Java, класс узнаётся по имени модуля; в новых
 * версиях модулей нет, и класс узнаётся по файлу, из которого загружен. Имя модуля
 * у файла мода есть не во всех версиях загрузчика, поэтому спрашивается без прямой
 * ссылки на метод.
 */
public final class NeoModLookup implements ScreenRules.ModLookup {

    @Override
    public String[] find(Class<?> cls) {
        Module module = cls.getModule();
        String moduleName = module.isNamed() ? module.getName() : null;
        Path where = location(cls);
        for (IModInfo mod : ModList.get().getMods()) {
            IModFileInfo file = mod.getOwningFile();
            if (file == null) continue;
            boolean same = moduleName != null && moduleName.equals(moduleName(file));
            if (!same && where != null) {
                try {
                    same = where.equals(file.getFile().getFilePath().toAbsolutePath().normalize());
                } catch (RuntimeException ignored) {
                    // файл мода без пути на диске
                }
            }
            if (same) return new String[] {mod.getModId(), mod.getDisplayName()};
        }
        return null;
    }

    private static Path location(Class<?> cls) {
        try {
            CodeSource source = cls.getProtectionDomain().getCodeSource();
            URL url = source == null ? null : source.getLocation();
            return url == null ? null : Path.of(url.toURI()).toAbsolutePath().normalize();
        } catch (Exception e) {
            return null;
        }
    }

    private static String moduleName(IModFileInfo file) {
        try {
            Object name = file.getClass().getMethod("moduleName").invoke(file);
            return name instanceof String s ? s : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
