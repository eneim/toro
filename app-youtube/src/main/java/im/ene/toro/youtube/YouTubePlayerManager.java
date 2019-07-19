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
import android.app.FragmentManager;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * This class relies on the behavior of {@link FragmentManager}, so it must be tight to
 * {@link Activity}'s life cycle.
 *
 * @author eneim (2017/12/10).
 */

final class YouTubePlayerManager {

  private final FragmentManager manager;
  private final YouTubePlayerHelper.Callback callback;
  private final Map<ToroPlayer, YouTubePlayerHelper> helpers = new HashMap<>();

  YouTubePlayerManager(@NonNull FragmentManager manager,
      @Nullable YouTubePlayerHelper.Callback callback) {
    this.manager = manager;
    this.callback = callback;
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
      helper = new YouTubePlayerHelper(player, videoId);
      helper.setCallback(this.callback);
      helpers.put(player, helper);
    }

    ToroYouTubePlayerFragment fragment = ToroYouTubePlayerFragment.newInstance();
    fragment.setPlayerHelper(helper);
    manager.beginTransaction().replace(viewId, fragment).commitAllowingStateLoss();
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
      helper.setCallback(null);
    }
  }
}
