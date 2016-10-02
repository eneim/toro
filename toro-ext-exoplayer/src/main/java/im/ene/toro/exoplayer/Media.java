/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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

import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by eneim on 6/3/16.
 *
 * Simple media item definition. Application <b>MUST</b> extends this for custom usage.
 */
public class Media {

  private final Uri mediaUri;

  public Media(@NonNull Uri mediaUri) {
    this.mediaUri = mediaUri;
  }

  public final Uri getMediaUri() {
    return mediaUri;
  }
}
