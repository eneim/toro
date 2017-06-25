# Toro 

> ***Video list auto playback made simple, specially built for RecyclerView***

<img src="/extra/web_hi_res_512.png" width="256">

<a href='https://ko-fi.com/A342OWW' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Toro-green.svg?style=true)](https://android-arsenal.com/details/1/3106)
[![Join the chat at https://gitter.im/eneim/Toro](https://badges.gitter.im/eneim/Toro.svg)](https://gitter.im/eneim/Toro?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 

### Menu

* [Features](#features)
* [Demo](#demo)
* [Getting start](#getting-start)
* [Advance usage and class documentation](#advance-usage-and-class-documentation-i-need-your-request-to-update-this-list)
* [Contribution & Donation](#contribution--donation)
* [Hall of Fame](#hall-of-fame)
* [License](#license)

### Features

- Auto start/pause Media playback on Attach/Detach/Scroll/... events.
- Optional playback position save/restore (no position save/restore by default).
  - If enabled, Toro will also save/restore them on Configuration change (orientation change, multi-window mode ...).
- Customizable playback component: either MediaPlayer or ExoPlayer will work. Toro comes with default helper classes to support these 2.
- Customizable player selector: custom the selection of the player to start, among many other players.
  - Which in turn support single/multiple players.
- First class Support ExoPlayer 2 and MediaPlayer (by Helper classes). 

### Demo (Youtube Video)

[![](https://img.youtube.com/vi/rSAGaNM2_t8/0.jpg)](https://www.youtube.com/watch?v=rSAGaNM2_t8)

### Getting start

1. Update module build.gradle.

```groovy
ext {
  toroVersion = '3.0.0-alpha1'
  // below: other dependencies' versions maybe
}

dependencies {
   compile "im.ene.toro3:toro:${toroVersion}" // deprecated in Android Studio 3.0
   
   // TODO: uncomment if using Android Studio 3.+ only
   // implementation "im.ene.toro3:toro:${toroVersion}"
}
```

2. Using ```Container``` in place of Video list. 

Below: a simple Container with default max simultaneous players count to 1.

```xml
<im.ene.toro.widget.Container
  android:id="@+id/recycler_view"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  app:max_player_number="1"
/>
```

3. Implement ```ToroPlayer``` to ViewHolder that should be a Video player.

```kotlin
// Better naming after import
import android.view.LayoutInflater.from as inflater

class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ToroPlayer {

  companion object {
    internal val LAYOUT_RES = R.layout.vh_skeleton_exoplayer

    // Static Factory method for Adapter to create this ViewHolder
    fun createNew(parent: ViewGroup) = PlayerViewHolder(inflater(parent.context).inflate(
        LAYOUT_RES, parent, false))
  }

  internal var playerView = itemView.findViewById(R.id.player)
  internal var playerViewHelper: SimpleExoPlayerViewHelper? = null
  internal lateinit var mediaUri: Uri

  // Called by Adapter to pass a valid media uri here.
  fun bind(uri: Uri) {
    this.mediaUri = uri
  }

  override fun getPlayerView() = playerView!!

  override fun getCurrentPlaybackInfo(): PlaybackInfo {
    return playerViewHelper?.updatePlaybackInfo() ?: PlaybackInfo()
  }

  override fun initialize(container: Container, playbackInfo: PlaybackInfo) {
    if (playerViewHelper == null) {
      playerViewHelper = SimpleExoPlayerViewHelper(container, this, mediaUri)
    }
    playerViewHelper!!.initialize(playbackInfo)
  }

  override fun play() {
    playerViewHelper?.play()
  }

  override fun pause() {
    playerViewHelper?.pause()
  }

  override fun isPlaying() = playerViewHelper != null && playerViewHelper!!.isPlaying

  override fun release() {
    try {
      playerViewHelper?.cancel()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    playerViewHelper = null
  }

  override fun wantsToPlay(): Boolean {
    val parent = itemView.parent
    var offset = 0f
    if (parent is View) {
      offset = ToroUtil.visibleAreaOffset(playerView, parent)
    }
    return offset >= 0.85
  }

  override fun getPlayerOrder() = adapterPosition
}
```

More advanced View holder implementations as well as Java version can be found in **app** module.

4. Setup Adapter to use the ViewHolder above, and setup Container to use that Adapter.

That's all. Your View should be ready to play.

### Advance usage and class documentation (I need your request to update this list)

1. Enable playback position save/restore: using ```PlayerStateManager```

The implementation is simple: create a class implementing ```PlayerStateManager```, then set it to the Container using ```Container#setPlayerStateManager(PlayerStateManager)```. Sample code can be found in [TimelineAdapter.java](/app/src/main/java/im/ene/toro/sample/features/facebook/timeline/TimelineAdapter.java). Note that here I implement the interface right into the Adapter for convenience. It can be done without Adapter. There is one thing worth noticing: a matching between **playback order** with its cached **playback info** should be unique.

Below is an example using TreeMap to cache playback state (copied from the file above)

```java
// Implement the PlayerStateManager;

private final Map<FbItem, PlaybackInfo> stateCache =
    new TreeMap<>((o1, o2) -> DemoUtil.compare(o1.getIndex(), o2.getIndex()));

@Override public void savePlaybackInfo(int order, @NonNull PlaybackInfo playbackInfo) {
  if (order >= 0) stateCache.put(getItem(order), playbackInfo);
}

@NonNull @Override public PlaybackInfo getPlaybackInfo(int order) {
  FbItem entity = order >= 0 ? getItem(order) : null;
  PlaybackInfo state = new PlaybackInfo();
  if (entity != null) {
    state = stateCache.get(entity);
    if (state == null) {
      state = new PlaybackInfo();
      stateCache.put(entity, state);
    }
  }
  return state;
}

// TODO return null if client doesn't want to save playback states on config change.
@Nullable @Override public Collection<Integer> getSavedPlayerOrders() {
  return Observable.fromIterable(stateCache.keySet()).map(items::indexOf).toList().blockingGet();
}
```

2. Multiple simultaneous playback

*Playing multiple Videos at once is considered bad practice*. It is a heavy power consuming task and also unfriendly to hardware. In fact, each device has its own limitation of multiple decoding ability, so developer must be aware of what you are doing. I don't officially support this behaviour. Developer should own the video source to optimize the video for this purpose.

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

3. Enable/Disable the autoplay on demand.

To disable the autoplay, simply use the ```PlayerSelector.NONE``` for the Container. To enable it again, just use a Selector that actually select the player. There is ```PlayerSelector.DEFAULT``` built-in.

4. Save/Restore playback info on config change.

By default, Container cannot save/restore the playback info on config change. To support this, it requires a ```PlayerStateManager``` whose ```getSavedPlayerOrders()``` returns a non-null collection of Integers. The example above also demonstrate this implementation.

### Contribution & Donation

- Issue report and Pull Requests are welcome. Please follow issue format for quick response.

- For Pull Requests, this project uses 2-space indent and **no** Hungarian naming convention.

- Also you can **buy me some coffee** for shorter update cycle ...

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
