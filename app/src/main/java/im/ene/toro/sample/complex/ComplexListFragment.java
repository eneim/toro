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

package im.ene.toro.sample.complex;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.widget.Container;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author eneim (7/1/17).
 */

public class ComplexListFragment extends BaseFragment {

  @SuppressWarnings("unused") public static ComplexListFragment newInstance() {
    Bundle args = new Bundle();
    ComplexListFragment fragment = new ComplexListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_basic, container, false);
  }

  @BindView(R.id.player_container) Container container;
  GridLayoutManager layoutManager;
  ComplexListAdapter adapter;

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    layoutManager = new GridLayoutManager(getContext(), 2);
    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override public int getSpanSize(int position) {
        return position % 3 == 0 ? 2 : 1;
      }
    });
    container.setLayoutManager(layoutManager);
    adapter = new ComplexListAdapter();
    container.setAdapter(adapter);

    // Custom player selector for a complicated playback in grid
    PlayerSelector selector = new PlayerSelector() {
      @NonNull @Override public Collection<ToroPlayer> select(@NonNull Container container,
          @NonNull List<ToroPlayer> items) {
        List<ToroPlayer> toSelect;
        int count = items.size();
        if (count < 1) {
          toSelect = Collections.emptyList();
        } else {
          int firstOrder = items.get(0).getPlayerOrder();
          int span = layoutManager.getSpanSizeLookup().getSpanSize(firstOrder);
          count = Math.min(count, layoutManager.getSpanCount() / span);
          toSelect = new ArrayList<>();
          for (int i = 0; i < count; i++) {
            toSelect.add(items.get(i));
          }
        }

        return toSelect;
      }

      @NonNull @Override public PlayerSelector reverse() {
        return this;
      }
    };
    container.setPlayerSelector(selector);
  }

  @Override public void onDestroy() {
    layoutManager = null;
    adapter = null;
    container.setPlayerSelector(null);
    super.onDestroy();
  }
}
