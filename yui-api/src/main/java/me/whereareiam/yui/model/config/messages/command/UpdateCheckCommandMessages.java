package me.whereareiam.yui.model.config.messages.command;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateCheckCommandMessages {
    private String description;
    private String example;
    private Variables variables;
    private Check check;
    private Selection selection;

    @Getter
    @Setter
    public static class Variables {
        private String target;
    }

    @Getter
    @Setter
    public static class Check {
        private Core core;
        private All all;
        private Plugin plugin;
        private Error error;

        @Getter
        @Setter
        public static class Core {
            private String title;
            private List<String> description;
        }

        @Getter
        @Setter
        public static class All {
            private String title;
            private List<String> description;
        }

        @Getter
        @Setter
        public static class Plugin {
            private String title;
            private List<String> description;
        }

        @Getter
        @Setter
        public static class Error {
            private String title;
            private List<String> description;
        }
    }

    @Getter
    @Setter
    public static class Selection {
        private String title;
        private List<String> description;
        private String placeholder;
        private NoPlugins noPlugins;

        @Getter
        @Setter
        public static class NoPlugins {
            private String title;
            private List<String> description;
        }
    }
}
