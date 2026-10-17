# Permissions

Every permission below is optional except notifications. The app works without
the rest, and each one is explained on screen before it is requested.

| Permission | Why | Declaration text for the Play form |
|---|---|---|
| `POST_NOTIFICATIONS` | To show a scam warning and the visible Call Guard notice. | "Shows a warning when a message looks like a scam." |
| `RECORD_AUDIO` | Voice input when you hold Speak, and Call Guard when you start it yourself. | "Turns your speech into text on the device so you can check a message by voice." |
| `CAMERA` | To scan a payment QR code. | "Reads a payment QR code so you can check it before paying." |
| `BIND_NOTIFICATION_LISTENER_SERVICE` (notification access) | Optional auto-check of new message notifications. | "Reads new message notification text on the device to warn about scams." |
| `READ_CONTACTS` | Optional; to skip callers you already know in call reminders. | "Skips callers already in your contacts when showing a call reminder." |
| `PACKAGE_USAGE_STATS` (usage access) | Optional phone-health check: notices when a screen-sharing tool and a money app were used close together. | "Checks whether a screen-sharing app and a money app were used at the same time, to warn about a takeover." |
| `BIND_SCREENING_SERVICE` (call screening role) | Optional; shows a quiet tip for an unknown incoming number, with no network. | "Checks an incoming number on the device and shows a quiet tip." |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE` | Call Guard, started only by you, with a visible ongoing notice. | "Keeps call safety running during a call, with a visible notice." |
| `RECEIVE_BOOT_COMPLETED` | Puts back the daily phone-health job after a restart. Starts no listening. | "Restores the daily phone health check after your phone restarts." |
| `INTERNET` | Only in a build that enables the optional Online mode. | "Only used if you switch on Online mode to get a plainer explanation." |
| `VIBRATE` | A short haptic tick when a verdict lands. | "A short vibration when a result appears." |
| `QUERY_ALL_PACKAGES` | **Not requested.** The manifest lists seven remote-access packages by name instead. | — |

The app never requests `QUERY_ALL_PACKAGES`, `READ_SMS`, `MANAGE_EXTERNAL_STORAGE`
or any location permission.
