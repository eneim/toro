/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.toro;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

/**
 * Created by eneim on 1/29/16.
 */
public interface VideoPlayerManager {

  /* BEGIN Setup current Player */

  /**
   * @return latest Video Player
   */
  @Nullable ToroPlayer getPlayer();

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
   * Stop current video. Used when the Video is detached from its parent.
   */
  void stopPlayback();

  /**
   * Save current video state
   */
  void saveVideoState(String videoId, @Nullable Long position, long duration);

  /**
   * Restore and setup state of a Video to current video player
   */
  void restoreVideoState(String videoId);

  @Nullable Long getSavedPosition(String videoId);
  /* END Directly control current player */
}
