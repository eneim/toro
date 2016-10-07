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

package im.ene.toro.sample.presentation.facebook;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import im.ene.toro.ToroAdapter;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.SimpleVideoObject;
import im.ene.toro.exoplayer2.ExoVideoView;
import im.ene.toro.extended.ExtVideoViewHolder;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by eneim on 5/13/16.
 *
 * Original base facebook feed item ViewHolder
 */
public abstract class FbItemViewHolder extends ToroAdapter.ViewHolder {

  public static final int POST_TYPE_TEXT = 1001;

  public static final int POST_TYPE_PHOTO = 1002;

  public static final int POST_TYPE_VIDEO = 1003;

  @IntDef({
      POST_TYPE_PHOTO, POST_TYPE_TEXT, POST_TYPE_VIDEO
  }) @Retention(RetentionPolicy.SOURCE) public @interface PostType {
  }

  public abstract void bind(RecyclerView.Adapter parent, @Nullable Object object);

  private static LayoutInflater inflater;

  public FbItemViewHolder(View itemView) {
    super(itemView);
  }

  public static ToroAdapter.ViewHolder createViewHolder(ViewGroup parent, @PostType int type) {
    if (inflater == null) {
      inflater = LayoutInflater.from(parent.getContext());
    }

    final ToroAdapter.ViewHolder viewHolder;
    final View view;
    switch (type) {
      case POST_TYPE_TEXT:
        view = inflater.inflate(TextPost.LAYOUT_RES, parent, false);
        viewHolder = new TextPost(view);
        break;
      case POST_TYPE_PHOTO:
        view = inflater.inflate(PhotoPost.LAYOUT_RES, parent, false);
        viewHolder = new PhotoPost(view);
        break;
      case POST_TYPE_VIDEO:
        view = inflater.inflate(VideoPost.LAYOUT_RES, parent, false);
        viewHolder = new VideoPost(view);
        break;
      default:
        throw new IllegalArgumentException("Un-supported Post type");
    }

    return viewHolder;
  }

  @Override public void onAttachedToWindow() {

  }

  @Override public void onDetachedFromWindow() {

  }

  static class TextPost extends FbItemViewHolder {

    static final int LAYOUT_RES = R.layout.vh_fb_feed_post_text;

    public TextPost(View itemView) {
      super(itemView);
    }

    @Override public void bind(RecyclerView.Adapter parent, @Nullable Object object) {

    }
  }

  static class PhotoPost extends FbItemViewHolder {

    static final int LAYOUT_RES = R.layout.vh_fb_feed_post_photo;

    public PhotoPost(View itemView) {
      super(itemView);
    }

    @Override public void bind(RecyclerView.Adapter parent, @Nullable Object object) {

    }
  }

  static class VideoPost extends ExtVideoViewHolder /* implements OnReleaseCallback */ {

    static final int LAYOUT_RES = R.layout.vh_fb_feed_post_video;

    private ImageView mThumbnail;
    private TextView mInfo;
    private boolean isPlayable = false;
    private boolean isReleased = false;
    private long latestPosition = 0;

    public VideoPost(View itemView) {
      super(itemView);
      mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
      mInfo = (TextView) itemView.findViewById(R.id.info);
      // videoView.setLastMomentCallback(this);
    }

    @Override protected ExoVideoView findVideoView(View itemView) {
      return (ExoVideoView) itemView.findViewById(R.id.video);
    }

    @Override public void setOnItemClickListener(View.OnClickListener listener) {
      super.setOnItemClickListener(listener);
      mInfo.setOnClickListener(listener);
      this.videoView.setOnClickListener(listener);
    }

    private SimpleVideoObject mItem;

    @Override public void bind(RecyclerView.Adapter adapter, Object item) {
      if (!(item instanceof SimpleVideoObject)) {
        throw new IllegalStateException("Unexpected object: " + item.toString());
      }

      mItem = (SimpleVideoObject) item;
      videoView.setMedia(Uri.parse(mItem.video));
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

    @Override public void onVideoPrepared() {
      super.onVideoPrepared();
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
          VideoPost.super.onPlaybackStarted();
        }
      }).start();
      mInfo.setText("Started");
    }

    @Override public void onPlaybackPaused() {
      mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          VideoPost.super.onPlaybackPaused();
        }
      }).start();
      mInfo.setText("Paused");
    }

    @Override public void onPlaybackCompleted() {
      isPlayable = false;
      mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          VideoPost.super.onPlaybackCompleted();
        }
      }).start();
      mInfo.setText("Completed");
    }

    @Override public boolean onPlaybackError(Exception error) {
      isPlayable = false;
      mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          VideoPost.super.onPlaybackCompleted();
        }
      }).start();
      mInfo.setText("Error: videoId = " + getMediaId());
      return super.onPlaybackError(error);
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

    @Override public void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume) {
      this.videoView.setVolume(volume);
    }

    //@Override public void onRelease(SimpleMediaPlayer player) {
    //  isReleased = true;
    //  latestPosition = player.getCurrentPosition();
    //}

    @Override public Target getNextTarget() {
      return Target.NONE;
    }
  }
}
