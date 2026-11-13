---
title: Events
description: Listen to what happens in Yui and in other plugins, and react once a change is saved.
---

A plugin listens with Spring's `@EventListener`. It hears Yui's events, its own and those of every other plugin:

```java
@Component
public class Welcome {
	@EventListener
	public void onVerified(MemberVerifiedEvent event) {
		// grant what follows from being verified
	}
}
```

Changes to what Yui knows about a member publish two events: one before, named in the present (`FluctlightLanguageChangeEvent`), whose listeners may only veto, and one after, named in the past (`FluctlightLanguageChangedEvent`), once the change is saved. Listen to the after event with `@TransactionalEventListener`, so a side effect happens only for a change that was kept; a listener that fails is logged and does not undo the change.

| Events | When |
| --- | --- |
| `FluctlightCreatedEvent` | Yui meets a user for the first time |
| `FluctlightLanguageChangedEvent`, `FluctlightAdditionalLanguageAddedEvent`, `…RemovedEvent` | a member's languages change |
| `FluctlightClearedEvent` | a member's profile is cleared (`/yui clear`) |
| `ConversationOpenedEvent`, `ConversationClosedEvent` | a [conversation](../../running/conversations/index.md) opens or closes, for whatever reason |
| `JourneyCompletedEvent`, `JourneyCancelledEvent`, `JourneyFailedEvent`, `JourneySessionTimeoutEvent` | a journey ends |
| `MemberVerifiedEvent` (Onboarding) | a member finished onboarding |

Discord's own events come through JDA: a `ListenerAdapter` bean is registered with Yui's JDA and removed when the plugin stops.
