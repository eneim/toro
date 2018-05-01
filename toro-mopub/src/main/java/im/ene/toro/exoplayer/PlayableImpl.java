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

package im.ene.toro.exoplayer;

import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

/**
 * [20180225]
 *
 * Instance of {@link Playable} should be reusable. Retaining instance of Playable across config
 * change must guarantee that all {@link EventListener} are cleaned up on config change.
 *
 * @author eneim (2018/02/25).
 */
@SuppressWarnings("WeakerAccess") //
class PlayableImpl extends BasePlayableImpl<SimpleExoPlayerView> {

  PlayableImpl(ExoCreator creator, Uri uri, String fileExt) {
    super(creator, uri, fileExt);
  }

  @CallSuper @Override public void setPlayerView(@Nullable SimpleExoPlayerView playerView) {
    if (this.playerView == playerView) return;
    if (playerView == null) {
      this.playerView.setPlayer(null);
    } else {
      if (this.player != null) {
        SimpleExoPlayerView.switchTargetView(this.player, this.playerView, playerView);
      }
    }

    this.playerView = playerView;
  }

  @Override protected void ensurePlayerView() {
    if (playerView != null && playerView.getPlayer() != player) playerView.setPlayer(player);
  }
}
