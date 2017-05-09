/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.dev;

import im.ene.toro.sample.feature.advance1.Advance1ListFragment;
import im.ene.toro.sample.feature.average1.Average1ListFragment;
import im.ene.toro.sample.feature.basic1.Basic1ListFragment;
import im.ene.toro.sample.feature.basic2.Basic2ListFragment;
import im.ene.toro.sample.feature.basic3.Basic3ListFragment;
import im.ene.toro.sample.feature.basic4.Basic4ListFragment;
import im.ene.toro.sample.feature.extended.ExtendedListFragment;
import im.ene.toro.sample.feature.facebook.FacebookTimelineFragment;

/**
 * @author eneim.
 * @since 4/12/17.
 */

enum Page {
  FACEBOOK("Facebook", FacebookTimelineFragment.class),
  BASIC1("Basic 1", Basic1ListFragment.class),
  BASIC2("Basic 2", Basic2ListFragment.class),
  BASIC3("Basic 3", Basic3ListFragment.class),
  BASIC4("Basic 4", Basic4ListFragment.class),
  AVERAGE1("Average 1", Average1ListFragment.class),
  ADVANCE1("Advance 1", Advance1ListFragment.class),
  EXTENDED("Extended", ExtendedListFragment.class),
  ;

  private final String title;
  private final Class<?> clazz;

  Page(String title, Class<?> clazz) {
    this.title = title;
    this.clazz = clazz;
  }

  public String getTitle() {
    return title;
  }

  public Class<?> getClazz() {
    return clazz;
  }
}
