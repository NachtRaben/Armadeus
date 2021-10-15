package dev.armadeus.discord.moderation.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

import static dev.armadeus.bot.api.config.ArmaConfig.logger;

public class SqlManager {
    private Path usersPath;
    private Path logsPath;

    public SqlManager(){
        try {
            Class.forName( "org.sqlite.JDBC" );
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }
    }

    public SqlManager getConnection( Guild guild  ) {
        String guildID = guild.getId();
        usersPath = Paths.get("guilds/" + guildID + "/users.db" );
        logsPath = Paths.get("guilds/" + guildID + "/logs.db" );

        String userName = "user";
        String password = "pass";

        boolean madeFolder = false;
        boolean madeUsersFile = false;
        boolean madeLogsFile = false;

        try {
            madeFolder = new File("guilds/" + guildID ).mkdir();
            madeUsersFile = new File( String.valueOf( usersPath.toAbsolutePath() ) ).createNewFile();
            madeLogsFile = new File( String.valueOf( logsPath.toAbsolutePath() ) ).createNewFile();
            if ( madeFolder && madeUsersFile && madeLogsFile ) logger.info( "Created Files/Directories." );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try {
            if ( madeUsersFile ) {
                String url = "jdbc:sqlite:" + usersPath.toAbsolutePath();
                Connection conn = DriverManager.getConnection( url, userName, password );
                conn.prepareStatement( "CREATE TABLE IF NOT EXISTS `users` (`i` INTEGER PRIMARY KEY AUTOINCREMENT, `timestamp` TEXT, `userTag` TEXT, `userID` TEXT, `warnings` INTEGER, `punished` INTEGER);" ).executeUpdate();
                conn.close();
            }
            if ( madeLogsFile ) {
                String url = "jdbc:sqlite:" + logsPath.toAbsolutePath();
                Connection conn = DriverManager.getConnection( url, userName, password );
                conn.prepareStatement( "CREATE TABLE IF NOT EXISTS `logs` (`i` INTEGER PRIMARY KEY AUTOINCREMENT, `report` INTEGER, `timestamp` TEXT, `msgID` TEXT, `channelName` TEXT, `channelID` TEXT, `userTag` TEXT, `userID` TEXT, `msg` TEXT);" ).executeUpdate();
                conn.close();
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        }

        return this;
    }

    public void insertUserEntry( User user ) {
        String userTag = user.getAsTag();
        String userID = user.getId();

        try ( Connection conn = DriverManager.getConnection( "jdbc:sqlite:" + usersPath.toAbsolutePath() ) ) {
            PreparedStatement q = conn.prepareStatement( "INSERT INTO `users` VALUES (?, ?, ?, ?, 0, 0)" );
            q.setString( 2, String.valueOf( new Timestamp(System.currentTimeMillis()) ) );
            q.setString( 3, userTag );
            q.setString( 4, userID );
            q.executeUpdate();
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
    }

    public void insertLogEntry( User user, Message msg ) {
        String msgID = msg.getId();
        String channelName = msg.getChannel().getName();
        String channelID = msg.getChannel().getId();
        String userTag = user.getAsTag();
        String userID = user.getId();

        try ( Connection conn = DriverManager.getConnection( "jdbc:sqlite:" + logsPath.toAbsolutePath() ) ) {
            PreparedStatement q = conn.prepareStatement( "INSERT INTO `logs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);" );
            q.setInt( 2, -1 );
            q.setString( 3, String.valueOf( new Timestamp(System.currentTimeMillis()) ) );
            q.setString( 4, msgID );
            q.setString(5, channelName );
            q.setString(6, channelID );
            q.setString(7, userTag );
            q.setString(8, userID );
            q.setString(9, msg.getContentDisplay() );
            q.executeUpdate();
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
    }
}
