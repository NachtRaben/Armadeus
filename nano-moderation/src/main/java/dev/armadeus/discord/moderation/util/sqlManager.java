package dev.armadeus.discord.moderation.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;

public class sqlManager {
    private static DSLContext sqlContextUsers;
    private static DSLContext sqlContextLogs;

    public sqlManager( Guild guild ) {
        String guildID = guild.getId();
        Path usersPath = Paths.get("../guilds/" + guildID + "/users.db" );
        Path logsPath = Paths.get("../guilds/" + guildID + "/logs.db" );

        String userName = "user";
        String password = "pass";
        String url = "jdbc:sqlite:" + usersPath.toAbsolutePath();

        try {
            Connection conn = DriverManager.getConnection( url, userName, password );
            sqlContextUsers = DSL.using(conn, SQLDialect.SQLITE);
            url = "jdbc:sqlite:" + logsPath.toAbsolutePath();
            conn = DriverManager.getConnection( url, userName, password );
            sqlContextLogs = DSL.using(conn, SQLDialect.SQLITE);
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
    }

    public DSLContext users() {
        return sqlContextUsers;
    }

    public DSLContext logs() {
        return sqlContextLogs;
    }

    public void insertLogEntry( User user, Message msg ) {
        String msgID = msg.getId();
        String channelName = msg.getChannel().getName();
        String channelID = msg.getChannel().getId();
        String userTag = user.getAsTag();
        String userID = user.getId();

        try {
            int exists = sqlContextLogs.select( count() ).from( "logs" ).fetchOne(0, int.class);
            if ( exists != 0 ) {
                sqlContextLogs.createTable( "logs" )
                        .column( "index", SQLDataType.INTEGER );
            }
        } catch( NullPointerException e ) {
            e.printStackTrace();
        }
    }
}
