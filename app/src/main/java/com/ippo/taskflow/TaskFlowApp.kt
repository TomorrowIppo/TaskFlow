package com.ippo.taskflow

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class TaskFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}