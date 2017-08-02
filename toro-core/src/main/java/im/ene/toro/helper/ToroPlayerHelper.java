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
import android.support.annotation.Nullable;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroPlayer.State;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.util.ArrayList;

/**
 * @author eneim | 6/11/17.
 *
 *         General helper class for a specific {@link ToroPlayer}. This class helps forwarding the
 *         playback state to the player if there is any {@link ToroPlayer.EventListener}
 *         registered. It also requests the initialization for the Player.
 */

@SuppressWarnings("WeakerAccess") //
public abstract class ToroPlayerHelper {

  protected static final String TAG = "ToroLib:ViewHelper";

  private final Handler handler = new Handler(new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      boolean playWhenReady = (boolean) msg.obj;
      switch (msg.what) {
        case State.STATE_BUFFERING /* ExoPlayer.STATE_BUFFERING */:
          internalListener.onBuffering();
          for (ToroPlayer.EventListener callback : eventListeners) {
            callback.onBuffering();
          }
          return true;
        case State.STATE_READY /*  ExoPlayer.STATE_READY */:
          if (playWhenReady) {
            internalListener.onPlaying();
          } else {
            internalListener.onPaused();
          }

          for (ToroPlayer.EventListener callback : eventListeners) {
            if (playWhenReady) {
              callback.onPlaying();
            } else {
              callback.onPaused();
            }
          }
          return true;
        case State.STATE_END /* ExoPlayer.STATE_ENDED */:
          internalListener.onCompleted(container, player);
          for (ToroPlayer.EventListener callback : eventListeners) {
            callback.onCompleted(container, player);
          }
          return true;
        default:
          return false;
      }
    }
  });

  @NonNull protected final Container container;
  @NonNull protected final ToroPlayer player;

  final ArrayList<ToroPlayer.EventListener> eventListeners = new ArrayList<>();
  final ToroPlayer.EventListener internalListener = new ToroPlayer.EventListener() {
    @Override public void onBuffering() {
      // do nothing
    }

    @Override public void onPlaying() {
      // do nothing
    }

    @Override public void onPaused() {
      // do nothing
    }

    @Override public void onCompleted(Container container, ToroPlayer player) {
      container.savePlaybackInfo(player.getPlayerOrder(), new PlaybackInfo());
    }
  };

  public ToroPlayerHelper(@NonNull Container container, @NonNull ToroPlayer player) {
    this.container = container;
    this.player = player;
  }

  // Hook into the scroll state change event. Called by the enclosing ToroPlayer.
  public void onContainerScrollStateChange(int newState) {
    // Do nothing, sub class can override this.
  }

  @SuppressWarnings("ConstantConditions")
  public final void addPlayerEventListener(@NonNull ToroPlayer.EventListener eventListener) {
    if (eventListener != null) this.eventListeners.add(eventListener);
  }

  public final void removePlayerEventListener(ToroPlayer.EventListener eventListener) {
    if (eventListener != null) {
      this.eventListeners.remove(eventListener);
    }
  }

  /**
   * Initialize the necessary resource for the incoming playback. For example, prepare the
   * ExoPlayer instance for SimpleExoPlayerView. The initialization is feed by an initial playback
   * info, telling if the playback should start from a specific position or from beginning.
   * Normally this info can be obtained from cache if there is cache manager, or null if there is no
   * such cached information.
   *
   * @param playbackInfo the initial playback info. {@code null} if no such info available.
   */
  public abstract void initialize(@Nullable PlaybackInfo playbackInfo);

  public abstract void play();

  public abstract void pause();

  public abstract boolean isPlaying();

  /**
   * Get latest playback info. Either on-going playback info if current player is playing, or latest
   * playback info available if player is pausing.
   *
   * @return latest {@link PlaybackInfo} of current Player.
   */
  @NonNull public abstract PlaybackInfo getLatestPlaybackInfo();

  // Mimic ExoPlayer
  protected final void onPlayerStateUpdated(boolean playWhenReady, @State int playbackState) {
    handler.obtainMessage(playbackState, playWhenReady).sendToTarget();
  }

  public void release() {
    handler.removeCallbacksAndMessages(null);
  }
}
