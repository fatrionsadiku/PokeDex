package com.example.pokedex.data.persistent

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
enum class HideDetails {
    SHOW_ONLY_POKEMON,
    SHOW_ALL_DETAILS
}
data class RedirectToDetails(val redirectState: RedirectState)
data class HideDetailsState(val detailsState: HideDetails)

private val PREFERENCES_KEY = stringPreferencesKey("redirect_to_details")
private val HIDE_DETAILS_KEY = stringPreferencesKey("hide_details")

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val hideDetailsFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException){
                emit(emptyPreferences())
            }
        }
        .map { state ->
            val hideDetailsState = HideDetails.valueOf(
                state[HIDE_DETAILS_KEY] ?: HideDetails.SHOW_ALL_DETAILS.name
            )
            HideDetailsState(hideDetailsState)
        }

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
    suspend fun updateDetailsState(detailsState: HideDetails){
        dataStore.edit {preferences ->
            preferences[HIDE_DETAILS_KEY] = detailsState.name
        }
    }

}























