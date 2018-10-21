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

package com.nhaarman.bravo.samples.hellotransitionanimation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import com.nhaarman.bravo.android.transition.Transition

object FirstSceneSecondSceneTransition : Transition {

    override fun execute(parent: ViewGroup, callback: Transition.Callback) {
        val currentLayout = parent.getChildAt(0)

        val newLayout = LayoutInflater.from(parent.context).inflate(R.layout.second_scene, parent, false)
        parent.addView(newLayout)

        parent.doOnPreDraw {
            newLayout.translationX = newLayout.width.toFloat()

            currentLayout
                .animate()
                .translationX((-currentLayout.width).toFloat())
                .withEndAction { parent.removeView(currentLayout) }

            newLayout
                .animate()
                .translationX(0f)
                .withEndAction {
                    callback.onComplete(SecondSceneViewController(newLayout))
                }
        }
    }
}