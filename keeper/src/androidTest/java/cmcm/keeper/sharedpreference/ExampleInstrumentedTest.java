package cmcm.keeper.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String UNIT_TEST = "instrumented_test";
    private static final String UNIT_TEST_ANOTHER = "instrumented_test_another";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("cmcm.keeper.sharedpreference.test", appContext.getPackageName());
    }

    @Test
    public void testGetBoolean() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferenceAccessor accessor = SharedPreferenceKeeper.INSTANCE.getSharedPreference(appContext, UNIT_TEST, Context.MODE_PRIVATE);

        assertTrue(accessor.getBoolean("test_boolean", true));
        assertFalse(accessor.getBoolean("test_boolean", false));
        accessor.putBoolean("test_boolean", false);
        assertFalse(accessor.getBoolean("test_boolean", true));

        accessor.clear();
    }

    @Test
    public void testGetString() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferenceAccessor accessor = SharedPreferenceKeeper.INSTANCE.getSharedPreference(appContext, UNIT_TEST, Context.MODE_PRIVATE);

        assertEquals("default", accessor.getString("test_string", "default"));
        assertEquals(null, accessor.getString("test_string", null));
        accessor.putString("test_string", "value");
        assertEquals("value", accessor.getString("test_string", null));

        accessor.clear();
    }

    @Test
    public void testGetPut() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferenceAccessor accessor = SharedPreferenceKeeper.INSTANCE.getSharedPreference(appContext, UNIT_TEST, Context.MODE_PRIVATE);

        accessor.putBoolean("key_boolean", true);
        accessor.putInt("key_int", Integer.MAX_VALUE);
        accessor.putLong("key_long", Long.MAX_VALUE);
        accessor.putFloat("key_float", Float.MAX_VALUE);
        accessor.putString("key_string", String.class.getSimpleName());

        assertEquals(accessor.getBoolean("key_boolean", false), true);
        assertEquals(accessor.getInt("key_int", Integer.MIN_VALUE), Integer.MAX_VALUE);
        assertEquals(accessor.getLong("key_long", Long.MIN_VALUE), Long.MAX_VALUE);
        assertEquals(accessor.getFloat("key_float", Float.MIN_VALUE), Float.MAX_VALUE, 0.1f);
        assertEquals(accessor.getString("key_string", null), String.class.getSimpleName());

        accessor.clear();
    }

    @Test
    public void testSP() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferences sp = appContext.getSharedPreferences(UNIT_TEST, Context.MODE_PRIVATE);
        SharedPreferenceAccessor accessor = SharedPreferenceKeeper.INSTANCE.getSharedPreference(appContext, UNIT_TEST, Context.MODE_PRIVATE);

        accessor.putString("test_string", "value");
        assertEquals("value", accessor.getString("test_string", null));
        assertNotEquals(sp.getString("test_string", null), accessor.getString("test_string", null));
        SharedPreferenceKeeper.INSTANCE.writeToDisk();
        assertEquals(sp.getString("test_string", null), accessor.getString("test_string", null));

        accessor.clear();
    }

    @Test
    public void testMultiModification() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferenceAccessor accessor = SharedPreferenceKeeper.INSTANCE.getSharedPreference(appContext, UNIT_TEST, Context.MODE_PRIVATE);
        SharedPreferences sp = appContext.getSharedPreferences(UNIT_TEST, Context.MODE_PRIVATE);

        long stamp = System.currentTimeMillis();
        int i = 0;
        for (; i < 100; i++) {
            accessor.putString("test_string", "value_" + i);
        }
        SharedPreferenceKeeper.INSTANCE.writeToDisk();
        Log.i(getClass().getSimpleName(), "SharedPreferenceAccessor performance " + (System.currentTimeMillis() - stamp));
        assertEquals("value_" + (i - 1), accessor.getString("test_string", null));
        assertEquals("value_" + (i - 1), sp.getString("test_string", null));

        SharedPreferences.Editor editor = sp.edit();
        stamp = System.currentTimeMillis();
        i = 0;
        for (; i < 100; i++) {
            editor.putString("test_string", "value_" + i).apply();
        }
        editor.commit();
        Log.i(getClass().getSimpleName(), "SharedPreferences performance " + (System.currentTimeMillis() - stamp));

        accessor.clear();
    }

    @Test
    public void testMultiAccessors() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferenceAccessor accessor = SharedPreferenceKeeper.INSTANCE.getSharedPreference(appContext, UNIT_TEST, Context.MODE_PRIVATE);
        SharedPreferenceAccessor accessorAnother = SharedPreferenceKeeper.INSTANCE.getSharedPreference(appContext, UNIT_TEST_ANOTHER, Context.MODE_PRIVATE);

        SharedPreferenceAccessor accessorTest = SharedPreferenceKeeper.INSTANCE.getSharedPreference(appContext, UNIT_TEST, Context.MODE_PRIVATE);
        SharedPreferenceAccessor accessorAnotherTest = SharedPreferenceKeeper.INSTANCE.getSharedPreference(appContext, UNIT_TEST_ANOTHER, Context.MODE_PRIVATE);

        assertTrue(accessor == accessorTest);
        assertTrue(accessorAnother == accessorAnotherTest);
        assertTrue(accessor != accessorAnother);

        SharedPreferences sp = appContext.getSharedPreferences(UNIT_TEST, Context.MODE_PRIVATE);
        SharedPreferences spAnother = appContext.getSharedPreferences(UNIT_TEST_ANOTHER, Context.MODE_PRIVATE);

        accessor.putString("test_string", "value");
        assertEquals("value", accessor.getString("test_string", null));
        assertNotEquals(sp.getString("test_string", null), accessor.getString("test_string", null));


        accessorAnother.putString("test_string_another", "value");
        assertEquals("value", accessorAnother.getString("test_string_another", null));
        assertNotEquals(spAnother.getString("test_string_another", null), accessorAnother.getString("test_string_another", null));


        SharedPreferenceKeeper.INSTANCE.writeToDisk();
        assertEquals(sp.getString("test_string", null), accessor.getString("test_string", null));
        assertEquals(spAnother.getString("test_string_another", null), accessorAnother.getString("test_string_another", null));

        accessor.clear();
        accessorAnother.clear();
    }
}
