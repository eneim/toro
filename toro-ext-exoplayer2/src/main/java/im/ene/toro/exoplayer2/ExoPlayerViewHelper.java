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

package im.ene.toro.exoplayer2;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import com.google.android.exoplayer2.ExoPlayer;
import im.ene.toro.PlayerViewHelper;
import im.ene.toro.ToroPlayer;

/**
 * Created by eneim on 10/3/16.
 *
 * This helper class provide internal access to Toro's helper methods. It will hook into each
 * ViewHolder's transaction to trigger the expected behavior. Client is not recommended to override
 * this, but in case it wants to provide custom behaviors, it is recommended to call super method
 * from this Helper.
 */
public class ExoPlayerViewHelper extends PlayerViewHelper implements PlayerCallback {

  private static final String TAG = "Toro:ExoHelper";

  // Require the player Object and the View holds it.
  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull View itemView) {
    super(player, itemView);
  }

  @Override public void onPlayerStateChanged(boolean playWhenReady, @State int state) {
    Log.w(TAG, "onPlayerStateChanged() called with: playWhenReady = ["
        + playWhenReady
        + "], state = ["
        + state
        + "]");
    switch (state) {
      case ExoPlayer.STATE_IDLE:
        // Do nothing
        this.itemView.setKeepScreenOn(false);
        break;
      case ExoPlayer.STATE_BUFFERING:
        this.itemView.setKeepScreenOn(true);
        if (!playWhenReady && !player.isPrepared()) {
          this.onPrepared(this.itemView, this.itemView.getParent());
          this.player.onVideoPrepared();
        }
        break;
      case ExoPlayer.STATE_READY:
        this.itemView.setKeepScreenOn(true);
        if (playWhenReady) {
          this.player.onPlaybackStarted();
        } else {
          this.player.onPlaybackPaused();
        }
        break;
      case ExoPlayer.STATE_ENDED:
        this.itemView.setKeepScreenOn(false);
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
