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

package com.nhaarman.bravo.samples.hellostartactivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nhaarman.bravo.android.BravoActivityDelegate
import com.nhaarman.bravo.android.presentation.bindViews
import com.nhaarman.bravo.android.transition.DefaultTransitionFactory
import com.nhaarman.bravo.presentation.SceneKey.Companion.defaultKey

@SuppressLint("CheckResult")
class MainActivity : AppCompatActivity() {

    private val delegate by lazy {
        val viewFactory = bindViews {
            bind(defaultKey<FirstScene>(), R.layout.first_scene)
        }

        BravoActivityDelegate.from(
            activity = this,
            navigatorProvider = HelloStartActivityNavigatorProvider,
            viewFactory = viewFactory,
            transitionFactory = DefaultTransitionFactory(viewFactory),
            intentProvider = MapsIntentProvider
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        delegate.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        delegate.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (!delegate.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
