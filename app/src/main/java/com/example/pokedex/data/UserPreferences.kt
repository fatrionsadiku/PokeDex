package com.example.pokedex.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class RedirectState {
    REDIRECT_TO_DETAILS,
    REDIRECT_TO_EVOTREE
}

data class RedirectToDetails(val redirectState: RedirectState)

private val PREFERENCES_KEY = stringPreferencesKey("redirect_to_details")

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException){
                emit(emptyPreferences())
            }
        }
        .map { preferences ->
            val redirectState = RedirectState.valueOf(
                preferences[PREFERENCES_KEY] ?: RedirectState.REDIRECT_TO_DETAILS.name
            )
            RedirectToDetails(redirectState)
        }

    suspend fun updateRedirectState(redirectState: RedirectState){
        dataStore.edit {preferences ->
            preferences[PREFERENCES_KEY] = redirectState.name
        }
    }

}























