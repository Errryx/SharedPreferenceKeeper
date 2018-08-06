package cmcm.keeper.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.concurrent.ConcurrentHashMap

class SharedPreferenceAccessor internal constructor(context: Context, name: String, flag: Int) : SharedPreferenceInterface {

    private val mListeners: MutableList<SharedPreferences.OnSharedPreferenceChangeListener> = ArrayList()
    private val mPreferencesMap: ConcurrentHashMap<String, Any?> = ConcurrentHashMap()
    private val mModified: ConcurrentHashMap<String, Any?> = ConcurrentHashMap()
    private var mSharedPreference: SharedPreferences = context.getSharedPreferences(name, flag)

    init {
        mSharedPreference.run {
            mPreferencesMap.putAll(all as Map<String, Any?>)
        }
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return get(key, default) as Boolean
    }

    override fun getInt(key: String, default: Int): Int {
        return get(key, default) as Int
    }

    override fun getFloat(key: String, default: Float): Float {
        return get(key, default) as Float
    }

    override fun getLong(key: String, default: Long): Long {
        return get(key, default) as Long
    }

    override fun getString(key: String, default: String?): String? {
        val value: Any? = get(key, default) ?: return null
        return value as String
    }

    override fun putBoolean(key: String, value: Boolean) {
        put(key, value)
    }

    override fun putInt(key: String, value: Int) {
        put(key, value)
    }

    override fun putLong(key: String, value: Long) {
        put(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        put(key, value)
    }

    override fun putString(key: String, value: String?) {
        put(key, value)
    }

    override fun contains(key: String): Boolean {
        return mPreferencesMap.contains(key) || mSharedPreference.contains(key)
    }

    override fun clear() {
        mPreferencesMap.clear()
        mModified.clear()
        mSharedPreference.edit().clear().commit()
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSharedPreference.registerOnSharedPreferenceChangeListener(listener)
        } else {
            synchronized(mListeners, {
                mListeners.add(listener)
            })
        }
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSharedPreference.unregisterOnSharedPreferenceChangeListener(listener)
        } else {
            synchronized(mListeners, {
                mListeners.remove(listener)
            })
        }
    }

    private fun notifyListeners(key: String) {
        synchronized(mListeners, {
            for (item in mListeners) {
                item.onSharedPreferenceChanged(mSharedPreference, key)
            }
        })
    }

    fun <T> get(key: String, default: T?): T? {
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
        return actual?.let {
            it as T
        }
    }

    private fun <T> put(key: String, value: T?) {
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
                it ?: flush()
            }
            notifyListeners(key)
        }
    }

    override fun flush() {
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