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

package im.ene.lab.toro.sample.viewholder;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import im.ene.lab.toro.ToroVideoViewHolder;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.widget.ToroVideoView;

/**
 * Created by eneim on 1/30/16.
 */
public class VideoViewHolder extends ToroVideoViewHolder {

  private final String TAG = getClass().getSimpleName();

  public static final int LAYOUT_RES = R.layout.vh_texture_video;

  public VideoViewHolder(View itemView) {
    super(itemView);
  }

  @Override protected ToroVideoView getVideoView(View itemView) {
    return (ToroVideoView) itemView.findViewById(R.id.video);
  }

  @Override public void bind(Object item) {
    if (!(item instanceof SimpleVideoObject)) {
      throw new IllegalStateException("Unexpected object: " + item.toString());
    }

    Log.d(TAG, "bind() called with: " + "item = [" + item + "]");
    // mCurrentState = State.STATE_IDLE;
    mVideoView.setVideoPath(((SimpleVideoObject) item).video);
  }

  @Override public boolean isPlaying() {
    return super.isPlaying();
  }

  @Override public boolean wantsToPlay(Rect parentRect, @NonNull Rect childRect) {
    int visibleHeight = childRect.bottom - childRect.top;
    return visibleHeight > itemView.getHeight() * 0.7;
  }

  @Override public float visibleAreaOffset() {
    Rect videoRect = getVideoRect();
    Rect parentRect = getRecyclerViewRect();
    if (!parentRect.contains(videoRect) && !parentRect.intersect(videoRect)) {
      return 0.f;
    }

    return mVideoView.getHeight() <= 0 ? 1.f : videoRect.height() / (float) mVideoView.getHeight();
  }

  @Nullable @Override public Long getVideoId() {
    return (long) getAdapterPosition();
  }

  @Override public void onStartPlayback() {
    super.onStartPlayback();
    Log.e(TAG, toString() + " START PLAYBACK");
  }

  @Override public void onPlaybackProgress(int position, int duration) {
    super.onPlaybackProgress(position, duration);
    Log.d(TAG, toString() + " position = [" + position + "], duration = [" + duration + "]");
  }

  @Override public void onPausePlayback() {
    super.onPausePlayback();
    Log.e(TAG, toString() + " PAUSE PLAYBACK");
  }

  @Override public void onStopPlayback() {
    super.onStopPlayback();
    Log.e(TAG, toString() + " STOP PLAYBACK");
  }

  @Override public String toString() {
    return "Video: " + getVideoId();
  }
}
