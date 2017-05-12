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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.ToroViewPagerHelper;
import im.ene.toro.sample.BaseActivity;
import im.ene.toro.sample.R;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author eneim.
 * @since 4/12/17.
 */

public class DemoActivity extends BaseActivity {

  @BindView(R.id.viewpager) ViewPager pager;
  @BindView(R.id.tab_layout) TabLayout tabLayout;
  ToroViewPagerHelper pagerHelper = new ToroViewPagerHelper();

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo_pager);
    ButterKnife.bind(this);

    PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
    pager.setAdapter(adapter);
    tabLayout.setupWithViewPager(pager);

    pager.addOnPageChangeListener(pagerHelper);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    pager.removeOnPageChangeListener(pagerHelper);
    try {
      pagerHelper.remove();
      pagerHelper = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static class PagerAdapter extends FragmentStatePagerAdapter {

    PagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override public Fragment getItem(int position) {
      try {
        return createItem(position);
      } catch (Throwable throwable) {
        throwable.printStackTrace();
        return null;
      }
    }

    private Fragment createItem(int position) throws Throwable {
      Page page = Page.values()[position];
      try {
        //noinspection ConfusingArgumentToVarargsMethod
        Method method = page.getClazz().getMethod("newInstance", null);
        //noinspection ConfusingArgumentToVarargsMethod
        return (Fragment) method.invoke(null, null);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
        throw e;
      } catch (InvocationTargetException e) {
        e.printStackTrace();
        throw e;
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        throw e;
      }
    }

    @Override public CharSequence getPageTitle(int position) {
      return Page.values()[position].getTitle();
    }

    @Override public int getCount() {
      return Page.values().length;
    }
  }
}
