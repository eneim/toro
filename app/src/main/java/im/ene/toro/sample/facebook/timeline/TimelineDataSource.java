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

package im.ene.toro.sample.facebook.timeline;

import im.ene.toro.sample.common.data.DataLoader;
import im.ene.toro.sample.common.data.Entity;
import im.ene.toro.sample.facebook.data.FbVideo;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author eneim | 6/18/17.
 */
@SuppressWarnings("WeakerAccess") public class TimelineDataSource implements DataLoader {

  private static final String TAG = "Toro:Fb:Timeline";

  private static TimelineDataSource singleton;
  final AtomicBoolean loading = new AtomicBoolean(false);

  public static TimelineDataSource getInstance() {
    if (singleton == null) {
      synchronized (TimelineDataSource.class) {
        if (singleton == null) {
          singleton = new TimelineDataSource();
        }
      }
    }

    return singleton;
  }

  public Observable<ArrayList<Entity>> fetchTimeline(boolean more, int count, int offset) {
    int delay = more ? 1000 : 100;
    final int[] index = { offset };
    return Observable.just(new ArrayList<Entity>())
        .repeat(count)
        .doOnNext(entities -> entities.add(
            FbVideo.getItem(index[0]++, index[0]++, System.nanoTime() / 1_000_000)))
        .delay(delay, TimeUnit.MILLISECONDS)
        .doOnSubscribe(disposable -> loading.set(true))
        .doOnComplete(() -> loading.set(false));
  }

  @Override public boolean isLoading() {
    return this.loading.get();
  }
}
