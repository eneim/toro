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
* [Advance usage and class documentation](#advance-usage-and-class-documentation)
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

### Advance usage and class documentation

> Website goes here.

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
