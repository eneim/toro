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

package im.ene.lab.toro.sample.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.player.Video;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 6/23/16.
 */
public class MyVideoViewHolder extends AbsVideoViewHolder {

  public static final int LAYOUT_RES = R.layout.vh_toro_video_multi;

  private ImageView mThumbnail;
  private TextView mInfo;

  private SimpleVideoObject mItem;

  public MyVideoViewHolder(View itemView) {
    super(itemView);
    mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
    mInfo = (TextView) itemView.findViewById(R.id.info);
  }

  public void setOnClickListener(View.OnClickListener listener) {
    videoView.setOnClickListener(listener);
  }

  @Override protected View findVideoView() {
    return itemView.findViewById(R.id.video);
  }

  @Override public void bind(RecyclerView.Adapter parent, Object item) {
    if (!(item instanceof SimpleVideoObject)) {
      throw new IllegalStateException("Unexpected object: " + item.toString());
    }

    mItem = (SimpleVideoObject) item;
    player.setMedia(new Video(Uri.parse(mItem.video), mItem.name));

    Picasso.with(itemView.getContext())
        .load(R.drawable.toro_place_holder)
        .fit()
        .centerInside()
        .into(mThumbnail);
    mInfo.setText("Bound");
  }

  @Nullable @Override public String getVideoId() {
    return mItem.toString() + "@" + getAdapterPosition();
  }

  @Override public void onVideoPrepared(Cineer mp) {
    super.onVideoPrepared(mp);
    mInfo.setText("Prepared");
  }

  @Override public void onPlaybackStarted() {
    mThumbnail.animate().alpha(0.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        MyVideoViewHolder.super.onPlaybackStarted();
      }
    }).start();
    mInfo.setText("Started");
    // isReleased = false;
  }

  @Override public void onPlaybackPaused() {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        MyVideoViewHolder.super.onPlaybackPaused();
      }
    }).start();
    mInfo.setText("Paused");
  }

  @Override public void onPlaybackCompleted() {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        MyVideoViewHolder.super.onPlaybackCompleted();
      }
    }).start();
    mInfo.setText("Completed");
  }

  @Override public boolean onPlaybackError(Cineer mp, PlaybackException error) {
    mThumbnail.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        MyVideoViewHolder.super.onPlaybackCompleted();
      }
    }).start();
    mInfo.setText("Error: videoId = " + getVideoId());
    return super.onPlaybackError(mp, error);
  }
}
