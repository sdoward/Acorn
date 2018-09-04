package com.nhaarman.bravo.notesapp.cli.note

import arrow.core.Option
import arrow.core.toOption
import com.nhaarman.bravo.notesapp.note.NoteItem
import com.nhaarman.bravo.notesapp.note.NoteItemsRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.atomic.AtomicLong

class MemoryNoteItemsRepository : NoteItemsRepository {

    private val availableId = AtomicLong()

    private val eventSubject = ReplaySubject.create<Event>()

    override val noteItems: Observable<List<NoteItem>> by lazy {
        eventSubject
            .scan(mutableMapOf<Long, NoteItem>()) { items, event ->
                when (event) {
                    is Event.Create -> items.also { it[event.item.id] = event.item }
                }
            }
            .map { it.values.toList() }
            .replay(1).refCount()
    }

    override fun create(text: String): Single<NoteItem> {
        return Single
            .fromCallable {
                NoteItem(availableId.incrementAndGet(), text)
                    .also { eventSubject.onNext(Event.Create(it)) }
            }
    }

    override fun delete(itemId: Long) {
        error("Not used")
    }

    override fun delete(item: NoteItem) {
        error("Not used")
    }

    override fun find(itemId: Long): Observable<Option<NoteItem>> {
        return noteItems
            .map {
                it
                    .find { item -> item.id == itemId }
                    .toOption()
            }
    }

    override fun update(itemId: Long, text: String): Single<Boolean> {
        error("Not used")
    }

    private sealed class Event {
        class Create(val item: NoteItem) : Event()
    }
}