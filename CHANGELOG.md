Changelog
===========

3.0.0 (release date TBD)
--------------

- Detail TBD

3.0.0 alpha 2 (2017/06/30)
-------------

#### Changes

- Drop support to ```VideoView``` (via ```ToroVideoView``` and ```LegacyVideoViewHelper```). User can find a demo of using VideoView in demo app, **legacy** package.

- Move ```PlayerSelector``` from ```im.ene.toro.widget``` to ```im.ene.toro```.

- Method name change: ```ToroPlayerHelper#updatePlaybackInfo``` becomes ```ToroPlayerHelper#getLatestPlaybackInfo```.

- All initialize method now requires a Nullable PlaybackInfo (was Nonnull).

#### Update

- Add demo for ```VideoView```.

#### Others

- Minor coding improvement and documentation improvement.

---

3.0.0 alpha 1 (2017/06/24)
-------------

#### Toro 3.0 is a completely rewritten version of Toro. I borrow just a few helper from 2.x.

To integrate Toro in your App, just replace the ```RecyclerView``` to ```Container``` in place of Video list.

```xml
<im.ene.toro.widget.Container
  android:id="@+id/recycler_view"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  app:max_player_number="1"
/>
```

Next: implement ```ToroPlayer``` to the ```ViewHolder``` that will be a Media player.

```java
public class SimpleExoPlayerViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  static final int LAYOUT_RES = R.layout.vh_exoplayer_basic;

  @Nullable SimpleExoPlayerViewHelper helper;
  @Nullable private Uri mediaUri;

  @BindView(R.id.player) SimpleExoPlayerView playerView;

  SimpleExoPlayerViewHolder(View itemView) {
    super(itemView);
  }

  @Override
  public void bind(@NonNull RecyclerView.Adapter adapter, Uri item, List<Object> payloads) {
    if (item != null) {
      mediaUri = item;
    }
  }

  @NonNull @Override public View getPlayerView() {
    return playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    PlaybackInfo state = new PlaybackInfo();
    if (helper != null) state = helper.getLatestPlaybackInfo();
    return state;
  }

  @Override
  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
    if (helper == null) {
      helper = new SimpleExoPlayerViewHelper(container, this, mediaUri);
      helper.setEventListener(eventListener);
    }
    helper.initialize(playbackInfo);
  }

  @Override public void release() {
    if (helper != null) {
      try {
        helper.cancel();
      } catch (Exception e) {
        e.printStackTrace();
      }
      helper = null;
    }
  }

  @Override public void play() {
    if (helper != null) helper.play();
  }

  @Override public void pause() {
    if (helper != null) helper.pause();
  }

  @Override public boolean isPlaying() {
    return helper != null && helper.isPlaying();
  }

  @Override public boolean wantsToPlay() {
    ViewParent parent = itemView.getParent();
    float offset = 0;
    if (parent != null && parent instanceof View) {
      offset = ToroUtil.visibleAreaOffset(playerView, (View) parent);
    }
    return offset >= 0.85;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }
}
```

You are ready to have Toro 3.0 support. More advance usage are being updated.