package com.prexoft.prexocore.helper

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.prexoft.prexocore.loop

fun loop(repeat: Int, safeMode: Boolean = true, action: (Int) -> Unit) {
    repeat.loop(safeMode, action)
}

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun LifecycleOwner.observeNetworkStatus(
    context: Context,
    onStatusChanged: (Boolean) -> Unit
) {
    val liveData = MutableLiveData<Boolean>()
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            liveData.postValue(true)
        }

        override fun onLost(network: Network) {
            liveData.postValue(false)
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            liveData.postValue(hasInternet)
        }
    }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    liveData.observe(this, Observer(onStatusChanged))
    connectivityManager.registerNetworkCallback(networkRequest, callback)

    lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    })
}