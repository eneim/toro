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
import android.support.annotation.CallSuper;
import android.view.View;

/**
 * Created by eneim on 1/31/16.
 */
abstract class ToroViewHolder extends BaseAdapter.ViewHolder
    implements ToroPlayer, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener {

  private static final String TAG = "ToroViewHolder";
  private final ToroViewHelper mHelper;

  public ToroViewHolder(View itemView) {
    super(itemView);
    mHelper = Toro.RECYCLER_VIEW_HELPER;
  }

  @CallSuper @Override public void onAttachedToParent() {
    mHelper.onAttachedToParent(this, itemView, itemView.getParent());
  }

  @Override public void onDetachedFromParent() {
    mHelper.onDetachedFromParent(this, itemView, itemView.getParent());
  }

  @CallSuper @Override public void onPrepared(MediaPlayer mp) {
    Toro.onPrepared(this, itemView, itemView.getParent(), mp);
  }

  @CallSuper @Override public void onCompletion(MediaPlayer mp) {
    Toro.onCompletion(this, mp);
  }

  @Override public int getPlayerPosition() {
    return getAdapterPosition();
  }
}
