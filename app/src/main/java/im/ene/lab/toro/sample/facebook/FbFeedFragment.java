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

package im.ene.lab.toro.sample.facebook;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import im.ene.lab.toro.sample.fragment.RecyclerViewFragment;

/**
 * Created by eneim on 5/12/16.
 *
 * This is the normal Facebook feed list, which is different to 'Video playlist' which contains
 * only
 * Video.
 */
public class FbFeedFragment extends RecyclerViewFragment {

  public static final String TAG = "FbVideoListFragment";

  public static FbFeedFragment newInstance() {
    return new FbFeedFragment();
  }

  @NonNull @Override protected RecyclerView.LayoutManager getLayoutManager() {
    return new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
  }

  @NonNull @Override protected RecyclerView.Adapter getAdapter() {
    return new FbFeedAdapter();
  }
}
