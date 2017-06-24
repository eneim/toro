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

package im.ene.toro.media;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.exoplayer2.C;

/**
 * @author eneim | 6/6/17.
 */

public class PlaybackInfo implements Parcelable {

  private int resumeWindow;
  private long resumePosition;

  public PlaybackInfo(int resumeWindow, long resumePosition) {
    this.resumeWindow = resumeWindow;
    this.resumePosition = resumePosition;
  }

  public PlaybackInfo() {
    this(C.INDEX_UNSET, C.TIME_UNSET);
  }

  public PlaybackInfo(PlaybackInfo other) {
    this(other.getResumeWindow(), other.getResumePosition());
  }

  protected PlaybackInfo(Parcel in) {
    resumeWindow = in.readInt();
    resumePosition = in.readLong();
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(resumeWindow);
    dest.writeLong(resumePosition);
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Creator<PlaybackInfo> CREATOR = new ClassLoaderCreator<PlaybackInfo>() {
    @Override public PlaybackInfo createFromParcel(Parcel source, ClassLoader loader) {
      return new PlaybackInfo(source);
    }

    @Override public PlaybackInfo createFromParcel(Parcel source) {
      return new PlaybackInfo(source);
    }

    @Override public PlaybackInfo[] newArray(int size) {
      return new PlaybackInfo[size];
    }
  };

  public int getResumeWindow() {
    return resumeWindow;
  }

  public void setResumeWindow(int resumeWindow) {
    this.resumeWindow = resumeWindow;
  }

  public long getResumePosition() {
    return resumePosition;
  }

  public void setResumePosition(long resumePosition) {
    this.resumePosition = resumePosition;
  }

  public void reset() {
    resumeWindow = C.INDEX_UNSET;
    resumePosition = C.TIME_UNSET;
  }

  @Override public String toString() {
    return "State{" + "window=" + resumeWindow + ", position=" + resumePosition + '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PlaybackInfo)) return false;

    PlaybackInfo that = (PlaybackInfo) o;

    if (resumeWindow != that.resumeWindow) return false;
    return resumePosition == that.resumePosition;
  }

  @Override public int hashCode() {
    int result = resumeWindow;
    result = 31 * result + (int) (resumePosition ^ (resumePosition >>> 32));
    return result;
  }
}
