/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.helper;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import im.ene.toro.Cancellable;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroPlayer.State;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.util.ArrayList;

/**
 * @author eneim | 6/11/17.
 */

@SuppressWarnings("WeakerAccess") public abstract class ToroPlayerHelper implements Cancellable {

  protected static final String TAG = "ToroLib:ViewHelper";

  private final Handler handler = new Handler(new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      if (eventListeners == null || eventListeners.isEmpty()) return false;
      boolean playWhenReady = (boolean) msg.obj;
      switch (msg.what) {
        case State.STATE_BUFFERING /* ExoPlayer.STATE_BUFFERING */:
          for (ToroPlayer.EventListener callback : eventListeners) {
            callback.onBuffering();
          }
          return true;
        case State.STATE_READY /*  ExoPlayer.STATE_READY */:
          for (ToroPlayer.EventListener callback : eventListeners) {
            if (playWhenReady) {
              callback.onPlaying();
            } else {
              callback.onPaused();
            }
          }
          return true;
        case State.STATE_END /* ExoPlayer.STATE_ENDED */:
          for (ToroPlayer.EventListener callback : eventListeners) {
            callback.onCompleted(container, player);
          }
          return true;
        default:
          return false;
      }
    }
  });

  @NonNull final Container container;
  @NonNull final ToroPlayer player;

  ArrayList<ToroPlayer.EventListener> eventListeners;

  public ToroPlayerHelper(@NonNull Container container, @NonNull ToroPlayer player) {
    this.container = container;
    this.player = player;
  }

  public final void addPlayerEventListener(ToroPlayer.EventListener eventListener) {
    if (this.eventListeners == null) {
      this.eventListeners = new ArrayList<>();
    }
    this.eventListeners.add(eventListener);
  }

  public final void removePlayerEventListener(ToroPlayer.EventListener eventListener) {
    if (this.eventListeners != null && eventListener != null) {
      this.eventListeners.remove(eventListener);
    }
  }

  public abstract void initialize(@NonNull PlaybackInfo playbackInfo);

  /**
   * Update latest playback info. Either live playback info if current player is playing, or latest
   * playback info if player is pausing.
   *
   * @return latest {@link PlaybackInfo} of current Player.
   */
  public abstract PlaybackInfo updatePlaybackInfo();

  // Mimic ExoPlayer
  protected final void onPlayerStateUpdated(boolean playWhenReady, @State int playbackState) {
    handler.obtainMessage(playbackState, playWhenReady).sendToTarget();
  }

  @Override public void cancel() throws Exception {
    handler.removeCallbacksAndMessages(null);
  }
}
