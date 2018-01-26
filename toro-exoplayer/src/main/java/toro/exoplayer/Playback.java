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

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import im.ene.toro.media.PlaybackInfo;

/**
 * @author eneim (2018/01/23).
 */

public interface Playback {

  interface Helper {

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
  }
}
