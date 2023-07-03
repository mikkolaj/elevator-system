package org.example
package common

import drawing.Drawable
import elevator.Elevator.StateSwitchingDelay
import elevator.PathManager
import elevator.dtos.ElevatorStateDto

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

case class ElevatorState(
  elevatorId: Int,
  floor: Floor,
  direction: MovementDirection,
  stateType: StateType,
  registry: StopsRegistry
) {
  def distanceTo(stop: Floor, desiredDirection: MovementDirection): Int = {
    val updatedRegistry = registry.addStop(stop, desiredDirection)
    val (stopsInCurrentDirectionAfterCurrentFloor, stopsInCurrentDirectionBeforeCurrentFloor) = updatedRegistry.stopsWithDirections(direction)
      .partition(_._1.isAfterOrEqual(direction)(floor))
    val stopsInOppositeDirection = updatedRegistry.stopsWithDirections(direction.opposite)

    val listOfStops = stopsInCurrentDirectionAfterCurrentFloor ::: (stopsInOppositeDirection ::: stopsInCurrentDirectionBeforeCurrentFloor)
    val stopsToDestination = (listOfStops.takeWhile(elem => elem != (stop, desiredDirection)) :+ (stop, direction)).map(_._1)
    distanceOverStops(floor, stopsToDestination)
  }

  private def distanceOverStops(start: Floor, stops: List[Floor]): Int = {
    stops.foldLeft((startDelay(start, stops.head), start)) { case ((distance, previousFloor), floor) =>
      (distance + previousFloor.distanceTo(floor) + floorStopDelay(floor, start, stops.last), floor)
    }._1
  }

  private def startDelay(start: Floor, firstStop: Floor): Int = {
    if (start == firstStop || stateType.isInstanceOf[Moving.type]) {
      0
    } else {
      StateSwitchingDelay
    }
  }

  private def floorStopDelay(stop: Floor, startFloor: Floor, endFloor: Floor): Int = {
    if (stop == startFloor && stop == endFloor) {
      takingPassengersDelay
    } else if (stop == startFloor) {
      takingPassengersDelay + 1
    } else if (stop == endFloor) {
      StateSwitchingDelay
    } else {
      2 * StateSwitchingDelay
    }
  }

  def toDto: ElevatorStateDto = {
    ElevatorStateDto(elevatorId, floor.number, direction, nextFloorNumber, stateType)
  }

  def nextFloorNumber: Option[Int] = registry
    .nextFloor(floor, direction)
    .map(_.number)

  private def takingPassengersDelay: Int = stateType match {
    case Idle | Moving    => StateSwitchingDelay
    case TakingPassengers => 0
  }
}

object ElevatorState {
  def apply(elevatorId: Int, pathManager: PathManager, stateType: StateType): ElevatorState = {
    new ElevatorState(elevatorId, pathManager.currentFloor, pathManager.currentDirection, stateType, pathManager.registry)
  }
}

sealed trait StateType extends Drawable {
  override def draw: String = this.toString.charAt(0).toString
}

case object Idle extends StateType

case object TakingPassengers extends StateType

case object Moving extends StateType


object StateType {
  implicit val stateTypeCodec: Codec[StateType] = deriveEnumerationCodec[StateType]
}