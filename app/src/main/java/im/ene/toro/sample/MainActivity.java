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

package im.ene.toro.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.sample.common.BaseActivity;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.sample.intro.IntroFragment;

public class MainActivity extends BaseActivity implements IntroFragment.Callback {

  @BindView(R.id.tab_layout) TabLayout tabLayout;
  @BindView(R.id.pager) ViewPager viewPager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);

    HomePagerAdapter adapter = new HomePagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(adapter);
    tabLayout.setupWithViewPager(viewPager);
  }

  @Override public void onToolbarCreated(Toolbar toolbar) {
    setSupportActionBar(toolbar);
    setTitle(R.string.app_name);
  }

  @Override public void onDemoClick(View view, IntroFragment.Demo demo) {
    startActivity(new Intent(this, demo.getActivityClass()));
  }

  //// HomePagerAdapter

  static class HomePagerAdapter extends FragmentStatePagerAdapter {

    HomePagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override public Fragment getItem(int position) {
      Fragment fragment = IntroFragment.newInstance();
      try {
        fragment = Deck.createFragment(Deck.Slide.values()[position].getFragmentClass());
      } catch (Deck.ToroDemoException e) {
        e.printStackTrace();
      }

      if (fragment instanceof BaseFragment) ((BaseFragment) fragment).setViewPagerMode(true);
      return fragment;
    }

    @Override public int getCount() {
      return Deck.Slide.values().length;
    }

    @Override public CharSequence getPageTitle(int position) {
      return "";
    }
  }
}
