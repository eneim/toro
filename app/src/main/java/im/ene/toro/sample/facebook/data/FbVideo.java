/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.facebook.data;

import android.os.Parcel;
import android.os.Parcelable;
import im.ene.toro.sample.common.MediaUrl;

/**
 * @author eneim | 6/18/17.
 */

public class FbVideo extends FbItem implements MediaEntity, Parcelable {

  public final MediaItem mediaItem;

  public FbVideo(FbUser author, long index, long timeStamp, MediaItem mediaItem) {
    super(author, index, timeStamp);
    this.mediaItem = mediaItem;
  }

  @Override public MediaUrl getMediaUrl() {
    return this.mediaItem.getMediaUrl();
  }

  @Override public String toString() {
    return "FbVideo{" + "media=" + mediaItem.getMediaUrl().name() + '}';
  }

  public static FbVideo getItem(int index, int urlIdx, long timeStamp) {
    int urlCount = MediaUrl.values().length;
    return new FbVideo(FbUser.getUser(), index, timeStamp,
        new MediaItem(index, MediaUrl.values()[urlIdx % urlCount]));
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(this.author, flags);
    dest.writeLong(this.index);
    dest.writeLong(this.timeStamp);
    dest.writeParcelable(this.mediaItem, flags);
  }

  private FbVideo(Parcel in) {
    super(in.readParcelable(FbUser.class.getClassLoader()), in.readLong(), in.readLong());
    this.mediaItem = in.readParcelable(MediaItem.class.getClassLoader());
  }

  public static final Creator<FbVideo> CREATOR = new Creator<FbVideo>() {
    @Override public FbVideo createFromParcel(Parcel source) {
      return new FbVideo(source);
    }

    @Override public FbVideo[] newArray(int size) {
      return new FbVideo[size];
    }
  };
}
