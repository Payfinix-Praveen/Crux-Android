package com.sujanix.cruxmdm.features.core.data.model.fencing

import com.sujanix.cruxmdm.features.core.data.model.socket.location.Coordinate

sealed class Coordinates {
    data class Polygon(val coordinates: List<List<Coordinate>>) : Coordinates()
    data class MultiPolygon(val coordinates: List<List<List<Coordinate>>>) : Coordinates()
    data class DoubleNested(val coordinates: List<List<List<List<Coordinate>>>>) : Coordinates()
}