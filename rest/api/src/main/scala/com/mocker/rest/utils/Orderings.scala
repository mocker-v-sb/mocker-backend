package com.mocker.rest.utils

import com.mocker.rest.request.KVPair

object Orderings {

  implicit def kvPairOrdering[A <: KVPair]: Ordering[A] =
    Ordering.by(e => (e.name, e.value))

}
