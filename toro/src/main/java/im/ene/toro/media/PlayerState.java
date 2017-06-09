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

import com.google.android.exoplayer2.C;

/**
 * @author eneim | 6/6/17.
 */

public class PlayerState {

  private int resumeWindow;
  private long resumePosition;

  public PlayerState(int resumeWindow, long resumePosition) {
    this.resumeWindow = resumeWindow;
    this.resumePosition = resumePosition;
  }

  public PlayerState() {
    this(C.INDEX_UNSET, C.TIME_UNSET);
  }

  public PlayerState(PlayerState other) {
    this(other.getResumeWindow(), other.getResumePosition());
  }

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
    if (!(o instanceof PlayerState)) return false;

    PlayerState that = (PlayerState) o;

    if (resumeWindow != that.resumeWindow) return false;
    return resumePosition == that.resumePosition;
  }

  @Override public int hashCode() {
    int result = resumeWindow;
    result = 31 * result + (int) (resumePosition ^ (resumePosition >>> 32));
    return result;
  }
}
