package org.example
package system.dtos

import common.Floor
import system.ElevatorSystem.StopAt

case class StopAtDto(floor: Int) {
  def toStopAt(elevatorId: Int): StopAt = StopAt(Floor(floor), elevatorId)
}
