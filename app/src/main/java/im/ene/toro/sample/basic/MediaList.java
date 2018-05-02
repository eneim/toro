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

package im.ene.toro.sample.basic;

import java.util.ArrayList;

/**
 * A special {@link ArrayList}
 *
 * @author eneim (7/1/17).
 */

final class MediaList extends ArrayList<Content.Media> {

  @Override public int size() {
    return Integer.MAX_VALUE;
  }

  @Override public Content.Media get(int index) {
    return Content.Media.getItem(index);
  }

  @Override public int indexOf(Object o) {
    return o instanceof Content.Media ? ((Content.Media) o).index : -1;
  }

  @Override public boolean add(Content.Media media) {
    throw new UnsupportedOperationException("Unsupported");
  }

  @Override public Content.Media remove(int index) {
    throw new UnsupportedOperationException("Unsupported");
  }
}
