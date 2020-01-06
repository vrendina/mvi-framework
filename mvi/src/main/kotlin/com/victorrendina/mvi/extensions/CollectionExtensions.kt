package com.victorrendina.mvi.extensions

import java.util.Collections
import kotlin.reflect.KClass

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

/**
 * Update items in an immutable list that are of [type] and match the given [predicate].
 *
 * @param type class of item to match in the list
 * @param predicate function that takes an item from the list and returns a boolean
 * @param reducer function that will be called to update any item matching the predicate
 * @return copy of the list with the item updated
 */
inline fun <T : Any, reified S : T> List<T>.updateTypedItems(
    @Suppress("UNUSED_PARAMETER") type: KClass<S>,
    predicate: (S) -> Boolean,
    reducer: S.() -> S
) = mapTo(ArrayList(size)) { item ->
    if (item is S && predicate(item)) item.reducer() else item
}

fun <T> List<T>.moveItem(fromIndex: Int, toIndex: Int): List<T> {
    val mutableList = ArrayList(this)
    val bounds = 0 until size
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
    if (index in 0 until size) {
        removeAt(index)
    }
}

/**
 * Append the contents of a list at the specified offset, replacing any existing items that may
 * be at that offset currently.
 *
 * This method will replace all contents starting at the offset with the new list.
 *
 * For example [1,2,3].appendAt(2, [4,5]) == [1,2,4,5]
 */
fun <T> List<T>.appendAt(offset: Int, other: List<T>) = subList(0, offset.coerceIn(0, size)) + other

/**
 * Insert the contents of a list at the specified offset, replacing any existing items that may
 * be at that offset currently.
 *
 * This method will replace contents starting at the offset with the new list and retain any existing
 * items before the offset and after the end of the other list.
 *
 * For example:
 * [1,2,3].insertAt(1, [4]) == [1,4,3]
 */
fun <T> List<T>.insertAt(offset: Int, other: List<T>) = appendAt(offset, other) + subList(
    (offset.coerceIn(0, size) + other.size).coerceIn(0, size), size
)

/**
 * Convert an untyped list to a typed list.
 */
inline fun <reified T> List<*>.toTypedList(): List<T> {
    val destination = ArrayList<T>(size)
    forEach {
        if (it is T) {
            destination.add(it)
        }
    }
    return destination
}

/**
 * Convert an untyped map to a typed map.
 */
inline fun <reified K, reified V> Map<*, *>.toTypedMap(): Map<K, V> {
    val destination = HashMap<K, V>(size)
    forEach { (key, value) ->
        if (key is K && value is V) {
            destination[key] = value
        }
    }
    return destination
}