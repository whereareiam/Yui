package me.whereareiam.yui.model.config.languages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranslationSettings {
    private int cacheStaticSize = 1000;
    private int cacheStaticTtl = 60; // minutes

    private int cacheSemiStaticSize = 500;
    private int cacheSemiStaticTtl = 30; // minutes

    private int cacheDynamicSize = 100;
    private int cacheDynamicTtl = 5; // minutes

    // Performance settings
    private boolean cacheEnabled = true;
    private boolean prerenderStatic = true;
    private boolean buildDependencyGraph = true;
    private boolean logTimings = false;
}
