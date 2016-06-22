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
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import im.ene.lab.toro.ext.ToroVideoViewHolder;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.player.widget.VideoPlayerView;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.facebook.TrackablePlayer;

/**
 * Created by eneim on 1/30/16.
 */
public class SimpleToroVideoViewHolder extends ToroVideoViewHolder
    implements TrackablePlayer /*, TextureVideoView.OnReleasedListener */ {

  private final String TAG = getClass().getSimpleName();

  public static final int LAYOUT_RES = R.layout.vh_toro_video_simple;

  private ImageView mThumbnail;
  private TextView mInfo;

  public SimpleToroVideoViewHolder(View itemView) {
    super(itemView);
    mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
    mInfo = (TextView) itemView.findViewById(R.id.info);
    // mVideoView.setOnReleasedListener(this);
  }

  @Override protected VideoPlayerView findVideoView(View itemView) {
    return (VideoPlayerView) itemView.findViewById(R.id.video);
  }

  @Override public void setOnItemClickListener(View.OnClickListener listener) {
    super.setOnItemClickListener(listener);
    mInfo.setOnClickListener(listener);
  }

  private SimpleVideoObject mItem;

  @Override public void bind(Object item) {
    if (!(item instanceof SimpleVideoObject)) {
      throw new IllegalStateException("Unexpected object: " + item.toString());
    }

    mItem = (SimpleVideoObject) item;
    mVideoView.setMediaUri(Uri.parse(mItem.video));
  }

  @Override public boolean wantsToPlay() {
    return visibleAreaOffset() >= 0.75;
  }

  @Nullable @Override public String getVideoId() {
    return mItem.toString() + "-" + getAdapterPosition();
  }

  @Override public void onVideoPrepared(Cineer mp) {
    super.onVideoPrepared(mp);
    mInfo.setText("Prepared");
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

  @Override public void onPlaybackStarted() {
    mThumbnail.animate().alpha(0.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SimpleToroVideoViewHolder.super.onPlaybackStarted();
      }
    }).start();
    mInfo.setText("Started");
    isReleased = false;
  }

  @Override public void onPlaybackProgress(long position, long duration) {
    super.onPlaybackProgress(position, duration);
    // mInfo.setText(Util.timeStamp(position, duration));
  }

  @Override public void onPlaybackPaused() {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SimpleToroVideoViewHolder.super.onPlaybackPaused();
      }
    }).start();
    mInfo.setText("Paused");
  }

  @Override public void onPlaybackStopped() {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SimpleToroVideoViewHolder.super.onPlaybackStopped();
      }
    }).start();
    mInfo.setText("Completed");
  }

  @Override public boolean onPlaybackError(Cineer mp, PlaybackException error) {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SimpleToroVideoViewHolder.super.onPlaybackStopped();
      }
    }).start();
    mInfo.setText("Error: videoId = " + getVideoId());
    return super.onPlaybackError(mp, error);
  }

  @Override protected boolean allowLongPressSupport() {
    return itemView != null && itemView.getResources().getBoolean(R.bool.accept_long_press);
  }

  @Override public boolean isLoopAble() {
    return true;
  }

  @Override public String toString() {
    return "Video: " + getVideoId();
  }

  private long latestPosition;
  private long duration;
  private boolean isReleased = false;

  @Override public long getLatestPosition() {
    return !isReleased ? getCurrentPosition() : latestPosition;
  }

  @Override public long getPlaybackDuration() {
    return !isReleased ? getDuration() : duration;
  }

  //@Override public void onReleased(@Nullable Uri video, long position, long duration) {
  //  this.isReleased = true;
  //  this.latestPosition = position;
  //  this.duration = duration;
  //}
}
