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
import android.view.View;
import android.view.ViewParent;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.OnInfoListener;
import im.ene.lab.toro.media.OnPlayerStateChangeListener;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.media.PlaybackInfo;
import java.util.Map;

/**
 * Created by eneim on 2/1/16.
 *
 * A helper class to support Video's callbacks from {@link Cineer} as well as {@link
 * RecyclerView.Adapter}
 */
public abstract class PlayerViewHelper {

  /* BEGIN: Callback for View */

  /**
   * Callback from {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
   *
   * @param player the {@link ToroPlayer} which is attached to current ViewHolder
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   */
  public abstract void onAttachedToParent(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent);

  /**
   * Callback from {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)}
   *
   * @param player the {@link ToroPlayer} which is attached to current ViewHolder
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   */
  public abstract void onDetachedFromParent(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent);

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
   * @param player current ToroPlayer instance
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   * @param mediaPlayer current MediaPlayer
   */
  @CallSuper public void onPrepared(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent, @Nullable Cineer mediaPlayer) {
    player.onVideoPrepared(mediaPlayer);
    VideoPlayerManager manager = null;
    ToroScrollListener listener;
    RecyclerView view;
    // Find correct Player manager for this player
    for (Map.Entry<Integer, ToroScrollListener> entry : Toro.sInstance.mListeners.entrySet()) {
      Integer key = entry.getKey();
      view = Toro.sInstance.mViews.get(key);
      if (view != null && view == parent) { // Found the parent view in our cache
        listener = entry.getValue();
        manager = listener.getManager();
        break;
      }
    }

    if (manager == null) {
      return;
    }

    // 1. Check if current manager wrapped this player
    if (player.equals(manager.getPlayer())) {
      if (player.wantsToPlay() && Toro.getStrategy().allowsToPlay(player, parent)) {
        manager.restoreVideoState(player.getVideoId());
        manager.startPlayback();
        player.onPlaybackStarted();
      }
    } else {
      // There is no current player, but this guy is prepared, so let's him go ...
      if (manager.getPlayer() == null) {
        // ... if it's possible
        if (player.wantsToPlay() && Toro.getStrategy().allowsToPlay(player, parent)) {
          manager.setPlayer(player);
          manager.restoreVideoState(player.getVideoId());
          manager.startPlayback();
          player.onPlaybackStarted();
        }
      }
    }
  }

  /**
   * Callback from {@link OnPlayerStateChangeListener} with {@link Cineer#PLAYER_ENDED}
   *
   * @param player current ToroPlayer instance
   * @param mp completed MediaPlayer
   */
  @CallSuper public void onCompletion(@NonNull ToroPlayer player, @Nullable Cineer mp) {
    // 1. find manager for this player
    VideoPlayerManager manager = null;
    for (ToroScrollListener listener : Toro.sInstance.mListeners.values()) {
      manager = listener.getManager();
      if (player.equals(manager.getPlayer())) {
        break;
      } else {
        manager = null;
      }
    }

    // Normally stop playback
    if (manager != null) {
      manager.saveVideoState(player.getVideoId(), 0L, player.getDuration());
      manager.stopPlayback();
      player.onPlaybackStopped();
    }

    // It's loop-able, so restart it immediately
    if (player.isLoopAble()) {
      if (manager != null) {
        // immediately repeat
        manager.restoreVideoState(player.getVideoId());
        manager.startPlayback();
        player.onPlaybackStarted();
      }
    }
  }

  /**
   * Callback from {@link OnPlayerStateChangeListener#onPlayerError(Cineer,
   * PlaybackException)}
   *
   * @param player current ToroPlayer instance
   * @param mp current MediaPlayer
   */
  @CallSuper public boolean onError(@NonNull ToroPlayer player, @Nullable Cineer mp,
      @NonNull PlaybackException error) {
    boolean handle = player.onPlaybackError(mp, error);
    for (ToroScrollListener listener : Toro.sInstance.mListeners.values()) {
      VideoPlayerManager manager = listener.getManager();
      if (player.equals(manager.getPlayer())) {
        manager.saveVideoState(player.getVideoId(), 0L, player.getDuration());
        manager.pausePlayback();
        return true;
      }
    }
    return handle;
  }

  /**
   * Callback from {@link OnInfoListener}
   *
   * @param player current ToroPlayer instance
   * @param mp current MediaPlayer
   */
  @CallSuper public boolean onInfo(@NonNull ToroPlayer player, @Nullable Cineer mp,
      @NonNull PlaybackInfo info) {
    return true;
  }

  /* END: Callback for MediaPlayer */
}
