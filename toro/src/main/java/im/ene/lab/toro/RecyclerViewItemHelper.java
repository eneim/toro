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

package im.ene.lab.toro;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewParent;

/**
 * Created by eneim on 2/6/16.
 *
 * This helper class provide internal access to Toro's helper methods. It will hook into each
 * ViewHolder's transaction to trigger the expected behavior. Client is not recommended to override
 * this, but in case it wants to provide custom behaviors, it is recommended to call super method
 * from this Helper.
 *
 * Extending this class is prohibited. An extension should have an instance of this as a delegate.
 */
public final class RecyclerViewItemHelper extends VideoViewItemHelper {

  private static RecyclerViewItemHelper INSTANCE;

  public static RecyclerViewItemHelper getInstance() {
    if (INSTANCE == null) {
      synchronized (RecyclerViewItemHelper.class) {
        INSTANCE = new RecyclerViewItemHelper();
      }
    }

    return INSTANCE;
  }

  @Override public void onAttachedToParent(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {
    ToroScrollListener listener =
        parent != null ? Toro.sInstance.mListeners.get(parent.hashCode()) : null;
    if (listener != null && listener.getManager().getPlayer() == null) {
      if (player.wantsToPlay() && Toro.getStrategy().allowsToPlay(player, parent)) {
        listener.getManager().setPlayer(player);
        listener.getManager().restoreVideoState(player.getVideoId());
        listener.getManager().startPlayback();
        player.onPlaybackStarted();
      }
    }
  }

  @Override public void onDetachedFromParent(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {
    ToroScrollListener listener =
        parent != null ? Toro.sInstance.mListeners.get(parent.hashCode()) : null;
    // Manually save Video state
    if (listener != null && player.equals(listener.getManager().getPlayer())) {
      if (player.isPlaying()) {
        listener.getManager()
            .saveVideoState(player.getVideoId(), player.getCurrentPosition(), player.getDuration());
        listener.getManager().stopPlayback();
        player.onPlaybackPaused();
      }
    }
  }

  @Override public boolean onItemLongClick(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {
    ToroScrollListener listener =
        parent != null ? Toro.sInstance.mListeners.get(parent.hashCode()) : null;
    // Important components are missing, return
    if (listener == null) {
      return false;
    }

    // Being pressed player is not be able to play, return
    if (!player.wantsToPlay() || !Toro.getStrategy().allowsToPlay(player, parent)) {
      return false;
    }

    VideoPlayerManager manager = listener.getManager();
    ToroPlayer currentPlayer = manager.getPlayer();

    if (!player.equals(currentPlayer)) {
      // Being pressed player is a new one
      // All conditions to switch players has passed, process the switching
      // Manually save Video state
      // Not the current player, and new player wants to play, so switch players
      if (currentPlayer != null) {
        if (currentPlayer.isPlaying()) {
          manager.saveVideoState(currentPlayer.getVideoId(), currentPlayer.getCurrentPosition(),
              currentPlayer.getDuration());
        }
        // Force pause
        manager.pausePlayback();
        currentPlayer.onPlaybackPaused();
      }

      // Trigger new player
      manager.setPlayer(player);
      manager.restoreVideoState(player.getVideoId());
      manager.startPlayback();
      player.onPlaybackStarted();
      return true;
    } else {
      // Pressing current player, pause it if it is playing
      if (currentPlayer.isPlaying()) {
        manager.saveVideoState(currentPlayer.getVideoId(), currentPlayer.getCurrentPosition(),
            currentPlayer.getDuration());
        manager.pausePlayback();
        currentPlayer.onPlaybackPaused();
      } else {
        // It's paused, so we resume it
        manager.restoreVideoState(currentPlayer.getVideoId());
        manager.startPlayback();
        currentPlayer.onPlaybackStarted();
      }
      return true;
    }
  }
}
