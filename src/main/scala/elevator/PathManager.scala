package org.example
package elevator

import common.{Floor, MovementDirection, StopsRegistry}


case class PathManager(currentFloor: Floor, currentDirection: MovementDirection, registry: StopsRegistry) {
  def addStop(stop: Floor): PathManager = {
    addStop(stop, stopDirection(stop))
  }

  def addStop(stop: Floor, movementDirection: MovementDirection): PathManager = {
    this.copy(registry = registry.addStop(stop, movementDirection))
  }

  private def stopDirection(stop: Floor) = {
    if (stop == currentFloor) {
      currentDirection
    } else {
      MovementDirection(currentFloor, stop)
    }
  }

  def stopAtCurrentFloor(): PathManager = {
    val directionToRemoveFrom = if (noMoreCurrentDirectionStops) {
      currentDirection.opposite
    } else {
      currentDirection
    }
    val updatedRegistry = registry.removeStop(currentFloor, directionToRemoveFrom)
    this.copy(currentDirection = nextDirection, registry = updatedRegistry)
  }


  def hasNextStops: Boolean = {
    currentDirectionStops.nonEmpty || oppositeDirectionStops.nonEmpty
  }

  def shouldStopAtThisFloor: Boolean = {
    currentDirectionStops.contains(currentFloor) || noMoreStepsInThisDirection && oppositeDirectionStops.contains(currentFloor)
  }

  def updateDirection(): PathManager = {
    this.copy(currentDirection = nextDirection)
  }

  private def nextDirection = {
    if (shouldSwapDirection) {
      currentDirection.opposite
    } else {
      currentDirection
    }
  }

  private def shouldSwapDirection: Boolean = {
    noMoreStepsInThisDirection && oppositeDirectionStops.nonEmpty
  }

  private def noMoreStepsInThisDirection: Boolean = noMoreCurrentDirectionStops && !oppositeDirectionStops.exists(_.isAfter(currentDirection)(currentFloor))

  private def noMoreCurrentDirectionStops: Boolean = !currentDirectionStops.exists(_.isAfterOrEqual(currentDirection)(currentFloor))

  def moveFloor(): PathManager = nextFloor.map { floor =>
    val step = MovementDirection(currentFloor, floor).step
    this.copy(currentFloor = currentFloor + step)
  }.getOrElse(this)

  private def nextFloor: Option[Floor] = registry.nextFloor(currentFloor, currentDirection)

  private def currentDirectionStops: List[Floor] = registry.stops(currentDirection)

  private def oppositeDirectionStops: List[Floor] = registry.stops(currentDirection.opposite)
}

object PathManager {
  def apply(currentFloor: Floor, currentDirection: MovementDirection) = new PathManager(currentFloor, currentDirection, StopsRegistry.empty)
}
