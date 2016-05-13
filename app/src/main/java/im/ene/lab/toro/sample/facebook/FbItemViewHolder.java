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

package im.ene.lab.toro.sample.facebook;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.viewholder.SimpleToroVideoViewHolder;
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

  @Override public void bind(@Nullable Object object) {

  }

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

  static class TextPost extends FbItemViewHolder {

    static final int LAYOUT_RES = R.layout.vh_fb_feed_post_text;

    public TextPost(View itemView) {
      super(itemView);
    }
  }

  static class PhotoPost extends FbItemViewHolder {

    static final int LAYOUT_RES = R.layout.vh_fb_feed_post_photo;

    public PhotoPost(View itemView) {
      super(itemView);
    }
  }

  static class VideoPost extends SimpleToroVideoViewHolder {

    static final int LAYOUT_RES = R.layout.vh_fb_feed_post_video;

    public VideoPost(View itemView) {
      super(itemView);
    }
  }
}
