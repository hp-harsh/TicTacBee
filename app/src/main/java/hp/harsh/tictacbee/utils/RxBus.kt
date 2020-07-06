package hp.harsh.tictacbee.utils

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * @purpose RxBus - Even bus that is worked on Rx concept to notify something from one class to another class.
 *
 * It is worked on Observer, Observable and Operator patterns.
 *
 * @author Harsh Patel
 */
class RxBus {

    private val bus = PublishSubject.create<Any>()

    fun send(o: Any) {
        bus.onNext(o)
    }

    fun toObservable(): Observable<Any> {
        return bus
    }

    fun hasObservers(): Boolean {
        return bus.hasObservers()
    }
}
