# Toro

<img src="https://github.com/eneim/Toro/blob/master/art/web_hi_res_512.png" width="256">

[![](https://jitpack.io/v/eneim/Toro.svg)](https://jitpack.io/#eneim/Toro)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Toro-green.svg?style=true)](https://android-arsenal.com/details/1/3106)



> ***Auto playback on Video list made easy, special built for RecyclerView***

## 1. Introduction and motivation

- Add ***Auto playback on scrolling*** features like Facebook App or Twitter App into your App by **Toro**:
  - Easy to use. Target Android SDK from 16 up (I hate the rest, sorry).
  - Adding support layers on tops of RecyclerView's **LayoutManager/Adapter/ViewHolder**.
  - **Transparent code with original RecyclerView**. Provide useful methods set on top of them. See [ToroAdapter](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/ToroAdapter.java), [ToroAdapter$ViewHolder](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/ToroAdapter.java#L59).
  - **OUT-OF-THE-BOX** usability: just extends my pre-coded classes, I have prepared the rest for them. See [ToroVideoViewHolder and its relatives](https://github.com/eneim/Toro/wiki/2.-ToroPlayer,-ToroAdapter,-ToroViewHolder:-Toro's-heart(s))
  - Highly customizeable: use can custom almost the important logic parts, **Toro** listens to your changes.
  - Video Player lifecycle: Toro listen to Activity's life cycle to init/release its resource. Further more, Toro creates and supports **Playback Lifecycle** which is super helpful for those who want to catch up with the playback progress as well as update UI before/after playing. All of those features are usable out of the box. See [Video Player lifecycle](https://github.com/eneim/Toro/wiki/2.1-Video-Player-Lifecycle).
  - Super easy to setup: see usage section.

- There are some other [working ideas](https://github.com/danylovolokh/VideoPlayerManager), but those libraries have different approaches, which are in **my oppinion**, not as good as I expected: User of those libraries need to re-write a lot of code to match their implementation. I try to make our components as close to official **RecyclerView/Adapter/ViewHolder/LayoutManager** as possible, and just provide some useful methods on top of them. 

- I drop supporting for ListView/GridView since those components are obsolete and hard to maintaince. But I made a on-going branch which provide some code base with the same thinking with the rest of this library. So any one can take a look and create their own helpers.

## 2. Main features:

- Auto start/pause/resume by scrolling your RecyclerView, support all Layout managers. Last-minute caching: Toro remembers last playback position and resume from where you left (**note that: in Android default Media Player, depend on Video's format and codec, the resume timestamp may varies**). 

- Playback behavior decided by User, not by library:
  - Customizable Strategy to decide which is the best component to start playback. Optimized built-in Strategies to help you start.
  - UI-based logic, straight-forward approach: you see the Video, then it should play. **Default**: you see 75% of the Video then it should start playing. **Advance**: you decide how much the visible Video should trigger the playback.
  - Decision from both side: Toro's core and your components. Toro listen to your components: does it want to play, is it able to play (well-prepared or had error), then Toro's strategy will decide if it allows your component to play or not. **Default**: built-in Strategy and components do the rest. **Advance**: you have control to the both side.
  
- Built-in widgets: an abstract ViewHolder for original VideoView, an abstract ViewHolder for "TextureView version" of VideoView: [TextureVideoView](https://github.com/sprylab/texturevideoview), and an abstract ViewHolder for my customized version of TextureVideoView: ToroVideoView, with more flexible API and less *annoying* error processing.

- Powerful, flexible and highly customizable API. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Selective playback: find the best playable item, defined by smart, flexible [Strategies](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/ToroStrategy.java) to decide when and how a player should start playing. Toro comes with optimized built-in [Strategies](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/Toro.java#L516), but user could always create their own. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Smartly support Grid (with many Video in one window) by built-in [Long press listener](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/Toro.java#L108). Turning ON/OFF in one line of code. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.
 
- **I create lots of built-in code, but keep Toro highly customizable.** You are free to decide how you want to start your player, but if you don't know, just let Toro help you to decide.

**See [Wiki](https://github.com/eneim/Toro/wiki) for more details.**

## 3. Toro in Action

<img src="https://github.com/eneim/Toro/blob/master/art/sample_1.gif" width="180">
<img src="https://github.com/eneim/Toro/blob/master/art/sample_2.gif" width="180">
<img src="https://github.com/eneim/Toro/blob/master/art/sample_4.gif" width="180">
<img src="https://github.com/eneim/Toro/blob/master/art/sample_5.gif" width="180">

<img src="https://github.com/eneim/Toro/blob/master/art/sample_3.gif" width="360">

## 4. How to use

##### 0. Prerequirement

- From Android SDK level 16 (4.1)

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
dependencies {
	compile 'com.github.eneim:Toro:1.1.0'
}
```

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

- Composing... please wait.

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
