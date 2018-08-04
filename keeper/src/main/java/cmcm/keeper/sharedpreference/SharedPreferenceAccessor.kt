package cmcm.keeper.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.concurrent.ConcurrentHashMap

class SharedPreferenceAccessor internal constructor(context: Context, name: String, flag: Int) {

    private val mListeners: MutableList<SharedPreferences.OnSharedPreferenceChangeListener> = ArrayList()
    private val mPreferencesMap: ConcurrentHashMap<String, Any?> = ConcurrentHashMap()
    private val mModified: ConcurrentHashMap<String, Any?> = ConcurrentHashMap()
    private var mSharedPreference: SharedPreferences = context.getSharedPreferences(name, flag)

    init {
        mSharedPreference.run {
            mPreferencesMap.putAll(all as Map<String, Any?>)
        }
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return get(key, default) as Boolean
    }

    fun getInt(key: String, default: Int = 0): Int {
        return get(key, default) as Int
    }

    fun getFloat(key: String, default: Float = 0F): Float {
        return get(key, default) as Float
    }

    fun getLong(key: String, default: Long = 0L): Long {
        return get(key, default) as Long
    }

    fun getString(key: String, default: String? = null): String? {
        val value: Any? = get(key, default) ?: return null
        return value as String
    }

    fun putBoolean(key: String, value: Boolean = false) {
        put(key, value)
    }

    fun putInt(key: String, value: Int = 0) {
        put(key, value)
    }

    fun putLong(key: String, value: Long = 0L) {
        put(key, value)
    }

    fun putFloat(key: String, value: Float = 0F) {
        put(key, value)
    }

    fun putString(key: String, value: String? = null) {
        put(key, value)
    }

    fun contains(key: String): Boolean {
        return mPreferencesMap.contains(key) || mSharedPreference.contains(key)
    }

    fun clear() {
        mPreferencesMap.clear()
        mModified.clear()
        mSharedPreference.edit().clear().commit()
    }

    @Synchronized
    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSharedPreference.registerOnSharedPreferenceChangeListener(listener)
        } else {
            mListeners.add(listener)
        }
    }

    @Synchronized
    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSharedPreference.unregisterOnSharedPreferenceChangeListener(listener)
        } else {
            mListeners.remove(listener)
        }
    }

    @Synchronized
    private fun notifyListeners(key: String) {
        for (item in mListeners) {
            item.onSharedPreferenceChanged(mSharedPreference, key)
        }
    }

    fun get(key: String, default: Any?): Any? {
        var actual = mPreferencesMap[key]
        if (actual == null) {
            when (default) {
                null -> actual = mSharedPreference.getString(key, null)
                is String -> actual = mSharedPreference.getString(key, default)
                is Boolean -> actual = mSharedPreference.getBoolean(key, default)
                is Int -> actual = mSharedPreference.getInt(key, default)
                is Long -> actual = mSharedPreference.getLong(key, default)
                is Float -> actual = mSharedPreference.getFloat(key, default)
            }
            if (mSharedPreference.contains(key)) {
                mPreferencesMap[key] = actual
            }
        }
        return actual
    }

    private fun put(key: String, value: Any?) {
        mPreferencesMap[key] = value

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val editor = mSharedPreference.edit()
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                null -> editor.remove(key)
            }
            editor.apply()
        } else {
            mModified[key] = value.also {
                it ?: writeToDisk()
            }
            notifyListeners(key)
        }
    }

    internal fun writeToDisk() {
        if (mModified.size == 0) {
            return
        }

        val editor = mSharedPreference.edit()
        for (key in mModified.keys()) {
            val value = mModified[key]
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                null -> editor.remove(key)
            }
        }
        mModified.clear()
        editor.commit()
    }

}