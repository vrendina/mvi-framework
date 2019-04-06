package com.victorrendina.mvi.extensions

import java.util.Collections

/**
 * Update an item in an immutable list. If the index does not exist in the list then this method will return a copy of
 * the list with no changes.
 *
 * @param index index of item to update
 * @param reducer function that will be called to update the item if the index exists
 * @return copy of the list with the item updated
 */
fun <T> List<T>.updateItem(index: Int, reducer: T.() -> T): List<T> = mapIndexedTo(ArrayList(size)) { cursor, item ->
    if (cursor == index) item.reducer() else item
}

/**
 * Update items in an immutable list that match the given [predicate]. If the predicate does not match anything then
 * this method will return a copy of the list with no changes. It is possible for multiple items to match the predicate.
 *
 * @param predicate function that takes an item from the list and returns a boolean
 * @param reducer function that will be called to update any item matching the predicate
 * @return copy of the list with the item updated
 */
fun <T> List<T>.updateItems(predicate: (T) -> Boolean, reducer: T.() -> T) = mapTo(ArrayList(size)) { item ->
    if (predicate(item)) item.reducer() else item
}

/**
 * Update an item in an immutable map and return a copy of the map with the item updated. If the key does not exist
 * a copy of the map will be returned without any changes unless a factory is provided to create a default instance
 * of the item when it can't be found.
 *
 * @param key the key of the item to update
 * @param factory optional function that will return a default instance of the item if it doesn't exist in the map
 * @param reducer function that will be called to update the item if it exists
 * @return copy of the map with the item updated
 */
fun <K, V> Map<K, V>.updateItem(key: K, factory: ((K) -> V)? = null, reducer: V.() -> V): Map<K, V> {
    return HashMap<K, V>(this).apply {
        (get(key) ?: factory?.invoke(key))?.reducer()?.also { put(key, it) }
    }
}

fun <T> List<T>.moveItem(fromIndex: Int, toIndex: Int): List<T> {
    val mutableList = ArrayList(this)
    val bounds = 0..(size - 1)
    if (fromIndex in bounds && toIndex in bounds) {
        if (fromIndex < toIndex) {
            for (i in fromIndex until toIndex) {
                Collections.swap(mutableList, i, i + 1)
            }
        } else {
            for (i in fromIndex downTo toIndex + 1) {
                Collections.swap(mutableList, i, i - 1)
            }
        }
    }
    return mutableList
}

fun <T> List<T>.removeItem(index: Int): List<T> = ArrayList(this).apply {
    if (index in 0..(size - 1)) {
        removeAt(index)
    }
}