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

package im.ene.toro.exoplayer2;

import android.support.annotation.NonNull;
import com.google.android.exoplayer2.source.MediaSource;

/**
 * Created by eneim on 2/7/17.
 */

public abstract class MediaBundle {

  private final MediaSource mediaSource;

  public MediaBundle(@NonNull MediaSource mediaSource) {
    this.mediaSource = mediaSource;
  }

  @NonNull public MediaSource getMediaSource() {
    return mediaSource;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MediaBundle)) return false;

    MediaBundle that = (MediaBundle) o;

    return mediaSource.equals(that.mediaSource);
  }

  @Override public int hashCode() {
    return mediaSource.hashCode();
  }
}
