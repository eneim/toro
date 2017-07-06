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

package im.ene.toro.sample.flexible;

import android.net.Uri;

/**
 * @author eneim (7/1/17).
 */

class Content {

  static final String MP4_BUNNY = "file:///android_asset/big_buck_bunny.mp4";

  public static class Media {
    final int index;
    public final Uri mediaUri;

    Media(int index, Uri mediaUri) {
      this.index = index;
      this.mediaUri = mediaUri;
    }

    static Media getItem(int index) {
      return new Media(index, Uri.parse(MP4_BUNNY));
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Media)) return false;

      Media media = (Media) o;

      if (index != media.index) return false;
      return mediaUri.equals(media.mediaUri);
    }

    @Override public int hashCode() {
      int result = index;
      result = 31 * result + mediaUri.hashCode();
      return result;
    }
  }
}
