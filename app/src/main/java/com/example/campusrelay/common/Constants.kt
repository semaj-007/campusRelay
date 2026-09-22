package com.example.campusrelay.common

/**
 * Central place for values that show up in more than one layer of the app.
 *
 * NOTE ON [BASE_URL]: this points at the placeholder Azure App Service host from the
 * Part 1 design doc's REST API section. Swap it for the real deployment URL once the
 * ASP.NET Core Web API backend is deployed, or point it at a local emulator/staging
 * instance (e.g. "http://10.0.2.2:5000/") while you build the backend out.
 */
object Constants {
    const val BASE_URL = "https://10.0.2.2:5001/"
    // For production, change to: "https://campusrelay-api.azurewebsites.net/"

    const val PREFS_DATASTORE_NAME = "campus_relay_prefs"
    const val DATABASE_NAME = "campus_relay.db"

    // REQ-DEL-3: match requests to offers whose route passes within this radius.
    const val DELIVERY_MATCH_RADIUS_KM = 1.0

    // REQ-LANG-1
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_ZULU = "zu"
    const val LANGUAGE_AFRIKAANS = "af"

    const val SYNC_WORK_NAME = "campus_relay_offline_sync"

    const val WEIGHT_CATEGORY_SMALL = "Small"
    const val WEIGHT_CATEGORY_MEDIUM = "Medium"
    const val WEIGHT_CATEGORY_LARGE = "Large"
}
