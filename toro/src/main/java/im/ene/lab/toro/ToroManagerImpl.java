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

package im.ene.lab.toro;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.WeakHashMap;

/**
 * Created by eneim on 1/31/16.
 */
public final class ToroManagerImpl implements ToroManager {

  private static final String TAG = "ToroManager";
  private final WeakHashMap<Long, Integer> mVideoStates = new WeakHashMap<>();
  private ToroPlayer mCurrentPlayer;

  public ToroPlayer getPlayer() {
    return this.mCurrentPlayer;
  }

  public void setPlayer(ToroPlayer player) {
    this.mCurrentPlayer = player;
  }

  @Override public void startVideo(ToroPlayer player) {
    player.start();
  }

  @Override public void pauseVideo(ToroPlayer player) {
    player.pause();
  }

  @Override
  public void saveVideoState(Long videoId, @Nullable Integer position, long duration) {
    if (videoId != null) {
      mVideoStates.put(videoId, position == null ? 0 : position);
    }
  }

  @Override public void restoreVideoState(ToroPlayer player, Long videoId) {
    Log.d(TAG, "restoreVideoState() called with: " + "player = [" + player + "], videoId = ["
        + videoId + "]");
    Integer position = mVideoStates.get(videoId);
    if (position == null) {
      position = 0;
    }

    Log.d(TAG, "restoreVideoState: " + position);
    // See {@link android.media.MediaPlayer#seekTo(int)}
    try {
      player.seekTo(position);
    } catch (IllegalStateException er) {
      er.printStackTrace();
    }
  }
}
