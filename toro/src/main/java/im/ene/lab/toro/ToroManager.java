package im.ene.lab.toro;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

/**
 * Created by eneim on 1/29/16.
 */
public interface ToroManager {

  /* BEGIN Setup current unique Player */

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

  /* END Setup current unique Player */

  /* BEGIN Setup own life cycle */

  /**
   * Called after being registered to a RecyclerView. See {@link Toro#register(RecyclerView)}
   */
  void onRegistered();

  /**
   * Called before being unregistered from a RecyclerView. See {@link
   * Toro#unregister(RecyclerView)}
   */
  void onUnregistered();

  /* END Setup own life cycle */

  /* BEGIN Directly control current player */

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

  /* END Directly control current player */
}
