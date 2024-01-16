package plugin.enemydown.data;

import lombok.Getter;
import lombok.Setter;

/**
 * EnemyDownのゲームを実行する際のプレイヤーズ情報を扱うオブジェクト
 * プレイヤー名、合計点数、日時などの情報をもつ
 */
@Getter
@Setter
public class ExecutingPlayer {
  private String playerName;
  private int score;
  private int gameTime;

  public ExecutingPlayer(String playerName) {
    this.playerName = playerName;
  }
}
