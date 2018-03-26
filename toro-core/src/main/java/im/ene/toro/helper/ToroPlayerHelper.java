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
import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroPlayer.EventListener;
import im.ene.toro.ToroPlayer.State;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import im.ene.toro.widget.Container;
import java.util.ArrayList;

/**
 * General definition of a helper class for a specific {@link ToroPlayer}. This class helps
 * forwarding the playback state to the {@link ToroPlayer} if there is any {@link EventListener}
 * registered. It also requests the initialization for the Player.
 *
 * @author eneim | 6/11/17.
 */
@SuppressWarnings("WeakerAccess") //
public abstract class ToroPlayerHelper {

  private final Handler handler = new Handler(new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      boolean playWhenReady = (boolean) msg.obj;
      switch (msg.what) {
        case State.STATE_IDLE:
          // TODO: deal with idle state, maybe error handling.
          break;
        case State.STATE_BUFFERING /* Player.STATE_BUFFERING */:
          internalListener.onBuffering();
          for (EventListener callback : eventListeners) {
            callback.onBuffering();
          }
          break;
        case State.STATE_READY /*  Player.STATE_READY */:
          if (playWhenReady) {
            internalListener.onPlaying();
          } else {
            internalListener.onPaused();
          }

          for (EventListener callback : eventListeners) {
            if (playWhenReady) {
              callback.onPlaying();
            } else {
              callback.onPaused();
            }
          }
          break;
        case State.STATE_END /* Player.STATE_ENDED */:
          internalListener.onCompleted();
          for (EventListener callback : eventListeners) {
            callback.onCompleted();
          }
          break;
        default:
          break;
      }
      return true;
    }
  });

  @NonNull protected final ToroPlayer player;

  // This instance should be setup from #initialize and cleared from #release
  protected Container container;
  protected ToroPlayer.OnVolumeChangeListener volumeChangeListener;

  final ArrayList<EventListener> eventListeners = new ArrayList<>();
  final EventListener internalListener = new EventListener() {
    @Override public void onBuffering() {
      // do nothing
    }

    @Override public void onPlaying() {
      player.getPlayerView().setKeepScreenOn(true);
    }

    @Override public void onPaused() {
      player.getPlayerView().setKeepScreenOn(false);
      if (container != null) {
        container.savePlaybackInfo(player.getPlayerOrder(), player.getCurrentPlaybackInfo());
      }
    }

    @Override public void onCompleted() {
      if (container != null) {
        container.savePlaybackInfo(player.getPlayerOrder(), new PlaybackInfo());
      }
    }
  };

  public ToroPlayerHelper(@NonNull ToroPlayer player) {
    this.player = player;
  }

  // Hook into the scroll state change event. Called by the enclosing ToroPlayer.
  public void onSettled() {
    // Do nothing, sub class can override this.
  }

  @SuppressWarnings("ConstantConditions")
  public final void addPlayerEventListener(@NonNull EventListener eventListener) {
    if (eventListener != null) this.eventListeners.add(eventListener);
  }

  public final void removePlayerEventListener(EventListener eventListener) {
    this.eventListeners.remove(eventListener);
  }

  public final void setOnVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener) {
    this.volumeChangeListener = listener;
  }

  /**
   * Initialize the necessary resource for the incoming playback. For example, prepare the
   * ExoPlayer instance for SimpleExoPlayerView. The initialization is feed by an initial playback
   * info, telling if the playback should start from a specific position or from beginning.
   *
   * Normally this info can be obtained from cache if there is cache manager, or null if there is no
   * such cached information.
   *
   * @param playbackInfo the initial playback info. {@code null} if no such info available.
   * @deprecated use {@link #initialize(Container, PlaybackInfo)} instead.
   */
  @Deprecated  //
  public abstract void initialize(@Nullable PlaybackInfo playbackInfo);

  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
    this.container = container;
    this.initialize(playbackInfo);
  }

  public abstract void play();

  public abstract void pause();

  public abstract boolean isPlaying();

  /**
   * @deprecated use {@link #setVolumeInfo(VolumeInfo)} instead.
   */
  @Deprecated public abstract void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume);

  /**
   * @deprecated use {@link #getVolumeInfo()} instead.
   */
  @Deprecated public abstract @FloatRange(from = 0.0, to = 1.0) float getVolume();

  public abstract void setVolumeInfo(@NonNull VolumeInfo volumeInfo);

  @NonNull public abstract VolumeInfo getVolumeInfo();

  /**
   * Get latest playback info. Either on-going playback info if current player is playing, or latest
   * playback info available if player is paused.
   *
   * @return latest {@link PlaybackInfo} of current Player.
   */
  @NonNull public abstract PlaybackInfo getLatestPlaybackInfo();

  // Mimic ExoPlayer
  @CallSuper protected final void onPlayerStateUpdated(boolean playWhenReady,
      @State int playbackState) {
    handler.obtainMessage(playbackState, playWhenReady).sendToTarget();
  }

  @CallSuper public void release() {
    handler.removeCallbacksAndMessages(null);
    this.container = null;
  }

  @Override public String toString() {
    return "Toro:Helper{" + "player=" + player + ", container=" + container + '}';
  }
}
