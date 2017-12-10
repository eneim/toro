/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.youtube;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks;
import android.util.Log;
import android.view.View;
import com.google.android.youtube.player.YouTubePlayer;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import im.ene.toro.youtube.YouTubePlayerDialog.InitData;
import java.util.HashMap;
import java.util.Map;

import static im.ene.toro.youtube.YouTubePlayerDialog.newInstance;

/**
 * @author eneim (2017/12/10).
 */

class YouTubePlayerManager implements YouTubePlayerHelper.Callback {

  private static final String TAG = "YouT:Manager";

  private final Activity activity;
  private final FragmentManager fragmentManager;
  private final Map<ToroPlayer, YouTubePlayerHelper> helpers = new HashMap<>();

  private final int orientation;

  YouTubePlayerManager(Activity activity, FragmentManager fragmentManager) {
    this.activity = activity;
    this.orientation = activity.getRequestedOrientation();
    this.fragmentManager = fragmentManager;
    FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentLifecycleCallbacks() {
      @Override public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v,
          Bundle savedInstanceState) {
        if (f instanceof ToroYouTubePlayerFragment) {
          ToroYouTubePlayerFragment fragment = (ToroYouTubePlayerFragment) f;
          YouTubePlayerHelper helper = fragment.getHelperKey();
          if (helper != null) helper.ytFragment = fragment;
        }
      }

      // Actively release resource base on Fragment life-cycle.
      @Override public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
        if (f instanceof ToroYouTubePlayerFragment) {
          ToroYouTubePlayerFragment fragment = (ToroYouTubePlayerFragment) f;
          YouTubePlayerHelper helper = fragment.getHelperKey();
          if (helper != null) {
            helper.release();
            if (helper.ytFragment != null) helper.ytFragment = null;
          }
          fragment.setHelperKey(null);
        }
      }
    };

    this.fragmentManager.registerFragmentLifecycleCallbacks(lifecycleCallbacks, false);
  }

  /// [2017/12/07] TEST: New YouTube player manage mechanism.

  private static final String STATE_KEY_VIDEO_ID = "yt:adapter:video_id";
  private static final String STATE_KEY_FULLSCREEN = "yt:adapter:fullscreen";
  private static final String STATE_KEY_PLAYBACK_INFO = "yt:adapter:playback_info";
  private static final String STATE_KEY_ORIENTATION = "yt:adapter:orientation";

  YouTubePlayerHelper obtainHelper(Container container, @NonNull ToroPlayer player, String video) {
    YouTubePlayerHelper helper = this.helpers.get(player);

    if (helper != null && helper.ytFragment != null) {
      fragmentManager.beginTransaction().remove(helper.ytFragment).commitNow();
    }

    if (helper == null) {
      helper = new YouTubePlayerHelper(this, container, player, video);
      helpers.put(player, helper);
    }

    ToroYouTubePlayerFragment fragment = ToroYouTubePlayerFragment.newInstance();
    fragment.setHelperKey(helper);
    fragmentManager.beginTransaction()
        .replace(player.getPlayerView().getId(), fragment)
        .commitNow();

    return helper;
  }

  void releaseHelper(ToroPlayer player) {
    YouTubePlayerHelper helper = this.helpers.remove(player);
    if (helper != null) {
      if (helper.ytFragment != null) {
        fragmentManager.beginTransaction().remove(helper.ytFragment).commitNow();
      } else {
        // Should not happen. We always try to release the helper along with Fragment's life-cycle.
        helper.release();
      }
    }
  }

  /// Deal with config change when User start Fullscreen YouTube player
  private boolean fullscreenRequested = false;

  void onSaveState(@NonNull Bundle outState) {
    outState.putBoolean(STATE_KEY_FULLSCREEN, fullscreenRequested);
    if (this.initData != null) {
      outState.putString(STATE_KEY_VIDEO_ID, initData.videoId);
      outState.putParcelable(STATE_KEY_PLAYBACK_INFO, initData.playbackInfo);
      outState.putInt(STATE_KEY_ORIENTATION, initData.orientation);
    }
  }

  void onRestoreState(@NonNull Bundle savedState) {
    boolean fullscreenRequested = savedState.getBoolean(STATE_KEY_FULLSCREEN);
    if (fullscreenRequested) {
      String videoId = savedState.getString(STATE_KEY_VIDEO_ID);
      PlaybackInfo playbackInfo = savedState.getParcelable(STATE_KEY_PLAYBACK_INFO);

      if (videoId == null || playbackInfo == null) {
        throw new IllegalStateException("Fullscreen requested with no valid data.");
      }

      int orientation = savedState.getInt(STATE_KEY_ORIENTATION);

      YouTubePlayerDialog playerDialog =
          (YouTubePlayerDialog) fragmentManager.findFragmentByTag(YouTubePlayerDialog.TAG);
      if (playerDialog != null) {
        playerDialog.dismissAllowingStateLoss();
      }

      playerDialog = newInstance(new InitData(videoId, playbackInfo, orientation));
      playerDialog.show(this.fragmentManager, YouTubePlayerDialog.TAG);
    }
  }

  /// YouTubePlayerHelper.Callback
  private YouTubePlayerHelper activeHelper = null;
  private InitData initData;

  @Override public void onPlayerCreated(YouTubePlayerHelper helper, YouTubePlayer player) {
    Log.d(TAG, "onPlayerCreated: " + helper);
    if (this.activeHelper != null) {
      throw new IllegalStateException("Another player is still active.");
    }

    this.activeHelper = helper;
  }

  @Override public void onPlayerDestroyed(YouTubePlayerHelper helper) {
    Log.d(TAG, "onPlayerDestroyed: " + helper);
    if (this.activeHelper == helper) this.activeHelper = null;
  }

  @Override
  public void onFullscreen(YouTubePlayerHelper helper, YouTubePlayer player, boolean fullscreen) {
    if (activeHelper != helper) return;
    if (helper == null || helper.ytFragment == null || helper.youTubePlayer == null) return;
    fullscreenRequested = fullscreen;
    if (fullscreenRequested) {
      initData = new InitData(helper.getVideoId(), helper.getLatestPlaybackInfo(), orientation);
    }
  }
}
