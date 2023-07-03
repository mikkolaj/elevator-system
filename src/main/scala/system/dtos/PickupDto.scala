package org.example
package system.dtos

import common.{Floor, MovementDirection}
import system.ElevatorSystem.Pickup

case class PickupDto(floor: Int, direction: MovementDirection) {
  def toPickup: Pickup = Pickup(Floor(floor), direction)
}
