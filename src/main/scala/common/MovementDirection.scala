package org.example
package common

import Config.FloorsPerStep
import drawing.Drawable

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

sealed trait MovementDirection extends Drawable {
  def step: Int

  def opposite: MovementDirection
}

case object Up extends MovementDirection {
  override def step: Int = FloorsPerStep

  override def opposite: MovementDirection = Down

  override def draw: String = "^"
}

case object Down extends MovementDirection {
  override def step: Int = -FloorsPerStep

  override def opposite: MovementDirection = Up

  override def draw: String = "v"
}

object MovementDirection {
  implicit val directionCodec: Codec[MovementDirection] = deriveEnumerationCodec[MovementDirection]

  def apply(currentFloor: Floor, destinationFloor: Floor): MovementDirection = {
    if (destinationFloor.isHigherOrEqual(currentFloor)) {
      Up
    } else {
      Down
    }
  }
}
