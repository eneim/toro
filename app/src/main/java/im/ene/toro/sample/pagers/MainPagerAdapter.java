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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * @author eneim (7/27/17).
 */

public class MainPagerAdapter extends FragmentStatePagerAdapter {

  MainPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override public Fragment getItem(int position) {
    // Each MainPageFragment contains another ViewPager where the actual Container stays.
    return MainPageFragment.newInstance();
  }

  @Override public int getCount() {
    return 4;
  }
}
