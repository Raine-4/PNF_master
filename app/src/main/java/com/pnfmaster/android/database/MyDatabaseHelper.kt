package com.pnfmaster.android.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

// name 是数据库名
class MyDatabaseHelper(val context: Context, name: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {

    private val tag = "DBHelper"

    private val createUser =
        "CREATE TABLE IF NOT EXISTS User (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username VARCHAR(50)," +
                "password VARCHAR(20))"

    private val createUserInfo =
        "CREATE TABLE IF NOT EXISTS UserInfo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(50)," +
                "age INTEGER," +
                "gender INTEGER," +
                "phone VARCHAR(20))"

    private val createRehabInfo =
        "CREATE TABLE IF NOT EXISTS RehabInfo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "diagnosisInfo TEXT," +
                "treatPlan TEXT," +
                "progressRecord TEXT," +
                "goals TEXT)"

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.execSQL(createUser)
            db.execSQL(createUserInfo)
            db.execSQL(createRehabInfo)
        } else {
            Log.e(tag, "database is null.")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null) {
            db.execSQL("DROP TABLE IF EXISTS User")
            db.execSQL("DROP TABLE IF EXISTS UserInfo")
            db.execSQL("DROP TABLE IF EXISTS RehabInfo")
            onCreate(db)
        } else {
            Log.e(tag, "database is null.")
        }
    }

}