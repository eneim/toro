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

package im.ene.toro.sample.feature;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.advance1.Advance1ListFragment;
import im.ene.toro.sample.feature.average1.Average1ListFragment;
import im.ene.toro.sample.feature.basic1.Basic1ListFragment;
import im.ene.toro.sample.feature.basic2.Basic2ListFragment;
import im.ene.toro.sample.feature.basic3.Basic3ListFragment;
import im.ene.toro.sample.feature.basic4.Basic4ListFragment;
import im.ene.toro.sample.feature.extended.ExtendedListFragment;
import im.ene.toro.sample.feature.facebook.FacebookTimelineFragment;
import im.ene.toro.sample.feature.legacy.LegacyListFragment;
import im.ene.toro.sample.feature.single.SingleItemFragment;

/**
 * Created by eneim on 2/15/17.
 */

public enum Feature {
  SINGLE(SingleItemFragment.class, R.string.single_item, R.string.single_item_description), //
  FACEBOOK(FacebookTimelineFragment.class, R.string.facebook_sample_1,
      R.string.facebook_sample_1_description), //
  BASIC_1(Basic1ListFragment.class, R.string.basic_sample_1,
      R.string.basic_sample_1_description),  //
  BASIC_2(Basic2ListFragment.class, R.string.basic_sample_2,
      R.string.basic_sample_2_description),  //
  BASIC_3(Basic3ListFragment.class, R.string.basic_sample_3,
      R.string.basic_sample_3_description),  //
  BASIC_4(Basic4ListFragment.class, R.string.basic_sample_4,
      R.string.basic_sample_4_description),  //
  AVERAGE_1(Average1ListFragment.class, R.string.average_sample_1,
      R.string.average_sample_1_description),  //
  ADVANCE_1(Advance1ListFragment.class, R.string.advance_sample_1,
      R.string.advance_sample_1_description),  //
  EXTENDED(ExtendedListFragment.class, R.string.extended_sample_1,
      R.string.extended_sample_1_description), //
  LEGACY(LegacyListFragment.class, R.string.legacy_sample_1,
      R.string.legacy_sample_1_description)  //
  ;

  public final Class<?> clazz;

  @StringRes public final int title;

  @StringRes public final int description;

  @DrawableRes public final int image;

  Feature(Class<?> clazz, int title, int description, int image) {
    this.clazz = clazz;
    this.title = title;
    this.description = description;
    this.image = image;
  }

  @Deprecated Feature(Class<?> clazz, int title, int description) {
    this(clazz, title, description, 0);
  }
}
