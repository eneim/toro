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

import java.util.Arrays;
import java.util.List;

/**
 * Created by eneim on 2/1/16.
 */
final class ToroUtils {

  private ToroUtils() {
    throw new AssertionError("Not supported");
  }

  static Integer[] asArray(int[] array) {
    if (array == null) {
      return null;
    }

    Integer[] result = new Integer[array.length];
    if (array.length > 0) {
      for (int i = 0; i < array.length; i++) {
        result[i] = array[i];
      }
    }

    return result;
  }

  static <T> List<T> asList(T[] array) {
    return Arrays.asList(array);
  }

  static List<Integer> asList(int[] array) {
    return asList(asArray(array));
  }
}
