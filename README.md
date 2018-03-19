# Toro 

> ***Video list auto playback made simple, specially built for RecyclerView***

<img src="/extra/web_hi_res_512.png" width="256">

<a href='https://ko-fi.com/A342OWW' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

 [ ![Download](https://api.bintray.com/packages/eneimlabs/Toro/toro/images/download.svg) ](https://bintray.com/eneimlabs/Toro/toro/_latestVersion)[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Toro-green.svg?style=true)](https://android-arsenal.com/details/1/3106)
[![Join the chat at https://gitter.im/eneim/Toro](https://badges.gitter.im/eneim/Toro.svg)](https://gitter.im/eneim/Toro?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 

### Menu

* [Features](#features)
* [Demo](#demo-youtube-video)
* [Getting start, basic implementation](#getting-start-basic-implementation)
* [Advance topics](#advance-topics)
* [Contribution](#contribution)
* [Donation](#donation)
* [Hall of Fame](#hall-of-fame)
* [License](#license)

### Features

Core:
  - Auto start/pause Media playback on user interaction: scroll, open/close App.
  - Optional playback position save/restore (default: disabled).
    - If enabled, Toro will also save/restore them on Configuration change (orientation change, multi-window mode ...).
  - Customizable playback component: either MediaPlayer or ExoPlayer will work. Toro comes with default helper classes to support these 2.
  - Customizable player selector: custom the selection of the player to start, among many other players.
    - Which in turn support single/multiple players.

Plus alpha:
  - First class support for ExoPlayer 2. 

### Demo (Youtube Video)

[![](https://img.youtube.com/vi/gw0awL_89V4/0.jpg)](https://www.youtube.com/watch?v=gw0awL_89V4)

### Getting start, basic implementation

1. Update module build.gradle.

Latest version:
 [ ![Download](https://api.bintray.com/packages/eneimlabs/Toro/toro/images/download.svg) ](https://bintray.com/eneimlabs/Toro/toro/_latestVersion)
 
```groovy
ext {
  latest_release = '3.4.1' // TODO check above for latest version
  // below: other dependencies' versions maybe
}

dependencies {
   implementation "im.ene.toro3:toro:${latest_release}"
   implementation "im.ene.toro3:toro-ext-exoplayer:${latest_release}"  // to get ExoPlayer support
}
```

Using snapshot:

Update this to root's ``build.gradle``

```gradle
allprojects {
  repositories {
    google()
    jcenter()
    // Add url below to use snaphot
    maven { url 'https://oss.jfrog.org/artifactory/oss-snapshot-local' }
  }
  
  // TODO anything else
}
```

Application's build.gradle

```groovy
ext {
  latest_snapshot = '3.4.2-SNAPSHOT' // TODO check above for latest version
  // below: other dependencies' versions maybe
}

dependencies {
   implementation "im.ene.toro3:toro:${latest_snapshot}"
   implementation "im.ene.toro3:toro-ext-exoplayer:${latest_snapshot}"  // to get ExoPlayer support
}
```

2. Using ```Container``` in place of Video list/RecyclerView. 

```xml
<im.ene.toro.widget.Container
  android:id="@+id/my_fancy_videos"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
/>
```

3. Implement ```ToroPlayer``` to ViewHolder that should be a Video player.

```java
public class SimpleExoPlayerViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  static final int LAYOUT_RES = R.layout.vh_exoplayer_basic;

  @Nullable ExoPlayerViewHelper helper;
  @Nullable private Uri mediaUri;

  @BindView(R.id.player) PlayerView playerView;

  SimpleExoPlayerViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  // called from Adapter to setup the media
  void bind(@NonNull RecyclerView.Adapter adapter, Uri item, List<Object> payloads) {
    if (item != null) {
      mediaUri = item;
    }
  }

  @NonNull @Override public View getPlayerView() {
    return playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
  }

  @Override
  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
    if (helper == null) {
      helper = new ExoPlayerViewHelper(this, mediaUri);
    }
    helper.initialize(container, playbackInfo);
  }

  @Override public void release() {
    if (helper != null) {
      helper.release();
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
    return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.85;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }
}
```

More advanced View holder implementations can be found in **app**, **demo-??** module.

4. Setup Adapter to use the ViewHolder above, and setup Container to use that Adapter.

That's all. Your View should be ready to play.

### Advance topics

1. Enable playback position save/restore

By default, **toro**'s Container will always start a playback from beginning.
 
The implementation is simple: create a class implementing ```CacheManager```, then set it to the Container using ```Container#setCacheManager(CacheManager)```. Sample code can be found in [TimelineAdapter.java](/app/src/main/java/im/ene/toro/sample/features/facebook/timeline/TimelineAdapter.java). Note that here I implement the interface right into the Adapter for convenience. It can be done without Adapter. There is one thing worth noticing: a matching between **playback order** with its cached **playback info** should be unique.

2. Multiple simultaneous playback

***Playing multiple Videos at once is considered bad practice***. It is a heavy power consuming task and also unfriendly to hardware. In fact, each device has its own limitation of multiple decoding ability, so developer must be aware of what you are doing. I don't officially support this behaviour. Developer should own the video source to optimize the video for this purpose.

To be able to have more than one Video play at the same time, developer must use a custom ```PlayerSelector```. This can also provide a powerful control to number of playback in a dynamic way.

Below is an example using GridLayoutManager and custom PlayerSelector to have a fun multiple playback.

```java
layoutManager = new GridLayoutManager(getContext(), 2);
layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
  @Override public int getSpanSize(int position) {
    return position % 3 == 2 ? 2 : 1;
  }
});

// A custom Selector to work with Grid span: even row will has 2 Videos while odd row has one Video.
// This selector will select all videos available for each row, which will make the number of Players varies.
PlayerSelector selector = new PlayerSelector() {
  @NonNull @Override public Collection<ToroPlayer> select(@NonNull View container,
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
```

Behaviour: 

![](/extra/demo-player-selector.gif)

3. Enable/Disable the auto-play on demand

To disable the autoplay, simply use the ```PlayerSelector.NONE``` for the Container. To enable it again, just use a Selector that actually select the player. There is ```PlayerSelector.DEFAULT``` built-in.

4. Start playback with delay

It is expected that: when user scrolls so that the Player view is in playable state (maybe because it is fully visual), there should be a small delay before the Player starts playing. Toro supports this out of the box using ``PlayerDispatcher`` and via ``Container#setPlayerDispatcher()``. Further more, the delay ca be flexibly configured by per-Player. The snippet below shows how to use ``PlayerDispatcher``:
  
``java
// ToroPlayer whose order is divisible by 3 will be delayed by 250 ms, others will be delayed by 1000 ms (1 second). 
container.setPlayerDispatcher(player -> player.getPlayerOrder() % 3 == 0 ? 250 : 1000);
``

5. Works with ``CoordinatorLayout``

When using a ``Container`` in the following View hierarchy

```xml
<CoordinatorLayout>
  <AppBarLayout app:layout_behavior:AppBarLayout.Behavior.class>
    <CollapsingToolbarLayout>
    </CollapsingToolbarLayout>
  </AppBarLayout>
  <Container app:layout_behavior:ScrollingViewBehavior.class></Container>
</CoordinatorLayout>
```

In the layout above, when User 'scroll' the UI by flinging the CollapsingToolbarLayout (not the Container), neither CoordinatorLayout will not tell Container about the 'scroll', nor Container will trigger a call to ``Container#onScrollStateChanged(int)``. But in practice, an interaction like this will make the Player be visible, and User expects a playback to starts, which may not without some update in your codebase.

To support this use case, **Toro** adds a delegation ``Container.Behavior`` that can be used manually to catch the behavior like above.

The usage looks like below:
 
```java
// Add following snippet in Activity#onCreate or Fragment#onViewCreated
// Only when you use Container inside a CoordinatorLayout and depends on Behavior.
// 1. Request Container's LayoutParams
ViewGroup.LayoutParams params = container.getLayoutParams();
// 2. Only continue if it is of type CoordinatorLayout.LayoutParams
if (params != null && params instanceof CoordinatorLayout.LayoutParams) {
  // 3. Check if there is an already set CoordinatorLayout.Behavior. If not, just ignore everything. 
  CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
  if (behavior != null) {
    ((LayoutParams) params).setBehavior(new Container.Behavior(behavior,
        // 4. Container.Behavior requires a Container.BehaviorCallback to ask client what to do next. 
        new Container.BehaviorCallback() {
          @Override public void onFinishInteraction() {
            container.onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE);
          }
        }));
  }
}
```

Below is the behavior before and after we apply the code above:

|Before|After|
|---|---|
|![](/extra/behaviour_before.gif)|![](/extra/behaviour_after.gif)|

### Contribution

- **IMPORTANT:** Forkers of Toro should also rename file ```gradle.properties-sample``` to ```gradle.properties``` and update it before any build.

- Issue report and Pull Requests are welcome. Please follow issue format for quick response.

- For Pull Requests, this project uses 2-space indent and **no** Hungarian naming convention.

### Donation

- You can always **buy me some coffee** for shorter update cycle ...

<a href='https://ko-fi.com/A342OWW' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

### Hall of Fame

> Email to nam@ene.im with the description of your App using Toro to list it here.

### License

> Copyright 2017 eneim@Eneim Labs, nam@ene.im

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at

>        http://www.apache.org/licenses/LICENSE-2.0

> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
