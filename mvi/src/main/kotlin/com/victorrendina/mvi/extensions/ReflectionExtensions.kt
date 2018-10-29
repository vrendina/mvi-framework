package com.victorrendina.mvi.extensions

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberFunctions

internal val primitiveWrapperMap = mapOf(
        Boolean::class.javaPrimitiveType to Boolean::class.java,
        Byte::class.javaPrimitiveType to Byte::class.javaObjectType,
        Char::class.javaPrimitiveType to Char::class.javaObjectType,
        Double::class.javaPrimitiveType to Double::class.javaObjectType,
        Float::class.javaPrimitiveType to Float::class.javaObjectType,
        Int::class.javaPrimitiveType to Int::class.javaObjectType,
        Long::class.javaPrimitiveType to Long::class.javaObjectType,
        Short::class.javaPrimitiveType to Short::class.javaObjectType
)

internal fun isPrimitiveWrapperOf(targetClass: Class<*>, primitive: Class<*>): Boolean {
    if (!primitive.isPrimitive) {
        throw IllegalArgumentException("First argument has to be primitive type")
    }
    return primitiveWrapperMap[primitive] == targetClass
}

fun isAssignableTo(from: Class<*>, to: Class<*>): Boolean {
    if (to.isAssignableFrom(from)) {
        return true
    }
    if (from.isPrimitive) {
        return isPrimitiveWrapperOf(to, from)
    }
    return if (to.isPrimitive) {
        isPrimitiveWrapperOf(from, to)
    } else false
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> KClass<T>.copyMethod(): KFunction<T> = this.memberFunctions.first { it.name == "copy" } as KFunction<T>

/**
 * Find a parameter in a function that matches the property name and return type.
 */
internal fun KFunction<*>.findParameter(property: KProperty<*>): KParameter? = parameters.firstOrNull {
    it.name == property.name && it.type == property.returnType
}
