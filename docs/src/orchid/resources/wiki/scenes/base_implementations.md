---
---

The samples in the previous article show how you can create 
{{ anchor('Scenes', 'Scene') }} with just the basic interfaces, but there is a 
lot of boilerplate setup taking place.  
Fortunately, there are some base implementations that take some of this
boilerplate out of your hands.

### {{ anchor('BasicScene') }}<sup>1</sup>

The {{ anchor('BasicScene') }} is a very simple abstract Scene class that
provides a handle to the currently attached view, and it automatically saves and 
restores the view hierarchy state between subsequent `attach` calls.
We can take our `attach`  / `detach` example from before and re-implement it 
using the BasicScene class:

{% highlight 'kotlin' %}
interface MyContainer: Container

class MyScene(
    private val locationProvider: LocationProvider
): BasicScene<MyContainer> {

    private val listener = { location: Location? ->
        currentView?.location = location
    }

    override fun onStart() {
        locationProvider.registerLocationUpdates(listener)
    }

    override fun onStop() {
        locationProvider.unregisterLocationUpdates(listener)
    }
}
{% endhighlight %}

We don't have to manually keep a reference to the view anymore, and we don't
have to worry about releasing the reference since that is done for us now.

### {{ anchor('BaseSavableScene') }}<sup>1</sup>

The {{ anchor('BaseSavableScene') }} class is an abstract class that handles the 
view hierarchy state saving for you, and implements {{ anchor('SavableScene') }}.
If we take the sample from Scene state restoration before and re-implement it
using the BaseSavableScene class, we get the following:

{% highlight 'kotlin' %}
interface MyContainer: RestorableContainer

class MyScene(
    private val userId: String,
    savedState: SceneState? = null
) : BaseSavableScene<MyContainer>(savedState) {

    override fun saveInstanceState(): SceneState {
        return super.saveInstanceState().also {
            it["user_id"] = userId
        }
    }

    companion object {

        fun create(savedState: SceneState) : MyScene {
            return MyScene(
                savedState["user_id"],
                savedState
            )
        }
    }
}
{% endhighlight %}

We now only have to deal with saving and restoring our `userId`, and let the
BaseSavableScene handle the rest.

### {{ anchor('RxScene') }}<sup>2</sup>

The {{ anchor('RxScene') }} abstract class extends the 
{{ anchor('BaseSavableScene') }} class and provides helper functions for working 
with Rx streams.

### {{ anchor('LifecycleScene') }}<sup>2</sup>

The {{ anchor('LifecycleScene') }} abstract class extends the 
{{ anchor('BaseSavableScene') }} class and implements the 
`androidx.lifecycle.LifecycleOwner` interface.

----

1: This class is available in the `ext-acorn` artifact.  
2: This class is available in the `ext-acorn-rx` artifact.  
3: This class is available in the `ext-acorn-android-lifecycle` artifact.  
