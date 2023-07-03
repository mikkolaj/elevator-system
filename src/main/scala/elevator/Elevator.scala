package org.example
package elevator

import common._
import elevator.Elevator._

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

class Elevator(id: Int, stateObservers: List[ActorRef[ElevatorState]]) {
  def idle(pathManager: PathManager): Behavior[Command] = Behaviors.setup { _ =>
    notifyObservers(pathManager, Idle)
    Behaviors.receiveMessage {
      case stop: AddStop     => idle(registerStopInManager(pathManager, stop))
      case Step              => startMovingOrStopAtThisFloor(pathManager)
      case GetState(replyTo) => handleGetState(replyTo, pathManager, Idle)
    }
  }

  def takingPassengers(pathManager: PathManager): Behavior[Command] = Behaviors.setup { _ =>
    notifyObservers(pathManager, TakingPassengers)
    Behaviors.receiveMessage {
      case stop@AddStop(floor, _) => handleStopRequestDuringStop(pathManager, floor, registerStopInManager(pathManager, stop))
      case Step                   => moveOrBecomeIdle(pathManager)
      case GetState(replyTo)      => handleGetState(replyTo, pathManager, TakingPassengers)
    }
  }

  private def handleStopRequestDuringStop(pathManager: PathManager, requestedFloor: Floor, newPathManager: => PathManager): Behavior[Command] = {
    if (pathManager.currentFloor != requestedFloor) {
      takingPassengers(newPathManager)
    } else {
      Behaviors.same
    }
  }

  def moving(pathManager: PathManager): Behavior[Command] = Behaviors.setup { _ =>
    notifyObservers(pathManager, Moving)
    Behaviors.receiveMessage {
      case stop: AddStop     => moving(registerStopInManager(pathManager, stop))
      case Step              => moveOrStopAtThisFloor(pathManager)
      case GetState(replyTo) => handleGetState(replyTo, pathManager, Moving)
    }
  }

  private def registerStopInManager(pathManager: PathManager, addStop: AddStop) = addStop match {
    case AddStop(floor, Some(direction)) => pathManager.addStop(floor, direction)
    case AddStop(floor, _)               => pathManager.addStop(floor)
  }


  private def moveOrBecomeIdle(pathManager: PathManager): Behavior[Command] = {
    if (pathManager.hasNextStops) {
      moving(pathManager.updateDirection())
    } else {
      idle(pathManager)
    }
  }

  private def startMovingOrStopAtThisFloor(pathManager: PathManager): Behavior[Command] = {
    handleStopping(pathManager)
      .getOrElse(moving(pathManager.updateDirection()))
  }

  private def moveOrStopAtThisFloor(pathManager: PathManager): Behavior[Command] = {
    handleStopping(pathManager)
      .getOrElse(moving(pathManager.moveFloor()))
  }

  private def handleStopping(pathManager: PathManager): Option[Behavior[Command]] = {
    if (!pathManager.hasNextStops) {
      Some(idle(pathManager))
    } else if (pathManager.shouldStopAtThisFloor) {
      Some(takingPassengers(pathManager.stopAtCurrentFloor()))
    } else {
      None
    }
  }

  private def handleGetState(replyTo: ActorRef[ElevatorState], pathManager: PathManager, stateType: StateType): Behavior[Command] = {
    val response = ElevatorState(id, pathManager, stateType)
    replyTo ! response
    Behaviors.same
  }

  private def notifyObservers(pathManager: PathManager, stateType: StateType): Unit = {
    val newState = ElevatorState(id, pathManager, stateType)
    stateObservers.foreach(observer => observer ! newState)
  }

}

object Elevator {
  val StateSwitchingDelay = 1

  sealed trait Command

  case object Step extends Command

  case class AddStop(floor: Floor, direction: Option[MovementDirection] = None) extends Command

  case class GetState(replyTo: ActorRef[ElevatorState]) extends Command

  def apply(id: Int, stateObservers: List[ActorRef[ElevatorState]], currentFloor: Floor, currentDirection: MovementDirection = Up): Behavior[Command] = {
    new Elevator(id, stateObservers).idle(PathManager(currentFloor, currentDirection))
  }
}