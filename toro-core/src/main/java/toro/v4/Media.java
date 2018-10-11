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
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author eneim (2018/10/02).
 * @since 4.0.0
 */
public class Media implements Parcelable {

  @NonNull private final Uri uri;
  @Nullable private final String extension;

  public Media(@NonNull Uri uri, @Nullable String extension) {
    this.uri = uri;
    this.extension = extension;
  }

  @NonNull public Uri getUri() {
    return uri;
  }

  @Nullable public String getExtension() {
    return extension;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Media media = (Media) o;

    if (!uri.equals(media.uri)) return false;
    return extension != null ? extension.equals(media.extension) : media.extension == null;
  }

  @Override public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + (extension != null ? extension.hashCode() : 0);
    return result;
  }

  @Override public String toString() {
    return "Media{" + "uri=" + uri + ", extension='" + extension + '\'' + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(this.uri, flags);
    dest.writeString(this.extension);
  }

  protected Media(Parcel in) {
    //noinspection ConstantConditions
    this.uri = in.readParcelable(Uri.class.getClassLoader());
    this.extension = in.readString();
  }

  public static final Creator<Media> CREATOR = new Creator<Media>() {
    @Override public Media createFromParcel(Parcel source) {
      return new Media(source);
    }

    @Override public Media[] newArray(int size) {
      return new Media[size];
    }
  };
}
