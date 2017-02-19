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
import im.ene.toro.sample.feature.advance1.Advance1Activity;
import im.ene.toro.sample.feature.average1.Average1Activity;
import im.ene.toro.sample.feature.basic1.Basic1Activity;
import im.ene.toro.sample.feature.basic2.Basic2Activity;
import im.ene.toro.sample.feature.basic3.Basic3Activity;
import im.ene.toro.sample.feature.basic4.Basic4Activity;
import im.ene.toro.sample.feature.extended.ExtendedListActivity;
import im.ene.toro.sample.feature.facebook.FacebookTimelineActivity;
import im.ene.toro.sample.feature.legacy.LegacyActivity;
import im.ene.toro.sample.experiment.MediaListActivity;

/**
 * Created by eneim on 2/15/17.
 */

public enum Feature {
  EXPERIMENT(MediaListActivity.class, R.string.experiment, R.string.experiment_description), //
  BASIC_4(Basic4Activity.class, R.string.basic_sample_4, R.string.basic_sample_4_description),  //
  BASIC_1(Basic1Activity.class, R.string.basic_sample_1, R.string.basic_sample_1_description),  //
  BASIC_2(Basic2Activity.class, R.string.basic_sample_2, R.string.basic_sample_2_description),  //
  BASIC_3(Basic3Activity.class, R.string.basic_sample_3, R.string.basic_sample_3_description),  //
  AVERAGE_1(Average1Activity.class, R.string.average_sample_1,
      R.string.average_sample_1_description),  //
  ADVANCE_1(Advance1Activity.class, R.string.advance_sample_1,
      R.string.advance_sample_1_description),  //
  EXTENDED(ExtendedListActivity.class, R.string.extended_sample_1,
      R.string.extended_sample_1_description), //
  FACEBOOK(FacebookTimelineActivity.class, R.string.facebook_sample_1,
      R.string.facebook_sample_1_description), //
  LEGACY(LegacyActivity.class, R.string.legacy_sample_1, R.string.legacy_sample_1_description)  //
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
