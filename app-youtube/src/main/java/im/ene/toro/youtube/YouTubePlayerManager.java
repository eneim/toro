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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks;
import android.view.View;
import com.google.android.youtube.player.YouTubePlayer;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.youtube.YouTubePlayerDialog.FullscreenRequestType;
import im.ene.toro.youtube.YouTubePlayerDialog.InitData;
import java.util.HashMap;
import java.util.Map;

import static im.ene.toro.youtube.YouTubePlayerDialog.newInstance;

/**
 * @author eneim (2017/12/10).
 */

final class YouTubePlayerManager implements YouTubePlayerHelper.Callback {

  private static final String TAG = "Toro:Yt:Manager";

  private final FragmentManager manager;
  private final Map<ToroPlayer, YouTubePlayerHelper> helpers = new HashMap<>();

  private final int orientation;

  YouTubePlayerManager(Activity activity, FragmentManager manager) {
    this.orientation = activity.getRequestedOrientation();
    this.manager = manager;
    FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentLifecycleCallbacks() {
      @Override public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v,
          Bundle savedInstanceState) {
        if (f instanceof ToroYouTubePlayerFragment) {
          ToroYouTubePlayerFragment fragment = (ToroYouTubePlayerFragment) f;
          YouTubePlayerHelper helper = fragment.getHelperKey();
          if (helper != null) helper.ytFragment = fragment;
        }
      }

      @Override public void onFragmentStopped(FragmentManager fm, Fragment f) {
        if (f instanceof ToroYouTubePlayerFragment) {
          ToroYouTubePlayerFragment fragment = (ToroYouTubePlayerFragment) f;
          YouTubePlayerHelper helper = fragment.getHelperKey();
          if (helper != null) helper.release();
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

    this.manager.registerFragmentLifecycleCallbacks(lifecycleCallbacks, false);
  }

  /// [2017/12/07] TEST: New YouTube player manage mechanism.
  /// [2017/12/09] TEST: Support Fullscreen for Activity's config change (no manifest attribute).

  private static final String STATE_KEY_VIDEO_ORDER = "yt:adapter:video_order";
  private static final String STATE_KEY_VIDEO_ID = "yt:adapter:video_id";
  private static final String STATE_KEY_FULLSCREEN = "yt:adapter:fullscreen";
  private static final String STATE_KEY_PLAYBACK_INFO = "yt:adapter:playback_info";
  private static final String STATE_KEY_ORIENTATION = "yt:adapter:orientation";

  YouTubePlayerHelper obtainHelper(@NonNull ToroPlayer player, String video) {
    YouTubePlayerHelper helper = this.helpers.get(player);
    // Dirty helper, remove the Fragment first.
    if (helper != null && helper.ytFragment != null) {
      manager.beginTransaction().remove(helper.ytFragment).commitNow();
    }

    if (helper == null) {
      helper = new YouTubePlayerHelper(this, player, video);
      helpers.put(player, helper);
    }

    ToroYouTubePlayerFragment fragment = ToroYouTubePlayerFragment.newInstance();
    fragment.setHelperKey(helper);
    manager.beginTransaction().replace(player.getPlayerView().getId(), fragment).commitNow();

    return helper;
  }

  void releaseHelper(ToroPlayer player) {
    YouTubePlayerHelper helper = this.helpers.remove(player);
    if (helper != null) {
      if (helper.ytFragment != null) {
        manager.beginTransaction().remove(helper.ytFragment).commitNow();
      } else {
        // Should not happen. We always try to release the helper along with Fragment's life-cycle.
        helper.release();
      }
    }
  }

  /// Deal with config change when User start Fullscreen YouTube player
  private FullscreenRequestType fullscreenRequest = null;
  private InitData initData;

  void onSaveState(@NonNull Bundle outState, @Nullable InitData data, boolean configChange) {
    if (fullscreenRequest == null) {
      if (configChange && data != null) {
        fullscreenRequest = FullscreenRequestType.CONFIG_CHANGE;
        this.initData = data;
      }
    }

    if (fullscreenRequest != null && initData != null) {
      outState.putSerializable(STATE_KEY_FULLSCREEN, fullscreenRequest);
      outState.putInt(STATE_KEY_VIDEO_ORDER, initData.adapterOrder);
      outState.putString(STATE_KEY_VIDEO_ID, initData.videoId);
      outState.putParcelable(STATE_KEY_PLAYBACK_INFO, initData.playbackInfo);
      outState.putInt(STATE_KEY_ORIENTATION, initData.orientation);
    }
  }

  /**
   * @param savedState saved state
   * @param shouldFullScreen Current Window is suitable for fullscreen playback
   */
  void onRestoreState(@NonNull Bundle savedState, boolean shouldFullScreen) {
    FullscreenRequestType fullscreenRequested =
        (FullscreenRequestType) savedState.getSerializable(STATE_KEY_FULLSCREEN);
    if (fullscreenRequested != null && shouldFullScreen) {
      String videoId = savedState.getString(STATE_KEY_VIDEO_ID);
      PlaybackInfo playbackInfo = savedState.getParcelable(STATE_KEY_PLAYBACK_INFO);
      int videoOrder = savedState.getInt(STATE_KEY_VIDEO_ORDER);

      if (videoId == null || playbackInfo == null || videoOrder < 0) {
        throw new IllegalStateException("Fullscreen requested with no valid data.");
      }

      int orientation = savedState.getInt(STATE_KEY_ORIENTATION);

      YouTubePlayerDialog playerDialog =
          (YouTubePlayerDialog) manager.findFragmentByTag(YouTubePlayerDialog.TAG);
      if (playerDialog != null) {
        playerDialog.dismissAllowingStateLoss();
      }

      playerDialog = newInstance(new InitData(videoOrder, videoId, playbackInfo, orientation));
      playerDialog.show(this.manager, YouTubePlayerDialog.TAG);
    }
  }

  /// YouTubePlayerHelper.Callback
  private YouTubePlayerHelper activeHelper = null;

  @Override public void onPlayerCreated(YouTubePlayerHelper helper, YouTubePlayer player) {
    if (this.activeHelper != null) {
      throw new IllegalStateException("Another player is still active.");
    }

    this.activeHelper = helper;
  }

  @Override public void onPlayerDestroyed(YouTubePlayerHelper helper) {
    if (this.activeHelper == helper) this.activeHelper = null;
  }

  @Override
  public void onFullscreen(YouTubePlayerHelper helper, YouTubePlayer player, boolean fullscreen) {
    if (activeHelper != helper) return;
    if (helper == null || helper.ytFragment == null || helper.youTubePlayer == null) return;
    if (fullscreen) fullscreenRequest = FullscreenRequestType.USER_REQUEST;
    if (fullscreenRequest != null) {
      this.initData = new InitData(helper.getToroPlayer().getPlayerOrder(), helper.getVideoId(),
          helper.getLatestPlaybackInfo(), orientation);
    }
  }
}
