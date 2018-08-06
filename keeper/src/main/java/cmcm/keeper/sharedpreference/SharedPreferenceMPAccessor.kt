package cmcm.keeper.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import android.database.ContentObserver
import android.net.Uri
import cmcm.keeper.sharedpreference.utils.KeeperDBAccessor
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class SharedPreferenceMPAccessor internal constructor(context: Context, private val tableName: String, flag: Int) : SharedPreferenceInterface {

    private val appContext: Context = context.applicationContext
    private val mListeners: MutableList<SharedPreferences.OnSharedPreferenceChangeListener> = ArrayList()
    private val mPreferencesMap: ConcurrentHashMap<String, String?> = ConcurrentHashMap()

    init {
        KeeperDBAccessor.queryAll(appContext, tableName)
                .forEach { (key, value) ->
                    mPreferencesMap[key] = value
                }.also {
                    appContext.contentResolver.registerContentObserver(
                            Uri.Builder().scheme("content")
                                    .authority(("${appContext.packageName}.keeper"))
                                    .path(tableName)
                                    .build(),
                            true,
                            KeeperObserver())
                }
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return get(key, default)!!
    }

    override fun getInt(key: String, default: Int): Int {
        return get(key, default)!!
    }

    override fun getFloat(key: String, default: Float): Float {
        return get(key, default)!!
    }

    override fun getLong(key: String, default: Long): Long {
        return get(key, default)!!
    }

    override fun getString(key: String, default: String?): String? {
        return get(key, default)
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
        if (mPreferencesMap.contains(key)) {
            return true
        }
        val value = KeeperDBAccessor.query(appContext, tableName, key)
        return value == null || value == "null"
    }

    override fun clear() {
        mPreferencesMap.clear()
        KeeperDBAccessor.clear(appContext, tableName)
    }

    override fun flush() {
        // Nothing to do
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        synchronized(mListeners, {
            mListeners.add(listener)
        })
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        synchronized(mListeners, {
            mListeners.remove(listener)
        })
    }

    private fun notifyListeners(key: String) {
        synchronized(mListeners, {
            for (item in mListeners) {
                item.onSharedPreferenceChanged(null, key)
            }
        })
    }

    private fun <T> get(key: String, default: T?): T? {
        var actual = mPreferencesMap[key]
        if (actual == null) {
            actual = KeeperDBAccessor.query(appContext, tableName, key)
            if (actual != null) {
                mPreferencesMap[key] = actual
            }
        }
        if (actual != null && actual != "null") {
            var result: Any? = null
            when (default) {
                null -> result = actual
                is Boolean -> result = actual.toBoolean()
                is Int -> result = actual.toIntOrNull()
                is Long -> result = actual.toLongOrNull()
                is Float -> result = actual.toFloatOrNull()
                is String -> result = actual
            }
            return result?.let {
                result as T
            } ?: default
        }

        return default
    }

    private fun <T> put(key: String, value: T?, sync: Boolean = false) {
        mPreferencesMap[key] = value.toString()
        notifyListeners(key)
        if (value == null) {
            KeeperDBAccessor.delete(appContext, tableName, key)
            return
        }
        if (sync) {
            KeeperDBAccessor.insertOrUpdate(appContext, tableName, key, value.toString())
        } else {
            thread(block = {
                KeeperDBAccessor.insertOrUpdate(appContext, tableName, key, value.toString())
            })
        }
    }

    fun onObserveChange(uri: Uri) {
        for (param in uri.queryParameterNames) {
            mPreferencesMap[param] = uri.getQueryParameter(param)
        }
    }

    inner class KeeperObserver : ContentObserver(null) {
        override fun deliverSelfNotifications(): Boolean {
            return false
        }

        override fun onChange(selfChange: Boolean, uri: Uri) {
            super.onChange(selfChange, uri)
            onObserveChange(uri)
        }
    }

}