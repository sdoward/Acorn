---
---

Usually in a mobile application, the user can navigate from one screen to
another.
The {{ anchor('Navigator') }} interface provides a way to handle 
flow through an application.
Instead of {{ anchor('Scenes', 'Scene') }} determining the next destination in 
the application by themselves, the Navigator can listen to events a Scene 
publishes and determine 
the appropriate action to take.

Take for example a simple login screen, where the user enters their username
and password.
When the user presses the 'log in' button, the application verifies the
credentials and navigates to a different screen.
In Acorn, the `LoginScene` provides an `Events` interface with a method
`onLoggedIn(User)`. 
The Navigator provides an implementation of this interface to the Scene,
and navigates to the next Scene.

The Navigator in turn has the responsibility to let interested parties know that
the active Scene has changed.
This way the UI layer can react to a Scene change and show the proper UI.

### Lifecycle

Like Scenes, Navigators also have lifecycles.
Most often these are used to control the Scene lifecycle, but a Navigator
implementation can also choose to hook into this lifecycle itself.

The Navigator's lifecycle is  similar to that of Scenes: they can be 'started',
'stopped' and 'destroyed':

 - 'stopped ' : The Navigator is dormant, waiting to become active or to be
                destroyed. A change in its Scenes is not propagated to its 
                listeners.
 - 'started'  : The Navigator is currently active, and changes in scenery are
                propagated to the listeners.
 - 'destroyed': The Navigator is destroyed and will not become active anymore.
 
During the lifetime of a Navigator it can go from 'stopped' to 'started' and
vice versa multiple times, until it reaches the 'destroyed' state.

{% highlight 'kotlin' %}
interface Navigator {

    fun onStart()
    fun onStop()
    fun onDestroy()
}
{% endhighlight %}

### State modelling

Navigator implementations are free to choose how they implement their internal
state.
For example, it could use a structure like a stack to provide functionality
similar to a back stack, or it could use a state machine for the state
representation.

This freedom that the Navigator gets also means that it is free to choose how
the lifecycles of its Scenes behave, as long as it is according to the Scene
specification. The order of the Scene's callback methods must honored, and the
Navigator's lifecycle state must always outlive that of a Scene.
This means that a Navigator's Scenes may only be active when the Navigator is
active, and no Scenes may be active when the Navigator is inactive.
Finally, the Scenes must always be properly destroyed when the Navigator is
destroyed.

Other than that, the Navigator implementation is free to decide how its Scene's
lifecycle is structured, and often depends on the strategy that is chosen for
modelling the internal state.

A Navigator that uses a stack for its state for example will stop but not
destroy the currently active Scene when a new Scene is pushed on the Stack.
Scenes are only destroyed when they're popped off the stack or when the Navigator
is destroyed.
A Navigator that merely replaces Scenes without any 'back' behavior will
immediately stop and destroy the currently active Scene when a new Scene becomes
active.

### Scene propagation

The Navigator implementation is in control of determining which Scene is active,
and must propagate it to any listeners.
The Navigator interface declares a 
{{ anchor('Navigator.Events', 'Events') }} interface that contains 
callback methods to trigger interested parties.
The Navigator interface itself has a method to let these interested parties
register themselves:


{% highlight 'kotlin' %}
interface Navigator {

    fun addNavigatorEventsListener(listener: Navigator.Events) : DisposableHandle

    /* ... */

    interface Events {

        fun scene(scene: Scene<out Container>, data: TransitionData? = null)

        fun finished()
    }
}
{% endhighlight %}

If appropriate, the Navigator can invoke the `Navigator.Events.scene` method
when the active Scene changes.

### Reacting to Scene events

As mentioned in {{ anchor('About Scenes', 'pageId=about_scenes') }}, Scenes may accept callback interfaces to push
events to the Navigator.
The Navigator implementation can use these callbacks to make an internal state
change.
For example, assuming there is a base StackNavigator class, we can do the
following:

{% highlight 'kotlin' %}
class MyNavigator : StackNavigator() {

    override fun initialStack() = listOf(MyScene(MySceneListener()))

    private inner class MySceneListener: MyScene.Events {

        override fun onEvent() {
            push(MyScene(this))
        }
    }
}
{% endhighlight %}

### Back presses

When the user presses the back button, this can ultimately be viewed as an
event, much like regular button presses.
Navigators can choose to implement the 
{{ anchor('OnBackPressListener') }} interface to indicate 
they're interesting in handling these back presses.
Since the Activity is the entry point for back presses, it should delegate
this request first to the Navigator if possible.
The Navigator can use this event to make a transition in its internal state.


### Navigator results

A Navigator implementation can also provide a callback interface to publish
results.
This is useful for example when creating a login flow: the user can be guided
through several Scenes, after which the Navigator finishes with an auth token
result.

There are two ways to implement callbacks for Navigator results.
The first is similar to the way this is implemented for Scenes, by passing a
callback to the Navigator constructor:

{% highlight 'kotlin' %}
class MyNavigator(
    private val listener: Events
) : Navigator, MyScene.Events {

    /* ... */

    override fun onAuthToken(authToken: String) {
        listener.onResult(authToken)
    }

    interface Events {

        onResult(authToken: String)
    }
}
{% endhighlight %}

There are cases however where the Activity is interested in the Navigator's
result, to be able to call `Activity.setResult` and finish.
Since the Navigator should outlive the Activity, the Activity must be able to
register itself as a listener to the Navigator.
This can be done by keeping a list of interested listeners:

{% highlight 'kotlin' %}
class MyNavigator : Navigator, MyScene.Events {

    private var listeners = listOf<Events>()

    fun register(listener: Events) {
        listeners += listener
    }

    fun remove(listener: Events) {
        listeners -= listener
    }

    /* ... */

    override fun onAuthToken(authToken: String) {
        listeners.forEach { it.onResult(authToken) }
    }

    interface Events {

        fun onResult(authToken: String)
    }
}
{% endhighlight %}

### Saving and restoring state

Just like Scenes, Navigator instances need to be able to have their state saved
as well, and must be able to be restored from this saved state.
Navigators that save their state must also save the states of the Scenes they
are hosting.
This means that the Navigator instance is responsible for the restoration of the
Scenes as well.

Depending on the chosen strategy for modelling the internal navigation state,
the Navigator must be able to restore one or more of its Scenes and restore its
internal state as well.

### Navigator composition

The power behind the Navigator interface is that instances can be composed
together.
An application may for example have several flows that make up the entire
application flow.
These flows can all be implemented using their own Navigator implementations,
and then tied together using a composing Navigator.

These composing Navigators can decide their internal state just as the 'normal'
Navigators, which means that you can create a Navigator implementation that can
push and pop other Navigators on and off a stack.

## Advanced topics

Usually you don't need to implement the Navigator interface directly; you can
use the existing base classes to compose the behavior you need.
However if you do choose to implement your own Navigator, you might want to have
a look at {{ anchor('Scene Management', 'pageId=scene_management') }}.

