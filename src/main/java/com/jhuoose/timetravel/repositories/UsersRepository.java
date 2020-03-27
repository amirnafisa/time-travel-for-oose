package com.jhuoose.timetravel.repositories;

import com.jhuoose.timetravel.models.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsersRepository {
    private Connection connection;

    public UsersRepository(Connection connection) throws SQLException {
        this.connection = connection;
        var statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS users (login TEXT PRIMARY KEY, password TEXT, secQuestion TEXT, secAns TEXT)");
        statement.close();
    }

    public User getOne(String login) throws SQLException, UserNotFoundException {

        var statement = connection.prepareStatement("SELECT login, password, secQuestion , secAns   FROM users WHERE login = ?");
        statement.setString(1, login);

        var result = statement.executeQuery();
        try {
            if (result.next()) {
                return new User(
                        result.getString("login"),
                        result.getString("password"),
                        result.getString("secQuestion"),
                        result.getString("secAns")
                );
            } else {
                throw new UserNotFoundException();
            }
        }
        finally {
            statement.close();
            result.close();
        }
    }
    public User getSecOne(String login, String secPass) throws SQLException, UserNotFoundException {

        var statement = connection.prepareStatement("SELECT login, password, secQuestion , secAns FROM users WHERE login = ? AND secAns = ?");
        statement.setString(1, login);
        statement.setString(2, secPass);
        var result = statement.executeQuery();
        try {
            if (result.next()) {
                return new User(
                        result.getString("login"),
                        result.getString("password"),
                        result.getString("secQuestion"),
                        result.getString("secAns")
                );
            } else {
                throw new UserNotFoundException();
            }
        }
        finally {
            statement.close();
            result.close();
        }
    }
    public void create(User user) throws SQLException {
        var statement = connection.prepareStatement("INSERT INTO users (login, password, secQuestion, secAns) VALUES (?, ?, ?, ?)");
        statement.setString(1, user.getLogin());
        statement.setString(2, user.getPassword());
        statement.setString(3, user.getSecQuestion());
        statement.setString(4, user.getSecAns());
        statement.execute();
        statement.close();
    }

    public List<User> getAll () throws SQLException {
        var statement = connection.prepareStatement("SELECT login, password, secQuestion, secAns FROM users");
        var result = statement.executeQuery();
        List<User> allUsers = new ArrayList<>();

        try {
            while (result.next()) {
                allUsers.add(new User(
                        result.getString("login"),
                        result.getString("password"),
                        result.getString("secQuestion"),
                        result.getString("secAns")
                ));
            }
        }
        finally {
            statement.close();
            result.close();
        }
        return allUsers;
    }
    public void updatePassword(User user) throws SQLException, UserNotFoundException {
        var statement = connection.prepareStatement("UPDATE users SET password = ? WHERE login = ?");
        statement.setString(1, user.getPassword());
        statement.setString(2, user.getLogin());
        try {
            if (statement.executeUpdate() == 0) throw new UserNotFoundException();
        }
        finally {
            statement.close();
        }
    }
}