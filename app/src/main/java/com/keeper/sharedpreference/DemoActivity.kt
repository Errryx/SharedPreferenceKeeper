package com.keeper.sharedpreference

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import cmcm.keeper.sharedpreference.SharedPreferenceKeeper

class DemoActivity : Activity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessor = SharedPreferenceKeeper.getSharedPreference(applicationContext, "demo")

        accessor.registerOnSharedPreferenceChangeListener(this)
        var test = accessor.getBoolean("a_boolean", false)
        Log.d(javaClass.simpleName, "A boolean default: $test")

        accessor.putBoolean("a_boolean", true)
        test = accessor.getBoolean("a_boolean", false)
        Log.d(javaClass.simpleName, "A boolean default updated: $test")
    }

    override fun onDestroy() {
        super.onDestroy()
        val accessor = SharedPreferenceKeeper.getSharedPreference(applicationContext, "demo")
        accessor.clear()
        accessor.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        Log.d(javaClass.simpleName, "onSharedPreferenceChanged: $p1")
    }
}