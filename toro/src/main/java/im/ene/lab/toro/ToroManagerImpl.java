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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import java.util.WeakHashMap;

/**
 * Created by eneim on 1/31/16.
 */
public final class ToroManagerImpl implements ToroManager {

  private static final String TAG = "ToroManager";
  private static final int MESSAGE_PLAYBACK_PROGRESS = 1;
  private final WeakHashMap<Long, Integer> mVideoStates = new WeakHashMap<>();
  private ToroPlayer mCurrentPlayer;
  // This Handler will send Message to Main Thread
  private Handler mMainThreadHandler;
  private Handler.Callback mCallback = new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MESSAGE_PLAYBACK_PROGRESS:
          if (mCurrentPlayer != null) {
            mCurrentPlayer.onPlaybackProgress(mCurrentPlayer.getCurrentPosition(),
                mCurrentPlayer.getDuration());
          }
          mMainThreadHandler.removeMessages(MESSAGE_PLAYBACK_PROGRESS);
          mMainThreadHandler.sendEmptyMessageDelayed(MESSAGE_PLAYBACK_PROGRESS, 250);
          return true;
        default:
          return false;
      }
    }
  };

  public ToroPlayer getPlayer() {
    return this.mCurrentPlayer;
  }

  public void setPlayer(ToroPlayer player) {
    this.mCurrentPlayer = player;
  }

  @Override public void startPlayback() {
    if (mCurrentPlayer != null) {
      mCurrentPlayer.start();
      mCurrentPlayer.onStartPlayback();
    }

    if (mMainThreadHandler != null) {
      mMainThreadHandler.sendEmptyMessageDelayed(MESSAGE_PLAYBACK_PROGRESS, 250);
    }
  }

  @Override public void pausePlayback() {
    if (mCurrentPlayer != null) {
      mCurrentPlayer.pause();
      mCurrentPlayer.onPausePlayback();
    }
  }

  @Override public void saveVideoState(Long videoId, @Nullable Integer position, long duration) {
    if (videoId != null) {
      mVideoStates.put(videoId, position == null ? Integer.valueOf(0) : position);
    }
  }

  @Override public void restoreVideoState(Long videoId) {
    if (mCurrentPlayer == null) {
      return;
    }

    Integer position = mVideoStates.get(videoId);
    if (position == null) {
      position = 0;
    }

    // See {@link android.media.MediaPlayer#seekTo(int)}
    try {
      mCurrentPlayer.seekTo(position);
    } catch (IllegalStateException er) {
      er.printStackTrace();
    }
  }

  @Override public void onRegistered() {
    mMainThreadHandler = new Handler(Looper.getMainLooper(), mCallback);
  }

  @Override public void onUnregistered() {
    mMainThreadHandler.removeCallbacksAndMessages(null);
    mMainThreadHandler = null;
  }
}
