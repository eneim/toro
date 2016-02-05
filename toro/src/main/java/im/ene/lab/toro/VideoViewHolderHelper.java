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

import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;

/**
 * Created by eneim on 2/1/16.
 *
 * A helper class to support Video's callbacks from {@link MediaPlayer} as well as {@link
 * RecyclerView.Adapter}
 */
public interface VideoViewHolderHelper {

  /* BEGIN: Callback for View */

  /**
   * Callback from {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
   *
   * @param player the {@link ToroPlayer} which is attached to current ViewHolder
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   */
  void onAttachedToParent(ToroPlayer player, View itemView, ViewParent parent);

  /**
   * Callback from {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)}
   *
   * @param player the {@link ToroPlayer} which is attached to current ViewHolder
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   */
  void onDetachedFromParent(ToroPlayer player, View itemView, ViewParent parent);

  /**
   * Support long press on Video, called by {@link View.OnLongClickListener#onLongClick(View)}
   *
   * @param player the {@link ToroPlayer} which is attached to current ViewHolder
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   * @return boolean response to {@link View.OnLongClickListener#onLongClick(View)}
   */
  boolean onItemLongClick(ToroPlayer player, View itemView, ViewParent parent);
  /* END: Callback for View */

  /* BEGIN: Callback for MediaPlayer */

  /**
   * Callback from {@link MediaPlayer.OnPreparedListener#onPrepared(MediaPlayer)}
   *
   * @param player current ToroPlayer instance
   * @param itemView main View of current ViewHolder
   * @param parent parent which holds current ViewHolder
   * @param mediaPlayer current MediaPlayer
   */
  void onPrepared(ToroPlayer player, View itemView, ViewParent parent, MediaPlayer mediaPlayer);

  /**
   * Callback from {@link MediaPlayer.OnCompletionListener#onCompletion(MediaPlayer)}
   *
   * @param player current ToroPlayer instance
   * @param mp completed MediaPlayer
   */
  void onCompletion(ToroPlayer player, MediaPlayer mp);

  /**
   * Callback from {@link MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int)}
   *
   * @param player current ToroPlayer instance
   * @param mp current MediaPlayer
   */
  boolean onError(ToroPlayer player, MediaPlayer mp, int what, int extra);

  /**
   * Callback from {@link MediaPlayer.OnInfoListener#onInfo(MediaPlayer, int, int)}
   *
   * @param player current ToroPlayer instance
   * @param mp current MediaPlayer
   */
  boolean onInfo(ToroPlayer player, MediaPlayer mp, int what, int extra);

  /**
   * Callback from {@link MediaPlayer.OnSeekCompleteListener#onSeekComplete(MediaPlayer)}
   */
  void onSeekComplete(ToroPlayer player, MediaPlayer mp);
  /* END: Callback for MediaPlayer */
}
