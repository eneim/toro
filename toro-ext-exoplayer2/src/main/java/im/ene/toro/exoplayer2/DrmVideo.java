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

package im.ene.toro.exoplayer2;

import android.net.Uri;

/**
 * Created by eneim on 10/2/16.
 */

public class DrmVideo extends Media {

  private final String type;
  private final String licenseUrl;
  private final String[] keyRequestPropertiesArray;

  public DrmVideo(Uri mediaUri, String type, String licenseUrl,
      String[] keyRequestPropertiesArray) {
    super(mediaUri);
    this.type = type;
    this.licenseUrl = licenseUrl;
    this.keyRequestPropertiesArray = keyRequestPropertiesArray;
  }

  public String getType() {
    return type;
  }

  public String getLicenseUrl() {
    return licenseUrl;
  }

  public String[] getKeyRequestPropertiesArray() {
    return keyRequestPropertiesArray;
  }
}
