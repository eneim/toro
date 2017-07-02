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
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
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

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;

public class MainActivity extends BaseActivity implements IntroFragment.Callback {

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
  @BindView(R.id.pager) ViewPager viewPager;
  ViewPager.OnPageChangeListener pageChangeListener;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);

    pageChangeListener = new PageChangeHelper() {
      int lastPage = -1;
      @Override void dispatchCurrentPageChanged(int currentPage) {
        if (lastPage != currentPage) {
          lastPage = currentPage;
          toolbarLayout.setTitle(Deck.Slide.values()[lastPage].getTitle());
        }
      }
    };

    viewPager.addOnPageChangeListener(pageChangeListener);
    HomePagerAdapter adapter = new HomePagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(adapter);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    viewPager.removeOnPageChangeListener(pageChangeListener);
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
      Fragment fragment;
      try {
        fragment = Deck.createFragment(Deck.Slide.values()[position].getFragmentClass());
      } catch (Deck.ToroDemoException e) {
        e.printStackTrace();
        fragment = IntroFragment.newInstance();
      }
      if (fragment instanceof BaseFragment) {
        ((BaseFragment) fragment).setViewPagerMode(true);
      }
      return fragment;
    }

    @Override public int getCount() {
      return Deck.Slide.values().length;
    }

    @Override public CharSequence getPageTitle(int position) {
      return Deck.Slide.values()[position].getTitle();
    }
  }

  static abstract class PageChangeHelper extends ViewPager.SimpleOnPageChangeListener {

    private static final int MSG_LAYOUT_STABLE = -100;  // Negative, to prevent conflict

    private boolean firstScroll = true;
    private int currentPage = -1;

    private Handler handler = new Handler(msg -> {
      dispatchCurrentPageChanged(msg.arg1);
      return true;
    });

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      if (firstScroll) {
        currentPage = position;
        handler.removeMessages(MSG_LAYOUT_STABLE);
        firstScroll = false;
      }
    }

    @Override public void onPageSelected(int position) {
      if (currentPage != position) {
        currentPage = position;
        handler.removeMessages(MSG_LAYOUT_STABLE);
      }
    }

    @Override public void onPageScrollStateChanged(int state) {
      super.onPageScrollStateChanged(state);
      if (state == SCROLL_STATE_IDLE) {
        handler.sendMessage(handler.obtainMessage(MSG_LAYOUT_STABLE, currentPage, 0));
      }
    }

    abstract void dispatchCurrentPageChanged(int currentPage);
  }
}
