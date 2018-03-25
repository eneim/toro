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

package im.ene.toro.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.FloatRange;

/**
 * @author eneim (2018/03/14).
 */
public final class VolumeInfo implements Parcelable {

  // Indicate that the playback is in muted state or not.
  private boolean mute;
  // The actual Volume value if 'mute' is false.
  @FloatRange(from = 0, to = 1) private float volume;

  public VolumeInfo(boolean mute, @FloatRange(from = 0, to = 1) float volume) {
    this.mute = mute;
    this.volume = volume;
  }

  public boolean isMute() {
    return mute;
  }

  public void setMute(boolean mute) {
    this.mute = mute;
  }

  @FloatRange(from = 0, to = 1) public float getVolume() {
    return volume;
  }

  public void setVolume(@FloatRange(from = 0, to = 1) float volume) {
    this.volume = volume;
  }

  public void setTo(boolean mute, @FloatRange(from = 0, to = 1) float volume) {
    this.mute = mute;
    this.volume = volume;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeByte(this.mute ? (byte) 1 : (byte) 0);
    dest.writeFloat(this.volume);
  }

  protected VolumeInfo(Parcel in) {
    this.mute = in.readByte() != 0;
    this.volume = in.readFloat();
  }

  public static final Creator<VolumeInfo> CREATOR = new ClassLoaderCreator<VolumeInfo>() {
    @Override public VolumeInfo createFromParcel(Parcel source, ClassLoader loader) {
      return new VolumeInfo(source);
    }

    @Override public VolumeInfo createFromParcel(Parcel source) {
      return new VolumeInfo(source);
    }

    @Override public VolumeInfo[] newArray(int size) {
      return new VolumeInfo[size];
    }
  };

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VolumeInfo that = (VolumeInfo) o;

    if (mute != that.mute) return false;
    return Float.compare(that.volume, volume) == 0;
  }

  @Override public int hashCode() {
    int result = (mute ? 1 : 0);
    result = 31 * result + (volume != +0.0f ? Float.floatToIntBits(volume) : 0);
    return result;
  }
}