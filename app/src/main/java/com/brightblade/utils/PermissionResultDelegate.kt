package com.brightblade.utils

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class PermissionResultDelegate<R : ActivityResultLauncher<String>>(
    private val fragment: Fragment,
    private val permission: String,
    private val granted: (permission: String) -> Unit,
    private val denied: (permission: String) -> Unit,
    private val explained: (permission: String) -> Unit,
) : ReadOnlyProperty<Fragment, R> {

    private var permissionResult: ActivityResultLauncher<String>? = null


    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.apply {
                    permissionResult = registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted: Boolean ->

                        when {
                            isGranted                                        -> granted(permission)
                            shouldShowRequestPermissionRationale(permission) -> denied(permission)
                            else                                             -> explained(permission)
                        }
                    }
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                permissionResult = null
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): R {
        permissionResult?.let { return (it as R) }

        error("Failed to Initialize Permission")
    }
}