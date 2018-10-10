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

package com.nhaarman.bravo.notesapp.android.ui.itemlist

import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.nhaarman.bravo.android.presentation.RestorableLayoutContainer
import com.nhaarman.bravo.notesapp.note.NoteItem
import com.nhaarman.bravo.notesapp.presentation.itemlist.ItemListContainer
import io.reactivex.Observable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.itemlist_scene.*

class ItemListView(
    override val containerView: ViewGroup
) : ItemListContainer, LayoutContainer, RestorableLayoutContainer {

    override var items: List<NoteItem> = emptyList()
        set(value) {
            itemsRecyclerView.items = value
        }

    override val createClicks: Observable<Unit> by lazy {
        createButton.clicks()
    }

    override val itemClicks: Observable<NoteItem> by lazy {
        Observable
            .create<NoteItem> { emitter ->
                val listener = object : ItemsRecyclerView.ClicksListener {

                    override fun onItemClicked(item: NoteItem) {
                        emitter.onNext(item)
                    }

                    override fun onDeleteClicked(item: NoteItem) {
                    }
                }
                itemsRecyclerView.addClicksListener(listener)
                emitter.setCancellable { itemsRecyclerView.removeClicksListener(listener) }
            }
            .share()
    }

    override val deleteClicks: Observable<NoteItem> by lazy {
        Observable
            .create<NoteItem> { emitter ->
                val listener = object : ItemsRecyclerView.ClicksListener {

                    override fun onItemClicked(item: NoteItem) {
                    }

                    override fun onDeleteClicked(item: NoteItem) {
                        emitter.onNext(item)
                    }
                }
                itemsRecyclerView.addClicksListener(listener)
                emitter.setCancellable { itemsRecyclerView.removeClicksListener(listener) }
            }
            .share()
    }
}