# YUI - Yggdrasil’s Unseen Intelligence

<table style="border: none; width: 80%">
  <tr style="border: none;">
    <td style="border: none;text-align: center;">
      Hi, I'm YUI. I look after one Discord server: I know its members and the languages they speak, talk with them in
      private when there is something to settle, keep an audit trail of what happens and give roles that follow facts.
      Plugins teach me the rest.
    </td>
    <td style="border: none;">
      <img src="https://cdn.discordapp.com/avatars/1210500906534174770/363f848b510cf621941919977cad0ad3.png?size=512" alt="Yui image" style="border-radius: 8px" />
    </td>
  </tr>
</table>

## Starting Yui

Yui needs Java 25 and a Discord bot with the **Server Members** and **Message Content** intents. Put `Yui.jar` in a
folder of its own with a `settings.yml`:

```yaml
token: "${DISCORD_TOKEN}"
guild: "123456789012345678"
locale: "en-US"
```

```bash
DISCORD_TOKEN=... java -jar Yui.jar
```

Yui keeps its data in an H2 database in that folder unless `persistence.yml` names PostgreSQL or MariaDB. Its own
plugins, Onboarding and Interests, are jars for its `plugins/` folder.

## Learn more

The guides for running Yui, writing plugins and contributing are in `docs/content/`: start with
[Getting started](docs/content/running/index.md) or [Your first plugin](docs/content/plugins/index.md).
