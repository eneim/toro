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

package im.ene.toro.extended;

import android.support.annotation.NonNull;
import android.view.View;
import com.google.android.exoplayer2.ExoPlayer;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.VideoPlayerManager;
import im.ene.toro.exoplayer2.ExoPlayerViewHelper;
import im.ene.toro.exoplayer2.State;

/**
 * Created by eneim on 10/5/16.
 */

public class ExtendedPlayerViewHelper extends ExoPlayerViewHelper {

  public ExtendedPlayerViewHelper(@NonNull ToroPlayer player, @NonNull View itemView) {
    super(player, itemView);
    if (!(player instanceof ExtToroPlayer)) {
      throw new IllegalArgumentException("This helper requires a ExtToroPlayer");
    }
  }

  @Override public void onPlayerStateChanged(boolean playWhenReady, @State int playbackState) {
    final ExtToroPlayer.Target nextTarget = ((ExtToroPlayer) this.player).getNextTarget();
    // get Next player, scroll to it
    final VideoPlayerManager manager = super.getPlayerManager(this.itemView.getParent());
    if (playbackState == ExoPlayer.STATE_ENDED) {
      super.onPlayerStateChanged(playWhenReady, playbackState);
      switch (nextTarget) {
        case NEXT_PLAYER:
          // TODO
          break;
        case PREV_PLAYER:
          // TODO
          break;
        case THIS:
          // immediately repeat
          if (manager != null) {
            manager.restoreVideoState(player.getMediaId());
            manager.startPlayback();
          }
          break;
        case NONE:
        default:
          break;
      }
    } else {
      super.onPlayerStateChanged(playWhenReady, playbackState);
    }
  }
}
