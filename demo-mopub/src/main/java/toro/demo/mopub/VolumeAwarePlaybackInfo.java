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

package toro.demo.mopub;

import android.os.Parcel;
import android.support.annotation.NonNull;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;

/**
 * @author eneim (2018/03/26).
 */
public class VolumeAwarePlaybackInfo extends PlaybackInfo {

  @NonNull private final VolumeInfo volumeInfo;

  @Override public int describeContents() {
    return 0;
  }

  @NonNull public VolumeInfo getVolumeInfo() {
    return volumeInfo;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeParcelable(this.volumeInfo, flags);
  }

  @SuppressWarnings({ "unused", "WeakerAccess" }) public VolumeAwarePlaybackInfo() {
    this.volumeInfo = new VolumeInfo(false, 1f);
  }

  @SuppressWarnings("WeakerAccess") protected VolumeAwarePlaybackInfo(Parcel in) {
    super(in);
    this.volumeInfo = in.readParcelable(VolumeInfo.class.getClassLoader());
  }

  public static final Creator<VolumeAwarePlaybackInfo> CREATOR =
      new Creator<VolumeAwarePlaybackInfo>() {
        @Override public VolumeAwarePlaybackInfo createFromParcel(Parcel source) {
          return new VolumeAwarePlaybackInfo(source);
        }

        @Override public VolumeAwarePlaybackInfo[] newArray(int size) {
          return new VolumeAwarePlaybackInfo[size];
        }
      };
}