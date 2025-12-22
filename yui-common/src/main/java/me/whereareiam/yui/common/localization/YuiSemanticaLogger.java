package me.whereareiam.yui.common.localization;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.semantica.SemanticaLogger;

@Slf4j
public class YuiSemanticaLogger implements SemanticaLogger {
    @Override
    public void debug(String message, Object... args) {
        log.debug("[localization] " + message, args);
    }

    @Override
    public void info(String message, Object... args) {
        log.info("[localization] " + message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        log.warn("[localization] " + message, args);
    }

    @Override
    public void error(String message, Object... args) {
        log.error("[localization] " + message, args);
    }
}
