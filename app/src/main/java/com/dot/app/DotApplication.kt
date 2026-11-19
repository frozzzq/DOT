package com.dot.app

import android.app.Application
import com.dot.app.core.notifications.DotNotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DotApplication : Application() {

    @Inject
    lateinit var notificationHelper: DotNotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannelsCreated()
    }
}
