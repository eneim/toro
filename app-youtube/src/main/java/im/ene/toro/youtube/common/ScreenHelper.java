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

package im.ene.toro.youtube.common;

import android.graphics.Point;
import android.util.Log;
import android.view.Display;

/**
 * @author eneim | 6/21/17.
 */

@SuppressWarnings("unused") public class ScreenHelper {

  private static final String TAG = "YouT:Screen";

  private ScreenHelper() {
    throw new RuntimeException("Meh!");
  }

  // If current window has the horizontal edge longer than vertical edge, it is a hint to
  // open and use a big player.
  // Of course if there is no available resource to play, we just ignore it.
  public static boolean shouldUseBigPlayer(Display display) {
    Point displaySize = new Point();
    display.getSize(displaySize);
    Log.i(TAG, "shouldUseBigPlayer: " + displaySize);
    return displaySize.x >= displaySize.y;
  }
}
