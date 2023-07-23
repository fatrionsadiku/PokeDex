package com.brightblade.pokedex

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.brightblade.pokedex.data.persistent.SplashScreenAnimation
import com.brightblade.pokedex.data.persistent.UserPreferences
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HiltApplication : Application(), LifecycleObserver {
    @Inject
    lateinit var preference: UserPreferences
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifeCycleEventObserver)
    }

    private var lifeCycleEventObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP) {
            CoroutineScope(Dispatchers.IO).launch {
                preference.updateSplashScreenAnimationState(SplashScreenAnimation.PLAYANIMATION)
            }
        }
    }
}