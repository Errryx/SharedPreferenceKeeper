package cmcm.keeper.sharedpreference

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.util.concurrent.ConcurrentHashMap

object SharedPreferenceKeeper {

    private var mRegistered = false
    private val mAccessorMap: ConcurrentHashMap<String, SharedPreferenceAccessor> = ConcurrentHashMap()

    fun getSharedPreference(context: Context, name: String, flag: Int = Context.MODE_PRIVATE): SharedPreferenceAccessor {
        var accessor = mAccessorMap[name]
        if (accessor == null) {
            accessor = SharedPreferenceAccessor(context, name, flag)
            mAccessorMap[name] = accessor
            registerLifeCycleCallback(context)
        }
        return accessor
    }

    fun writeToDisk() {
        for (item in mAccessorMap.values) {
            item.writeToDisk()
        }
    }

    @Synchronized
    private fun registerLifeCycleCallback(context: Context) {
        if (mRegistered) {
            return
        }
        mRegistered = true
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
            }

            override fun onActivityStarted(p0: Activity?) {
            }

            override fun onActivityResumed(p0: Activity?) {
            }

            override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
            }

            override fun onActivityPaused(p0: Activity?) {
            }

            override fun onActivityStopped(p0: Activity?) {
            }

            override fun onActivityDestroyed(p0: Activity?) {
                writeToDisk()
            }
        })
    }
}