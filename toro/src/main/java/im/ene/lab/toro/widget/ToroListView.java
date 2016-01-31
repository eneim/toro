package im.ene.lab.toro.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.AbsListView;

import im.ene.lab.toro.ListViewScrollListener;

import java.util.ArrayList;

/**
 * Created by eneim on 1/31/16.
 * <p/>
 * A ListView, with fixed OnScrollListener
 */
public abstract class ToroListView extends AbsListView {

  public ToroListView(Context context) {
    super(context);
  }

  public ToroListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ToroListView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ToroListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  private ArrayList<OnScrollListener> mListeners;
  private OnScrollListener mLegacyOnScrollListener;

  // Don't use this
  @Deprecated
  @Override public void setOnScrollListener(final OnScrollListener l) {
    if (l == null) {
      return;
    }
    mLegacyOnScrollListener = l;
    // prevent NPE
    if (mListeners == null) {
      mListeners = new ArrayList<>();
    }

    super.setOnScrollListener(new OnScrollListener() {
      @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
        l.onScrollStateChanged(view, scrollState);
        for (OnScrollListener listener : mListeners) {
          if (listener instanceof ListViewScrollListener) {
            listener.onScrollStateChanged(view, scrollState);
          }
        }
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                           int totalItemCount) {
        l.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        for (OnScrollListener listener : mListeners) {
          if (listener instanceof ListViewScrollListener) {
            listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
          }
        }
      }
    });
  }

  public void addOnScrollListener(final OnScrollListener listener) {
    if (mListeners == null) {
      mListeners = new ArrayList<>();
    }
    mListeners.add(listener);

    super.setOnScrollListener(new OnScrollListener() {
      @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
        for (OnScrollListener item : mListeners) {
          item.onScrollStateChanged(view, scrollState);
        }
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                           int totalItemCount) {
        for (OnScrollListener item : mListeners) {
          item.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
      }
    });
  }

  public void removeOnScrollListener(OnScrollListener listener) {
    if (mLegacyOnScrollListener == listener) {
      mLegacyOnScrollListener = null;
    }

    if (mListeners == null) {
      return;
    }
    mListeners.remove(listener);
    // Replace old listeners
    super.setOnScrollListener(new OnScrollListener() {
      @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
        for (OnScrollListener item : mListeners) {
          item.onScrollStateChanged(view, scrollState);
        }
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                           int totalItemCount) {
        for (OnScrollListener item : mListeners) {
          item.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
      }
    });
  }
}
