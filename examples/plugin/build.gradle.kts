plugins {
    id("module-plugin")
    id("me.whereareiam.yui.plugin")
}

description = "An example plugin of Yui: a settings file, a command and its texts. Copy it to start a plugin."

yuiPlugin {
    id.set("greeting")
    main.set("me.whereareiam.yui.example.Greeting")
}
