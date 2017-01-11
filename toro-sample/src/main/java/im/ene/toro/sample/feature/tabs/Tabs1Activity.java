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

package im.ene.toro.sample.feature.tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import im.ene.toro.sample.R;

/**
 * Created by eneim on 6/30/16.
 */
public class Tabs1Activity extends AppCompatActivity {

  TabLayout tabLayout;
  TabAdapter adapter;
  int currentSelectedTab = 0;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tabs);

    ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
    tabLayout = (TabLayout) findViewById(R.id.tabLayout);

    adapter = new TabAdapter(getSupportFragmentManager());
    viewPager.setAdapter(adapter);
    tabLayout.setupWithViewPager(viewPager);

    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override public void onPageSelected(int position) {
        // notify previous and current fragments so they can pause/resume video playback
        if (currentSelectedTab != position) {
          notifyTabSelected(currentSelectedTab, false);
        }
        notifyTabSelected(position, true);
        currentSelectedTab = position;
      }

      @Override public void onPageScrollStateChanged(int state) {
      }
    });
  }

  private void notifyTabSelected(int position, boolean isSelected) {
    // get tab and notify it of selection state
    TabsListFragment prevFragment = (TabsListFragment) adapter.getItem(position);
    prevFragment.notifyTabSelected(isSelected);
  }

  public class TabAdapter extends FragmentStatePagerAdapter {
    Fragment one;
    Fragment two;

    TabAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override public Fragment getItem(int i) {
      if (i == 0) {
        if (one == null) {
          one = TabsListFragment.newInstance();
        }
        return one;
      } else {
        if (two == null) {
          two = TabsListFragment.newInstance();
        }
        return two;
      }
    }

    @Override public int getCount() {
      return 2;
    }

    @Override public CharSequence getPageTitle(int position) {
      return position == 0 ? "TAB ONE" : "TAB TWO";
    }
  }
}
