package org.example
package drawing

import common.{ElevatorState, Floor}
import drawing.Drawable.OptionalFloor
import drawing.Drawer._

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

class Drawer(numberOfElevators: Int, numberOfFloors: Int) {
  private lazy val emptyElevator = EmptyElevator * (ElevatorPrefixLength + longestFloorNumber)

  def handleDrawing(): Behavior[Command] = Behaviors.receiveMessage {
    case Draw(states) =>
      drawStates(states)
      Behaviors.same
  }

  private def drawStates(states: Iterable[ElevatorState]): Unit = {
    val mapOfElevators = states.map(state => (state.elevatorId, state)).toMap
    val sb = new StringBuilder()
    for {
      floor <- (0 until numberOfFloors).reverse
    } addFloor(floor, mapOfElevators, sb)
    println(sb.toString())
  }

  private def addFloor(floor: Int, elevatorMap: Map[Int, ElevatorState], sb: StringBuilder): Unit = {
    addFloorDescription(floor, sb)
    for {
      elevator <- 0 until numberOfElevators
    } addElevator(floor, elevatorMap, elevator, sb)
    sb.append(LineTerminator)
  }

  private def addFloorDescription(floor: Int, sb: StringBuilder) = {
    sb.append(Separator)
    sb.append(s"Floor: $floor")
    (1 until longestFloorNumber).foreach(_ => sb.append(Space))
    sb.append(Separator)
  }

  private def addElevator(floor: Int, elevatorMap: Map[Int, ElevatorState], elevator: Int, sb: StringBuilder): Any = {
    val elevatorState = elevatorMap(elevator)
    if (elevatorState.floor == Floor(floor)) {
      sb.append(elevatorState.stateType.draw)
      sb.append(elevatorState.direction.draw)
      sb.append(elevatorState.nextFloorNumber.draw)
    } else {
      sb.append(emptyElevator)
    }
    sb.append(Separator)
  }

  private def longestFloorNumber = numberOfFloors.toString.length
}

object Drawer {
  private val ElevatorPrefixLength = 2
  private val Space = " "
  private val EmptyElevator = "-"
  private val Separator = '|'
  private val LineTerminator = '\n'

  sealed trait Command

  case class Draw(states: Iterable[ElevatorState]) extends Command

  def apply(numberOfElevators: Int, numberOfFloors: Int): Behavior[Command] = new Drawer(numberOfElevators, numberOfFloors).handleDrawing()
}
