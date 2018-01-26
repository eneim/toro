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

import android.support.annotation.NonNull;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.media.PlaybackInfo;

/**
 * @author eneim (2018/01/05).
 */

public interface Playback {

  void play();

  void pause();

  boolean isPlaying();

  float getVolume();

  void setVolume(float volume);

  PlaybackInfo getPlaybackInfo();

  void setPlaybackInfo(PlaybackInfo playbackInfo);

  void release();

  class Resource {

    @NonNull final SimpleExoPlayerView playerView;
    @NonNull final MediaSource mediaSource;

    Resource(@NonNull SimpleExoPlayerView playerView, @NonNull MediaSource mediaSource) {
      this.playerView = playerView;
      this.mediaSource = mediaSource;
    }
  }
}
