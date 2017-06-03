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
import android.support.v7.widget.GridLayoutManager;
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
import im.ene.toro.Selector;
import im.ene.toro.ToroHelper;
import im.ene.toro.ToroUtil;
import im.ene.toro.widget.Container;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

  @SuppressWarnings("unused") private static final String TAG = "ToroLib:Sample";

  ToroHelper helper;
  @BindView(R.id.recycler_view) Container container;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    RecyclerView.Adapter adapter = new SimpleAdapter();
    container.setAdapter(adapter);
    GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override public int getSpanSize(int position) {
        return position % 4 == 0 || position % 4 == 3 ? 2 : 1;
      }
    });

    container.setLayoutManager(layoutManager);

    helper = new ToroHelper(new DefaultPlayerManager(2), Selector.DEFAULT);
  }

  @Override protected void onStart() {
    super.onStart();
    helper.registerContainer(container);
  }

  @Override protected void onStop() {
    super.onStop();
    helper.registerContainer(null);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    Log.w(TAG, "onDestroy() called");
    container.setAdapter(null);
  }

  static class PlayerViewHolder extends BaseViewHolder implements Player {

    PlayerViewHolder(View itemView) {
      super(itemView);
    }

    @NonNull @Override public View getPlayerView() {
      return container;
    }

    @Override public boolean prepare() {
      indicator.setText("PREPARED");
      return true;
    }

    @Override public void release() {
      indicator.setText("RELEASED");
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
      return visible >= 0.75;
    }

    @Override public int getPlayOrder() {
      return getAdapterPosition();
    }

    @Override public String toString() {
      return String.format(Locale.getDefault(), "Player{%d}", getAdapterPosition()) + isPlaying();
    }
  }

  static class BaseViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.item_container) LinearLayout container;
    @BindView(R.id.text_content) TextView content;
    @BindView(R.id.text_indicator) TextView indicator;

    BaseViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  private static class SimpleAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    final int TYPE_BASE = 1;
    final int TYPE_PLAYER = 2;

    SimpleAdapter() {
    }

    @Override public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      if (viewType != TYPE_BASE && viewType != TYPE_PLAYER) {
        throw new IllegalArgumentException("Un-supported viewType: " + viewType);
      }

      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.vh_simple_player, parent, false);
      return viewType == TYPE_BASE ? new BaseViewHolder(view) : new PlayerViewHolder(view);
    }

    @Override public void onBindViewHolder(BaseViewHolder holder, int position) {
      holder.content.setText("POS: " + (position + 1));
      holder.indicator.setText("^＿^!");
    }

    @Override public int getItemViewType(int position) {
      return position % 4 == 0 || position % 4 == 3 ? TYPE_PLAYER : TYPE_BASE;
    }

    @Override public int getItemCount() {
      return Integer.MAX_VALUE;
    }
  }
}
