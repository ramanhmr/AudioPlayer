package com.ramanhmr.audioplayer.utils

import java.util.*

class LastItemsQueue<E>(private val maxItemCount: Int) {
    private val list = LinkedList<E>()
    private var currentIndex = 0

    fun add(e: E): Boolean {
        with(list) {
            addFirst(e)
            while (size > maxItemCount) removeLast()
        }
        return true
    }

    fun hasPrevious(): Boolean {
        try {
            list[currentIndex + 1]
        } catch (e: IndexOutOfBoundsException) {
            return false
        }
        return true
    }

    fun previous(): E? {
        return if (currentIndex < maxItemCount - 1) {
            list[++currentIndex]
        } else list[currentIndex]
    }


    fun hasNext(): Boolean {
        try {
            list[currentIndex - 1]
        } catch (e: IndexOutOfBoundsException) {
            return false
        }
        return true
    }

    fun next(): E? = if (currentIndex > 0) list[--currentIndex] else null

    fun deleteHeadToCurrent() {
        for (i in 0 until currentIndex) {
            list.remove()
        }
        currentIndex = 0
    }
}