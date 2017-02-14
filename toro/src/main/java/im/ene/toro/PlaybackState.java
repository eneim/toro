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

package im.ene.toro;

/**
 * Created by eneim on 1/20/17.
 */

public final class PlaybackState {

  private final String mediaId;

  private Long duration;

  private Long position;

  PlaybackState(String mediaId) {
    this(mediaId, null, null);
  }

  PlaybackState(String mediaId, Long duration, Long position) {
    this.mediaId = mediaId;
    this.duration = duration;
    this.position = position;
  }

  public String getMediaId() {
    return mediaId;
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public Long getPosition() {
    return position;
  }

  public void setPosition(Long position) {
    this.position = position;
  }
}
