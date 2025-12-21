package me.whereareiam.yui.common.translation;

import me.whereareiam.semantica.translation.base.TranslationLocale;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class DiscordLocaleAdapter implements TranslationLocale {
    private final DiscordLocale delegate;

    private DiscordLocaleAdapter(DiscordLocale delegate) {
        this.delegate = delegate;
    }

    public static DiscordLocaleAdapter wrap(DiscordLocale locale) {
        return new DiscordLocaleAdapter(locale);
    }

    @Override
    public String getLanguage() {
        return delegate.getLocale();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscordLocaleAdapter that)) return false;
        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.getLocale();
    }
}
