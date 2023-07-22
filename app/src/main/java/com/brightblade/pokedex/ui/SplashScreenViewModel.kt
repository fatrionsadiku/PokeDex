package com.brightblade.pokedex.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightblade.pokedex.data.persistent.SplashScreenAnimation
import com.brightblade.pokedex.data.persistent.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val preferences: UserPreferences
) : ViewModel() {
    val shouldSplashScreenAnimate = preferences.shouldSplashScreenAnimate

    fun onSplashScreenAnimationStateChange(shouldAnimation: SplashScreenAnimation) = viewModelScope.launch {
        preferences.updateSplashScreenAnimationState(shouldAnimation)
    }
}