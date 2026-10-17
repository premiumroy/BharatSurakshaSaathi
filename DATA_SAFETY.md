# Play Console — Data safety form answers

These answers match the code. The app has no analytics, no ads and no account.

**Does your app collect or share any of the required user data types?**
No.

**Is all of the user data collected by your app encrypted in transit?**
Not applicable — no user data is collected or transmitted by us.

**Do you provide a way for users to request that their data is deleted?**
Yes. In-app: Settings → "Delete all my data", and Settings → Privacy and data → "Delete all my data". It erases everything stored on the device immediately, with no request needed.

## Per data type

| Data type | Collected | Shared | Notes |
|---|---|---|---|
| Name, email, address, phone number | No | No | Never asked for. |
| Contacts | No | No | Read on-device only if you grant Contacts, to skip known callers. Never leaves the device. |
| Messages / SMS | No | No | Notification text is read on-device for auto-check only. The SMS store is never read. |
| Photos / videos | No | No | A chosen screenshot is read on-device for OCR. Never uploaded. |
| Files / docs | No | No | No file access. |
| Location | No | No | Not requested. |
| Health / fitness | No | No | Not requested. |
| Financial info | No | No | The app reads nothing from your bank. It only analyses text you paste or scan. |
| Device or other IDs | No | No | No advertising ID, no device ID. |
| App activity / analytics | No | No | No analytics SDK at all. |
| Crash logs / diagnostics | No | No | No crash reporting SDK. |

## Security practices

- Data is encrypted in transit: not applicable (nothing is transmitted by default).
- Data deletion: available in-app, immediate.
- Data is not transferred to third parties.
- The app is not directed at children under 13, but is safe for them.

## Optional feature disclosure

Online mode is OFF by default and off in the release build unless the user turns
it on and enters their own server address. When used, only the single message
text the user explicitly confirms is sent, to the address the user configured.
The release build has no INTERNET permission unless that build variant is
explicitly configured for it.
