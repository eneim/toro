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

package im.ene.lab.toro.ext;

import android.util.Log;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

/**
 * Created by eneim on 6/6/16.
 *
 * !For debug purpose only
 * {@hide}
 */
/* public */ class YouTubeEventLogger
    implements YouTubePlayer.PlaybackEventListener, YouTubePlayer.PlayerStateChangeListener,
    YouTubePlayer.OnInitializedListener {

  private String TAG = "EventLogger";
  private final boolean D;  // D or I, just to separate log colors.
  private final boolean DEBUG = BuildConfig.DEBUG;

  public YouTubeEventLogger(String id, int position) {
    TAG = "Logger:" + id;
    D = position % 2 == 0;
  }

  @Override
  public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer,
      boolean b) {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onInitializationSuccess() called with: "
          + "provider = ["
          + provider
          + "], youTubePlayer = ["
          + youTubePlayer
          + "], b = ["
          + b
          + "]");
    } else {
      Log.i(TAG, "onInitializationSuccess() called with: "
          + "provider = ["
          + provider
          + "], youTubePlayer = ["
          + youTubePlayer
          + "], b = ["
          + b
          + "]");
    }
  }

  @Override public void onInitializationFailure(YouTubePlayer.Provider provider,
      YouTubeInitializationResult youTubeInitializationResult) {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onInitializationFailure() called with: "
          + "provider = ["
          + provider
          + "], youTubeInitializationResult = ["
          + youTubeInitializationResult
          + "]");
    } else {
      Log.i(TAG, "onInitializationFailure() called with: "
          + "provider = ["
          + provider
          + "], youTubeInitializationResult = ["
          + youTubeInitializationResult
          + "]");
    }
  }

  @Override public void onPlaying() {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onPlaying() called with: " + "");
    } else {
      Log.i(TAG, "onPlaying() called with: " + "");
    }
  }

  @Override public void onPaused() {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onPaused() called with: " + "");
    } else {
      Log.i(TAG, "onPaused() called with: " + "");
    }
  }

  @Override public void onStopped() {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onStopped() called with: " + "");
    } else {
      Log.i(TAG, "onStopped() called with: " + "");
    }
  }

  @Override public void onBuffering(boolean b) {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onBuffering() called with: " + "b = [" + b + "]");
    } else {
      Log.i(TAG, "onBuffering() called with: " + "b = [" + b + "]");
    }
  }

  @Override public void onSeekTo(int i) {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onSeekTo() called with: " + "i = [" + i + "]");
    } else {
      Log.i(TAG, "onSeekTo() called with: " + "i = [" + i + "]");
    }
  }

  @Override public void onLoading() {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onLoading() called with: " + "");
    } else {
      Log.i(TAG, "onLoading() called with: " + "");
    }
  }

  @Override public void onLoaded(String s) {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onLoaded() called with: " + "s = [" + s + "]");
    } else {
      Log.i(TAG, "onLoaded() called with: " + "s = [" + s + "]");
    }
  }

  @Override public void onAdStarted() {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onAdStarted() called with: " + "");
    } else {
      Log.i(TAG, "onAdStarted() called with: " + "");
    }
  }

  @Override public void onVideoStarted() {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onVideoStarted() called with: " + "");
    } else {
      Log.i(TAG, "onVideoStarted() called with: " + "");
    }
  }

  @Override public void onVideoEnded() {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onVideoEnded() called with: " + "");
    } else {
      Log.i(TAG, "onVideoEnded() called with: " + "");
    }
  }

  @Override public void onError(YouTubePlayer.ErrorReason errorReason) {
    if (!DEBUG) return;
    if (D) {
      Log.d(TAG, "onError() called with: " + "errorReason = [" + errorReason + "]");
    } else {
      Log.i(TAG, "onError() called with: " + "errorReason = [" + errorReason + "]");
    }
  }
}
