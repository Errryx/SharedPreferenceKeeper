package cmcm.keeper.sharedpreference.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

object KeeperDBAccessor {
    private const val SQL_SELECTION = "${KeeperDBHelper.COLUMN_KEY}=?"

    fun queryAll(context: Context, table: String): MutableMap<String, String> {
        var cursor: Cursor? = null
        val map = HashMap<String, String>()

        try {
            cursor = context.contentResolver.query(getUri(context, table), null, null, null, null)
            if (!cursor.moveToFirst()) {
                return map
            }

            val keyIndex = cursor.getColumnIndex(KeeperDBHelper.COLUMN_KEY)
            val valueIndex = cursor.getColumnIndex(KeeperDBHelper.COLUMN_VALUE)
            do {
                map[cursor.getString(keyIndex)] = cursor.getString(valueIndex)
            } while (cursor.moveToNext())
        } catch (e: Exception) {
        } finally {
            cursor?.close()
        }
        return map
    }

    fun query(context: Context, table: String, key: String): String? {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(getUri(context, table), null, SQL_SELECTION, arrayOf(key), null)
            if (cursor?.moveToFirst() == true) {
                return cursor.getString(cursor.getColumnIndex(KeeperDBHelper.COLUMN_VALUE))
            }
        } catch (e: Exception) {
        } finally {
            cursor?.close()
        }
        return null
    }

    fun insertOrUpdate(context: Context, table: String, key: String, value: String) {
        val contentValues = ContentValues().apply {
            put(KeeperDBHelper.COLUMN_KEY, key)
            put(KeeperDBHelper.COLUMN_VALUE, value)
        }

        val result = context.contentResolver.update(getUri(context, table), contentValues, SQL_SELECTION, arrayOf(key))
        if (result <= 0) {
            context.contentResolver.insert(getUri(context, table), contentValues)
        }
    }

    fun delete(context: Context, table: String, key: String) {
        context.contentResolver.delete(getUri(context, table), SQL_SELECTION, arrayOf(key))
    }

    fun clear(context: Context, table: String) {
        context.contentResolver.delete(getUri(context, table), null, null)
    }

    private fun getUri(context: Context, table: String): Uri {
        return Uri.Builder().scheme("content")
                .authority(("${context.packageName}.keeper"))
                .path(table)
                .build()
    }
}