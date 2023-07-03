package org.example
package common

case class Floor(number: Int) extends AnyVal {
  def -(step: Int): Floor = new Floor(number - step)

  def +(step: Int): Floor = new Floor(number + step)

  def >(floor: Floor): Boolean = number > floor.number

  def <(floor: Floor): Boolean = number < floor.number

  def >=(floor: Floor): Boolean = number >= floor.number

  def <=(floor: Floor): Boolean = number <= floor.number

  def isHigherOrEqual(floor: Floor): Boolean = number >= floor.number

  def isAfter(movementDirection: MovementDirection)(floor: Floor): Boolean = {
    if (movementDirection == Up) {
      this > floor
    } else {
      this < floor
    }
  }

  def isAfterOrEqual(movementDirection: MovementDirection)(floor: Floor): Boolean = {
    if (movementDirection == Up) {
      this >= floor
    } else {
      this <= floor
    }
  }

  def distanceTo(floor: Floor): Int = {
    math.abs(number - floor.number)
  }
}
