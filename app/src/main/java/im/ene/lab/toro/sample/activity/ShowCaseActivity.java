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

package im.ene.lab.toro.sample.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.fragment.MultiVideoComplicatedGridFragment;
import im.ene.lab.toro.sample.fragment.MultiVideoStaggeredGridFragment;
import im.ene.lab.toro.sample.fragment.SingleVideoSimpleListFragment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ShowCaseActivity extends AppCompatActivity {

  @StringDef({
      SingleVideoSimpleListFragment.TAG, MultiVideoStaggeredGridFragment.TAG,
      MultiVideoComplicatedGridFragment.TAG
  }) @Retention(RetentionPolicy.SOURCE) public @interface ShowcaseName {
  }

  private static final String EXTRA_FRAGMENT_NAME = "toro_showcase_fragment_name";

  public static Intent createIntent(Context context, @ShowcaseName String name) {
    Intent intent = new Intent(context, ShowCaseActivity.class);
    intent.putExtra(EXTRA_FRAGMENT_NAME, name);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_case);

    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
    if (fragment == null) {
      fragment = getFragment(getIntent().getStringExtra(EXTRA_FRAGMENT_NAME));
      if (fragment != null) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
      }
    }
  }

  @Nullable private Fragment getFragment(String name) {
    if (SingleVideoSimpleListFragment.TAG.equals(name)) {
      return SingleVideoSimpleListFragment.newInstance();
    } else if (MultiVideoStaggeredGridFragment.TAG.equals(name)) {
      return MultiVideoStaggeredGridFragment.newInstance();
    } else if (MultiVideoComplicatedGridFragment.TAG.equals(name)){
      return MultiVideoComplicatedGridFragment.newInstance();
    } else {
      return null;
    }
  }
}
