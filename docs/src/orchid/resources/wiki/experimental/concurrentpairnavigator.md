---
extraCss:
    - |
        inline:.scss:
        .image-preview {
            text-align: center; 
            img {
                max-width:480px;
            }    
        }
---

The {{ anchor('ConcurrentPairNavigator') }} is a stacking
{{ anchor('Navigator') }} that allows up to two 
{{ anchor('Scenes','Scene') }} in its stack.
Whenever a second Scene is stacked upon the initial Scene, both the initial
Scene and the second Scene will _simultaneously_ be in their 'started' states,
hereby differing from navigators such as the {{ anchor('StackNavigator') }} 
which only allow a single Scene in the 'started' state.

This Navigator can come in useful when implementing complex overlays that 
warrant their own Scene instance:

![]({{ 'wiki/experimental/media/overlay_example.gif'|asset }})
{.image-preview}

## Usage

_You can find a working sample project demonstrating usage of this class 
[here](https://github.com/nhaarman/Acorn/tree/master/samples/hello-concurrentpairnavigator)_

To be able to allow two {{ anchor('Scenes','Scene') }} to be active at once, the
{{ anchor('ConcurrentPairNavigator') }} wraps the two Scenes in a 
{{ anchor('CombinedScene') }} instance with the key of the second Scene.
This special Scene implementation ignores any lifecycle calls, and ensures both
Scenes receive the proper {{ anchor('Container') }} instances in `attach` and
`detach`.
It does this by accepting a special {{ anchor('CombinedContainer') }} 
specialization of the Container interface, which allows access to the two sub
Containers.
It is then the responsibility of the UI layer to properly provide instances of
the CombinedContainer interface.

In the following sections you can find out how to make use of the 
ConcurrentPairNavigator.

### The initial Scene

The {{ anchor('ConcurrentPairNavigator') }} requires a first, initial 
{{ anchor('Scene') }}. 
This Scene is just like any other regular Scene, and can implement 
{{ anchor('ProvidesView') }} if you want to. 

For example, we can create the first Scene from above using RxJava like this<sup>1</sup>:

{% highlight 'kotlin' %}
interface FirstSceneContainer : Container {

    var count: Long

    /** Registers a listener for when an action is clicked */
    fun onActionClicked(f: () -> Unit)
}

/**
 * Displays a counter value that continuously increases starting when this Scene
 * is started, until this Scene is destroyed.
 */
class FirstScene(
    private val listener: Events,
    scheduler: Scheduler = AndroidSchedulers.mainThread()
) : RxScene<FirstSceneContainer>(null), ProvidesView {

    /**
     * Emits a continuously increasing stream of Longs every 100 milliseconds,
     * starting at the first subscription, until the Scene is destroyed.
     */
    private val counter: Observable<Long> = Observable
        .interval(0, 100, TimeUnit.MILLISECONDS, scheduler)
        .replay(1).autoConnect(this)

    /**
     * Conveniently provides a View and ViewController for this Scene.
     */
    override fun createViewController(parent: ViewGroup): ViewController {
        return FirstSceneViewController(parent.inflate(R.layout.first_scene))
    }

    override fun onStart() {
        super.onStart()

        // Subscribe to the counter, updating the container when available.
        disposables += counter
            .combineWithLatestView()
            .subscribe { (count, container) ->
                container?.count = count
            }
    }

    override fun attach(v: FirstSceneContainer) {
        super.attach(v)
        
        // Registers a listener with the container.
        v.onActionClicked { listener.actionClicked() }
    }

    interface Events {

        /**
         * Invoked when this Scene's action is clicked.
         */
        fun actionClicked()
    }
}
{% endhighlight %}

As mentioned, there is nothing special about this Scene that makes it suitable
for the ConcurrentPairNavigator.

### The second Scene



### The ConcurrentPairNavigator 

### Providing the container

---

1: This example uses the {{ anchor('RxScene') }} class available in the 
`com.nhaarman.acorn.ext:acorn-rx` artifact.



