package cmcm.keeper.sharedpreference

import android.content.SharedPreferences

interface SharedPreferenceInterface {

    fun getBoolean(key: String, default: Boolean = false): Boolean

    fun getInt(key: String, default: Int = 0): Int

    fun getFloat(key: String, default: Float = 0F): Float

    fun getLong(key: String, default: Long = 0L): Long

    fun getString(key: String, default: String? = null): String?

    fun putBoolean(key: String, value: Boolean = false)

    fun putInt(key: String, value: Int = 0)

    fun putLong(key: String, value: Long = 0L)

    fun putFloat(key: String, value: Float = 0F)

    fun putString(key: String, value: String? = null)

    fun contains(key: String): Boolean

    fun clear()

    fun flush()

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)
}