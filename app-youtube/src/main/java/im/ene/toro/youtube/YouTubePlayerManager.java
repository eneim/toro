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
import android.support.v4.app.FragmentManager;
import android.view.View;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.youtube.YouTubePlayerDialog.FullscreenRequestType;
import im.ene.toro.youtube.YouTubePlayerDialog.InitData;
import java.util.HashMap;
import java.util.Map;

import static im.ene.toro.youtube.YouTubePlayerDialog.newInstance;

/**
 * This class relies on the behavior of {@link FragmentManager}, so it must be tight to
 * {@link Activity}'s life cycle.
 *
 * @author eneim (2017/12/10).
 */

final class YouTubePlayerManager {

  private final FragmentManager manager;
  private final Map<ToroPlayer, YouTubePlayerHelper> helpers = new HashMap<>();

  private final OrientationHelper orientationHelper = new OrientationHelper();

  YouTubePlayerManager(FragmentManager manager) {
    this.manager = manager;
  }

  /// [2017/12/07] TEST: New YouTube player manage mechanism.

  YouTubePlayerHelper obtainHelper(@NonNull ToroPlayer player, String videoId) {
    int viewId = ToroUtil.checkNotNull(player.getPlayerView()).getId();
    if (viewId == View.NO_ID) {
      throw new IllegalStateException("PlayerView must have a valid Id. Found: " + viewId);
    }

    YouTubePlayerHelper helper = this.helpers.get(player);
    // Dirty helper, remove the Fragment first.
    if (helper != null && helper.ytFragment != null) {
      manager.beginTransaction().remove(helper.ytFragment).commitNow();
    }

    if (helper == null) {
      helper = new YouTubePlayerHelper(player, videoId, orientationHelper);
      helpers.put(player, helper);
    }

    ToroYouTubePlayerFragment fragment = ToroYouTubePlayerFragment.newInstance();
    fragment.setPlayerHelper(helper);
    manager.beginTransaction().replace(viewId, fragment).commitNow();
    return helper;
  }

  void releaseHelper(ToroPlayer player) {
    // We don't update the map by Fragment's lifecycle, instead we manually do it here.
    YouTubePlayerHelper helper = this.helpers.remove(player);
    if (helper != null) {
      if (helper.ytFragment != null) {  // Not been released by Fragment lifecycle.
        manager.beginTransaction().remove(helper.ytFragment).commitNow();
      } else {
        // Should not happen. We always try to release the helper by Fragment's life-cycle.
        helper.release();
      }
    }
  }

  /// [2017/12/09] TEST: Support Fullscreen for Activity's config change (no manifest attribute).
  /// TODO [20180402] Re-think about how we deal with orientation change.

  private static final String STATE_KEY_VIDEO_ORDER = "yt:adapter:video_order";
  private static final String STATE_KEY_VIDEO_ID = "yt:adapter:video_id";
  private static final String STATE_KEY_FULLSCREEN = "yt:adapter:fullscreen";
  private static final String STATE_KEY_PLAYBACK_INFO = "yt:adapter:playback_info";
  private static final String STATE_KEY_ORIENTATION = "yt:adapter:orientation";

  /// Deal with config change when User start Fullscreen YouTube player
  //private FullscreenRequestType fullscreenRequest = null;
  //private InitData initData;

  //void onSaveState(@NonNull Bundle outState, @Nullable InitData data, boolean configChange) {
  //  if (fullscreenRequest == null) {
  //    if (configChange && data != null) {
  //      fullscreenRequest = FullscreenRequestType.CONFIG_CHANGE;
  //      this.initData = data;
  //    }
  //  }
  //
  //  if (fullscreenRequest != null && initData != null) {
  //    outState.putSerializable(STATE_KEY_FULLSCREEN, fullscreenRequest);
  //    outState.putInt(STATE_KEY_VIDEO_ORDER, initData.adapterOrder);
  //    outState.putString(STATE_KEY_VIDEO_ID, initData.videoId);
  //    outState.putParcelable(STATE_KEY_PLAYBACK_INFO, initData.playbackInfo);
  //    outState.putInt(STATE_KEY_ORIENTATION, initData.orientation);
  //  }
  //}

  //void onRestoreState(@NonNull Bundle savedState, boolean shouldFullScreen) {
  //  FullscreenRequestType fullscreenRequested =
  //      (FullscreenRequestType) savedState.getSerializable(STATE_KEY_FULLSCREEN);
  //  if (fullscreenRequested != null && shouldFullScreen) {
  //    String videoId = savedState.getString(STATE_KEY_VIDEO_ID);
  //    PlaybackInfo playbackInfo = savedState.getParcelable(STATE_KEY_PLAYBACK_INFO);
  //    int videoOrder = savedState.getInt(STATE_KEY_VIDEO_ORDER);
  //
  //    if (videoId == null || playbackInfo == null || videoOrder < 0) {
  //      throw new IllegalStateException("Fullscreen requested with no valid data.");
  //    }
  //
  //    int orientation = savedState.getInt(STATE_KEY_ORIENTATION);
  //
  //    YouTubePlayerDialog playerDialog =
  //        (YouTubePlayerDialog) manager.findFragmentByTag(YouTubePlayerDialog.TAG);
  //    if (playerDialog != null) {
  //      playerDialog.dismissAllowingStateLoss();
  //    }
  //
  //    playerDialog = newInstance(new InitData(videoOrder, videoId, playbackInfo, orientation));
  //    playerDialog.show(this.manager, YouTubePlayerDialog.TAG);
  //  }
  //}
}
