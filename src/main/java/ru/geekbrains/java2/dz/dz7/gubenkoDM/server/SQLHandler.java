package ru.geekbrains.java2.dz.dz7.gubenkoDM.server;

import java.sql.*;


/**
 * Класс для работы с базой данных
*/

public class SQLHandler {
    private static final String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
    private static final String user = "postgres";
    private static final String password = "postgreS"; // наверно нехорошо делать пароль публичным, но пока так
    private static final String SQL_SELECT_GET_NICK = "SELECT Nickname FROM main WHERE Login = ? AND Password = ?;";
    private static Connection conn;
    private static PreparedStatement stmt;
    //для добавления пользователя в базу
    private static PreparedStatement stmtAdd;
    private static final String SQL_SELECT_ADD = "INSERT INTO main (login, password, nickname) VALUES (?, ?, ?);";
    //для модификации пользовательского пароля
    private static PreparedStatement stmtSet;
    private static final String SQL_SELECT_SET = "UPDATE main SET password = ? WHERE login=?;";


    /**
     *  Метод подключения к базе PostgreSQL
     */

    public static void connect() {
        try {
            //регистрируем драйвер  для работы с базой PostgreSQL
            DriverManager.registerDriver(new org.postgresql.Driver());
            //получаем конект к базе, передаем
            //url базы: тип драйвера,ip(доменное имя),порт,имя процесса
            //логин админа базы
            //пароль админа базы
            conn = DriverManager.getConnection(dbUrl, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Метод отключения от базы
     */
    public static void disconnect() {
        try {
            //закрываем коннект к базе
            conn.close();
        } catch (Exception c) {
            System.out.println("Connection Error");
        }
    }

    /**
     * Метод проверки есть ли такой клиент в базе для его авторизации
     * @param login  переданный логин авторизации
     * @param password переданный пароль авторизации
     * @return Возвращает логин если авторизация прошла, либо null если нет
     */

    public static String getNickByLoginPassword(String login, String password) {
        String w = null;
        try {
            stmt = conn.prepareStatement(SQL_SELECT_GET_NICK);
            stmt.setString(1, login);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if(rs.next())
                w = rs.getString("Nickname");
        } catch (SQLException e) {
            System.out.println("SQL Query Error");
        }
        return w;
    }

    /**
     * Метод добавления пользователя в базу
     * @param login
     * @param password
     * @param nick
     * @return возвращает true если успешно, false если нет
     */


    public static boolean addUser(String login,String password,String nick){
        try {
            //
            stmtAdd = conn.prepareStatement(SQL_SELECT_ADD);
            stmtAdd.setString(1, login);
            stmtAdd.setString(2, password);
            stmtAdd.setString(3, nick);
            //проверим, сколько строк модифицировано, если но то неудачно
            if (stmtAdd.executeUpdate()==0){
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL Query Error");
        }
        return true;
    }


    public static boolean setPass(String login,String password){
        try {
            stmtSet = conn.prepareStatement(SQL_SELECT_SET);
            stmtSet.setString(1, password);
            stmtSet.setString(2, login);

            //проверим, сколько строк модифицировано, если 0 то неудачно
            if (stmtSet.executeUpdate()==0){
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL Query Error");
        }
        return true;
    }


}
