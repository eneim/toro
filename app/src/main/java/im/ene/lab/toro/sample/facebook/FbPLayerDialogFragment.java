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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.widget.LargeDialogFragment;

/**
 * Created by eneim on 5/13/16.
 */
public class FbPLayerDialogFragment extends LargeDialogFragment {

  public static final String TAG = "FbPLayer";

  public static FbPLayerDialogFragment newInstance() {
    return new FbPLayerDialogFragment();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dialog_player, container, false);
  }

  @Override public void show(FragmentManager manager, String tag) {
    super.show(manager, tag);
    Toro.rest(true);
  }

  @Override public void onDismiss(DialogInterface dialog) {
    Toro.rest(false);
    super.onDismiss(dialog);
  }
}
