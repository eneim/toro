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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author eneim | 6/6/17.
 */

public final class Deck {

  private Deck() {
  }

  public static void present(FragmentActivity activity, Class<?> fragmentClass)
      throws ToroDemoException {
    Fragment fragment;
    //noinspection TryWithIdenticalCatches
    try {
      //noinspection ConfusingArgumentToVarargsMethod
      Method method = fragmentClass.getMethod("newInstance", null); // <-- must implement this method.
      //noinspection ConfusingArgumentToVarargsMethod
      fragment = (Fragment) method.invoke(null, null);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      throw new ToroDemoException(e.getLocalizedMessage(), e);
    } catch (InvocationTargetException e) {
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

  public static void present(FragmentActivity activity, String fragmentClassName)
      throws ToroDemoException {
    try {
      present(activity, Class.forName(fragmentClassName));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new ToroDemoException(e.getLocalizedMessage(), e);
    }
  }

  // naming this exception by intent, for log filtering purpose.
  @SuppressWarnings("WeakerAccess") public static class ToroDemoException extends Exception {

    public ToroDemoException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
