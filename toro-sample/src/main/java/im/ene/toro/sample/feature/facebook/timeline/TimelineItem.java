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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.facebook.data.FbUser;

/**
 * Created by eneim on 10/11/16.
 */

public class TimelineItem {

  @NonNull private final FbUser author;
  @NonNull private final String itemContent;
  @NonNull private final EmbedItem embedItem;

  public TimelineItem(Context context) {
    this(new FbUser(), context.getString(R.string.sample), Factory.newItem(context, Math.random()));
  }

  public TimelineItem(Context context, @NonNull EmbedItem embedItem) {
    this(new FbUser(), context.getString(R.string.sample), embedItem);
  }

  TimelineItem(@NonNull FbUser author, @NonNull String itemContent, @NonNull EmbedItem embedItem) {
    this.author = author;
    this.itemContent = itemContent;
    this.embedItem = embedItem;
  }

  @NonNull public FbUser getAuthor() {
    return author;
  }

  @NonNull public String getItemContent() {
    return itemContent;
  }

  @NonNull public EmbedItem getEmbedItem() {
    return embedItem;
  }

  // Like a Shared Item inside a Facebook Post
  public static class OgpItem implements EmbedItem {

    @DrawableRes private final int imageUrl;
    private final String title;
    private final String subTitle;
    private final String itemUrl;

    public OgpItem(Context context) {
      imageUrl = R.drawable.toro_icon;
      title = context.getString(R.string.readme);
      subTitle = context.getString(R.string.sample);
      itemUrl = "https://github.com/eneim/Toro";
    }

    @DrawableRes public int getImageUrl() {
      return imageUrl;
    }

    public String getTitle() {
      return title;
    }

    public String getSubTitle() {
      return subTitle;
    }

    public String getItemUrl() {
      return itemUrl;
    }

    @Override public String getClassName() {
      return getClass().getSimpleName();
    }
  }

  public static class PhotoItem implements EmbedItem {

    @DrawableRes private final int photoUrl;

    public PhotoItem() {
      photoUrl = R.drawable.google_io;
    }

    @DrawableRes public int getPhotoUrl() {
      return photoUrl;
    }

    @Override public String getClassName() {
      return getClass().getSimpleName();
    }
  }

  public static class VideoItem implements Parcelable, EmbedItem {

    private final String videoUrl;

    public VideoItem() {
      videoUrl = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    }

    public VideoItem(String videoUrl) {
      this.videoUrl = videoUrl;
    }

    protected VideoItem(Parcel in) {
      videoUrl = in.readString();
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(videoUrl);
    }

    @Override public int describeContents() {
      return 0;
    }

    public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
      @Override public VideoItem createFromParcel(Parcel in) {
        return new VideoItem(in);
      }

      @Override public VideoItem[] newArray(int size) {
        return new VideoItem[size];
      }
    };

    public String getVideoUrl() {
      return videoUrl;
    }

    @Override public String getClassName() {
      return getClass().getSimpleName();
    }
  }

  interface EmbedItem {

    @NonNull String getClassName();
  }

  static class Factory {

    static EmbedItem newItem(Context context, double random) {
      if (random <= 0.35) {
        return new VideoItem();
      } else if (random < 0.80) {
        return new OgpItem(context);
      } else {
        return new PhotoItem();
      }
    }
  }
}
