/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

import im.ene.toro.annotations.Beta;
import im.ene.toro.widget.Container;

/**
 * @author eneim (2018/10/04).
 */
@Beta public interface PreLoader {

  // Return an array of 2 integers, that are the first and last completely visible item positions.
  // Result must be of length 2.
  int[] getOrderBound(Container container);

  /**
   * Called by {@link Container} to request for Player preload. Implement of this interface should
   * consider to prepare any Players whose orders are less than beforeOrder and bigger than
   * afterOrder.
   *
   * beforeOrder is always less than afterOrder.
   *
   * Total number of preload items must not exceed limit.
   */
  void prepareAround(int beforeOrder, int afterOrder, int limit);
}
