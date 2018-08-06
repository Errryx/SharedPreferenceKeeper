package com.keeper.sharedpreference

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import cmcm.keeper.sharedpreference.SharedPreferenceKeeper

class DemoActivity : Activity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessor = SharedPreferenceKeeper.getSharedPreference(applicationContext, "demo", Context.MODE_PRIVATE)

        accessor.registerOnSharedPreferenceChangeListener(this)
        accessor.getBoolean("a_boolean", false)
        accessor.putBoolean("a_boolean", true)

        val mpAccessor = SharedPreferenceKeeper.getSharedPreference(applicationContext, "demo", Context.MODE_MULTI_PROCESS)
        mpAccessor.registerOnSharedPreferenceChangeListener(this)
        mpAccessor.getBoolean("a_boolean", false)
        mpAccessor.putBoolean("a_boolean", true)
    }

    override fun onDestroy() {
        super.onDestroy()
        val accessor = SharedPreferenceKeeper.getSharedPreference(applicationContext, "demo")
        accessor.clear()
        accessor.unregisterOnSharedPreferenceChangeListener(this)

        val mpAccessor = SharedPreferenceKeeper.getSharedPreference(applicationContext, "demo", Context.MODE_MULTI_PROCESS)
        mpAccessor.clear()
        mpAccessor.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        Log.d(javaClass.simpleName, "onSharedPreferenceChanged: $p1")
    }
}