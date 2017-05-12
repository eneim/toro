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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.BaseAdapter;
import im.ene.toro.Toro;
import im.ene.toro.ToroAdapter;
import im.ene.toro.sample.BaseToroFragment;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.SimpleVideoObject;
import im.ene.toro.sample.widget.SimpleMediaViewHolder;

/**
 * @author eneim.
 * @since 5/9/17.
 */

public class SingleItemFragment extends BaseToroFragment {

  public static SingleItemFragment newInstance() {
    Bundle args = new Bundle();
    SingleItemFragment fragment = new SingleItemFragment();
    fragment.setArguments(args);
    return fragment;
  }

  static class Adapter extends BaseAdapter<ToroAdapter.ViewHolder> {

    @Nullable @Override protected Object getItem(int position) {
      return new SimpleVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new SimpleMediaViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(SimpleMediaViewHolder.LAYOUT_RES, parent, false));
    }

    @Override public int getItemCount() {
      return 1;
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.generic_recycler_view, container, false);
  }

  RecyclerView recyclerView;

  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
        ((LinearLayoutManager) layoutManager).getOrientation()));

    Adapter adapter = new Adapter();
    recyclerView.setHasFixedSize(false);
    recyclerView.setAdapter(adapter);

    Toro.register(recyclerView);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    Toro.unregister(recyclerView);
  }
}
