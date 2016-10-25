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

import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eneim on 1/31/16.
 *
 * Extension/Implementation of {@link MediaPlayerManager}. Can be used as a delegation.
 */
public final class MediaPlayerManagerImpl implements MediaPlayerManager {

  private static final String TAG = "MediaPlayerManager";

  private final Map<String, Long> mVideoStates = new HashMap<>();

  private ToroPlayer mPlayer;

  @Override public final ToroPlayer getPlayer() {
    return mPlayer;
  }

  @Override public final void setPlayer(ToroPlayer player) {
    this.mPlayer = player;
  }

  @Override public void startPlayback() {
    // Should prepare video if need?
    if (mPlayer != null) {
      if (!mPlayer.isPrepared()) {
        mPlayer.preparePlayer(false);
      } else {
        mPlayer.start();
      }
    }
  }

  @Override public void pausePlayback() {
    if (mPlayer != null) {
      mPlayer.pause();
    }
  }

  @Override public void stopPlayback() {
    if (mPlayer != null) {
      mPlayer.stop();
    }
  }

  @Override public void saveVideoState(String videoId, @Nullable Long position, long duration) {
    if (videoId != null) {
      mVideoStates.put(videoId, position == null ? Long.valueOf(0) : position);
    }
  }

  @Override public void restoreVideoState(String videoId) {
    if (mPlayer == null) {
      return;
    }

    Long position = mVideoStates.get(videoId);
    if (position == null) {
      position = 0L;
    }

    try {
      mPlayer.seekTo(position);
    } catch (IllegalStateException er) {
      er.printStackTrace();
    }
  }

  @Nullable @Override public Long getSavedPosition(String videoId) {
    return mVideoStates.get(videoId);
  }

  @Override public void onRegistered() {

  }

  @Override public void onUnregistered() {

  }
}
