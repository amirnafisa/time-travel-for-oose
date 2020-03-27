package com.jhuoose.timetravel.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.jhuoose.timetravel.models.User;
import com.jhuoose.timetravel.repositories.UserNotFoundException;
import com.jhuoose.timetravel.repositories.UsersRepository;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;

import java.sql.SQLException;
import java.util.List;

public class UsersController {
    private UsersRepository usersRepository;

    public UsersController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public void signup(Context ctx) throws SQLException {
        var user = new User(ctx.formParam("login", ""), BCrypt.withDefaults().hashToString(12, ctx.formParam("password", "").toCharArray()), ctx.formParam("secQuestion",""), ctx.formParam("secAns",""));
        usersRepository.create(user);
        ctx.sessionAttribute("user", user);
        ctx.status(201);
    }

    public void login(Context ctx) throws SQLException, UserNotFoundException {
        var login = ctx.formParam("login", "");
        var user = usersRepository.getOne(login);
        var orig_pswd = ctx.formParam("password", "");
        var second_pswd = ctx.formParam("secAns", "");
        var new_pswd = ctx.formParam("newPassword", "");

        if (orig_pswd.toCharArray().length == 0) {

            if (second_pswd != null) {
                try {
                    var retrive_from_database = this.usersRepository.getSecOne(login, second_pswd);
                    if (retrive_from_database.getSecAns().equals(second_pswd)) {
                         usersRepository.updatePassword(new
                             User(login, BCrypt.withDefaults().hashToString(12, new_pswd.toCharArray()), user.getSecQuestion(), user.getSecAns()));
                        ctx.status(200);
                    } else {
                        ctx.status(401);
                    }
                } catch (Exception e) {
                    ctx.status(400);
                }
            }
        } else {

            if (!BCrypt.verifyer().verify(orig_pswd.toCharArray(), user.getPassword()).verified) {

                ctx.status(401);
                throw new ForbiddenResponse();
            } else {

                ctx.sessionAttribute("user", user);
                ctx.status(200);
            }
        }
    }

    public User currentUser(Context ctx) {
        var user = (User) ctx.sessionAttribute("user");
        if (user == null) throw new ForbiddenResponse();
        return user;
    }

    public List<User> getAll() throws SQLException {
        return this.usersRepository.getAll();
    }

    public void is_login(Context ctx) {
        var user = (User) ctx.sessionAttribute("user");
        if (user == null) {
            ctx.status(404);
        } else {
            ctx.status(200);
        }

    }
}