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

package im.ene.lab.toro;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.OnPlayerStateChangeListener;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.media.State;

/**
 * Created by eneim on 2/1/16.
 *
 * A helper class to support Video's callbacks from {@link Cineer} as well as {@link
 * RecyclerView.Adapter}
 */
public abstract class PlayerViewHelper implements OnPlayerStateChangeListener {

  public static final String TAG = "PlayerViewHelper";

  protected final ToroPlayer player;
  protected final View itemView;

  public PlayerViewHelper(@NonNull ToroPlayer player, @NonNull View itemView) {
    this.player = player;
    this.itemView = itemView;
  }

  /**
   * {@hide}
   */
  @NonNull public ToroPlayer getPlayer() {
    return player;
  }

  /* BEGIN: Callback for View */

  /**
   * Callback from {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
   */
  @CallSuper public void onAttachedToParent() {
    ToroScrollListener listener = itemView.getParent() != null ?  //
        Toro.sInstance.mListeners.get(itemView.getParent().hashCode()) : null;
    if (listener != null && listener.getManager().getPlayer() == null) {
      if (player.wantsToPlay() && Toro.getStrategy().allowsToPlay(player, itemView.getParent())) {
        listener.getManager().setPlayer(player);
        listener.getManager().restoreVideoState(player.getVideoId());
        listener.getManager().startPlayback();
      } else {
        // Prepare
        player.preparePlayer(false);
      }
    }
  }

  /**
   * Callback from {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)}
   */
  @CallSuper public void onDetachedFromParent() {
    ToroScrollListener listener = itemView.getParent() != null ?  //
        Toro.sInstance.mListeners.get(itemView.getParent().hashCode()) : null;
    // Manually save Video state
    if (listener != null && player.equals(listener.getManager().getPlayer())) {
      if (player.isPlaying()) {
        listener.getManager().saveVideoState( //
            player.getVideoId(), player.getCurrentPosition(), player.getDuration());
        listener.getManager().pausePlayback();
      }
      // Release player.
      player.releasePlayer();
      // Detach current Player
      listener.getManager().setPlayer(null);
    }
  }

  /**
   * Support long press on Video, called by {@link View.OnLongClickListener#onLongClick(View)}
   *
   * @param player the {@link ToroPlayer} which is attached to current ViewHolder
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   * @return boolean response to {@link View.OnLongClickListener#onLongClick(View)}
   */
  public boolean onItemLongClick(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {
    return false;
  }
  /* END: Callback for View */

  /* BEGIN: Callback for MediaPlayer */

  /**
   * Callback from {@link OnPlayerStateChangeListener} with {@link Cineer#PLAYER_PREPARED}
   *
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   * @param mediaPlayer current MediaPlayer
   */
  @CallSuper public void onPrepared(@NonNull View itemView, @Nullable ViewParent parent,
      @Nullable Cineer mediaPlayer) {
    Toro.sInstance.onVideoPrepared(this.player, itemView, parent, mediaPlayer);
  }

  /**
   * Callback from {@link OnPlayerStateChangeListener} with {@link Cineer#PLAYER_ENDED}
   *
   * @param mp completed MediaPlayer
   */
  @CallSuper public void onCompletion(@Nullable Cineer mp) {
    Toro.sInstance.onPlaybackCompletion(this.player, mp);
  }

  /* END: Callback for MediaPlayer */

  /**
   * Implement {@link OnPlayerStateChangeListener}
   */
  @Override public final void onPlayerStateChanged(Cineer player, boolean playWhenReady,
      @State int playbackState) {
    Log.d(TAG, "onPlayerStateChanged() called with: "
        + "player = ["
        + this.player.getPlayOrder()
        + "], playWhenReady = ["
        + playWhenReady
        + "], playbackState = ["
        + playbackState
        + "]");
    switch (playbackState) {
      case Cineer.PLAYER_PREPARED:
        this.player.onVideoPrepared(player);
        this.onPrepared(this.itemView, this.itemView.getParent(), player);
        break;
      case Cineer.PLAYER_ENDED:
        this.player.onPlaybackCompleted();
        this.onCompletion(player);
        break;
      case Cineer.PLAYER_BUFFERING:
        break;
      case Cineer.PLAYER_IDLE:
        break;
      case Cineer.PLAYER_PREPARING:
        this.player.onVideoPreparing();
        break;
      case Cineer.PLAYER_READY:
        if (playWhenReady) {
          this.player.onPlaybackStarted();
        } else {
          this.player.onPlaybackPaused();
        }
        break;
      default:
        break;
    }
  }

  @Override public final boolean onPlayerError(Cineer player, PlaybackException error) {
    return this.player.onPlaybackError(player, error) &&  //
        Toro.sInstance.onPlaybackError(this.player, player, error);
  }
}
