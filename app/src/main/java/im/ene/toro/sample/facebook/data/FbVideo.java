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
import androidx.annotation.NonNull;
import im.ene.toro.sample.common.MediaUrl;

/**
 * @author eneim | 6/18/17.
 */

public class FbVideo extends FbItem implements Parcelable {

  public final MediaUrl mediaUrl;

  public FbVideo(FbUser author, long index, long timeStamp, MediaUrl mediaUrl) {
    super(author, index, timeStamp);
    this.mediaUrl = mediaUrl;
  }

  public MediaUrl getMediaUrl() {
    return this.mediaUrl;
  }

  @NonNull @Override public String toString() {
    return "FbVideo{" + "media=" + mediaUrl.name() + '}';
  }

  public static FbVideo getItem(int index, int urlIdx, long timeStamp) {
    int urlCount = MediaUrl.values().length;
    return new FbVideo(FbUser.getUser(), index, timeStamp, MediaUrl.values()[urlIdx % urlCount]);
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(this.mediaUrl == null ? -1 : this.mediaUrl.ordinal());
  }

  protected FbVideo(Parcel in) {
    super(in);
    int tmpMediaUrl = in.readInt();
    this.mediaUrl = tmpMediaUrl == -1 ? null : MediaUrl.values()[tmpMediaUrl];
  }

  public static final Creator<FbVideo> CREATOR = new ClassLoaderCreator<FbVideo>() {
    @Override public FbVideo createFromParcel(Parcel source) {
      return new FbVideo(source);
    }

    @Override public FbVideo createFromParcel(Parcel source, ClassLoader loader) {
      return new FbVideo(source);
    }

    @Override public FbVideo[] newArray(int size) {
      return new FbVideo[size];
    }
  };
}
