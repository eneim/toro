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

package im.ene.toro.sample.nested;

import android.os.Parcel;
import android.util.SparseArray;
import im.ene.toro.media.PlaybackInfo;

/**
 * Proxy for PlaybackInfo. The top Container would save this version of PlaybackInfo for this
 * ToroPlayer, we un-wrap it here to get the actual PlaybackInfo data.
 *
 * @author eneim (2018/02/20).
 */
@SuppressWarnings("WeakerAccess") //
public class ExtraPlaybackInfo extends PlaybackInfo {

  final SparseArray actualInfo;

  public ExtraPlaybackInfo(SparseArray<PlaybackInfo> actualInfo) {
    this.actualInfo = actualInfo;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    //noinspection unchecked
    dest.writeSparseArray(this.actualInfo);
  }

  protected ExtraPlaybackInfo(Parcel in) {
    super(in);
    this.actualInfo = in.readSparseArray(PlaybackInfo.class.getClassLoader());
  }

  public static final Creator<ExtraPlaybackInfo> CREATOR =
      new ClassLoaderCreator<ExtraPlaybackInfo>() {
        @Override public ExtraPlaybackInfo createFromParcel(Parcel source) {
          return new ExtraPlaybackInfo(source);
        }

        @Override public ExtraPlaybackInfo createFromParcel(Parcel source, ClassLoader loader) {
          return new ExtraPlaybackInfo(source);
        }

        @Override public ExtraPlaybackInfo[] newArray(int size) {
          return new ExtraPlaybackInfo[size];
        }
      };

  @Override public String toString() {
    return "ExtraInfo{" + "actualInfo=" + actualInfo + '}';
  }
}
