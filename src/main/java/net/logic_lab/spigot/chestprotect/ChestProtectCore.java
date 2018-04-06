package net.logic_lab.spigot.chestprotect;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.material.Sign;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.HashMap;

public class ChestProtectCore extends JavaPlugin {

    //
    private Config config = null;

    // データベースへのコネクションを保持するよ
    private Connection connection;

    private ChestProtectListener listener;

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

    private class ChestProtectListener implements Listener {

        /*
        // エンティティが右クリックされたとき
        @EventHandler
        public void onPlayerInteractAtEntityEvent( PlayerInteractAtEntityEvent event ){
            getLogger().info("PlayerInteractAtEntityEvent");
        }

        @EventHandler
        public void onPlayerInteractEntityEvent( PlayerInteractEntityEvent event ){
            getLogger().info("PlayerInteractEntityEvent");
        }
         */

        /*
        // プレイヤーがクリックしたとき（左右クリック）
        @EventHandler
        public void onPlayerInteractEvent( PlayerInteractEvent event ){
            getLogger().info("PlayerInteractEvent");
        }
        */

        /*
        // プレイヤーがアイテムを壊した時
        // 耐久値が0になったとき
        @EventHandler
        public void onPlayerItemBreakEvent( PlayerItemBreakEvent event ){
            getLogger().info("PlayerItemBreakEvent");
        }
        */

        @EventHandler
        public void onPlayerLoginEvent( PlayerLoginEvent event ){

            getLogger().info( event.getPlayer().getUniqueId().toString() );

        }

        @EventHandler
        public void onBlockBreakEvent( BlockBreakEvent event ){

            // 壊されたブロック
            Block break_block = event.getBlock();

            // ブロックが存在すれば
            if( break_block != null ){

                // ブロックの種類を取得
                Material material = break_block.getType();

                // チェストか、トラップチェストの場合
                if( material == Material.CHEST || material == Material.TRAPPED_CHEST ){
                    getLogger().info("チェスト壊すの禁止");
                    event.getPlayer().sendMessage("こわすなや");
                    event.setCancelled(true);
                }

            }

        }

        @EventHandler
        public void onChestRightClick( PlayerInteractEvent event ){

            // クリックされたブロックの取得
            Block clicked_block = event.getClickedBlock();

            // ブロックが存在すれば
            if( clicked_block != null ) {

                // そのブロックのタイプを取得
                Material block_material = clicked_block.getType();

                // チェストかトラップチェストの場合
                if( block_material == Material.CHEST || block_material == Material.TRAPPED_CHEST ){

                    // どちらのクリックか？
                    Action action = event.getAction();

                    // 右クリックだった場合
                    if( action == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking() ) {




                        //Chest chest = (Chest)clicked_block.getState();

                        getLogger().info( clicked_block.getState().getLocation().toString() );
                        getLogger().info("X: "+clicked_block.getX()+" Y:"+clicked_block.getY()+ " Z:"+clicked_block.getZ() );
                        getLogger().info("チェスト開けるの禁止");
                        event.getPlayer().sendMessage("あけんなや");
                        event.setCancelled(true);
                    }

                }
            }

        }

        @EventHandler
        public void onSignPlace( SignChangeEvent event ){
            Sign sign = (Sign)event.getBlock().getState().getData();
            Block attached = event.getBlock().getRelative(sign.getAttachedFace());
            if( attached.getType() == Material.CHEST ){

                getLogger().info("張り付いたよ");
                getLogger().info( attached.toString() );
            }
        }


    }

    private void setupTables(){

        HashMap<String,String> sqls = new HashMap<>();

        sqls.put("users",
                  "CREATE TABLE IF NOT EXISTS " + config.getTablePrefix() + "users("
                + "    id         INTEGER     NOT NULL,"
                + "    name       VARCHAR(40) NOT NULL,"
                + "    uuid       VARCHAR(40) NOT NULL,"
                + "    created_at DATETIME,"
                + "    updated_at DATETIME,"
                + "    PRIMARY KEY(id),"
                + "    UNIQUE INDEX uuid_uniq_idx(uuid)"
                + ");"
        );
        sqls.put("chests",
                  "CREATE TABLE IF NOT EXISTS " + config.getTablePrefix() + "chests("
                + "    id         INTEGER     NOT NULL,"
                + "    uuid       VARCHAR(40) NOT NULL,"
                + "    line1      VARCHAR(40) NOT NULL,"
                + "    line2      VARCHAR(40) NOT NULL,"
                + "    line3      VARCHAR(40) NOT NULL,"
                + "    created_at DATETIME,"
                + "    updated_at DATETIME,"
                + "    PRIMARY KEY(id),"
                + "    UNIQUE INDEX uuid_uniq_idx(uuid)"
                + ");"
        );

        try {

            for( String key : sqls.keySet() ){
                Statement statement = connection.createStatement();
                int count = statement.executeUpdate(sqls.get(key));
                if( count != 0 ){
                    getLogger().info( key + " table: OK result: " + count );
                }
                else {
                    getLogger().info( key + " table: NG" );
                }
            }

        }
        catch ( SQLException e ) {
            getLogger().warning( "Table create failed: " + e.getMessage() );
        }

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

        // テーブルの下準備
        this.setupTables();

        // イベントの登録
        listener = new ChestProtectListener();
        getServer().getPluginManager().registerEvents( listener, this );

    }

    @Override
    public void onDisable(){
        getLogger().info("ChestProtect disabled.");

        // イベントの抹消
        HandlerList.unregisterAll(listener);

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
