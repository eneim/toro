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
import im.ene.lab.toro.player.PlaybackException;
import im.ene.lab.toro.player.PlaybackInfo;
import im.ene.lab.toro.player.TrMediaPlayer;
import im.ene.lab.toro.player.listener.OnInfoListener;
import im.ene.lab.toro.player.listener.OnPlayerStateChangeListener;

/**
 * Created by eneim on 2/1/16.
 *
 * A helper class to support Video's callbacks from {@link TrMediaPlayer} as well as {@link
 * RecyclerView.Adapter}
 */
class VideoViewItemHelper {

  /* BEGIN: Callback for View */

  /**
   * Callback from {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
   *
   * @param player the {@link ToroPlayer} which is attached to current ViewHolder
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   */
  public void onAttachedToParent(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {

  }

  /**
   * Callback from {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)}
   *
   * @param player the {@link ToroPlayer} which is attached to current ViewHolder
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   */
  public void onDetachedFromParent(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {

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
   * Callback from {@link OnPlayerStateChangeListener} with {@link TrMediaPlayer#STATE_PREPARED}
   *
   * @param player current ToroPlayer instance
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   * @param mediaPlayer current MediaPlayer
   */
  @CallSuper public void onPrepared(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent, @Nullable TrMediaPlayer mediaPlayer) {
    Toro.sInstance.onPrepared(player, itemView, parent, mediaPlayer);
  }

  /**
   * Callback from {@link OnPlayerStateChangeListener} with {@link TrMediaPlayer#STATE_ENDED}
   *
   * @param player current ToroPlayer instance
   * @param mp completed MediaPlayer
   */
  @CallSuper public void onCompletion(@NonNull ToroPlayer player, @Nullable TrMediaPlayer mp) {
    Toro.sInstance.onCompletion(player, mp);
  }

  /**
   * Callback from {@link OnPlayerStateChangeListener#onPlayerError(TrMediaPlayer,
   * PlaybackException)}
   *
   * @param player current ToroPlayer instance
   * @param mp current MediaPlayer
   */
  @CallSuper public boolean onError(@NonNull ToroPlayer player, @Nullable TrMediaPlayer mp,
      @NonNull PlaybackException error) {
    return Toro.sInstance.onError(player, mp, error);
  }

  /**
   * Callback from {@link OnInfoListener}
   *
   * @param player current ToroPlayer instance
   * @param mp current MediaPlayer
   */
  @CallSuper public boolean onInfo(@NonNull ToroPlayer player, @Nullable TrMediaPlayer mp,
      @NonNull PlaybackInfo info) {
    return Toro.sInstance.onInfo(player, mp, info);
  }

  /* END: Callback for MediaPlayer */
}
