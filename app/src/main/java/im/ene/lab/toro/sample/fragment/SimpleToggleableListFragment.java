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

package im.ene.lab.toro.sample.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ToggleButton;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.ToroStrategy;
import im.ene.lab.toro.sample.R;
import java.util.List;

/**
 * Created by eneim on 4/8/16.
 */
public class SimpleToggleableListFragment extends Fragment {

  public static final String TAG = "ToggleableListFragment";

  boolean mPlayable = true;

  public static SimpleToggleableListFragment newInstance() {
    return new SimpleToggleableListFragment();
  }

  private final ToroStrategy FIRST_PLAYABLE_TOP_DOWN_TOGGLEABLE = new ToroStrategy() {
    @Override public String getDescription() {
      return "First playable Player, top-down. Triggering playback by Toggle Button";
    }

    @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
      return Toro.Strategies.FIRST_PLAYABLE_TOP_DOWN.findBestPlayer(candidates);
    }

    @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
      return mPlayable && Toro.Strategies.FIRST_PLAYABLE_TOP_DOWN.allowsToPlay(player, parent);
    }
  };

  private ToroStrategy mOldStrategy;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    // Backup this for other Fragments
    mOldStrategy = Toro.getStrategy();
    Toro.setStrategy(FIRST_PLAYABLE_TOP_DOWN_TOGGLEABLE);
  }

  @Override public void onDetach() {
    // Restore Strategy which is used by others
    Toro.setStrategy(mOldStrategy);
    super.onDetach();
  }

  @Bind(R.id.playback_toggle) ToggleButton mToggleButton;

  @OnClick(R.id.playback_toggle) void togglePlayback() {
    // A trick to refresh playback on a flag change
    Toro.rest(true);
    mPlayable = mToggleButton.isChecked();
    Toro.rest(false);
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_toggleable, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getChildFragmentManager().beginTransaction()
        .replace(R.id.fragment_list, DualVideoListFragment.BottomFragment.newInstance())
        .commit();

    // Listen to very first toggle value
    togglePlayback();
  }
}
