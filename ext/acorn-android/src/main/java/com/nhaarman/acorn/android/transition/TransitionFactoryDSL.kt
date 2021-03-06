/*
 *    Copyright 2018 Niek Haarman
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.nhaarman.acorn.android.transition

import com.nhaarman.acorn.android.presentation.ViewControllerFactory
import com.nhaarman.acorn.android.transition.internal.BindingTransitionFactory
import com.nhaarman.acorn.android.transition.internal.ClassBinding
import com.nhaarman.acorn.android.transition.internal.KeyBinding
import com.nhaarman.acorn.android.transition.internal.LazyClassBinding
import com.nhaarman.acorn.android.transition.internal.TransitionBinding
import com.nhaarman.acorn.presentation.Scene
import com.nhaarman.acorn.presentation.SceneKey
import kotlin.reflect.KClass

/**
 * Entry point for the [TransitionFactory] DSL.
 *
 * @see [TransitionFactoryBuilder]
 */
fun transitionFactory(
    viewControllerFactory: ViewControllerFactory,
    init: TransitionFactoryBuilder.() -> Unit
): TransitionFactory {
    return TransitionFactoryBuilder(viewControllerFactory).apply(init).build()
}

/**
 * A DSL that can create [TransitionFactory] instances by binding pairs of Scenes
 * to [Transition] instances.
 *
 * @param viewControllerFactory The [ViewControllerFactory] instance to use for
 * layout inflation for fallback transition animations.
 */
class TransitionFactoryBuilder internal constructor(
    private val viewControllerFactory: ViewControllerFactory
) {

    private val bindings = mutableListOf<TransitionBinding>()

    /**
     * Binds two [SceneKey]s to a [Transition] instance.
     */
    infix fun Pair<SceneKey, SceneKey>.use(transition: Transition) {
        bindings += KeyBinding(first, second, transition)
    }

    /**
     * Binds two [Scene] classes to a [Transition] instance.
     */
    @JvmName("useWithClasses")
    infix fun Pair<KClass<out Scene<*>>, KClass<out Scene<*>>>.use(transition: Transition) {
        bindings += ClassBinding(first, second, transition)
    }

    /**
     * Binds two [Scene] classes to a lazily evaluated [Transition] instance.
     *
     * @param transition A function that provides a [Transition] instance. Its
     * parameter is the destination [Scene] of the Transition.
     */
    infix fun Pair<KClass<out Scene<*>>, KClass<out Scene<*>>>.use(transition: (Scene<*>) -> Transition) {
        bindings += LazyClassBinding(first, second, transition)
    }

    fun build(): TransitionFactory {
        return BindingTransitionFactory(
            viewControllerFactory,
            bindings.asSequence()
        )
    }
}
