# Advanced capabilities

## Build and configure

Use Java 21, the pinned Gradle 9.5 distribution, Android platform 37.0 and build tools 36.0.0.

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest
```

The cloud environment's retained toolchains can be activated with `source /workspace/.setup/env.sh`. There, use `gradle -Dorg.gradle.java.installations.paths="$JAVA_HOME"` in place of `./gradlew` to use its proxy and verified cached distribution.

Supply the reachable API URL with `-PapiBaseUrl=https://your-api.example/` (trailing slash required). Debug builds permit local emulator HTTP, e.g. `-PapiBaseUrl=http://10.0.2.2:5080/`; release builds require HTTPS. No certificate validation is disabled. The existing default points to local emulator HTTPS.

Use your Firebase project's `app/google-services.json`. Enable the Google/Microsoft providers in Firebase Authentication and configure the Android package and signing SHA fingerprints. Download updated Google configuration after configuring OAuth. A Google web OAuth client ID must be present in that configuration or supplied with `-PgoogleWebClientId=...`. An absent ID shows an actionable sign-in error. Microsoft uses Firebase's native provider flow. These are real sign-ins; they require configured providers and the backend to validate the Firebase token.

## Biometric session protection

Sign in with SSO first. In Settings, enable biometrics and save. Android's native BiometricPrompt accepts a strong fingerprint or face authenticator. Success authorizes an AES-GCM operation with a per-use Android Keystore key; a callback alone cannot decrypt the token. Tokens are never stored in plaintext in DataStore. Existing plaintext sessions migrate to the vault on startup.

The app hides its content and clears the unlocked token when it backgrounds. Returning to it requires another biometric operation. Cancellation keeps the content hidden and offers retry or sign-out. Enrollment changes invalidate the key; sign out and authenticate through SSO to enroll again. Unsupported hardware/enrollment cannot enable protection. Disabling protection requires an already unlocked session. Screenshots and app backups are disabled to protect session data.

Biometrics unlock an existing account, rather than creating or impersonating an SSO identity. While biometric protection is locked, authenticated background sync waits until the next successful unlock; unprotected sessions can sync in the background normally.

## Offline delivery requests

Delivery creation is the supported offline feature. Every action is written to Room before sending. WorkManager runs with network constraints and exponential retry, surviving process death and restart. The same UUID is reused for every attempt, and the API uses that UUID as the delivery ID. A lost success response cannot create a second delivery.

Queues are scoped to the signed-in user. Only confirmed successes are removed. Connection, server, throttling, and authentication failures remain queued; permanent HTTP validation failures are retained with an error. The home screen shows pending/rejected counts and opens the saved request descriptions and error codes. Rejected actions need correction and a new submission. Successful feed refreshes replace the Room cache transactionally; offline reads show the actual cache, without demo tasks. The version 1 → 2 Room migration preserves existing queues.

Existing handoff sync acknowledges only records absent from the server's failed-ID list. Failed handoffs remain local; they no longer get silently marked as synced. Cancellation is propagated.

## Push notifications

The FirebaseMessagingService handles data messages in foreground and background. A WorkManager job registers/refreshes the FCM token with the authenticated API after sign-in, unlock, token rotation, and preference changes. Sign-out removes the device registration when the network allows. Messages are also checked against the current account locally, so a failed unregister does not display another account's alerts. Delivery preferences and Android notification permission are respected.

The API saves `delivery_created` / `delivery_fulfilled` events in its database outbox in the same transaction as the delivery change. The dispatcher polls every five seconds, sends high-priority FCM data messages, retries provider errors, and removes unregistered tokens. Delivery actions remain saved during an FCM outage. After ten failed attempts an event is retained for operator inspection; reset its Attempts after fixing provider configuration to retry. Multi-instance dispatch is at-least-once; duplicate notifications replace the same delivery's notification.

FCM payloads carry event/account/delivery IDs, not delivery descriptions or credentials. Notification wording is rendered on-device in the selected language; notification taps enter the activity's biometric gate.

See the API's `ADVANCED_CAPABILITIES.md` for server configuration. FCM needs a real Firebase Admin credential and a device token from this app; debug compilation alone does not validate delivery.

## Languages and accessibility

English, isiZulu (`zu`) and Afrikaans (`af`) are declared in Android's per-app language configuration. Selection from login, settings, or Android app language settings is persisted by the platform. All app string keys have both translations, including biometric prompts, notification text, offline status, forms, and accessibility labels. Marketplace categories/conditions and vehicle names use localized labels. User-entered descriptions and place names are preserved.

## Device acceptance checks

On an API 37+ emulator/device, run `./gradlew :app:connectedDebugAndroidTest`. Instrumentation tests cover Room outbox persistence across database reopen and encrypted vault persistence. They use a test installation and clear that installation's session.

Also verify these native behaviors manually:

1. Enroll a fingerprint/strong face, sign in, enable biometrics, background/reopen, cancel/retry, rotate the screen, and change biometric enrollment. Confirm no protected content is exposed before authentication.
2. Load the feed online, disconnect networking, post a delivery, terminate/relaunch, reconnect, and verify the queue drains once with one server delivery. Repeat while protection is locked, then unlock.
3. Grant notification permission, register the device, background the app, and create/complete a delivery. Verify the FCM alert in foreground and background. Disable alerts, deny permission, and sign out to verify suppression.
4. Select isiZulu/Afrikaans, restart the app, and verify login/settings/forms, TalkBack labels, and notification text use the selected language.

No device was attached in the cloud workspace; biometric hardware, device persistence, and real FCM delivery require these checks.
