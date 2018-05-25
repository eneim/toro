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

package toro.pixabay.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.squareup.moshi.Json;

@Entity public class VideoSizes {

  @PrimaryKey @NonNull private String videoId;  // set by client

  @Json(name = "large") private VideoSize large;
  @Json(name = "small") private VideoSize small;
  @Json(name = "medium") private VideoSize medium;
  @Json(name = "tiny") private VideoSize tiny;

  public VideoSize getLarge() {
    return large;
  }

  public void setLarge(VideoSize large) {
    this.large = large;
  }

  public VideoSize getSmall() {
    return small;
  }

  public void setSmall(VideoSize small) {
    this.small = small;
  }

  public VideoSize getMedium() {
    return medium;
  }

  public void setMedium(VideoSize medium) {
    this.medium = medium;
  }

  public VideoSize getTiny() {
    return tiny;
  }

  public void setTiny(VideoSize tiny) {
    this.tiny = tiny;
  }

  @NonNull public String getVideoId() {
    return videoId;
  }

  public void setVideoId(@NonNull String videoId) {
    this.videoId = videoId;
  }
}
