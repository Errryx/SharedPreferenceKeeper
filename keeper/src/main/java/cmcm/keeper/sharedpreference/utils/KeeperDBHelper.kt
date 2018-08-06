package cmcm.keeper.sharedpreference.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class KeeperDBHelper(context: Context, name: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {

    companion object {
        internal const val COLUMN_KEY = "sp_key"
        internal const val COLUMN_VALUE = "value"
    }

    override fun onCreate(sqliteDatabse: SQLiteDatabase?) {
    }

    override fun onUpgrade(sqliteDatabse: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}