package com.brightblade.pokedex

import android.app.Application
import android.util.Log
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
        when (event) {
            Lifecycle.Event.ON_CREATE  -> {
                Log.d("ApplicationClass", "ON_CREATE?")
            }

            Lifecycle.Event.ON_START   -> {
                Log.d("ApplicationClass", "ON_START?")
            }

            Lifecycle.Event.ON_RESUME  -> {
                Log.d("ApplicationClass", "ON_RESUME?")
            }

            Lifecycle.Event.ON_PAUSE   -> {
                Log.d("ApplicationClass", "ON_PAUSE?")
            }

            Lifecycle.Event.ON_STOP    -> {
                Log.d("ApplicationClass", "ON_STOP?")
                CoroutineScope(Dispatchers.IO).launch {
                    preference.updateSplashScreenAnimationState(SplashScreenAnimation.PLAYANIMATION)
                }
            }

            Lifecycle.Event.ON_DESTROY -> {
                Log.d("ApplicationClass", "ON_DESTROY?")
                CoroutineScope(Dispatchers.IO).launch {
                    preference.updateSplashScreenAnimationState(SplashScreenAnimation.PLAYANIMATION)
                }
            }

            Lifecycle.Event.ON_ANY     -> {
                Log.d("ApplicationClass", "ON_ANY?")
            }
        }
    }
}