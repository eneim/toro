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
import android.view.View;
import im.ene.toro.PlayerViewHelper;
import im.ene.toro.ToroPlayer;
import im.ene.toro.exoplayer.internal.ExoMediaPlayer;

/**
 * Created by eneim on 2/6/16.
 *
 * This helper class provide internal access to Toro's helper methods. It will hook into each
 * ViewHolder's transaction to trigger the expected behavior. Client is not recommended to override
 * this, but in case it wants to provide custom behaviors, it is recommended to call super method
 * from this Helper.
 *
 */
public class ExoPlayerViewHelper extends PlayerViewHelper implements PlayerCallback {

  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull View itemView) {
    super(player, itemView);
  }

  @Override public void onPlayerStateChanged(boolean playWhenReady, @State int state) {
    switch (state) {
      case ExoMediaPlayer.STATE_IDLE:
        // Do nothing
        break;
      case ExoMediaPlayer.STATE_BUFFERING:
        if (!playWhenReady) {
          this.player.onVideoPrepared();
          this.onPrepared(this.itemView, this.itemView.getParent());
        }
        break;
      case ExoMediaPlayer.STATE_PREPARING:
        this.player.onVideoPreparing();
        break;
      case ExoMediaPlayer.STATE_READY:
        if (playWhenReady) {
          this.player.onPlaybackStarted();
        } else {
          this.player.onPlaybackPaused();
        }
        break;
      case ExoMediaPlayer.STATE_ENDED:
        if (playWhenReady) {
          this.onCompletion();
          this.player.onPlaybackCompleted();
        }
        break;
      default:
        // Do nothing
        break;
    }
  }

  @Override public final boolean onPlayerError(Exception error) {
    return super.onPlaybackError(error);
  }
}
