/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.widget.VideoView;

/**
 * Created by eneim on 10/2/16.
 */

public interface BaseMediaPlayer {

  /**
   * See {@link android.media.MediaPlayer#prepareAsync()}
   *
   * @param playWhenReady Immediately start playback when Ready.
   */
  void preparePlayer(boolean playWhenReady);

  /**
   * See {@link VideoView#start()}
   */
  void start();

  /**
   * See {@link VideoView#pause()}
   */
  void pause();

  /**
   * See {@link VideoView#stopPlayback()} VideoView#stopPlayback()
   */
  void stop();

  /**
   * Release player's resource.
   */
  void releasePlayer();

  /**
   * See {@link VideoView#getDuration()}
   *
   * @return media's duration.
   */
  long getDuration();

  /**
   * See {@link VideoView#getCurrentPosition()}
   *
   * @return current playback position.
   */
  long getCurrentPosition();

  /**
   * See {@link VideoView#seekTo(int)}
   *
   * @param pos seek to specific position.
   */
  void seekTo(long pos);

  /**
   * See {@link VideoView#isPlaying()}
   *
   * @return {@code true} if the media is being played, {@code false} otherwise.
   */
  boolean isPlaying();

  /**
   * See {@link android.media.MediaPlayer#setVolume(float, float)}
   *
   * @param volume volume level.
   */
  void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume);

  /**
   * See {@link VideoView#getBufferPercentage()}
   *
   * @return current buffered percentage.
   */
  @IntRange(from = 0, to = 100) int getBufferPercentage();

  /**
   * Get the audio session id for the player used by this VideoView. This can be used to
   * apply audio effects to the audio track of a video.
   *
   * See {@link VideoView#getAudioSessionId()}
   *
   * @return The audio session, or 0 if there was an error.
   */
  // TODO Comment out. Un-comment if necessary
  // int getAudioSessionId();

  // TODO Comment out. Un-comment if necessary
  // void setBackgroundAudioEnabled(boolean enabled);

}
