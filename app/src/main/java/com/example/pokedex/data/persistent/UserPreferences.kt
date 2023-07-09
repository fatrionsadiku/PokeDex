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

enum class SortOrder {
    BY_ID_DESCENDING,
    BY_ID_ASCENDING
}
data class RedirectToDetails(val redirectState: RedirectState)
data class HideDetailsState(val detailsState: HideDetails)
data class PokemonSortOrder(val sortOrder: SortOrder)

private val PREFERENCES_KEY = stringPreferencesKey("redirect_to_details")
private val HIDE_DETAILS_KEY = stringPreferencesKey("hide_details")
private val SORT_ORDER_KEY = stringPreferencesKey("sort_order")

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
    val sortOrderFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException){
                emit(emptyPreferences())
            }
        }
        .map { sortOrder ->
            val sortOrder = SortOrder.valueOf(
                sortOrder[SORT_ORDER_KEY] ?: SortOrder.BY_ID_ASCENDING.name
            )
            PokemonSortOrder(sortOrder)
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
    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStore.edit {preferences ->
            preferences[SORT_ORDER_KEY] = sortOrder.name
        }
    }

}























