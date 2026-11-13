---
title: Texts
description: Translate Yui and change what it says, file by file, in messages/<locale>/.
---

Everything Yui says is a text with a key. Yui brings English texts; you change or translate them in `messages/<locale>/`, a folder per Discord locale, with one file per subject:

```text
messages/
  de/
    commands.yml      descriptions, arguments and replies of commands
    conversations.yml the close warning and the transcript
    audit.yml         the audit trail's entries
    embeds.yml        the author and footer of embeds
    languages.yml     the names of languages
    words.yml         buttons and small words: Cancel, Confirm, Back
plugins/onboarding/messages/de/
    journey.yml       a plugin's texts, in its own folder
```

A file needs only the texts you change; everything else keeps its default. Yui speaks a member's primary language when it has texts in it, and falls back to `en-US` for any text a locale does not have.

```yaml
# messages/de/commands.yml
clear:
  description: "Löscht das Profil eines Mitglieds"
```

Texts are ICU messages: values go in braces, and plurals and choices are written in the text itself:

```yaml
done: "{count, plural, one {# Nachricht gelöscht} other {# Nachrichten gelöscht}}."
```

A value Yui inserts, such as a member's name, is shown as written, never read as Discord markdown. A text a file lists as several lines is one text with line breaks.
