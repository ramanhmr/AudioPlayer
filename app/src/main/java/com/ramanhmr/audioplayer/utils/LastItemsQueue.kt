package com.ramanhmr.audioplayer.utils

import java.util.*

class LastItemsQueue<E>(private val maxItemCount: Int) : Iterable<E> {
    private val items = LinkedList<E>()
    private var currentIndex = 0

    fun add(e: E): Boolean {
        with(items) {
            addFirst(e)
            while (size > maxItemCount) removeLast()
        }
        return true
    }

    fun hasPrevious(): Boolean = currentIndex + 1 < items.size

    fun previous(): E? {
        return if (currentIndex < maxItemCount - 1) {
            items[++currentIndex]
        } else items[currentIndex]
    }

    fun hasNext(): Boolean = currentIndex - 1 >= 0

    fun next(): E? = if (currentIndex > 0) items[--currentIndex] else null

    fun deleteHeadToCurrent() {
        for (i in 0 until currentIndex) {
            items.remove()
        }
        currentIndex = 0
    }

    override fun iterator() = object : Iterator<E> {
        val size = items.size
        var i = 0

        override fun hasNext(): Boolean = i < size

        override fun next(): E = items[i++]
    }
}