package cmcm.keeper.sharedpreference.providers

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import cmcm.keeper.sharedpreference.utils.KeeperDBHelper
import java.net.URLEncoder

class SharedPreferenceProvider : ContentProvider() {

    companion object {
        private const val FILE_NAME = "keeper.db"
        private const val VERSION = 1
    }

    private lateinit var keeperDBHelper: KeeperDBHelper
    private var tables = ArrayList<String>()

    override fun onCreate(): Boolean {
        keeperDBHelper = KeeperDBHelper(context, FILE_NAME, VERSION)
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        return getDatabase(uri.lastPathSegment).query(uri.lastPathSegment, projection, selection, selectionArgs, null, null, sortOrder)
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri {
        val id = getDatabase(uri.lastPathSegment, true).insertWithOnConflict(uri.lastPathSegment, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
        if (id > 0) {
            notifyChange(uri, contentValues?.getAsString(KeeperDBHelper.COLUMN_KEY), URLEncoder.encode(contentValues?.getAsString(KeeperDBHelper.COLUMN_VALUE)))
        }
        return ContentUris.withAppendedId(uri, id)
    }

    override fun update(uri: Uri, contentValues: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return getDatabase(uri.lastPathSegment).update(uri.lastPathSegment, contentValues, selection, selectionArgs).also {
            if (it > 0) {
                notifyChange(uri, contentValues?.getAsString(KeeperDBHelper.COLUMN_KEY), URLEncoder.encode(contentValues?.getAsString(KeeperDBHelper.COLUMN_VALUE)))
            }
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return getDatabase(uri.lastPathSegment).delete(uri.lastPathSegment, selection, selectionArgs).also {
            if (it > 0) {
                notifyChange(uri, selectionArgs?.get(0), null)
            }
        }
    }

    override fun getType(uri: Uri?): String {
        return uri?.lastPathSegment ?: String()
    }

    private fun notifyChange(uri: Uri, key: String?, value: String?) {
        context.contentResolver.notifyChange(Uri.Builder()
                .scheme("content")
                .authority(("${context.packageName}.keeper"))
                .path(uri.lastPathSegment)
                .appendQueryParameter(key, value)
                .build(),
                null)
    }

    private fun getDatabase(table: String, writable: Boolean = false): SQLiteDatabase {
        synchronized(tables, {
            if (tables.indexOf(table) == -1) {
                keeperDBHelper.writableDatabase?.execSQL("CREATE TABLE IF NOT EXISTS $table(${KeeperDBHelper.COLUMN_KEY} text primary key, " + "${KeeperDBHelper.COLUMN_VALUE} text)")
                tables.add(table)
            }
        })
        return if (writable) keeperDBHelper.writableDatabase else keeperDBHelper.readableDatabase
    }
}