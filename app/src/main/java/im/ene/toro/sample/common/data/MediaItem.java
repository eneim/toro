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

package im.ene.toro.sample.common.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.exoplayer2.util.Util;

/**
 * @author eneim | 6/7/17.
 */

public class MediaItem implements MediaEntity, Parcelable {

  private final long index;
  private final MediaUrl mediaUrl;

  public MediaItem(long index, MediaUrl mediaUrl) {
    this.index = index;
    this.mediaUrl = mediaUrl;
  }

  @Override public MediaUrl getMediaUrl() {
    return mediaUrl;
  }

  public int inferContentType() {
    return Util.inferContentType(this.mediaUrl.getUri());
  }

  @Override public long getIndex() {
    return index;
  }

  @Override public String toString() {
    return "Media{" + "index=" + index + ", url=" + mediaUrl.toString() + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(this.index);
    dest.writeSerializable(this.mediaUrl == null ? -1 : this.mediaUrl.ordinal());
  }

  protected MediaItem(Parcel in) {
    this.index = in.readLong();
    int tmpMediaUrl = in.readInt();
    this.mediaUrl = tmpMediaUrl == -1 ? null : MediaUrl.values()[tmpMediaUrl];
  }

  public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
    @Override public MediaItem createFromParcel(Parcel source) {
      return new MediaItem(source);
    }

    @Override public MediaItem[] newArray(int size) {
      return new MediaItem[size];
    }
  };
}
