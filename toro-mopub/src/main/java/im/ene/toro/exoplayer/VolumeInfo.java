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

package im.ene.toro.exoplayer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author eneim (2018/03/14).
 */
public final class VolumeInfo implements Parcelable {

  private boolean mute;
  private float volume;

  public VolumeInfo(boolean mute, float volume) {
    this.mute = mute;
    this.volume = volume;
  }

  public boolean isMute() {
    return mute;
  }

  public void setMute(boolean mute) {
    this.mute = mute;
  }

  public float getVolume() {
    return volume;
  }

  public void setVolume(float volume) {
    this.volume = volume;
  }

  public void setTo(boolean mute, float volume) {
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
}
