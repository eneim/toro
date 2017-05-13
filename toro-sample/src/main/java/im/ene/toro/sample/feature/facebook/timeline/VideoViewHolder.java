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

package im.ene.toro.sample.feature.facebook.timeline;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import im.ene.toro.exoplayer2.ExoPlayerHelper;
import im.ene.toro.exoplayer2.ExoPlayerView;
import im.ene.toro.extended.ExtPlayerViewHolder;
import im.ene.toro.sample.R;
import im.ene.toro.sample.util.DemoUtil;

/**
 * @author eneim
 * @since 10/11/16
 */

public class VideoViewHolder extends ExtPlayerViewHolder {

  static final int LAYOUT_RES = R.layout.vh_fb_feed_post_video;

  private TimelineItem.VideoItem videoItem;
  private ImageView mThumbnail;
  private TextView mInfo;

  private MediaSource mediaSource;

  public VideoViewHolder(View itemView) {
    super(itemView);
    mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
    mInfo = (TextView) itemView.findViewById(R.id.info);
    this.playerView.setUseController(false);
  }

  @NonNull @Override protected ExoPlayerView findVideoView(View itemView) {
    return (ExoPlayerView) itemView.findViewById(R.id.video);
  }

  @NonNull @Override protected MediaSource getMediaSource() {
    return this.mediaSource;
  }

  @Override protected void onBind(RecyclerView.Adapter adapter, @Nullable Object object) {
    if (!(object instanceof TimelineItem)
        || !(((TimelineItem) object).getEmbedItem() instanceof TimelineItem.VideoItem)) {
      throw new IllegalArgumentException("Only VideoItem is accepted");
    }

    this.videoItem = (TimelineItem.VideoItem) ((TimelineItem) object).getEmbedItem();

    this.mediaSource = ExoPlayerHelper.buildMediaSource(itemView.getContext(), //
        Uri.parse(this.videoItem.getVideoUrl()), new DefaultDataSourceFactory(itemView.getContext(),
            Util.getUserAgent(itemView.getContext(), "Toro-Sample")), itemView.getHandler(), null);
  }

  @Override public void setOnItemClickListener(View.OnClickListener listener) {
    super.setOnItemClickListener(listener);
    mInfo.setOnClickListener(listener);
    this.playerView.setOnClickListener(listener);
  }

  @Nullable @Override public String getMediaId() {
    return DemoUtil.genVideoId( //
        this.videoItem.getVideoUrl(), getAdapterPosition());
  }

  @Override public void onVideoPreparing() {
    super.onVideoPreparing();
    mInfo.setText("Preparing");
  }

  @Override public void onVideoPrepared() {
    super.onVideoPrepared();
    mInfo.setText("Prepared");
  }

  @Override public void onViewHolderBound() {
    super.onViewHolderBound();
    Picasso.with(itemView.getContext()).load(R.drawable.toro_place_holder)  //
        .fit().centerInside().into(mThumbnail);
    mInfo.setText("Bound");
  }

  @Override public void onPlaybackStarted() {
    mThumbnail.animate().alpha(0.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        VideoViewHolder.super.onPlaybackStarted();
      }
    }).start();
    mInfo.setText("Started");
  }

  @Override public void onPlaybackPaused() {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        VideoViewHolder.super.onPlaybackPaused();
      }
    }).start();
    mInfo.setText("Paused");
  }

  @Override public void onPlaybackCompleted() {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        VideoViewHolder.super.onPlaybackCompleted();
      }
    }).start();
    mInfo.setText("Completed");
  }

  @Override public boolean onPlaybackError(Exception error) {
    mThumbnail.animate().alpha(1.f).setDuration(0).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        // Immediately finish the animation.
      }
    }).start();
    mInfo.setText("Error: videoId = " + getMediaId());
    return super.onPlaybackError(error);
  }

  @Override public Target getNextTarget() {
    return Target.THIS_PLAYER;
  }
}
