package com.geisyanne.taskapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.fragment.app.Fragment
import com.geisyanne.taskapp.R

object NetworkUtils {

    fun showBottomSheetNoInternet(fragment: Fragment) {

            if (!isNetworkConnected(fragment.requireContext())) {
                fragment.showBottomSheet(
                    titleDialog = R.string.attention,
                    titleButton = android.R.string.ok,
                    message = fragment.getString(R.string.msg_no_internet)
                )
            }
    }

     private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}