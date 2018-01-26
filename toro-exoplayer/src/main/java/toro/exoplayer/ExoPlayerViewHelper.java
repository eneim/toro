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

package toro.exoplayer;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.util.ArrayList;

import static toro.exoplayer.MediaSourceBuilder.DEFAULT;

/**
 * @author eneim (2018/01/24).
 */

public class ExoPlayerViewHelper extends ToroPlayerHelper {

  @NonNull final Playback.Helper helper;
  @NonNull final Uri mediaUri;
  @Nullable final MediaSourceEventListener listener;

  private EventListeners listeners;

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri mediaUri, @Nullable MediaSourceEventListener listener,
      @NonNull PlayerHub playerHub) {
    super(container, player);
    this.mediaUri = mediaUri;
    this.listener = listener;
    if (!(player.getPlayerView() instanceof SimpleExoPlayerView)) {
      throw new IllegalArgumentException("Require SimpleExoPlayerView");
    }

    listeners = new EventListeners();
    helper = playerHub.createHelper((SimpleExoPlayerView) player.getPlayerView(), //
        mediaUri, DEFAULT, listener != null ? new Handler() : null, listener, listeners);
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    helper.prepare();
    if (playbackInfo != null) helper.setPlaybackInfo(playbackInfo);
  }

  @Override public void release() {
    super.release();
    this.listeners = null;
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

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    return helper.getPlaybackInfo();
  }

  public void addEventListener(@NonNull Player.EventListener listener) {
    //noinspection ConstantConditions
    if (listener != null) this.listeners.add(listener);
  }

  public void removeEventListener(Player.EventListener listener) {
    this.listeners.remove(listener);
  }

  private class EventListeners extends ArrayList<Player.EventListener>
      implements Player.EventListener {

    EventListeners() {
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {
      for (Player.EventListener eventListener : this) {
        eventListener.onTimelineChanged(timeline, manifest);
      }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      for (Player.EventListener eventListener : this) {
        eventListener.onTracksChanged(trackGroups, trackSelections);
      }
    }

    @Override public void onLoadingChanged(boolean isLoading) {
      for (Player.EventListener eventListener : this) {
        eventListener.onLoadingChanged(isLoading);
      }
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      ExoPlayerViewHelper.super.onPlayerStateUpdated(playWhenReady, playbackState);
      for (Player.EventListener eventListener : this) {
        eventListener.onPlayerStateChanged(playWhenReady, playbackState);
      }
    }

    @Override public void onRepeatModeChanged(int repeatMode) {
      for (Player.EventListener eventListener : this) {
        eventListener.onRepeatModeChanged(repeatMode);
      }
    }

    @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      for (Player.EventListener eventListener : this) {
        eventListener.onShuffleModeEnabledChanged(shuffleModeEnabled);
      }
    }

    @Override public void onPlayerError(ExoPlaybackException error) {
      for (Player.EventListener eventListener : this) {
        eventListener.onPlayerError(error);
      }
    }

    @Override public void onPositionDiscontinuity(int reason) {
      for (Player.EventListener eventListener : this) {
        eventListener.onPositionDiscontinuity(reason);
      }
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      for (Player.EventListener eventListener : this) {
        eventListener.onPlaybackParametersChanged(playbackParameters);
      }
    }

    @Override public void onSeekProcessed() {
      for (Player.EventListener eventListener : this) {
        eventListener.onSeekProcessed();
      }
    }
  }
}
