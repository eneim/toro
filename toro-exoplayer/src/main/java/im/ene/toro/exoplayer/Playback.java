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
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import im.ene.toro.media.PlaybackInfo;
import java.util.List;

/**
 * @author eneim (2018/01/23).
 */

public interface Playback {

  interface EventListener extends Player.EventListener, SimpleExoPlayer.VideoListener, TextOutput {

  }

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

    @Override public void onRepeatModeChanged(int repeatMode) {

    }

    @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override public void onPlayerError(ExoPlaybackException error) {

    }

    @Override public void onPositionDiscontinuity(int reason) {

    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override public void onSeekProcessed() {

    }

    @Override public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio) {

    }

    @Override public void onRenderedFirstFrame() {

    }

    @Override public void onCues(List<Cue> cues) {

    }
  }

  interface Helper {

    boolean initialized();

    void prepare();

    void play();

    void pause();

    // Should not be called before prepare() or after release()
    boolean isPlaying();

    void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume);

    @FloatRange(from = 0.0, to = 1.0) float getVolume();

    long getDuration();

    void reset();

    @NonNull PlaybackInfo getPlaybackInfo();

    void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo);

    void release();

    void recycle();
  }
}
