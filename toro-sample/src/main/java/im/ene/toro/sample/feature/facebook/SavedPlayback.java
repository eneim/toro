/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.feature.facebook;

import android.os.Parcel;
import android.os.Parcelable;
import im.ene.toro.PlaybackState;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;

/**
 * @author eneim
 * @since 5/18/17
 */
public class SavedPlayback implements Parcelable {

  public final TimelineItem.VideoItem videoItem;
  public final PlaybackState playbackState;

  public SavedPlayback(TimelineItem.VideoItem videoItem, PlaybackState playbackState) {
    this.videoItem = videoItem;
    this.playbackState = playbackState;
  }

  protected SavedPlayback(Parcel in) {
    videoItem = in.readParcelable(TimelineItem.VideoItem.class.getClassLoader());
    playbackState = in.readParcelable(PlaybackState.class.getClassLoader());
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(videoItem, flags);
    dest.writeParcelable(playbackState, flags);
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Creator<SavedPlayback> CREATOR = new Creator<SavedPlayback>() {
    @Override public SavedPlayback createFromParcel(Parcel in) {
      return new SavedPlayback(in);
    }

    @Override public SavedPlayback[] newArray(int size) {
      return new SavedPlayback[size];
    }
  };
}
