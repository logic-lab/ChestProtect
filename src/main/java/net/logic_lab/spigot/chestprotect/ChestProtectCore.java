package net.logic_lab.spigot.chestprotect;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ChestProtectCore extends JavaPlugin {

    //
    private Config config = null;

    // データベースへのコネクションを保持するよ
    private Connection connection;

    private void loadConfig(){

        // 初期設定ファイルの保存
        saveDefaultConfig();

        // リロードの場合
        if( config != null ){
            reloadConfig();
        }

        // コンフィグの取得
        config = new Config(getConfig());

    }

    @Override
    public void onEnable(){
        getLogger().info("ChestProtect loaded.");

        // Configの読込と設定
        this.loadConfig();

        if( connection == null ){
            getLogger().info("Try connect to MySQL server: " + config.getServerPort() );
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                connection = DriverManager.getConnection("jdbc:mysql://" + config.getServerPort() + "/" + config.getDatabaseName(), config.getDatabaseUser(), config.getDatabasePassword());
                getLogger().info("Connected to MySQL server.");
            }
            catch( InstantiationException | IllegalAccessException | ClassNotFoundException e ){
                getLogger().warning("Could not load JDBC driver.");
            }
            catch( SQLException e ){
                getLogger().warning("Could not connect MySQL server.");
            }
        }
    }

    @Override
    public void onDisable(){
        getLogger().info("ChestProtect disabled.");
        if( connection != null ){
            try {
                getLogger().info("Disconnect from server.");
                connection.close();
                connection = null;
            }
            catch( SQLException e ){
                getLogger().warning("Failed to disconnect from server.");
            }
        }
    }

    @Override
    public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args ){
        return false;
    }

}
