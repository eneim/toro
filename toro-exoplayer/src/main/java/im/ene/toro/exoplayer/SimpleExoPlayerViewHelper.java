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
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import im.ene.toro.ToroPlayer;
import im.ene.toro.widget.Container;

/**
 * {@link Deprecated}. Keep this for backward compatibility. Will be removed from 3.5.0.
 *
 * @author eneim (2018/02/05).
 */

@Deprecated //
public final class SimpleExoPlayerViewHelper extends ExoPlayerViewHelper {

  public SimpleExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri) {
    super(container, player, uri);
  }

  private ListenerWrapper listener;
  private Player.EventListener eventListener;

  public final void setEventListener(Player.EventListener eventListener) {
    if (this.eventListener == eventListener) return;  // nothing change
    if (this.listener != null) { // == this.eventListener != null
      // new listener is different, so we clean current listener first.
      super.removeEventListener(this.listener);
      this.listener = null;
    }

    this.eventListener = eventListener;
    //noinspection StatementWithEmptyBody
    if (this.eventListener == null) { // Client is trying to clear current listener.
      // do nothing, cleanup is done up there, but keep this block for acknowledgement.
    } else {  // eventListener is not null --> Client is setting an actual listener.
      this.listener = new ListenerWrapper(this.eventListener);
      super.addEventListener(this.listener);
    }
  }

  static class ListenerWrapper extends Playable.DefaultEventListener {
    @NonNull final Player.EventListener weakerDelegate; // weaker than Playable.EventListener.

    ListenerWrapper(@NonNull Player.EventListener weakerDelegate) {
      this.weakerDelegate = weakerDelegate;
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
      weakerDelegate.onTimelineChanged(timeline, manifest, reason);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      weakerDelegate.onTracksChanged(trackGroups, trackSelections);
    }

    @Override public void onLoadingChanged(boolean isLoading) {
      weakerDelegate.onLoadingChanged(isLoading);
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      weakerDelegate.onPlayerStateChanged(playWhenReady, playbackState);
    }

    @Override public void onRepeatModeChanged(int repeatMode) {
      weakerDelegate.onRepeatModeChanged(repeatMode);
    }

    @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      weakerDelegate.onShuffleModeEnabledChanged(shuffleModeEnabled);
    }

    @Override public void onPlayerError(ExoPlaybackException error) {
      weakerDelegate.onPlayerError(error);
    }

    @Override public void onPositionDiscontinuity(int reason) {
      weakerDelegate.onPositionDiscontinuity(reason);
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      weakerDelegate.onPlaybackParametersChanged(playbackParameters);
    }

    @Override public void onSeekProcessed() {
      weakerDelegate.onSeekProcessed();
    }
  }
}
