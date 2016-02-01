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

import android.view.View;
import com.devbrackets.android.exomedia.EMVideoView;

/**
 * Created by eneim on 2/1/16.
 */
public abstract class EMVideoViewHolder extends ToroViewHolder {

  private final EMVideoView mVideoView;

  public EMVideoViewHolder(View itemView) {
    super(itemView);
    mVideoView = getVideoView(itemView);
  }

  protected abstract EMVideoView getVideoView(View itemView);

  @Override public void onActivityPaused() {

  }

  @Override public void onActivityResumed() {

  }

  @Override public int getDuration() {
    return mVideoView != null ? (int) mVideoView.getDuration() : -1;
  }

  @Override public int getCurrentPosition() {
    return mVideoView != null ? (int) mVideoView.getCurrentPosition() : 0;
  }

  @Override public boolean isPlaying() {
    return mVideoView != null && mVideoView.isPlaying();
  }

  @Override public int getBufferPercentage() {
    return mVideoView != null ? mVideoView.getBufferPercentage() : 0;
  }

  @Override public boolean canPause() {
    return true;
  }

  @Override public boolean canSeekBackward() {
    return true;
  }

  @Override public boolean canSeekForward() {
    return true;
  }

  @Override public int getAudioSessionId() {
    // Because ExoPlayer doesn't support this operation
    throw new UnsupportedOperationException();
  }
}
