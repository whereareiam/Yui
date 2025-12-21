package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.vocabulary.Vocabulary;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

@Component
public class VocabularyTemplate implements TemplateProvider<Vocabulary> {
    @Override
    public Vocabulary supply(Vocabulary vocabulary) {
        DiscordLocale locale = DiscordLocale.ENGLISH_US;
        vocabulary.setCancel(new Vocabulary.LocaleMap().with(locale, "Cancel"));
        vocabulary.setProceed(new Vocabulary.LocaleMap().with(locale, "Continue"));
        vocabulary.setConfirm(new Vocabulary.LocaleMap().with(locale, "Confirm"));
        vocabulary.setBack(new Vocabulary.LocaleMap().with(locale, "Back"));
        vocabulary.setNext(new Vocabulary.LocaleMap().with(locale, "Next"));
        vocabulary.setYes(new Vocabulary.LocaleMap().with(locale, "Yes"));
        vocabulary.setNo(new Vocabulary.LocaleMap().with(locale, "No"));

        Vocabulary.Category category = new Vocabulary.Category();
        category.setUtility(new Vocabulary.LocaleMap().with(locale, "🛠️ Utility"));
        category.setFun(new Vocabulary.LocaleMap().with(locale, "🎮 Fun"));
        category.setModeration(new Vocabulary.LocaleMap().with(locale, "🛡️ Moderation"));
        category.setAdministration(new Vocabulary.LocaleMap().with(locale, "⚙️ Administration"));
        category.setNone(new Vocabulary.LocaleMap().with(locale, "📌 None"));
        vocabulary.setCategory(category);

        return vocabulary;
    }
}
