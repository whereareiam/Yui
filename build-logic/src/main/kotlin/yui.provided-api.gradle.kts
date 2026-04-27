val providedApi = configurations.create("providedApi") {
    description = "Dependencies exposed to API consumers and provided externally at runtime."
    isCanBeConsumed = false
    isCanBeResolved = false
}

configurations.named("compileOnlyApi") {
    extendsFrom(providedApi)
}

pluginManager.withPlugin("me.whereareiam.attache") {
    configurations.named("attacheApi") {
        extendsFrom(providedApi)
    }
}
