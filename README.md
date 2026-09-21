# CampusRelay — Android prototype

This is the Part 2 prototype scaffold for the CampusRelay POE (PROG7314), built from the
Part 1 planning & design document into the blank Android Studio project.

## Stack

- **Language / UI**: Kotlin, traditional Views (Fragments + ViewBinding + Navigation
  Component), not Jetpack Compose. The Part 1 doc's conclusion mentions Compose, but the
  blank project you uploaded was generated as an Empty *Views* Activity (AppCompatActivity
  + XML layout), and Compose needs its own Kotlin compiler plugin whose version has to be
  paired with whatever Kotlin version this AGP 9 "built-in Kotlin" setup resolves to at
  build time — something I can't verify without a live Gradle/Android SDK environment. So
  I built on top of what you gave me instead of guessing that pairing. Converting the UI
  layer to Compose later is a contained, mechanical change if you'd rather have it.
- **Networking**: Retrofit + OkHttp + Gson, matching the two documented endpoints
  (`POST /api/v1/deliveries`, `POST /api/v1/deliveries/sync-offline`) plus a couple of
  natural additions (`POST /api/v1/auth/sso`, `GET /api/v1/deliveries/feed`).
- **Local persistence**: RoomDB (via KSP) for the offline delivery cache and two offline
  queues (queued handoffs, queued delivery-request creations), DataStore for session +
  settings.
- **Background sync**: WorkManager (`OfflineSyncWorker`), constrained to run once the
  device has connectivity.
- **Other**: androidx.biometric for REQ-BIO-1/2, a local notification as a stand-in for
  Firebase Cloud Messaging, multi-language strings (English / isiZulu / Afrikaans).

## Before you run it

1. **Backend URL**: `common/Constants.kt` → `BASE_URL` is a placeholder
   (`https://campusrelay-api.azurewebsites.net/`). Point it at your real ASP.NET Core API
   once it's deployed (or a local/staging host) — everything falls back to on-device demo
   data automatically while that call fails, so the app is fully click-through-able either
   way.
2. **First Gradle sync**: this project uses AGP 9's built-in Kotlin (no
   `org.jetbrains.kotlin.android` plugin) plus KSP 2.3.4 for Room's annotation processing.
   I picked versions that should be compatible, but I can't run a real Gradle sync in this
   environment to confirm — if Android Studio's sync flags a KSP/Kotlin version mismatch,
   accept its suggested "Upgrade Assistant" fix; it's a one-line version bump in
   `libs.versions.toml`, nothing structural.
3. **SSO / Firebase**: REQ-AUTH's Microsoft Entra ID (Azure AD) sign-in and REQ-NOT-1's
   Firebase Cloud Messaging both need registered app credentials you don't have yet, so
   both are simulated for now (`AuthRepository.signInWithSso`, `NotificationHelper`) —
   each has a comment explaining exactly what to swap in.

## Known gaps (by design, for a prototype)

- Marketplace and carpooling (REQ-CAR-*, listings) aren't wired into the UI yet — only
  delivery requests are, since that's the flow the wizard/home-feed screens in Part 1
  cover in detail. The data models for all three exist in `data/model/`.
- No runtime permission prompts yet for location / notifications (`ACCESS_FINE_LOCATION`,
  `POST_NOTIFICATIONS`) — they're declared in the manifest but not requested at runtime.
- The Home Dashboard's "map canvas" is a list placeholder, not a real map view.
