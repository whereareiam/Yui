package me.whereareiam.yui.common.localization;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.semantica.SemanticaLogger;

@Slf4j
public class YuiSemanticaLogger implements SemanticaLogger {
    @Override
    public void debug(String message, Object... args) {
        log.debug("[Localization] " + message, args);
    }

    @Override
    public void info(String message, Object... args) {
        log.info("[Localization] " + message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        log.warn("[Localization] " + message, args);
    }

    @Override
    public void error(String message, Object... args) {
        log.error("[Localization] " + message, args);
    }
}
