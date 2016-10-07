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

package im.ene.toro.exoplayer2;

import android.support.annotation.NonNull;
import android.view.View;
import com.google.android.exoplayer2.ExoPlayer;
import im.ene.lab.toro.PlayerViewHelper;
import im.ene.lab.toro.ToroPlayer;

/**
 * Created by eneim on 10/3/16.
 */

public class ExoPlayerViewHelper extends PlayerViewHelper implements PlayerCallback {

  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull View itemView) {
    super(player, itemView);
  }

  @Override public void onPlayerStateChanged(boolean playWhenReady, @State int playbackState) {
    switch (playbackState) {
      case ExoPlayer.STATE_IDLE:
        break;
      case ExoPlayer.STATE_BUFFERING:
        this.onPrepared(this.itemView, this.itemView.getParent());
        this.player.onVideoPrepared();
        break;
      case ExoPlayer.STATE_READY:
        if (playWhenReady) {
          this.player.onPlaybackStarted();
        } else {
          this.player.onPlaybackPaused();
        }
        break;
      case ExoPlayer.STATE_ENDED:
        this.onCompletion();
        this.player.onPlaybackCompleted();
        this.player.releasePlayer();
        break;
      default:
        break;
    }
  }

  @Override public boolean onPlayerError(Exception error) {
    return true;
  }
}
