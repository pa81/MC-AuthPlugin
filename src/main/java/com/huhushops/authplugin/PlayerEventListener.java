package com.huhushops.authplugin;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class PlayerEventListener implements Listener {

    private final AuthPlugin plugin;

    public PlayerEventListener(AuthPlugin plugin) {
        this.plugin = plugin;
    }

    // プレイヤーがサーバーに参加した時の処理
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkPlayer(event.getPlayer());
    }

    // プレイヤーがゲームモードを変更した時の処理
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        // クリエイティブモードやスペクテイターモードへの変更を試みた場合
        if (event.getNewGameMode() == GameMode.CREATIVE || event.getNewGameMode() == GameMode.SPECTATOR) {
            if (!isAllowed(player)) {
                event.setCancelled(true);
                player.setGameMode(GameMode.SURVIVAL);
                sendWarningTitle(player);
                plugin.getLogger().warning(player.getName() + " が不正にゲームモードを変更しようとしました。");
            }
        }
    }

    // プレイヤーがコマンドを実行する前の処理
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // 許可されていないプレイヤーの場合はコマンドをチェック
        if (!isAllowed(player)) {
            String command = event.getMessage().toLowerCase();

            // 禁止するコマンドのリスト（/minecraft: プレフィックスも考慮）
            String[] blockedCommands = {
                    "/give ", "/minecraft:give ",
                    "/setblock ", "/minecraft:setblock ",
                    "/item ", "/minecraft:item ",
                    "/op ", "/minecraft:op "
            };

            boolean isBlocked = false;
            for (String blockedCmd : blockedCommands) {
                if (command.startsWith(blockedCmd)) {
                    isBlocked = true;
                    break;
                }
            }

            if (isBlocked) {
                event.setCancelled(true); // コマンドの実行をキャンセル
                player.sendMessage(ChatColor.RED + "あなたにはこのコマンドを実行する権限がありません。");
                plugin.getLogger().warning(player.getName() + " が禁止されたコマンド (" + event.getMessage() + ") を使用しようとしました。");
                sendWarningTitle(player); // 警告タイトルを表示
            }
        }
    }

    /**
     * プレイヤーの権限とゲームモードをチェックし、必要であれば修正するメソッド
     * @param player チェック対象のプレイヤー
     */
    private void checkPlayer(Player player) {
        if (!isAllowed(player)) {
            boolean modified = false;
            // プレイヤーがOP権限を持っているかチェック
            if (player.isOp()) {
                player.setOp(false);
                plugin.getLogger().warning(player.getName() + " のOP権限を剥奪しました。");
                modified = true;
            }
            // プレイヤーがサバイバルモードでない場合
            if (player.getGameMode() != GameMode.SURVIVAL) {
                player.setGameMode(GameMode.SURVIVAL);
                plugin.getLogger().warning(player.getName() + " をサバイバルモードに変更しました。");
                modified = true;
            }

            if (modified) {
                sendWarningTitle(player);
            }
        }
    }

    /**
     * プレイヤーがconfig.ymlで許可されているか確認するメソッド
     * @param player チェック対象のプレイヤー
     * @return 許可されていればtrue
     */
    private boolean isAllowed(Player player) {
        List<String> allowedUuids = plugin.getConfig().getStringList("allowed-players");
        return allowedUuids.contains(player.getUniqueId().toString());
    }

    /**
     * 警告タイトルをプレイヤーに送信するメソッド
     * @param player 送信対象のプレイヤー
     */
    private void sendWarningTitle(Player player) {
        player.sendTitle(
                ChatColor.RED + "" + ChatColor.BOLD + "Not do this",
                "",
                10,
                70,
                20
        );
    }
}