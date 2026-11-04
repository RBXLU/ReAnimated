package com.pycodder.reanimated.compat;

/**
 * Флаг «настройки мода уже показаны страницей внутри Sodium».
 *
 * Отдельным классом намеренно: {@code SodiumIntegration} ссылается на классы Sodium, и
 * простое чтение поля оттуда потянуло бы за собой их загрузку — без Sodium это
 * NoClassDefFoundError. Здесь же нет ни одной ссылки наружу, поэтому читать флаг
 * безопасно всегда.
 */
public final class SodiumPageState {

    /** Ставится точкой входа Sodium при регистрации страницы; volatile — пишется на старте, читается при открытии экрана. */
    public static volatile boolean registered = false;

    private SodiumPageState() {
    }
}
