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

package im.ene.lab.toro.sample.data;

import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.ToroSampleApp;

/**
 * Created by eneim on 1/30/16.
 */
public class VideoSource {

  private static final String MP4 = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

  private static final String HORIZONTAL =
      "android.resource://" + ToroSampleApp.packageName() + "/" + R.raw.horizontal;

  private static final String SQUARE =
      "android.resource://" + ToroSampleApp.packageName() + "/" + R.raw.square;

  private static final String VERTICAL =
      "android.resource://" + ToroSampleApp.packageName() + "/" + R.raw.vertical;

  public static final String[] SOURCES = {
      MP4, SQUARE, HORIZONTAL, VERTICAL
  };
}
