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

package im.ene.toro.sample.feature.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.ene.toro.sample.BaseActivity;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.advance1.Advance1Activity;
import im.ene.toro.sample.feature.average1.Average1Activity;
import im.ene.toro.sample.feature.basic1.Basic1Activity;
import im.ene.toro.sample.feature.basic2.Basic2Activity;
import im.ene.toro.sample.feature.basic3.Basic3Activity;
import im.ene.toro.sample.feature.extended.ExtendedListActivity;
import im.ene.toro.sample.feature.facebook.FacebookTimelineActivity;
import im.ene.toro.sample.feature.legacy.LegacyActivity;
import im.ene.toro.sample.feature.tabs.Tabs1Activity;

/**
 * Created by eneim on 6/30/16.
 */
public class HomeActivity extends BaseActivity {

  @Bind(R.id.toolbar) Toolbar toolbar;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);
  }

  @OnClick(R.id.basic_sample_1) void openBasicSample1() {
    startActivity(new Intent(this, Basic1Activity.class));
  }

  @OnClick(R.id.basic_sample_2) void openBasicSample2() {
    startActivity(new Intent(this, Basic2Activity.class));
  }

  @OnClick(R.id.basic_sample_3) void openBasicSample3() {
    startActivity(new Intent(this, Basic3Activity.class));
  }

  @OnClick(R.id.average_sample_1) void openAverageSample1() {
    startActivity(new Intent(this, Average1Activity.class));
  }

  @OnClick(R.id.advance_sample_1) void openAdvanceSample1() {
    startActivity(new Intent(this, Advance1Activity.class));
  }

  @OnClick(R.id.custom_sample_1) void openCustomSample1() {
    startActivity(new Intent(this, FacebookTimelineActivity.class));
  }

  @OnClick(R.id.legacy_sample_1) void openLegacySample1() {
    startActivity(new Intent(this, LegacyActivity.class));
  }

  @OnClick(R.id.exoplayer2_sample) void openExoPlayer2Sample1() {
    startActivity(new Intent(this, ExtendedListActivity.class));
  }

  @OnClick(R.id.tabs_sample) void openTabsSample() {
    startActivity(new Intent(this, Tabs1Activity.class));
  }
}
