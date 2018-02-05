/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eneim (2018/01/24).
 */

@SuppressWarnings("WeakerAccess") //
public class ExoPlayerViewHelper extends ToroPlayerHelper {

  private static final String TAG = "Toro:ExoPlayer";

  @NonNull private final Playback.Helper helper;
  @NonNull /* p */ final EventListeners listeners;

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri) {
    this(container, player, uri, ToroExo.with(container.getContext()).getHub());
  }

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri, @NonNull PlayerHub playerHub) {
    this(container, player, uri, null, playerHub);
  }

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri, @Nullable Playback.EventListener eventListener,
      @NonNull PlayerHub playerHub) {
    super(container, player);
    if (!(player.getPlayerView() instanceof SimpleExoPlayerView)) {
      throw new IllegalArgumentException("Require SimpleExoPlayerView");
    }

    listeners = new EventListeners();
    if (eventListener != null) listeners.add(eventListener);
    helper = playerHub.createHelper((SimpleExoPlayerView) player.getPlayerView(), uri, listeners);
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    Log.d(TAG, "initialize() called with: playbackInfo = [" + playbackInfo + "]");
    if (!helper.initialized()) helper.prepare();
    if (playbackInfo != null) {
      helper.setPlaybackInfo(playbackInfo);
    }
  }

  @Override public void release() {
    super.release();
    helper.release();
    this.listeners.clear();
  }

  @Override public void play() {
    helper.play();
  }

  @Override public void pause() {
    helper.pause();
  }

  @Override public boolean isPlaying() {
    return helper.isPlaying();
  }

  @Override public void setVolume(float volume) {
    helper.setVolume(volume);
  }

  @Override public float getVolume() {
    return helper.getVolume();
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    return helper.getPlaybackInfo();
  }

  public void addEventListener(@NonNull Playback.EventListener listener) {
    //noinspection ConstantConditions
    if (listener != null) this.listeners.add(listener);
  }

  public void removeEventListener(Playback.EventListener listener) {
    this.listeners.remove(listener);
  }

  private class EventListeners extends ArrayList<Playback.EventListener>
      implements Playback.EventListener {

    EventListeners() {
    }

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onVideoSizeChanged(width, height, unAppliedRotationDegrees,
            pixelWidthHeightRatio);
      }
    }

    @Override public void onRenderedFirstFrame() {
      for (Playback.EventListener eventListener : this) {
        eventListener.onRenderedFirstFrame();
      }
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onTimelineChanged(timeline, manifest);
      }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onTracksChanged(trackGroups, trackSelections);
      }
    }

    @Override public void onLoadingChanged(boolean isLoading) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onLoadingChanged(isLoading);
      }
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      ExoPlayerViewHelper.super.onPlayerStateUpdated(playWhenReady, playbackState);
      for (Playback.EventListener eventListener : this) {
        eventListener.onPlayerStateChanged(playWhenReady, playbackState);
      }
    }

    @Override public void onRepeatModeChanged(int repeatMode) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onRepeatModeChanged(repeatMode);
      }
    }

    @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onShuffleModeEnabledChanged(shuffleModeEnabled);
      }
    }

    @Override public void onPlayerError(ExoPlaybackException error) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onPlayerError(error);
      }
    }

    @Override public void onPositionDiscontinuity(int reason) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onPositionDiscontinuity(reason);
      }
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onPlaybackParametersChanged(playbackParameters);
      }
    }

    @Override public void onSeekProcessed() {
      for (Playback.EventListener eventListener : this) {
        eventListener.onSeekProcessed();
      }
    }

    @Override public void onCues(List<Cue> cues) {
      for (Playback.EventListener eventListener : this) {
        eventListener.onCues(cues);
      }
    }
  }
}
