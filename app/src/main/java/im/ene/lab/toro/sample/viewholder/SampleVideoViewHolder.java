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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import com.squareup.picasso.Picasso;
import im.ene.lab.toro.AbsVideoViewHolder;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.util.Util;

/**
 * Created by eneim on 1/30/16.
 */
public class SampleVideoViewHolder extends AbsVideoViewHolder {

  private final String TAG = getClass().getSimpleName();

  public static final int LAYOUT_RES = R.layout.vh_video_view;

  private ImageView mThumbnail;
  private TextView mInfo;

  public SampleVideoViewHolder(View itemView) {
    super(itemView);
    mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
    mInfo = (TextView) itemView.findViewById(R.id.info);
  }

  @Override protected VideoView findVideoView(View itemView) {
    return (VideoView) itemView.findViewById(R.id.video);
  }

  @Override public void bind(Object item) {
    if (!(item instanceof SimpleVideoObject)) {
      throw new IllegalStateException("Unexpected object: " + item.toString());
    }

    mVideoView.setVideoPath(((SimpleVideoObject) item).video);
  }

  @Override public boolean wantsToPlay() {
    Rect childRect = new Rect();
    itemView.getGlobalVisibleRect(childRect, new Point());
    int visibleHeight = childRect.bottom - childRect.top;
    // wants to play if user could see at lease 0.75 of video
    return visibleHeight > itemView.getHeight() * 0.75;
  }

  @Nullable @Override public Long getVideoId() {
    return (long) getAdapterPosition();
  }

  @Override public void onViewHolderBound() {
    super.onViewHolderBound();
    Picasso.with(itemView.getContext())
        .load(R.drawable.toro_place_holder)
        .fit()
        .centerInside()
        .into(mThumbnail);
    mInfo.setText("Bound");
  }

  @Override public void onVideoPrepared(MediaPlayer mp) {
    super.onVideoPrepared(mp);
    mInfo.setText("Prepared");
  }

  @Override public void onPlaybackStarted() {
    mThumbnail.animate().alpha(0.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SampleVideoViewHolder.super.onPlaybackStarted();
      }
    }).start();
    mInfo.setText("Started");
  }

  @Override public void onPlaybackProgress(int position, int duration) {
    super.onPlaybackProgress(position, duration);
    mInfo.setText(Util.timeStamp(position, duration));
  }

  @Override public void onPlaybackPaused() {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SampleVideoViewHolder.super.onPlaybackPaused();
      }
    }).start();
    mInfo.setText("Paused");
  }

  @Override public void onPlaybackStopped() {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SampleVideoViewHolder.super.onPlaybackStopped();
      }
    }).start();
    mInfo.setText("Completed");
  }

  @Override public void onPlaybackError(MediaPlayer mp, int what, int extra) {
    super.onPlaybackError(mp, what, extra);
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SampleVideoViewHolder.super.onPlaybackStopped();
      }
    }).start();
    mInfo.setText("Error");
  }

  @Override protected boolean allowLongPressSupport() {
    return itemView != null && itemView.getResources().getBoolean(R.bool.accept_long_press);
  }

  @Override public String toString() {
    return "Video: " + getVideoId();
  }
}
