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

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.exoplayer.ui.PlayerView;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Define an interface to control a playback.
 *
 * This interface is designed to be reused across Config change. Implementation must not hold any
 * strong reference to Activity, and if it supports any kind of that, make sure to implicitly clean
 * it up.
 *
 * @param <T> the View that acts as the Player. It should be a {@link SimpleExoPlayerView} or {@link PlayerView}.
 * @author eneim
 * @since 3.4.0
 */

@SuppressWarnings("DeprecatedIsStillUsed")  //
public interface Playable<T> {

  /**
   * Prepare the resource for a {@link SimpleExoPlayer}. This method should:
   * - Request for new SimpleExoPlayer instance if there is not a usable one.
   * - Configure EventListener for it.
   * - If there is non-trivial PlaybackInfo, update it to the SimpleExoPlayer.
   * - If client request to prepare MediaSource, then prepare it.
   *
   * This method must be called before {@link #setPlayerView(Object)}.
   *
   * @param prepareSource if {@code true}, also prepare the MediaSource so that it could be played
   * immediately, if {@code false} just do nothing for the MediaSource.
   */
  void prepare(boolean prepareSource);

  /**
   * Set the {@link T} for this Playable. It is expected that a playback doesn't require a
   * UI, so this setup is optional. But it must be called after the SimpleExoPlayer is prepared,
   * which is after {@link #prepare(boolean)} and before {@link #release()}.
   *
   * Changing the PlayerView during playback is expected, though not always recommended, especially
   * on old Devices with low Android API.
   *
   * @param playerView the PlayerView to set to the SimpleExoPlayer.
   */
  void setPlayerView(@Nullable T playerView);

  /**
   * Get current {@link T} of this Playable.
   *
   * @return current PlayerView instance of this Playable.
   */
  @Nullable T getPlayerView();

  /**
   * Start the playback. If the {@link MediaSource} is not prepared, then also prepare it.
   */
  void play();

  /**
   * Pause the playback.
   */
  void pause();

  /**
   * Reset all resource, so that the playback can start all over again. This is to cleanup the
   * playback for reuse. The SimpleExoPlayer instance must be still usable without calling
   * {@link #prepare(boolean)}.
   */
  void reset();

  /**
   * Release all resource. After this, the SimpleExoPlayer is released to the Player pool and the
   * Playable must call {@link #prepare(boolean)} again to use it again.
   */
  void release();

  /**
   * Get current {@link PlaybackInfo} of the playback.
   *
   * @return current PlaybackInfo of the playback.
   */
  @NonNull PlaybackInfo getPlaybackInfo();

  /**
   * Set the custom {@link PlaybackInfo} for this playback. This could suggest a seek.
   *
   * @param playbackInfo the PlaybackInfo to set for this playback.
   */
  void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo);

  /**
   * Add a new {@link EventListener} to this Playable. As calling {@link #prepare(boolean)} also
   * triggers some internal events, this method should be called before {@link #prepare(boolean)} so
   * that Client could received them all.
   *
   * @param listener the EventListener to add, must be not {@code null}.
   */
  void addEventListener(@NonNull EventListener listener);

  /**
   * Remove an {@link EventListener} from this Playable.
   *
   * @param listener the EventListener to be removed. If null, nothing happens.
   */
  void removeEventListener(EventListener listener);

  /**
   * !This must only work if the Player in use is a {@link ToroExoPlayer}.
   */
  void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener);

  void removeOnVolumeChangeListener(@Nullable ToroPlayer.OnVolumeChangeListener listener);

  /**
   * Check if current Playable is playing or not.
   *
   * @return {@code true} if this Playable is playing, {@code false} otherwise.
   */
  boolean isPlaying();

  /**
   * Change the volume of current playback.
   *
   * @param volume the volume value to be set. Must be a {@code float} of range from 0 to 1.
   * @deprecated use {@link #setVolumeInfo(VolumeInfo)} instead.
   */
  @Deprecated void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume);

  /**
   * Obtain current volume value. The returned value is a {@code float} of range from 0 to 1.
   *
   * @return current volume value.
   * @deprecated use {@link #getVolumeInfo()} instead.
   */
  @Deprecated @FloatRange(from = 0.0, to = 1.0) float getVolume();

  /**
   * Update playback's volume.
   *
   * @param volumeInfo the {@link VolumeInfo} to update to.
   * @return {@code true} if current Volume info is updated, {@code false} otherwise.
   */
  boolean setVolumeInfo(@NonNull VolumeInfo volumeInfo);

  /**
   * Get current {@link VolumeInfo}.
   */
  @NonNull VolumeInfo getVolumeInfo();

  /**
   * Same as {@link ExoPlayer#setPlaybackParameters(PlaybackParameters)}
   */
  void setParameters(@Nullable PlaybackParameters parameters);

  /**
   * Same as {@link ExoPlayer#getPlaybackParameters()}
   */
  @Nullable PlaybackParameters getParameters();

  // Combine necessary interfaces.
  interface EventListener
      extends ExoPlayer.EventListener, SimpleExoPlayer.VideoListener, TextRenderer.Output,
      MetadataRenderer.Output {

  }

  /** Default empty implementation */
  class DefaultEventListener implements EventListener {

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override public void onLoadingChanged(boolean isLoading) {

    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override public void onPlayerError(ExoPlaybackException error) {

    }

    @Override public void onPositionDiscontinuity() {

    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio) {

    }

    @Override public void onRenderedFirstFrame() {

    }

    @Override public void onCues(List<Cue> cues) {

    }

    @Override public void onMetadata(Metadata metadata) {

    }
  }

  /** List of EventListener */
  class EventListeners extends ArrayList<EventListener> implements EventListener {

    EventListeners() {
    }

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      for (EventListener eventListener : this) {
        eventListener.onVideoSizeChanged(width, height, unAppliedRotationDegrees,
            pixelWidthHeightRatio);
      }
    }

    @Override public void onRenderedFirstFrame() {
      for (EventListener eventListener : this) {
        eventListener.onRenderedFirstFrame();
      }
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {
      for (EventListener eventListener : this) {
        eventListener.onTimelineChanged(timeline, manifest);
      }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      for (EventListener eventListener : this) {
        eventListener.onTracksChanged(trackGroups, trackSelections);
      }
    }

    @Override public void onLoadingChanged(boolean isLoading) {
      for (EventListener eventListener : this) {
        eventListener.onLoadingChanged(isLoading);
      }
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      for (EventListener eventListener : this) {
        eventListener.onPlayerStateChanged(playWhenReady, playbackState);
      }
    }

    @Override public void onPlayerError(ExoPlaybackException error) {
      for (EventListener eventListener : this) {
        eventListener.onPlayerError(error);
      }
    }

    @Override public void onPositionDiscontinuity() {
      for (EventListener eventListener : this) {
        eventListener.onPositionDiscontinuity();
      }
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      for (EventListener eventListener : this) {
        eventListener.onPlaybackParametersChanged(playbackParameters);
      }
    }

    @Override public void onCues(List<Cue> cues) {
      for (EventListener eventListener : this) {
        eventListener.onCues(cues);
      }
    }

    @Override public void onMetadata(Metadata metadata) {
      for (EventListener eventListener : this) {
        eventListener.onMetadata(metadata);
      }
    }
  }
}
