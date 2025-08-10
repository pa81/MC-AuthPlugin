package com.huhushops.authplugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class AuthPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // config.ymlが存在しない場合にデフォルトのファイルをコピー
        saveDefaultConfig();

        // イベントリスナーを登録
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        getLogger().info("AuthPluginが有効になりました。");
    }

    @Override
    public void onDisable() {
        getLogger().info("AuthPluginが無効になりました。");
    }
}