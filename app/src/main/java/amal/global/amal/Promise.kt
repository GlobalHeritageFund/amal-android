package amal.global.amal

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class Promise<T> private constructor(state: State<T>) {

    data class Callback<T>(
            val onFulfilled: (T) -> Unit,
            val onRejected: (kotlin.Error) -> Unit
    )

    sealed class State<T> {

        data class Pending<T>(
                val callbacks: MutableSet<Callback<T>>
        ) : State<T>()

        data class Fulfilled<T>(
                val value: T
        ) : State<T>()

        data class Rejected<T>(
                val error: kotlin.Error
        ) : State<T>()

    }

    private val state: AtomicReference<State<T>> = AtomicReference(state)

    constructor(resolver: (fulfill: (T) -> Unit, reject: (Error) -> Unit) -> Unit = { _, _ -> }) : this(
            State.Pending(
                    callbacks = mutableSetOf()
            )
    ) {
        thread {
            resolver({ value -> this.fulfill(value) }, { error -> this.reject(error) })
        }
    }

    constructor() : this(State.Pending(mutableSetOf()))

    constructor(error: kotlin.Error) : this(State.Rejected(error))

    constructor(value: T) : this(State.Fulfilled(value))

    fun fulfill(value: T) {
        update(State.Fulfilled(value))
    }

    fun reject(error: Error) {
        update(State.Rejected(error))
    }

    fun value(): T? {
        val currentState = this.state.get()
        when (currentState) {
            is State.Fulfilled -> return currentState.value
            else -> return null
        }
    }

    private fun update(state: State<T>) {
        val oldState = this.state.get()
        this.state.set(state)
        when(oldState) {
            is State.Pending -> fireCallbacks(oldState.callbacks, state)
            is State.Fulfilled -> return
            is State.Rejected -> return
        }
    }

    private fun fireCallbacks(callbacks: Set<Callback<T>>, newState: State<T>) {
        when (newState) {
            is State.Fulfilled -> callbacks.forEach { callback -> callback.onFulfilled(newState.value) }
            is State.Rejected -> callbacks.forEach { callback -> callback.onRejected(newState.error) }
            is State.Pending -> {}
        }
    }

    private fun addCallback(onFulfilled: (T) -> Unit, onRejected: (Error) -> Unit) {
        do {
            val oldState = state.get()
            val newState = when (oldState) {
                is State.Pending -> oldState.callbacks.add(Callback(onFulfilled, onRejected)).let { oldState }
                is State.Fulfilled -> onFulfilled(oldState.value).let { oldState }
                is State.Rejected -> onRejected(oldState.error).let { oldState }
                else -> oldState
            }
        } while (oldState !== newState && !state.compareAndSet(oldState, newState))

    }

    public fun then(onSuccess: (T) -> Unit): Promise<T> {
        this.addCallback(
                { value ->
                    try {
                        onSuccess(value)
                    } catch(e: Exception) {

                    }
                },
                { error ->

                }
        )
        return this
    }

    public fun catch(onError: (Error) -> Unit): Promise<T> {
        this.addCallback(
                { value ->

                },
                { error ->
                    onError(error)
                }
        )
        return this
    }

    public fun <V> map(map: (T) -> V): Promise<V> {
        return Promise<V>({ fulfill, reject ->
            this.addCallback(
                    { value ->
                        try {
                            fulfill(map(value))
                        } catch(e: Exception) {
                            reject(Error(e.message))
                        }
                    },
                    { error ->
                        reject(error)
                    }
            )
        })
    }

    public fun <V> flatMap(flatMap: (T) -> Promise<V>): Promise<V> {
        return Promise<V>({ fulfill, reject ->
            this.addCallback(
                    { value ->
                        try {
                            flatMap(value)
                                    .then { value -> fulfill(value) }
                                    .catch { error -> reject(error) }
                        } catch(e: Exception){
                            reject(Error(e.message))
                        }
                    },
                    { error ->
                        reject(error)
                    }
            )
        })
    }

    companion object {
        fun <U: Any> all(promises: Sequence<Promise<U>>): Promise<List<U>> {
            val promiseList = promises.toList()

            val remainingCount = AtomicInteger(promiseList.size)

            return Promise<List<U>>{ fulfill, reject ->
                
                promiseList.forEach { promise ->
                    promise
                            .then { value ->
                                if (remainingCount.decrementAndGet() == 0) {
                                    fulfill(promiseList.mapNotNull { it.value() })
                                }
                            }
                            .catch { error ->
                                reject(error)
                            }
                }
            }
        }
    }
}
