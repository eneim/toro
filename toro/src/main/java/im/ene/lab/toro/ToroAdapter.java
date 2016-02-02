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

package im.ene.lab.toro;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by eneim on 1/30/16.
 */
public abstract class ToroAdapter<VH extends ToroAdapter.ViewHolder>
    extends RecyclerView.Adapter<VH> {

  protected OnItemClickListener mOnItemClickListener;

  private final String TAG = getClass().getSimpleName();

  /**
   * @param listener
   */
  public void setOnItemClickListener(OnItemClickListener listener) {
    this.mOnItemClickListener = listener;
  }

  // Unusable
  @Override public void onViewAttachedToWindow(VH holder) {
    Log.d(TAG, "onViewAttachedToWindow() called with: " + "holder = [" + holder + "]");
    holder.onAttachedToParent();
  }

  // Unusable
  @Override public void onViewDetachedFromWindow(VH holder) {
    Log.d(TAG, "onViewDetachedFromWindow() called with: " + "holder = [" + holder + "]");
    holder.onDetachedFromParent();
  }

  // Unusable
  @Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    Log.i(TAG, "onAttachedToRecyclerView: ");
  }

  // Unusable
  @Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    super.onDetachedFromRecyclerView(recyclerView);
    Log.i(TAG, "onDetachedFromRecyclerView: ");
  }

  /**
   *
   */
  public abstract static class ViewHolder extends RecyclerView.ViewHolder {

    private final String TAG = getClass().getSimpleName();

    public ViewHolder(View itemView) {
      super(itemView);
    }

    /**
     * @param listener
     */
    public void setOnItemViewClickListener(View.OnClickListener listener) {
      itemView.setOnClickListener(listener);
    }

    public void setOnItemViewLongClickListener(View.OnLongClickListener listener) {
      itemView.setOnLongClickListener(listener);
    }

    /**
     * see {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
     */
    public void onAttachedToParent() {
      Log.i(TAG, "onAttachedToParent: " + getAdapterPosition());
    }

    /**
     * see {@link RecyclerView.Adapter#onDetachedFromRecyclerView(RecyclerView)}
     */
    public void onDetachedFromParent() {
      Log.i(TAG, "onDetachedFromParent: " + getAdapterPosition());
    }

    public abstract void bind(Object object);
  }
}
