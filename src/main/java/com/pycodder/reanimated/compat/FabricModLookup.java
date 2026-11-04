package com.pycodder.reanimated.compat;

import com.pycodder.reanimated.anim.ScreenRules;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;

import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;

/**
 * Какому моду принадлежит класс экрана — для правил «на все экраны мода». Класс
 * загружен из файла мода, поэтому ищем мод, у которого этот файл (или папка в
 * разработке) указан как источник.
 */
public final class FabricModLookup implements ScreenRules.ModLookup {

    @Override
    public String[] find(Class<?> cls) {
        CodeSource source = cls.getProtectionDomain().getCodeSource();
        if (source == null) return null;
        URL location = source.getLocation();
        if (location == null) return null;
        Path where;
        try {
            where = Path.of(location.toURI()).toAbsolutePath().normalize();
        } catch (Exception e) {
            return null; // вложенный jar в памяти и прочие нефайловые источники
        }
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModOrigin origin = mod.getOrigin();
            if (origin.getKind() != ModOrigin.Kind.PATH) continue;
            for (Path p : origin.getPaths()) {
                if (p.toAbsolutePath().normalize().equals(where)) {
                    return new String[] {mod.getMetadata().getId(), mod.getMetadata().getName()};
                }
            }
        }
        return null;
    }
}
