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

package im.ene.toro.mediaplayer;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.VideoView;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.ToroUtil;

/**
 * Created by eneim on 9/29/16.
 */

public abstract class LegacyBaseVideoViewHolder extends ToroAdapter.ViewHolder
    implements ToroPlayer {

  protected final LegacyVideoViewHelper helper;
  protected boolean isPlayable = false;
  @NonNull
  protected final VideoView videoView;

  public LegacyBaseVideoViewHolder(View itemView) {
    super(itemView);
    videoView = findVideoView(itemView);
    if (videoView == null) {
      throw new NullPointerException("A valid VideoView is required");
    }

    helper = new LegacyVideoViewHelper(this, itemView);
    videoView.setOnPreparedListener(helper);
    videoView.setOnCompletionListener(helper);
    videoView.setOnErrorListener(helper);
  }

  protected abstract VideoView findVideoView(View itemView);

  /* BEGIN: ToroViewHolder callbacks */
  @Override public void onAttachedToWindow() {
    helper.onAttachedToWindow();
  }

  @Override public void onDetachedFromWindow() {
    helper.onDetachedFromWindow();
  }
  /* END: ToroViewHolder callbacks */

  /* BEGIN: ToroPlayer callbacks (partly) */
  @Override public void onActivityActive() {

  }

  @Override public void onActivityInactive() {

  }

  @Override public void onVideoPreparing() {

  }

  @Override public void onVideoPrepared() {
    this.isPlayable = true;
  }

  @Override public void onPlaybackStarted() {

  }

  @Override public void onPlaybackPaused() {

  }

  @Override public void onPlaybackCompleted() {
    this.isPlayable = false;
  }

  @Override public boolean onPlaybackError(Exception error) {
    this.isPlayable = false;
    return true;
  }

  @Override public boolean wantsToPlay() {
    return isPlayable && visibleAreaOffset() >= 0.85;
  }

  @Override public float visibleAreaOffset() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent());
  }

  @Override public int getPlayOrder() {
    return getAdapterPosition();
  }
  /* END: ToroPlayer callbacks (partly) */
}
