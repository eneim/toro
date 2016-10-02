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

package im.ene.toro.exoplayer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import im.ene.lab.toro.PlayerViewHelper;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.VideoPlayerManager;
import im.ene.toro.exoplayer.internal.DemoPlayer;

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
public final class ExoPlayerViewHelper extends PlayerViewHelper implements OnStateChangeListener {

  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull View itemView) {
    super(player, itemView);
  }

  @Override public boolean onItemLongClick(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {
    VideoPlayerManager manager = super.getPlayerManager(parent);
    // Important components are missing, return
    if (manager == null) {
      return false;
    }

    // Being pressed player is not be able to play, return
    if (!player.wantsToPlay() || !Toro.getStrategy().allowsToPlay(player, parent)) {
      return false;
    }

    ToroPlayer currentPlayer = manager.getPlayer();
    if (!player.equals(currentPlayer)) {
      // Being pressed player is a new one
      // All conditions to switch players has passed, process the switching
      // Manually save Video state
      // Not the current player, and new player wants to play, so switch players
      if (currentPlayer != null) {
        if (currentPlayer.isPlaying()) {
          manager.saveVideoState(currentPlayer.getMediaId(), currentPlayer.getCurrentPosition(),
              currentPlayer.getDuration());
        }
        // Force pause
        manager.pausePlayback();
      }

      // Trigger new player
      manager.setPlayer(player);
      manager.restoreVideoState(player.getMediaId());
      manager.startPlayback();
      return true;
    } else {
      // Pressing current player, pause it if it is playing
      if (currentPlayer.isPlaying()) {
        manager.saveVideoState(currentPlayer.getMediaId(), currentPlayer.getCurrentPosition(),
            currentPlayer.getDuration());
        manager.pausePlayback();
      } else {
        // It's paused, so we resume it
        manager.restoreVideoState(currentPlayer.getMediaId());
        manager.startPlayback();
      }
      return true;
    }
  }

  private static final String TAG = "ExoPlayerViewHelper";

  /**
   * Implement {@link OnStateChangeListener}
   */
  @Override public final void onPlayerStateChanged(boolean playWhenReady, @State int state) {
    Log.d(TAG, "onPlayerStateChanged() called with: playWhenReady = ["
        + playWhenReady
        + "], state = ["
        + state
        + "]");
    switch (state) {
      case DemoPlayer.STATE_ENDED:
        this.player.onPlaybackCompleted();
        this.onCompletion();
        break;
      case DemoPlayer.STATE_BUFFERING:
        this.player.onVideoPrepared();
        this.onPrepared(this.itemView, this.itemView.getParent());
        break;
      case DemoPlayer.STATE_IDLE:
        break;
      case DemoPlayer.STATE_PREPARING:
        this.player.onVideoPreparing();
        break;
      case DemoPlayer.STATE_READY:
        if (playWhenReady) {
          this.player.onPlaybackStarted();
        } else {
          this.player.onPlaybackPaused();
        }
        break;
      default:
        break;
    }
  }

  @Override public final boolean onPlayerError(Exception error) {
    return super.onPlaybackError(error);
  }
}
