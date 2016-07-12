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

package im.ene.lab.toro.sample.presentation.facebook;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import im.ene.lab.toro.ext.ToroVideoViewHolder;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.LastMomentCallback;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.player.widget.ToroVideoView;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 1/30/16.
 */
public class SimpleVideoViewHolder extends ToroVideoViewHolder implements LastMomentCallback {

  public static final int LAYOUT_RES = R.layout.vh_toro_video_simple;

  private ImageView mThumbnail;
  private TextView mInfo;
  private boolean isPlayable = false;
  private boolean isReleased = false;
  private long latestPosition = 0;

  public SimpleVideoViewHolder(View itemView) {
    super(itemView);
    mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
    mInfo = (TextView) itemView.findViewById(R.id.info);
    mVideoView.setLastMomentCallback(this);
  }

  @Override protected ToroVideoView findVideoView(View itemView) {
    return (ToroVideoView) itemView.findViewById(R.id.video);
  }

  @Override public void setOnItemClickListener(View.OnClickListener listener) {
    super.setOnItemClickListener(listener);
    mInfo.setOnClickListener(listener);
  }

  private SimpleVideoObject mItem;

  @Override public void bind(RecyclerView.Adapter adapter, Object item) {
    if (!(item instanceof SimpleVideoObject)) {
      throw new IllegalStateException("Unexpected object: " + item.toString());
    }

    mItem = (SimpleVideoObject) item;
    mVideoView.setMedia(Uri.parse(mItem.video));
  }

  @Override public boolean wantsToPlay() {
    return isPlayable && visibleAreaOffset() >= 0.75;
  }

  @Nullable @Override public String getMediaId() {
    return mItem.toString() + "@" + getAdapterPosition();
  }

  @Override public void onVideoPreparing() {
    super.onVideoPreparing();
    mInfo.setText("Preparing");
  }

  @Override public void onVideoPrepared(Cineer mp) {
    super.onVideoPrepared(mp);
    isPlayable = true;
    mInfo.setText("Prepared");
    latestPosition = 0;
    isReleased = false;
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
        SimpleVideoViewHolder.super.onPlaybackStarted();
      }
    }).start();
    mInfo.setText("Started");
  }

  @Override public void onPlaybackPaused() {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SimpleVideoViewHolder.super.onPlaybackPaused();
      }
    }).start();
    mInfo.setText("Paused");
  }

  @Override public void onPlaybackCompleted() {
    isPlayable = false;
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SimpleVideoViewHolder.super.onPlaybackCompleted();
      }
    }).start();
    mInfo.setText("Completed");
  }

  @Override public boolean onPlaybackError(Cineer mp, PlaybackException error) {
    isPlayable = false;
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        SimpleVideoViewHolder.super.onPlaybackCompleted();
      }
    }).start();
    mInfo.setText("Error: videoId = " + getMediaId());
    return super.onPlaybackError(mp, error);
  }

  @Override protected boolean allowLongPressSupport() {
    return itemView != null && itemView.getResources().getBoolean(R.bool.accept_long_press);
  }

  @Override public boolean isLoopAble() {
    return true;
  }

  @Override public String toString() {
    return "Video: " + getMediaId();
  }

  @Override public long getCurrentPosition() {
    if (!isReleased) {
      latestPosition = super.getCurrentPosition();
    }

    return latestPosition;
  }

  @Override public void onLastMoment(Cineer player) {
    isReleased = true;
    latestPosition = player.getCurrentPosition();
  }
}
