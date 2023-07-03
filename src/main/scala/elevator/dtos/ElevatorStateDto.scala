package org.example
package elevator.dtos

import common.{MovementDirection, StateType}

case class ElevatorStateDto(elevatorId: Int, floor: Int, direction: MovementDirection, optionalTargetFloor: Option[Int], stateType: StateType)
