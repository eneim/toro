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

package im.ene.toro;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

/**
 * Created by eneim on 2/1/16.
 *
 * A helper class to support Video's callbacks from {@link RecyclerView.Adapter}
 */
public abstract class PlayerViewHelper {

  @SuppressWarnings("unused") static final String TAG = "PlayerViewHelper";

  protected final ToroPlayer player;
  protected final View itemView;

  public PlayerViewHelper(@NonNull ToroPlayer player, @NonNull View itemView) {
    this.player = player;
    this.itemView = itemView;
  }

  /* BEGIN: Callback for View */

  /**
   * Callback from {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
   */
  @CallSuper public void onAttachedToWindow() {
    final PlayerManager manager = getPlayerManager(itemView.getParent());
    if (manager != null && manager.getPlayer() == null) {
      itemView.getViewTreeObserver().addOnGlobalLayoutListener( //
          new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
              itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
              if (player.wantsToPlay() && Toro.getStrategy()
                  .allowsToPlay(player, itemView.getParent())) {
                manager.setPlayer(player);
                if (!player.isPrepared()) {
                  player.preparePlayer(false);
                } else {
                  manager.restorePlaybackState(player.getMediaId());
                  manager.startPlayback();
                }
              }
            }
          });
    }
  }

  /**
   * Callback from {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)}
   */
  @CallSuper public void onDetachedFromWindow() {
    //
  }

  @CallSuper public void onBound() {
    //
  }

  @CallSuper public void onRecycled() {
    PlayerManager manager = getPlayerManager(itemView.getParent());
    // Manually save Video state
    if (manager != null && player.equals(manager.getPlayer())) {
      if (player.isPlaying()) {
        manager.savePlaybackState( //
            player.getMediaId(), player.getCurrentPosition(), player.getDuration());
        manager.pausePlayback();
      }
      // Detach current Player
      manager.setPlayer(null);
    }
    // Release player.
    player.releasePlayer();
  }

  /* BEGIN: Callback for MediaPlayer */

  /**
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   */
  @CallSuper protected void onPrepared(@NonNull View itemView, @Nullable ViewParent parent) {
    Toro.sInstance.onVideoPrepared(this.player, itemView, parent);
  }

  @Nullable protected final PlayerManager getPlayerManager(ViewParent parent) {
    return Toro.getManager(parent);
  }

  /**
   * Complete the playback
   */
  @CallSuper protected void onCompletion() {
    Toro.sInstance.onPlaybackCompletion(this.player);
  }

  protected final boolean onPlaybackError(Exception error) {
    return this.player.onPlaybackError(error) &&  //
        Toro.sInstance.onPlaybackError(this.player, error);
  }
}
