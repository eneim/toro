# Release note

### 1.2.0 (DRAFT, 2016.03.07)

Breaking changes. Client should take care of this release.

- Add customizable ```Toro$Config``` class (with currently support for ***loop playback*** only).
- Better error handling, see [this PR](https://github.com/eneim/Toro/pull/26). ToroPlayer#onPlaybackError(...) will now require a boolean response.
- Support ***loop playback***. Current player would automatically repeat **immediately** after it completes. Loop playback is disable by default. Setup via ```ToroPlayer#isLoopAble()```. A Player which is eager to loop must return true from there. Usage:

```java
public class MyToroPlayerViewHolder extends ToroViewHolder {

  // Do stuff
  
  // Support loop on this ViewHolder
  @Override public boolean isLoopAble() {
    return true;
    // You may want to control it better
    // return getAdapterPosition() % 2 == 0;
  }
}
```
- Ability to temporary pause all playing players by new **ToroStrategy**: ***REST***. This Strategy will disable all player. This is useful in case Client would like to open a Dialog (Eg: Bigger screen playback) on top of current screen, then it wants to pause the player until that Dialog dismisses. Usage (see sample App for more information):

```java
Toro.rest(true);  // Tell Toro you want to take a rest
// Do stuff which requires the player to be silent.
Toro.rest(false); // Done stuff, resume the playback.
```
Please note that using this feature should be careful. It's really simple implementation, for really simple usecase. So please don't screw up your App.

- Custom LayoutManager: Client can now add their own LayoutManager. The requirement is that LayoutManager must implement **ToroLayoutManager** interface to help Toro to travel throw the Children. Sample is coming soon.

- Custom ViewHolder with RecyclerViewItemHelper: RecyclerViewItemHelper is now public and non-final. This allows client to create their own ViewHolder, but still get support from Toro's core methods. Just use/extend RecyclerViewItemHelper wisely. See **CustomViewHolder.java** for the first step.

- Better UI for sample App.

### 1.1.0 (2016.02.09)

**Happy Vietnam New Lunar Year!**

This release contains some changes from internal API as well as code improvement:

- Narrow down visibility of some classes and object. Clients should test carefully for side-affect. So sorry for this inconvenient. Normally you would not be affect, unless you have some custom components.
  - VideoPlayerManagerImpl is now package private. Don't use it else where. Instead, implement to your Adapter.
  - ToroScrollListener is now package private. Don't use it else where. 

- Built-in strategies is strengthen by more accurate decision on ```#allowsToPlay()``` method.

- Some other code improvement.
- 
### 1.0.0 (2016.02.06)

- First stable release
