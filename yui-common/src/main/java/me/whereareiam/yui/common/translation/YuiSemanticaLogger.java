package me.whereareiam.yui.common.translation;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.semantica.SemanticaLogger;

@Slf4j
public class YuiSemanticaLogger implements SemanticaLogger {
    @Override
    public void debug(String message, Object... args) {
        log.debug("[Semantica] " + message, args);
    }

    @Override
    public void info(String message, Object... args) {
        log.info("[Semantica] " + message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        log.warn("[Semantica] " + message, args);
    }

    @Override
    public void error(String message, Object... args) {
        log.error("[Semantica] " + message, args);
    }
}
