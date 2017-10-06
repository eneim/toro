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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import im.ene.toro.sample.basic.BasicListFragment;
import im.ene.toro.sample.complex.ComplexListFragment;
import im.ene.toro.sample.flexible.FlexibleListFragment;
import im.ene.toro.sample.intro.IntroFragment;
import im.ene.toro.sample.nested.NestedListFragment;

/**
 * @author eneim | 6/6/17.
 *
 *         A "Deck" to "present" some demonstrations. Naming by the context, no big deal.
 */

public final class Deck {

  private Deck() {
  }

  public static void present(FragmentActivity activity, Class<? extends Fragment> fragmentClass)
      throws ToroDemoException {
    Fragment fragment;
    try {
      fragment = fragmentClass.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
      throw new ToroDemoException(e.getLocalizedMessage(), e);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      throw new ToroDemoException(e.getLocalizedMessage(), e);
    }

    if (fragment != null) {
      activity.getSupportFragmentManager().beginTransaction() //
          .replace(android.R.id.content, fragment).commit();
    } else {
      activity.finish();
    }
  }

  @SuppressWarnings("WeakerAccess") //
  public static Fragment createFragment(Class<? extends Fragment> fragmentClass)
      throws ToroDemoException {
    Fragment fragment;
    try {
      fragment = fragmentClass.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
      throw new ToroDemoException(e.getLocalizedMessage(), e);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      throw new ToroDemoException(e.getLocalizedMessage(), e);
    }
    return fragment;
  }

  // naming this exception by intent, for log filtering purpose.
  @SuppressWarnings("WeakerAccess") //
  public static class ToroDemoException extends Exception {

    public ToroDemoException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  //// present the ViewPager

  public enum Slide {
    INTRO("Intro", IntroFragment.class),  //
    // CUSTOM("Custom", CustomLayoutFragment.class), // This is shown only by Activity
    BASIC("Basic", BasicListFragment.class), //
    // TIMELINE("Timeline", TimelineFragment.class), // This is shown only by Activity
    NESTED("Nested Container", NestedListFragment.class),  //
    COMPLEX("Complex Grid", ComplexListFragment.class), //
    FLEXIBLE("Flexible Grid", FlexibleListFragment.class)  //
    ;
    private final String title;
    private final Class<? extends Fragment> fragmentClass;

    Slide(String title, Class<? extends Fragment> fragmentClass) {
      this.title = title;
      this.fragmentClass = fragmentClass;
    }

    public String getTitle() {
      return title;
    }

    public Class<? extends Fragment> getFragmentClass() {
      return fragmentClass;
    }
  }
}
