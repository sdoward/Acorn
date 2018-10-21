/*
 * Bravo - Decoupling navigation from Android
 * Copyright (C) 2018 Niek Haarman
 *
 * Bravo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bravo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bravo.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.nhaarman.bravo.android.presentation.internal

import android.app.Activity
import android.view.ViewGroup
import com.nhaarman.bravo.android.internal.v
import com.nhaarman.bravo.android.presentation.ViewController
import com.nhaarman.bravo.android.presentation.ViewFactory
import com.nhaarman.bravo.android.transition.Transition
import com.nhaarman.bravo.android.transition.TransitionFactory
import com.nhaarman.bravo.navigation.TransitionData
import com.nhaarman.bravo.presentation.Container
import com.nhaarman.bravo.presentation.Scene

/**
 * A sealed hierarchy that manages layout inflation and [Scene] transition
 * animations for usage in an [Activity].
 *
 * These set of classes form a state machine that can manage switching views
 * when a new Scene becomes active. It takes the Activity lifecycle into account,
 * as well as Scene transitions.
 *
 * Invoking methods on this class may cause a state transition: the resulting
 * state is returned. Consumers of this class must call its methods at the
 * appropriate times ([started], [stopped], [withScene]) and update their
 * reference to the resulting state accordingly.
 */
internal sealed class ViewState {

    /**
     * Must be invoked when [Activity.onStart] is called.
     *
     * @return The new state.
     */
    abstract fun started(): ViewState

    /**
     * Must be invoked when [Activity.onStop] is called.
     *
     * @return the new state.
     */
    abstract fun stopped(): ViewState

    /**
     * Must be invoked whenever a Scene change occurs.
     *
     * @param scene The new [Scene].
     * @return the new state.
     */
    abstract fun withScene(scene: Scene<out Container>, data: TransitionData?): ViewState

    /**
     * Must be invoked when no local [Scene] is currently active, for example
     * when an external application is launched.
     *
     * @return the new state.
     */
    abstract fun withoutScene(): ViewState

    companion object {

        /**
         * Creates the initial [ViewState].
         *
         * @param root The [ViewGroup] to show Scene views in, usually
         * [android.R.id.content].
         * @param viewFactory A [ViewFactory] that provides views for Scenes.
         * @param transitionFactory a [TransitionFactory] that provides
         * [Transition] instances for transition animations.
         */
        fun create(
            root: ViewGroup,
            viewFactory: ViewFactory,
            transitionFactory: TransitionFactory
        ): ViewState =
            Idle(
                root,
                viewFactory,
                transitionFactory
            )
    }
}

/**
 * Represents the initial state where the Activity is not started, and has no
 * Scene or View.
 */
internal class Idle(
    private val root: ViewGroup,
    private val viewFactory: ViewFactory,
    private val transitionFactory: TransitionFactory
) : ViewState() {

    /**
     * Transitions to the [Started] state, which has no active [Scene].
     */
    override fun started(): ViewState {
        v("ViewState.Idle", "Activity started.")
        return Started(root, viewFactory, transitionFactory)
    }

    /**
     * Makes no transition.
     */
    override fun stopped() = this

    /**
     * Transitions to the [IdleWithScene] state, which is not started.
     */
    override fun withScene(scene: Scene<out Container>, data: TransitionData?): ViewState {
        v("ViewState.Idle", "Scene changed while idle: $scene.")
        return IdleWithScene(
            root,
            viewFactory,
            transitionFactory,
            scene
        )
    }

    override fun withoutScene() = this
}

/**
 * Represents the state where the Activity is not started, but does have a Scene.
 * When the Activity enters the started state, the scene must be shown directly
 * without any animations.
 */
internal class IdleWithScene(
    private val root: ViewGroup,
    private val viewFactory: ViewFactory,
    private val transitionFactory: TransitionFactory,
    private val scene: Scene<out Container>
) : ViewState() {

    /**
     * Immediately shows the view for the active [Scene] without a transition
     * animation, and transitions to the [StartedWithScene] state.
     */
    override fun started(): ViewState {
        v(
            "ViewState.IdleWithScene",
            "Activity started with active scene: $scene"
        )
        v("ViewState.IdleWithScene", "Showing scene without animation.")

        val result = viewFactory.viewFor(scene.key, root)
            ?: error("No view could be created for Scene with key ${scene.key}.")

        root.removeAllViews()
        root.addView(result.view)

        scene.forceAttach(result)
        return StartedWithScene(
            root,
            viewFactory,
            transitionFactory,
            scene,
            result
        )
    }

    /**
     * Makes no transition.
     */
    override fun stopped() = this

    /**
     * Discards the current scene and transitions to a new [IdleWithScene] state
     * with the new given [Scene].
     */
    override fun withScene(scene: Scene<out Container>, data: TransitionData?): ViewState {
        v("ViewState.IdleWithScene", "Scene changed while idle to: $scene.")
        return IdleWithScene(
            root,
            viewFactory,
            transitionFactory,
            scene
        )
    }

    override fun withoutScene(): ViewState {
        v("ViewState.IdleWithScene", "Scene lost while idle.")
        return Idle(root, viewFactory, transitionFactory)
    }
}

/**
 * Represents the state where the Activity is started without a Scene or View.
 * Entering this state should usually be followed directly with a call to
 * [withScene], as otherwise the Activity shows a blank view.
 */
internal class Started(
    private val root: ViewGroup,
    private val viewFactory: ViewFactory,
    private val transitionFactory: TransitionFactory
) : ViewState() {

    /**
     * Makes no transition.
     */
    override fun started() = this

    /**
     * Transitions to the [Idle] state.
     */
    override fun stopped(): ViewState {
        v("ViewState.Started", "Activity stopped.")
        return Idle(root, viewFactory, transitionFactory)
    }

    /**
     * Immediately shows the view for given [Scene] without a transition
     * animation, and transitions to the [StartedWithScene] state.
     */
    override fun withScene(scene: Scene<out Container>, data: TransitionData?): ViewState {
        v("ViewState.Started", "Scene changed in Started state to: $scene.")
        v(
            "ViewState.Started",
            "No current scene active, showing scene without animation."
        )

        val result = viewFactory.viewFor(scene.key, root)
            ?: error("No view could be created for Scene with key ${scene.key}.")

        root.removeAllViews()
        root.addView(result.view)
        scene.forceAttach(result)

        return StartedWithScene(
            root,
            viewFactory,
            transitionFactory,
            scene,
            result
        )
    }

    override fun withoutScene() = this
}

/**
 * Represents the state where the Activity is started and is actively showing a
 * Scene. This is the state the Activity is in most of the time when the
 * application is in the foreground.
 *
 * @param currentScene The active [Scene].
 * @param currentViewController The [ViewController], must be attached to
 * [currentScene].
 */
internal class StartedWithScene(
    private val root: ViewGroup,
    private val viewFactory: ViewFactory,
    private val transitionFactory: TransitionFactory,
    private var currentScene: Scene<out Container>,
    private var currentViewController: ViewController
) : ViewState() {

    private var transitionCallback: CancellableTransitionCallback? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private var scheduledScene: Pair<Scene<out Container>, TransitionData?>? = null

    /**
     * Makes no transition.
     */
    override fun started() = this

    /**
     * Detaches the current view from the current [Scene] and transitions to the
     * [StoppedWithScene] state.
     */
    override fun stopped(): ViewState {
        v(
            "StartedWithScene",
            "Activity stopped, detaching container from $currentScene."
        )
        currentScene.forceDetach(currentViewController)
        transitionCallback = null

        return StoppedWithScene(
            root,
            viewFactory,
            transitionFactory,
            currentScene,
            currentViewController
        )
    }

    /**
     * Executes a transition from the current [Scene] to the given [Scene].
     *
     * If a transition animation is currently active, the transition to [scene]
     * is scheduled.
     */
    override fun withScene(scene: Scene<out Container>, data: TransitionData?): ViewState {
        v("ViewState.StartedWithScene", "Scene changed to: $scene.")
        if (transitionCallback != null) {
            v(
                "ViewState.StartedWithScene",
                "Transition already in progress, scheduling transition to $scene."
            )
            scheduledScene = scene to data
            return this
        }

        v(
            "ViewState.StartedWithScene",
            "Starting transition from $currentScene to $scene."
        )
        currentScene.forceDetach(currentViewController)

        val callback = MyCallback(scene).also { transitionCallback = it }
        transitionFactory.transitionFor(currentScene, scene, data)
            .execute(root, callback)

        return this
    }

    override fun withoutScene(): ViewState {
        v("ViewState.StartedWithScene", "Scene lost.")
        if (transitionCallback != null) {
            v("ViewState.StartedWithScene", "Transition in progress, canceling.")
            transitionCallback = null
            scheduledScene = null
        }

        return Started(root, viewFactory, transitionFactory)
    }

    private inner class MyCallback(
        private val newScene: Scene<out Container>
    ) : CancellableTransitionCallback {

        private var done = false
            set(done) {
                field = done
                if (done) {
                    transitionCallback = null
                }
            }

        override fun cancel() {
            if (done) return
            done = true

            v(
                "ViewState.StartedWithScene",
                "Transition to $newScene cancelled."
            )
        }

        private var attached = false
        override fun attach(viewController: ViewController) {
            if (done) return
            if (attached) return

            v(
                "ViewState.StartedWithScene",
                "Attaching container to $newScene before transition end."
            )
            attached = true

            currentScene = newScene
            currentViewController = viewController
            newScene.forceAttach(viewController)
        }

        override fun onComplete(viewController: ViewController) {
            if (done) return
            done = true

            v("ViewState.StartedWithScene", "Transition to $newScene complete.")

            if (!attached) {
                v(
                    "ViewState.StartedWithScene",
                    "Container not attached to $newScene; attaching."
                )
                currentScene = newScene
                currentViewController = viewController
                currentScene.forceAttach(viewController)
            }

            scheduledScene?.let { (scene, data) ->
                v("ViewState.StartWithScene", "Found scheduled scene: $scene")
                scheduledScene = null
                withScene(scene, data)
            }
        }
    }

    private interface CancellableTransitionCallback : Transition.Callback {

        fun cancel()
    }
}

/**
 * Represents the state where the Activity is stopped after showing a Scene:
 * the Scene and its view are available.
 * Especially, the [root] ViewGroup contains this view, so no view inflation or
 * binding to the parent is necessary.
 */
internal class StoppedWithScene(
    private val root: ViewGroup,
    private val viewFactory: ViewFactory,
    private val transitionFactory: TransitionFactory,
    private val scene: Scene<out Container>,
    private val viewController: ViewController
) : ViewState() {

    /**
     * Attaches the cached view to the active [Scene] and transitions to the
     * [StoppedWithScene] state.
     */
    override fun started(): ViewState {
        v("StoppedWithScene", "Activity started, attaching container to $scene.")

        scene.forceAttach(viewController)
        return StartedWithScene(
            root,
            viewFactory,
            transitionFactory,
            scene,
            viewController
        )
    }

    /**
     * Makes no transition.
     */
    override fun stopped() = this

    /**
     * Discards the current view and transitions to the [IdleWithScene] state.
     */
    override fun withScene(scene: Scene<out Container>, data: TransitionData?): ViewState {
        v("StoppedWithScene", "Scene changed while stopped: $scene.")
        return IdleWithScene(
            root,
            viewFactory,
            transitionFactory,
            scene
        )
    }

    override fun withoutScene(): ViewState {
        v("StoppedWithScene", "Scene lost while stopped.")
        return Idle(root, viewFactory, transitionFactory)
    }
}

@Suppress("UNCHECKED_CAST")
private fun Scene<out Container>.forceAttach(c: Container) {
    (this as Scene<Container>).attach(c)
}

@Suppress("UNCHECKED_CAST")
private fun Scene<out Container>.forceDetach(c: Container) {
    (this as Scene<Container>).detach(c)
}
