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

import android.support.annotation.CallSuper;
import android.view.View;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.ToroUtil;
import im.ene.lab.toro.ToroViewHolder;

/**
 * Created by eneim on 10/4/16.
 */

public abstract class BaseExtVideoViewHolder extends ToroAdapter.ViewHolder
    implements ToroPlayer, ToroViewHolder {

  protected final LongClickableViewHelper helper;

  public BaseExtVideoViewHolder(View itemView) {
    super(itemView);
    helper = new LongClickableViewHelper(this, itemView);
  }

  @Override public void setOnItemLongClickListener(final View.OnLongClickListener listener) {
    super.setOnItemLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        return listener.onLongClick(v) || helper.onLongClick(v);
      }
    });
  }

  @CallSuper @Override public void onActivityActive() {

  }

  @CallSuper @Override public void onActivityInactive() {

  }

  @CallSuper @Override public void onAttachedToWindow() {
    helper.onAttachedToWindow();
  }

  @CallSuper @Override public void onDetachedFromWindow() {
    helper.onDetachedFromWindow();
  }

  @Override public int getPlayOrder() {
    return getAdapterPosition();
  }

  @Override public void onVideoPreparing() {

  }

  @Override public void onVideoPrepared() {

  }

  @Override public void onPlaybackStarted() {

  }

  @Override public void onPlaybackPaused() {

  }

  @Override public void onPlaybackCompleted() {

  }

  @Override public boolean onPlaybackError(Exception error) {
    return true;  // don't want to see the annoying dialog
  }

  @Override public float visibleAreaOffset() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent());
  }

  /**
   * Indicate that this Player is able to replay right after it stops (loop-able) or not.
   *
   * @return true if this Player is loop-able, false otherwise
   */
  @Override public boolean isLoopAble() {
    return false;
  }
}
