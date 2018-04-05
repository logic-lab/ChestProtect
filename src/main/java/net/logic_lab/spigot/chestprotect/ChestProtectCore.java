package net.logic_lab.spigot.chestprotect;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.*;

public class ChestProtectCore extends JavaPlugin {

    // データベースへのコネクションを保持するよ
    Connection connection;

    @Override
    public void onEnable(){
        getLogger().info("ChestProtect loaded");

        if( connection == null ){
            getLogger().info("Try to connect MySQL server: localhost:3306"); // Config化する
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/minecraft", "root", ""); // Config化する
                getLogger().info("MySQL connected");
            }
            catch( InstantiationException | IllegalAccessException | ClassNotFoundException e ){
                getLogger().warning("Could not load JDBC driver");
            }
            catch( SQLException e ){
                getLogger().warning("Could not connect MySQL server");
            }
        }
    }

    @Override
    public void onDisable(){
        getLogger().info("ChestProtect disabled");
        if( connection != null ){
            try {
                getLogger().info("Disconnect to server");
                connection.close();
                connection = null;
            }
            catch( SQLException e ){
                getLogger().warning("Failed disconnect to server");
            }
        }
    }

    @Override
    public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args ){




        return false;
    }

}
