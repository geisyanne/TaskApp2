package com.geisyanne.taskapp.util

sealed class StateView<T>(val data: T? = null, val msg: String? = null ) {
    class OnLoading<T>: StateView<T>()
    class OnSuccess<T>(data: T, msg: String? = null): StateView<T>(data, msg)
    class OnError<T>(msg: String? = null): StateView<T>(null, msg)
}