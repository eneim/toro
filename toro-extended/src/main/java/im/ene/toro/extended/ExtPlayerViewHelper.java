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

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.google.android.exoplayer2.ExoPlayer;
import im.ene.toro.PlayerManager;

/**
 * @author eneim
 * @since 10/5/16
 */

public class ExtPlayerViewHelper extends LongClickableViewHelper {

  private static final String TAG = "ToroLib:ExtHelper";

  public ExtPlayerViewHelper(@NonNull ExtToroPlayer player, @NonNull View itemView) {
    super(player, itemView);
  }

  @Override public boolean handleMessage(Message msg) {
    boolean handled = super.handleMessage(msg);
    int state = msg.what;

    if (state == ExoPlayer.STATE_ENDED) {
      final ExtToroPlayer.Target nextTarget = ((ExtToroPlayer) this.player).getNextTarget();
      final PlayerManager manager = super.getPlayerManager(this.itemView.getParent());
      switch (nextTarget) {
        case NEXT_PLAYER:
          if (manager instanceof ExtToroAdapter) {
            ((ExtToroAdapter) manager).scrollToNextVideoFromPosition(
                ((RecyclerView.ViewHolder) player).getAdapterPosition());
          }
          break;
        // case PREV_PLAYER:
        // Currently this is not supported
        //  break;
        case THIS_PLAYER:
          // immediately repeat
          if (manager != null) {
            manager.setPlayer(player);
            manager.restorePlaybackState(player.getMediaId());
            manager.startPlayback();
          }
          break;
        case NONE:
        default:
          break;
      }
    }

    return handled;
  }
}
