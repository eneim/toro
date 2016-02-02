package im.ene.lab.toro;

import android.support.annotation.Nullable;

/**
 * Created by eneim on 1/29/16.
 */
public interface ToroManager {

  /**
   * @return latest Video Player
   */
  ToroPlayer getPlayer();

  /**
   * Set current video player. There would be at most one Video player at a time.
   *
   * @param player the current Video Player of this manager
   */
  void setPlayer(ToroPlayer player);

  /**
   * Start playing current video
   */
  void startPlayback();

  /**
   * Pause current video
   */
  void pausePlayback();

  /**
   * Save current video state
   */
  void saveVideoState(Long videoId, @Nullable Integer position, long duration);

  /**
   * Restore and setup state of a Video to current video player
   */
  void restoreVideoState(Long videoId);

  void onRegistered();

  void onUnregistered();
}
