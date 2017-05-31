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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.DefaultPlayerManager;
import im.ene.toro.Player;
import im.ene.toro.Strategy;
import im.ene.toro.ToroHelper;
import im.ene.toro.ToroUtil;
import im.ene.toro.widget.Container;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "ToroLib:Sample";

  ToroHelper helper;
  @BindView(R.id.recycler_view) Container container;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    RecyclerView.Adapter adapter = new SimpleAdapter();
    container.setAdapter(adapter);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    container.setLayoutManager(layoutManager);

    helper = new ToroHelper(new DefaultPlayerManager(), Strategy.DEFAULT);
    helper.registerContainer(container);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    helper.registerContainer(null);
  }

  static class ItemPlayer extends RecyclerView.ViewHolder implements Player {

    @BindView(R.id.item_container) LinearLayout container;
    @BindView(R.id.text_content) TextView content;
    @BindView(R.id.text_indicator) TextView indicator;

    ItemPlayer(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @NonNull @Override public View getPlayerView() {
      return container;
    }

    @Override public void play() {
      indicator.setText("PLAY");
    }

    @Override public void pause() {
      indicator.setText("PAUSE");
    }

    @Override public boolean isPlaying() {
      return "PLAY".equals(indicator.getText().toString());
    }

    @Override public boolean wantsToPlay() {
      ViewParent parent = itemView.getParent();
      float visible = parent != null && parent instanceof Container ? //
          ToroUtil.visibleAreaOffset(indicator, (Container) parent) : 0;
      Log.i(TAG, getAdapterPosition() + " | " + visible);
      return visible >= 0.75;
    }
  }

  private static class SimpleAdapter extends RecyclerView.Adapter<ItemPlayer> {

    SimpleAdapter() {
    }

    @Override public ItemPlayer onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.vh_simple_player, parent, false);
      return new ItemPlayer(view);
    }

    @Override public void onBindViewHolder(ItemPlayer holder, int position) {
      holder.content.setText("POS: " + (position + 1));
      holder.indicator.setText("BOUND");
    }

    @Override public int getItemCount() {
      return Integer.MAX_VALUE;
    }
  }
}
