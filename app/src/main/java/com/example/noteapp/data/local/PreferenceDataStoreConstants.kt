package com.example.noteapp.data.local

import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceDataStoreConstants {
    val LAST_NOTE_NAME = stringPreferencesKey("last_note_name")
}