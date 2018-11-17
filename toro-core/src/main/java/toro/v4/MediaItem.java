/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package toro.v4;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import im.ene.toro.media.MediaDrm;

/**
 * @author eneim (2018/10/02).
 * @since 3.7.0
 */
public class MediaItem implements Media {

  @NonNull private final Uri uri;
  @Nullable private final String extension;
  @Nullable private final MediaDrm mediaDrm;

  public MediaItem(@NonNull Uri uri, @Nullable String extension) {
    this.uri = uri;
    this.extension = extension;
    this.mediaDrm = null;
  }

  public MediaItem(@NonNull Uri uri, @Nullable String extension, @Nullable MediaDrm mediaDrm) {
    this.uri = uri;
    this.extension = extension;
    this.mediaDrm = mediaDrm;
  }

  @Override @NonNull public Uri getUri() {
    return uri;
  }

  @Override @Nullable public String getExtension() {
    return extension;
  }

  @Override @Nullable public MediaDrm getMediaDrm() {
    return mediaDrm;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MediaItem media = (MediaItem) o;

    if (!uri.equals(media.uri)) return false;
    if (extension != null ? !extension.equals(media.extension) : media.extension != null) {
      return false;
    }
    return mediaDrm != null ? mediaDrm.equals(media.mediaDrm) : media.mediaDrm == null;
  }

  @Override public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + (extension != null ? extension.hashCode() : 0);
    result = 31 * result + (mediaDrm != null ? mediaDrm.hashCode() : 0);
    return result;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(this.uri, flags);
    dest.writeString(this.extension);
    dest.writeParcelable(this.mediaDrm, flags);
  }

  protected MediaItem(Parcel in) {
    //noinspection ConstantConditions
    this.uri = in.readParcelable(Uri.class.getClassLoader());
    this.extension = in.readString();
    this.mediaDrm = in.readParcelable(MediaDrm.class.getClassLoader());
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
