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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import im.ene.toro.sample.BaseActivity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author eneim.
 * @since 5/13/17.
 */

public class DemoActivity extends BaseActivity {

  static final String DEMO_FEATURE = "torolib:demo:page_name";

  public static Intent createIntent(Context parent, Feature feature) {
    Intent intent = new Intent(parent, DemoActivity.class);
    Bundle extra = new Bundle();
    extra.putSerializable(DEMO_FEATURE, feature);
    intent.putExtras(extra);
    return intent;
  }

  @SuppressWarnings("TryWithIdenticalCatches")  //
  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Feature feature = (Feature) getIntent().getExtras().getSerializable(DEMO_FEATURE);
    if (feature != null) {
      setTitle(feature.title);
      if (savedInstanceState == null) {
        Fragment fragment = null;
        try {
          //noinspection ConfusingArgumentToVarargsMethod
          Method method = feature.clazz.getMethod("newInstance", null);
          //noinspection ConfusingArgumentToVarargsMethod
          fragment = (Fragment) method.invoke(null, null);
        } catch (NoSuchMethodException e) {
          e.printStackTrace();
          finish();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
          finish();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
          finish();
        }

        if (fragment != null) {
          getSupportFragmentManager().beginTransaction()
              .replace(android.R.id.content, fragment)
              .commit();
        } else {
          finish();
        }
      }
    } else {
      finish();
    }
  }
}
