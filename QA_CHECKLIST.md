# Manual QA checklist

Run this before every release. Tick each line on a real device.

## Devices and versions
- [ ] Android 8 (API 26) — oldest supported.
- [ ] Android 10 (API 29).
- [ ] Android 13 (API 33) — notification permission prompt.
- [ ] Android 14 (API 34) — foreground service type microphone.
- [ ] Android 15 (API 35) — target.
- [ ] One low-RAM device (2 GB) and one flagship.
- [ ] One Xiaomi/Redmi, one Samsung, one Realme or Vivo (battery savers).
- [ ] 360 dp width screen, and a large-font accessibility setting.

## Permission flows (each must show the disclosure card first)
- [ ] Notifications denied, then granted later.
- [ ] Microphone denied → Speak and Call Guard both explain and stop cleanly.
- [ ] Camera denied → QR screen offers "pick a picture" instead.
- [ ] Notification access never granted → auto-check stays off, no crash.
- [ ] Call screening role refused → the app keeps working.
- [ ] Usage access never granted → phone health says so, no crash.
- [ ] Every permission revoked in system settings while the app is closed.

## Core checks
- [ ] Paste a message, get a verdict with reasons.
- [ ] Every one of the 30 key messages gives the expected verdict.
- [ ] Photo OCR reads a clear screenshot and rejects a blank one politely.
- [ ] QR scan: a plain UPI ID, a collect request, and a pre-filled amount.
- [ ] Number check accepts +91 and 10-digit input.
- [ ] Voice in and read-aloud out.
- [ ] Share text into the app from another app.
- [ ] Report a wrong result opens the share sheet and sends nothing on its own.

## Agent (all switches off by default)
- [ ] Turn message watch on, get a warning on a scam-looking notification.
- [ ] Trusted sender is never warned about.
- [ ] Pause for 1 hour and pause today actually silence the agent.
- [ ] Call screening shows a quiet tip for an unknown number.
- [ ] Phone health "check now" returns a score.
- [ ] Remote-access warning appears when the overlap is simulated.

## Call Guard
- [ ] Starts only from the button, the notification action or the tile.
- [ ] The persistent notice is visible the whole time.
- [ ] It stops when the call ends, and after 30 minutes.
- [ ] No transcript is written anywhere.

## Languages, display and accessibility
- [ ] Switch to Hindi: every screen changes with no restart.
- [ ] Switch to Tamil and Bengali: text renders, no boxes.
- [ ] Switch to Urdu: layout mirrors right-to-left.
- [ ] Easy Mode on: bigger text and buttons everywhere.
- [ ] High contrast on: text stays readable.
- [ ] Kids Safe Mode on: parent PIN required to change settings.
- [ ] Reduce-motion setting respected.
- [ ] TalkBack can reach every button.

## Battery and background
- [ ] Battery guidance steps match the actual phone maker's menus.
- [ ] After a restart, the daily job returns and nothing else starts.
- [ ] App is killed in the background and reopened cleanly.

## Crash safety review
- [ ] No unhandled exception when every permission is denied.
- [ ] No crash when a role is refused.
- [ ] No crash with no network and airplane mode on.
- [ ] No crash on a very large pasted message (10,000+ characters).
- [ ] No crash with an empty or emoji-only message.
- [ ] No crash when the camera is unavailable or in use by another app.
- [ ] No crash when a QR contains binary or malformed data.
