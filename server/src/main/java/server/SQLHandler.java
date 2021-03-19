package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class SQLHandler {
    private static final Logger log = LogManager.getLogger();
    private static final String DATABASE_PATH = "main.db";
    private static Connection connection;
    private static PreparedStatement preparedStatement;

    /**
     * Установка соединения с базой данных
     */
    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
            log.info("Есть соединение с базой данных... " + connection);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.error("Не удалось зарегистрировать драйвер базы данных!");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Не удалось подключиться к базе данных!");
            return false;
        }
    }

    /**
     * Завершение соединения с базой данных
     */
    public static void disconnect() {
        try {
            preparedStatement.close();
            connection.close();
            log.info("Cоединение с базой данных корректно закрыто. " + connection);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static String getNickNameByLoginAndPassword(String login, String password) {
        String nick = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT nickname FROM users WHERE login=? AND pass=?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                nick = resultSet.getString("nickname");
            }
            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nick;
    }

    public static boolean registration(String login, String password, String nickname) {
        boolean result = false;
        try {
            //выберем количество записей с никами nickname или логинами login.
            //если оно отлично от нуля, значит пользователь с таким ником/логином уже есть.
            preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE login=? OR nickname=?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, nickname);
            ResultSet resultSet = preparedStatement.executeQuery();
            int countOfUpdated = -1;
            if (resultSet.next()) {
                countOfUpdated = resultSet.getInt(1);
                preparedStatement.close();
                resultSet.close();
                if (countOfUpdated == 0){
                    preparedStatement = connection.prepareStatement("INSERT INTO users (login, pass, nickname) VALUES (?, ?, ?)");
                    preparedStatement.setString(1, login);
                    preparedStatement.setString(2, password);
                    preparedStatement.setString(3, nickname);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    result = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean changeNickName(String login, String newNick) {
        boolean result = false;
        try {
            preparedStatement = connection.prepareStatement("UPDATE users SET nickname=? WHERE login=?");
            preparedStatement.setString(1, newNick);
            preparedStatement.setString(2, login);
            int countOfUpdated = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (countOfUpdated == 1) {
                result = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}


