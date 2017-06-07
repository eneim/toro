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

package im.ene.toro.sample.data;

import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author eneim | 6/7/17.
 */

public final class DataSource {

  private volatile static DataSource singleton;

  public static DataSource getInstance() {
    if (singleton == null) {
      synchronized (DataSource.class) {
        if (singleton == null) {
          singleton = new DataSource();
        }
      }
    }

    return singleton;
  }

  private final List<Entity> entities = new CopyOnWriteArrayList<>();
  private final Random random = new Random();
  @SuppressWarnings("WeakerAccess") final AtomicBoolean loading = new AtomicBoolean(false);

  public Observable<List<Entity>> getFromCloud(boolean loadMore, int count) {
    // the following codes are not wrapped by RxJava, user has the responsibility to execute this
    // method in io thread.
    if (!loadMore) this.entities.clear();
    loading.set(true);
    List<Entity> entities = new ArrayList<>();
    int urlCount = MediaUrl.values().length;
    if (count > 0) {
      int mediaIdx = 0;
      for (int i = 0; i < count; i++) {
        if (random.nextFloat() < 0.65 /* magic number */) {
          entities.add(new MediaItem(MediaUrl.values()[mediaIdx++ % urlCount]));
        } else {
          entities.add(new TextItem());
        }
      }
    }
    // use delay operator to simulate real API call.
    return Observable.just(entities).delay(1000, TimeUnit.MILLISECONDS).doOnNext(items -> {
      this.entities.addAll(items);
      loading.set(false);
    });
  }

  // just use to sync with Adapter data. real-world practice should not use this.
  public List<Entity> getEntities() {
    return entities;
  }

  public boolean isLoading() {
    return this.loading.get();
  }
}
