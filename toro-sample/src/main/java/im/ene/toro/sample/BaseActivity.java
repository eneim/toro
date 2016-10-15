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

package im.ene.toro.sample;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by eneim on 10/8/16.
 */

public abstract class BaseActivity extends AppCompatActivity {

  @Nullable protected String getScreenName() {
    return null;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void onStart() {
    super.onStart();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      onActive();
    }
  }

  @Override protected void onStop() {
    super.onStop();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      onInactive();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      onActive();
    }
  }

  @Override protected void onPause() {
    super.onPause();
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      onInactive();
    }
  }

  protected void onActive() {
  }

  protected void onInactive() {
  }
}
