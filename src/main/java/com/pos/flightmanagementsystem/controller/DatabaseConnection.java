package com.pos.flightmanagementsystem.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Database ချိတ်ဆက်ရန် လိုအပ်သော အချက်အလက်များ
    private static final String URL = "jdbc:mysql://localhost:3306/flightmanagementsystem";
    private static final String USER = "root"; // မိမိ MySQL ရဲ့ Username ကိုပြောင်းထည့်ပါ
    private static final String PASSWORD = "root2005"; // မိမိ MySQL ရဲ့ Password ကိုပြောင်းထည့်ပါ

    // Connection ရယူရန် Method
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // MySQL Driver ကို ခေါ်ယူခြင်း
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Database သို့ ချိတ်ဆက်ခြင်း
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database သို့ အောင်မြင်စွာ ချိတ်ဆက်ပြီးပါပြီ။");

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver ရှာမတွေ့ပါ။ Library ထည့်ထားခြင်း ရှိမရှိ စစ်ဆေးပါ။");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database သို့ ချိတ်ဆက်ရာတွင် အမှားအယွင်းရှိနေပါသည်။");
            e.printStackTrace();
        }
        return connection;
    }
}