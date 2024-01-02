package plugin.enemydown.command;

//import java.net.http.WebSocket.Listener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.event.Listener;
import plugin.enemydown.Main;
import plugin.enemydown.data.PlayerScore;

public class EnemyDownCommand implements CommandExecutor, Listener {

  private Main main;
  private List<PlayerScore> playerScoreList = new ArrayList<>();

  public EnemyDownCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      PlayerScore nowPlayer = getPlayerScore(player);
      nowPlayer.setGameTime(20);

      World world = player.getWorld();

      initPlayerStatus(player);

      Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
        if(nowPlayer.getGameTime() <= 0) {
          Runnable.cancel();
          player.sendTitle("ゲーム終了しました",
              nowPlayer.getPlayerName() + " 合計" + nowPlayer.getScore() + "点",
              0,30,0);
          nowPlayer.setScore(0);
          return;
        }
        world.spawnEntity(getEnemySpawnLocation(player, world), getEnemy());
        nowPlayer.setGameTime(nowPlayer.getGameTime() -5);
      },0,5*20);

    }

    return false;
  }

  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e) {
    LivingEntity enemy = e.getEntity();
    Player player = enemy.getKiller();
    if (Objects.isNull((player)) || playerScoreList.isEmpty()) {
      return;
    }

    for(PlayerScore playerScore : playerScoreList) {
      if(playerScore.getPlayerName().equals(player.getName())) {
        int point = switch (enemy.getType()) {
          case ZOMBIE -> 11;
          case SKELETON, WITCH -> 22;
          default -> 0;
        };

        playerScore.setScore(playerScore.getScore() + point);
        player.sendMessage("敵を倒したよ　現在のスコアは、" + playerScore.getScore() + "点です");
      }
    }
  }

  /**
   * 現在実行しているプレイヤーの情報を取得する
   *
   * @param player コマンドを実行したプレイヤー
   * @return 現在実行しているプレイヤーのスコア情報
   */
  private PlayerScore getPlayerScore(Player player) {
    if(playerScoreList.isEmpty()) {
      return addNewPlayer(player);
    } else {
      for(PlayerScore playerScore : playerScoreList) {
        if(!playerScore.getPlayerName().equals(player.getName())) {
          return addNewPlayer(player);
        } else {
          return playerScore;
        }
      }
    }
    return null;
  }

  /**
   * 新規のプレイヤー情報をリストに追加します
   * @param player コマンドを実行したプレイヤー
   * @return 新規プレーヤー
   */
  private PlayerScore addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
    return newPlayer;
  }

  /**
   * ゲームを始める前にプレイヤーの状態を設定する
   * 体力と空腹度を最大にして、装備は、ネザライト一式になる。
   * @param player コマンドを実行したプレイヤー
   */
  private static void initPlayerStatus(Player player) {
    player.setHealth(19);
    player.setFoodLevel(19);

    PlayerInventory inventory = player.getInventory();
    inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
    inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
    inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
  }


  /**
   * 敵の出現場所を取得します。
   * 出現エリアは、X軸とZ軸は、自分の位置から、ランダムで−１０−９の値が節制
   * Y軸は、プレイヤーと同じとなります。
   * @param player コマンドを実行したユーザ
   * @param world コマンドを実行したプレイヤーが所属するワールド
   * @return 敵の出現場所
   */
  private Location getEnemySpawnLocation(Player player, World world) {
    Location playerLocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(20) - 10;
    int randomZ = new SplittableRandom().nextInt(20) - 10;

    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    return new Location(world, x, y ,z);
  }


  /**
   * ランダムで敵を抽出して、その結果の敵を取得します。
   * @return 敵
   */
  private EntityType getEnemy() {
    List<EntityType> enemyList = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITCH);
    return enemyList.get(new SplittableRandom().nextInt(enemyList.size()));
  }

}

