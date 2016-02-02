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

package im.ene.lab.toro;

import android.graphics.Rect;
import java.util.List;

/**
 * Created by eneim on 2/1/16.
 */
public interface ToroStrategy {

  /**
   * @return Description of current Strategy
   * @hide
   */
  String getDescription();

  /**
   * Each item of #candidates returns true for {@link ToroPlayer#wantsToPlay(Rect, Rect)}. A Policy
   * gives the best fit Player to start playing
   */
  ToroPlayer getPlayer(List<ToroPlayer> candidates);

  /**
   * Indicate that this Policy requires a ToroPlayer's video view is fully visible or not
   *
   * @return true if this Policy requires a fully visible Video playable view, false otherwise.
   */
  boolean requireCompletelyVisible();
}
