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

package im.ene.toro.sample.pagers;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.material.tabs.TabLayout;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseActivity;

/**
 * @author eneim (7/27/17).
 */

public class ManyPagersActivity extends BaseActivity {

  @BindView(R.id.tab_layout) TabLayout tabLayout;
  @BindView(R.id.pager) ViewPager viewPager;
  @BindDimen(R.dimen.button_height) int tabHeight;

  MainPagerAdapter pagerAdapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    tabLayout.getLayoutParams().height = tabHeight;
    tabLayout.setSelectedTabIndicatorHeight(tabHeight);

    pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);
  }
}
