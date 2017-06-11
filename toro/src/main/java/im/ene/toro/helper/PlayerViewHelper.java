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
import im.ene.toro.media.MediaPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.util.ArrayList;

/**
 * @author eneim | 6/11/17.
 */

public abstract class PlayerViewHelper implements MediaPlayer, Handler.Callback, Cancellable {

  protected static final String TAG = "ToroLib:ViewHelper";

  private final Handler handler = new Handler(this);
  @NonNull private final Container container;
  @NonNull private final ToroPlayer player;

  private ArrayList<ToroPlayer.EventListener> eventListeners;

  @SuppressWarnings("WeakerAccess")
  public PlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player) {
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

  public abstract void initialize(@NonNull PlaybackInfo playbackInfo) throws Exception;

  public abstract PlaybackInfo getPlaybackInfo();

  // Mimic ExoPlayer
  @SuppressWarnings("WeakerAccess") //
  protected final void onPlayerStateUpdated(boolean playWhenReady, int playbackState) {
    handler.obtainMessage(playbackState, playWhenReady).sendToTarget();
  }

  @Override public boolean handleMessage(Message msg) {
    if (this.eventListeners == null || this.eventListeners.isEmpty()) return false;
    boolean playWhenReady = (boolean) msg.obj;
    switch (msg.what) {
      case 2 /* ExoPlayer.STATE_BUFFERING */:
        for (ToroPlayer.EventListener callback : eventListeners) {
          callback.onBuffering();
        }
        return true;
      case 3 /*  ExoPlayer.STATE_READY */:
        for (ToroPlayer.EventListener callback : eventListeners) {
          if (playWhenReady) {
            callback.onPlaying();
          } else {
            callback.onPaused();
          }
        }
        return true;
      case 4 /* ExoPlayer.STATE_ENDED */:
        for (ToroPlayer.EventListener callback : eventListeners) {
          callback.onCompleted(this.container, this.player);
        }
        return true;
      default:
        return false;
    }
  }

  @Override public void cancel() throws Exception {
    handler.removeCallbacksAndMessages(null);
  }
}
