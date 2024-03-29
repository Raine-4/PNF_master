package com.pnfmaster.android.database

import android.util.Log
import com.pnfmaster.android.MyApplication
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


object connect {

    const val DBNAME = "pnf_master"

    @Throws(SQLException::class)
    fun setConnection(dbName: String): Connection? {
        var conn: Connection? = null
        try {
            // 加载驱动
            Class.forName("com.mysql.jdbc.Driver")
            // 公网ip
            val ip = "pnfmaster.rwlb.rds.aliyuncs.com"
            conn = DriverManager.getConnection("jdbc:mysql://$ip:3307/$dbName",
                "nayufeng", "Znn@737402") as Connection
            // 用于向主函数传参，判断连接是否成功
            TestActivity.isConnected = 1
        } catch (ex: SQLException) {
            ex.printStackTrace()
            Log.e("connect", "SQLException")
            // 用于向主函数传参，连接失败
            TestActivity.isConnected = 0
        } catch (ex: ClassNotFoundException) {
            ex.printStackTrace()
            Log.e("connect", "ClassNotFoundException")
            TestActivity.isConnected = 0
        }
        return conn // 返回Connection型变量conn用于后续连接
    }

    // --------------------------------INSERT--------------------------------

    @Throws(SQLException::class)
    fun insertUser(username: String, password: String): Int {
        val connection = setConnection(DBNAME)!!
        val statement = connection.createStatement()
        val sql = "INSERT INTO User (username,password) VALUES ('$username','$password')"

        val res = statement.executeUpdate(sql)

        statement.close()
        connection.close()
        return res
    }

    @Throws(SQLException::class)
    fun insertUserInfo(name: String, gender: Int, age: Int, phone: String): Int {
        val connection = setConnection(DBNAME)!!
        val statement = connection.createStatement()
        val sql = "INSERT INTO UserInfo(name, gender, age, phone) VALUES ('$name', '$gender', '$age', '$phone')"

        val res = statement.executeUpdate(sql)

        statement.close()
        connection.close()
        return res
    }

    @Throws(SQLException::class)
    fun insertRehabInfo(diagnosisInfo: String, plan: String, progressRecord: String, goals: String): Int {
        val connection = setConnection(DBNAME)!!
        val statement = connection.createStatement()
        val sql = "INSERT INTO RehabInfo (diagnosisinfo, treatplan, progressrecord, goals) VALUES ('$diagnosisInfo', '$plan', '$progressRecord', '$goals')"

        val res = statement.executeUpdate(sql)

        statement.close()
        connection.close()
        return res
    }

    // --------------------------------UPDATE--------------------------------

    fun savePersonInfo(name: String, age: String, gender: Int, contact: String): Int {
        val connection = setConnection(DBNAME)!!
        val statement = connection.createStatement()
        val sql = "UPDATE UserInfo (name, age, gender, contact)VALUES('$name', '$age', '$gender', '$contact')"
        val res = statement.executeUpdate(sql)

        statement.close()
        connection.close()
        return res
    }

    fun saveRehabInfo(userInput: String, columnName: String, id: Int): Int {
        val connection = setConnection(DBNAME)!!
        val statement = connection.createStatement()
        val sql = "UPDATE RehabInfo SET $columnName = $userInput WHERE id = '$id')"

        val res = statement.executeUpdate(sql)

        statement.close()
        connection.close()
        return res
    }


    // --------------------------------QUERY--------------------------------

    @Throws(SQLException::class)
    fun queryPassword(username: String): String {
        val password: String
        val connection = setConnection(DBNAME)!!
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT password FROM User WHERE username = '$username'")

        resultSet.first()
        password = resultSet.getString("password")

        resultSet.close()
        statement.close()
        connection.close()
        return password
    }

    // 检查注册时用户名是否已被使用，已被使用会返回True
    fun isUsernameUsed(username: String): Boolean {
        val connection = setConnection(DBNAME)!!
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT 1 FROM User WHERE username = '$username'")
        // 如果resultSet中不存在任何数据，resultSet.next()会返回false
        val res = resultSet.next()
        resultSet.close()
        statement.close()
        connection.close()
        return res
    }

    // 查询数据库中是否存在用户名和密码符合的账户
    fun isRegistered(inputAccount:String, inputPassword: String): Boolean {
        try {
            val connection = setConnection(DBNAME)!!
            // 执行查询
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM User WHERE username = '$inputAccount' AND password = '$inputPassword'")
            // 处理查询结果
            if (resultSet.next()) {
                MyApplication.userId = resultSet.getInt("id")
                Log.d("connect", "id = ${MyApplication.userId}")
                // 关闭连接
                resultSet.close()
                statement.close()
                connection.close()
                return true
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    fun queryUserInfo(id: Int): List<Any> {
        val userInfo = mutableListOf("", -1, -1, "") // name, age, gender, phone
        val connection = setConnection(DBNAME)!!
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM userinfo WHERE id = '$id'")

        if (resultSet.first()) {
            userInfo.clear()
            userInfo.add(resultSet.getString("name"))
            userInfo.add(resultSet.getInt("age"))
            userInfo.add(resultSet.getInt("gender"))
            userInfo.add(resultSet.getString("phone"))
        } else {
            Log.e("connect", "fun queryUserInfo. No such id. id = $id")
        }

        resultSet.close()
        statement.close()
        connection.close()

        return userInfo
    }

    fun queryRehabInfo(id: Int): List<String> {
        val rehabInfo = mutableListOf("", "", "", "")
        val connection = setConnection(DBNAME)!!
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM RehabInfo WHERE id = '$id'")

        if (resultSet.first()) {
            rehabInfo.clear()
            rehabInfo.add(resultSet.getString("diagnosisinfo"))
            rehabInfo.add(resultSet.getString("treatplan"))
            rehabInfo.add(resultSet.getString("progressrecord"))
            rehabInfo.add(resultSet.getString("goals"))
        } else {
            Log.e("connect", "fun queryRehabInfo. No such id. id = $id")
        }

        resultSet.close()
        statement.close()
        connection.close()

        return rehabInfo
    }
}


