package org.example
package drawing

trait Drawable {
  def draw: String
}

object Drawable {
  implicit class OptionalFloor(floor: Option[Int]) extends Drawable {
    override def draw: String = floor.map(_.toString).getOrElse(" ")
  }
}
