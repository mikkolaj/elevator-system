package org.example
package common

case class StopsRegistry(directionsToStops: Map[MovementDirection, List[Floor]]) {
  def addStop(stop: Floor, direction: MovementDirection): StopsRegistry = {
    val stopsInDesiredDirection = directionsToStops(direction)
    val updatedDirectionToStops = directionsToStops + (direction -> addStopTo(stop, direction, stopsInDesiredDirection))
    this.copy(directionsToStops = updatedDirectionToStops)
  }

  def removeStop(stop: Floor, direction: MovementDirection): StopsRegistry = {
    val stopsInDesiredDirection = directionsToStops(direction)
    val updatedDirectionToStops = directionsToStops + (direction -> stopsInDesiredDirection.filter(_ != stop))
    this.copy(directionsToStops = updatedDirectionToStops)
  }

  def stops(direction: MovementDirection): List[Floor] = directionsToStops(direction)

  def stopsWithDirections(direction: MovementDirection): List[(Floor, MovementDirection)] = directionsToStops(direction)
    .map(stop => (stop, direction))

  def nextFloor(currentFloor: Floor, direction: MovementDirection): Option[Floor] = directionsToStops(direction)
    .find(_.isAfterOrEqual(direction)(currentFloor))
    .orElse(directionsToStops(direction.opposite).headOption)

  private def addStopTo(stop: Floor, movementDirection: MovementDirection, list: List[Floor]): List[Floor] = {
    if (list.contains(stop)) {
      list
    } else {
      val earlierStops = list.takeWhile(stop.isAfter(movementDirection))
      val laterStops = list.takeRight(list.length - earlierStops.length)
      earlierStops ::: (stop :: laterStops)
    }
  }
}

object StopsRegistry {
  def empty: StopsRegistry = StopsRegistry(emptyDirectionsMap)

  private def emptyDirectionsMap: Map[MovementDirection, List[Floor]] = Map(Up -> Nil, Down -> Nil)
}