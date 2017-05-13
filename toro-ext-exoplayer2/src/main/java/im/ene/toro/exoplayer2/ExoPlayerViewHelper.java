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

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
public class ExoPlayerViewHelper extends PlayerViewHelper
    implements PlayerCallback, Handler.Callback {

  private static final String TAG = "ToroLib:ExoHelper";

  private Handler handler;

  // Require the player Object and the View holds it.
  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull View itemView) {
    super(player, itemView);
  }

  // if lastPlayWhenReady == true and state == BUFFERING then this player is not buffering for the first time.
  private boolean lastPlayWhenReady = false;

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    handler = new Handler(this);
  }

  @Override public void onBound() {
    super.onBound();
    lastPlayWhenReady = false;
  }

  @Override public void onRecycled() {
    super.onRecycled();
    handler.removeCallbacksAndMessages(null);
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    handler = null;
  }

  @Override public void onPlayerStateChanged(boolean playWhenReady, @State int state) {
    // Using Handler to ensure that states are handled in queue.
    handler.obtainMessage(state, playWhenReady).sendToTarget();
  }

  @Override public final boolean onPlayerError(Exception error) {
    return super.onPlaybackError(error);
  }

  @Override public boolean handleMessage(Message msg) {
    int state = msg.what;
    boolean playWhenReady = (boolean) msg.obj;

    switch (state) {
      case ExoPlayer.STATE_IDLE:
        // Do nothing
        this.itemView.setKeepScreenOn(false);
        break;
      case ExoPlayer.STATE_BUFFERING:
        this.itemView.setKeepScreenOn(true);
        if (!lastPlayWhenReady && !playWhenReady && !player.isPrepared()) {
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

    lastPlayWhenReady = playWhenReady;
    return true;
  }
}
