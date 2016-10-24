# Toro

<a href="http://www.methodscount.com/?lib=com.github.eneim%3AToro%3A1.2.0"><img src="https://img.shields.io/badge/Methods and size-core: 610 | deps: 17357 | 76 KB-e91e63.svg"></img></a>[![Join the chat at https://gitter.im/eneim/Toro](https://badges.gitter.im/eneim/Toro.svg)](https://gitter.im/eneim/Toro?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [ ![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Toro-green.svg?style=true)](https://android-arsenal.com/details/1/3106)

<a href="https://www.iconfinder.com/icons/1105270/brand_connect_shape_square_icon#size=512""><img src="https://github.com/eneim/Toro/blob/develop/art/web_hi_res_512.png" width="256"></a>



> ***Video list auto playback made simple, specially built for RecyclerView***

## 0. Version 2.1.0

[ ![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)

> [![Maven Central](https://maven-badges.herokuapp.com/maven-central/im.ene.lab/toro/badge.svg)](https://maven-badges.herokuapp.com/maven-central/im.ene.lab/toro)

- Currently next update is under development with many improvement.

- SNAPSHOT is available, the usage is as follow:

  - Add the following line to your ```build.gradle```
  
  ```groovy
  repositories {
    // Other repositories
    // ...
    // Snapshot repo
    maven {
      url "https://oss.sonatype.org/content/repositories/snapshots/"
    }
  }
  ```

  - Include SNAPSHOT to dependencies
  
  ```groovy
  // Choose the suitable one
  // 1. Only use Toro
  compile "im.ene.lab:toro:2.0.0-SNAPSHOT"
  // 2. Or use custom player widgets, backed by ExoPlayer
  compile "im.ene.lab:toro-player:2.0.0-SNAPSHOT"
  // 3. Or even want to try some customized extensions for ease
  compile "im.ene.lab:toro-ext:2.0.0-SNAPSHOT"
  ```
  
  For more usage, please refer to ```toro-sample``` code base. Documentation will be available at release.
  
## 1. Main features:

- Auto start/pause/resume video by scrolling your RecyclerView, support all Official built-in LayoutManagers. 

- Smart caching: Toro remembers last playback position and resume from where you left (*note: in Android default Media Player, depend on Video's format and codec, the resume timestamp may varies*). 

- Playback behavior decided by User, not by library:
  - Customizable playback Strategy to decide which is the best component to start playback. Optimized built-in Strategies to help you start.
  - UI-based logic, straight-forward approach: you see the Video, then it should play. **Default**: you see 75% of the Video then it should start playing. **Advance**: you decide how much the visible Video should trigger the playback. See [Wiki](https://github.com/eneim/Toro/wiki) for more details. 
  - Decision from both side: Toro's core and your implementation. Toro listens to your widget: does it want to play?, is it able to play (video is well-prepared or not), then Toro's strategy will decide if it allows your video to play or not. **Default**: built-in Strategy and widget do the rest. **Advance**: you have control to the both side: your custom ViewHolder and your custom Strategy. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.
  
- Built-in widgets: an abstract ViewHolder for original VideoView, an abstract ViewHolder for "TextureView version" of VideoView: [TextureVideoView](https://github.com/sprylab/texturevideoview), and an abstract ViewHolder for my customized version of TextureVideoView: ToroVideoView, with more flexible API and less *annoying* error processing.

- Powerful, flexible and highly customizable API. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Selective playback: find the best playable item, defined by smart, flexible [Strategies](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/ToroStrategy.java) to decide when and how a player should start playing. Toro comes with optimized built-in [Strategies](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/Toro.java#L465), but user could always create their own. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Smart long press: support Grid (with many Videos in one window) by built-in [Long press listener](https://github.com/eneim/Toro/blob/develop/toro/src/main/java/im/ene/lab/toro/RecyclerViewItemHelper.java#L73). Turning ON/OFF in one line of code. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.
 
- **I create lots of built-in components, but keep Toro highly customizable.** You are free to decide how you want to start your player, but if you don't know, just let Toro help you to decide.

**See [Wiki](https://github.com/eneim/Toro/wiki) for more details.**

## 2. Toro in Action

<img src="https://github.com/eneim/Toro/blob/master/art/sample_1.gif" width="180">
<img src="https://github.com/eneim/Toro/blob/master/art/sample_2.gif" width="180">
<img src="https://github.com/eneim/Toro/blob/master/art/sample_4.gif" width="180">
<img src="https://github.com/eneim/Toro/blob/master/art/sample_5.gif" width="180">

<img src="https://github.com/eneim/Toro/blob/master/art/sample_3.gif" width="360">

## 3. How to use

##### 0. Prerequirement

- Min support version: Android SDK level 15 (4.0.x)

##### 1. Add Toro to your project

- Add this to Project's top level ```build.gradle```

```groovy
allprojects {
	repositories {
		maven { url "https://jitpack.io" }
	}
}
```

- Add this to dependencies

```groovy
ext {
	toro_latest_version = '1.2.0'
}

dependencies {
	compile "com.github.eneim:Toro:${toro_latest_version}"
}
```

Latest version is always visible by jitpack badge: [![](https://jitpack.io/v/eneim/Toro.svg)](https://jitpack.io/#eneim/Toro)

##### **TL,DR**: **app** module from this library comes with several good practice of this library. Please take a look.

##### 2. Integrate **Toro** into your Application: see [Toro starting guide](https://github.com/eneim/Toro/wiki/1.-Toro-starting-guide)

##### 3. Register/Unregister a RecyclerView to get support from **Toro**: see [Register/Unregister RecyclerView to Toro](https://github.com/eneim/Toro/wiki/1.-Toro-starting-guide#registerunregister-recyclerview-to-toro)

##### 4. Create ViewHolder to use with **Toro**: by default, just simply extend one of ```ToroVideoViewHolder```, ```TextureVideoViewHolder``` or ```AbsVideoViewHolder```. See [ToroViewHolder](https://github.com/eneim/Toro/wiki/2.-ToroPlayer,-ToroAdapter,-ToroViewHolder:-Toro's-heart(s)#toroviewholder) for more information.

  - A sample ViewHolder's code (see Sample app for more):
  
```java
public class DeadlySimpleToroVideoViewHolder extends ToroVideoViewHolder {

  public DeadlySimpleToroVideoViewHolder(View itemView) {
    super(itemView);
  }

  @Override protected ToroVideoView findVideoView(View itemView) {
    return (ToroVideoView) itemView.findViewById(R.id.video);
  }

  @Nullable @Override public String getVideoId() {
    return "my awesome video's id and its order: " + getAdapterPosition();
  }

  @Override public void bind(@Nullable Object object) {
    if (object != null && object instanceof SimpleVideoObject) {
      mVideoView.setVideoPath(((SimpleVideoObject) object).video);
    }
  }
}
```

##### 5. Core concepts and components of **Toro**: see [Wiki](https://github.com/eneim/Toro/wiki)

### Contribute to Toro

- Issue report and PRs are welcome.

### License

> Copyright 2016 eneim@Eneim Labs, nam@ene.im

> Licensed under the Apache License, Version 2.0 (the "License"); 
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
 
>        http://www.apache.org/licenses/LICENSE-2.0
       
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
