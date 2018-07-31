# SharedPreferenceKeeper

A library which wrapps the Android SharedPreferences API to avoid ANR when SharedPreferences#Editor.apply has been called too much under Android 8.0 (or you can just optimize your code to fix the problem, which will be better).

The idea is that we kept the whole preferences map in memory and only commit once as it should be, for now this happens at Activity's PRE onDestroy. This is not the best choice but it is straightforward, the biggest problem is that according to the AOSP, all preferences should be written at Activity's POST onStop (pls refer to ActivityThread and QueuedWork), moreover written should also been ensured at Service#onStartCommand and Service#onDestroy
