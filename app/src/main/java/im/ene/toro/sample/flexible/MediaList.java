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

import android.util.SparseArray;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author eneim (7/6/17).
 */

class MediaList extends ArrayList<Content.Media> {

  private final SparseArray<Content.Media> backup = new SparseArray<>();

  @Override public Content.Media get(int index) {
    Content.Media cache = backup.get(index);
    if (cache == null) {
      cache = Content.Media.getItem(index);
      backup.put(index, cache);
    }

    return cache;
  }

  @Override public int size() {
    return Integer.MAX_VALUE;
  }

  @Override public int indexOf(Object o) {
    return o instanceof Content.Media ? backup.indexOfValue((Content.Media) o) : -1;
  }

  @Override public Content.Media remove(int index) {
    Content.Media cache = backup.get(index);
    backup.remove(index);
    return cache;
  }

  void move(int fromPos, int toPos) {
    if (fromPos == toPos) return;
    if (fromPos < toPos) {
      for (int i = fromPos; i < toPos; i++) {
        Collections.swap(this, i, i + 1);
      }
    } else {
      for (int i = fromPos; i > toPos; i--) {
        Collections.swap(this, i, i - 1);
      }
    }
  }

  @Override public Content.Media set(int index, Content.Media element) {
    Content.Media old = backup.get(index);
    backup.put(index, element);
    return old;
  }

  @NonNull @Override public Iterator<Content.Media> iterator() {
    throw new UnsupportedOperationException("Un-supported.");
  }
}
