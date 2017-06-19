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

package im.ene.toro.sample.features.facebook.data;

import im.ene.toro.sample.data.MediaEntity;
import im.ene.toro.sample.data.MediaItem;
import im.ene.toro.sample.data.MediaUrl;

/**
 * @author eneim | 6/18/17.
 */

public class FbVideo extends FbItem implements MediaEntity {

  public final MediaItem mediaItem;

  public FbVideo(FbUser author, long index, long timeStamp, MediaItem mediaItem) {
    super(author, index, timeStamp);
    this.mediaItem = mediaItem;
  }

  @Override public MediaUrl getMediaUrl() {
    return this.mediaItem.getMediaUrl();
  }

  public static FbVideo getItem(int index, int urlIdx, long timeStamp) {
    int urlCount = MediaUrl.values().length;
    return new FbVideo(FbUser.getUser(), index, timeStamp,
        new MediaItem(index, MediaUrl.values()[urlIdx % urlCount]));
  }
}
