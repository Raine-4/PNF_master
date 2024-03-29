package com.pnfmaster.android.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// name 是数据库名
class MyDatabaseHelper(val context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

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
        db!!.let {
            it.execSQL(createUser)
            it.execSQL(createUserInfo)
            it.execSQL(createRehabInfo)
        }
    }

    // 用不到
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion <= 1) {
            db!!.let {
                it.execSQL("DROP TABLE IF EXISTS User")
                it.execSQL("DROP TABLE IF EXISTS UserInfo")
                it.execSQL("DROP TABLE IF EXISTS RehabInfo")
                onCreate(it)
            }
        }
    }
}