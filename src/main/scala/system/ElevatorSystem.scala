package org.example
package system

import common.{ElevatorState, Floor, MovementDirection}
import drawing.Drawer
import elevator.Elevator
import elevator.Elevator.{AddStop, Command => ElevatorCommand, Step => ElevatorStep}
import system.ElevatorSystem._

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

class ElevatorSystem(numberOfElevators: Int, numberOfFloors: Int, enableDrawing: Boolean) {
  def init(): Behavior[Command] = Behaviors.setup { context =>
    val elevators = spawnElevators(context).toList
    val state = ElevatorSystemState(elevators, optionalDrawer(context))
    initialized(state)
  }

  private def spawnElevators(context: ActorContext[Command]) = {
    val elevatorStateAdapter = context.messageAdapter(rsp => ElevatorStateResponse(rsp))
    val observers = List(elevatorStateAdapter)

    for {
      elevatorId <- 0 until numberOfElevators
      floor = readElevatorPosition(elevatorId)
      elevator = Elevator(elevatorId, observers, floor)
    } yield context.spawn(elevator, s"elevator-$elevatorId")
  }

  private def optionalDrawer(context: ActorContext[Command]) = {
    if (enableDrawing) {
      Some(context.spawn(Drawer(numberOfElevators, numberOfFloors), "drawer"))
    } else {
      None
    }
  }

  def initialized(state: ElevatorSystemState): Behavior[Command] = Behaviors.withTimers { timers =>
    state.optionalDrawer.foreach(_ => timers.startTimerAtFixedRate(Draw, 1.second, 1.second))
    Behaviors.receiveMessage {
      case GetStates(replyTo)            => handleGetStates(state, replyTo)
      case Step                          => handleStep(state)
      case Draw                          => handleDrawing(state)
      case request: StopAt               => handleStopAt(state, request)
      case request: Pickup               => handlePickupRequest(state, request)
      case update: ElevatorStateResponse => updateElevatorRegistry(state, update)
    }
  }

  private def handleGetStates(state: ElevatorSystemState, replyTo: ActorRef[List[ElevatorState]]): Behavior[Command] = {
    replyTo ! state.elevatorStates.toList
    Behaviors.same
  }

  private def handleStep(state: ElevatorSystemState): Behavior[Command] = {
    state.elevators.foreach(elevator => elevator ! ElevatorStep)
    Behaviors.same
  }

  private def handleDrawing(state: ElevatorSystemState): Behavior[Command] = {
    state.optionalDrawer.foreach(drawer => drawer ! Drawer.Draw(state.elevatorStates))
    Behaviors.same
  }

  private def handleStopAt(state: ElevatorSystemState, request: StopAt): Behavior[Command] = {
    state.elevators(request.elevatorId) ! request.toAddStop
    Behaviors.same
  }

  private def handlePickupRequest(state: ElevatorSystemState, request: Pickup): Behavior[Command] = {
    val elevator = findBestElevator(state, request)
    elevator ! request.toAddStop
    Behaviors.same
  }

  private def findBestElevator(state: ElevatorSystemState, request: Pickup): ActorRef[ElevatorCommand] = {
    val bestElevatorId = state.elevatorStatesMap.minBy(_._2.distanceTo(request.stop, request.movementDirection))._1
    state.elevators(bestElevatorId)
  }

  private def updateElevatorRegistry(state: ElevatorSystemState, update: ElevatorStateResponse): Behavior[Command] = {
    val newState = state.copy(elevatorStatesMap = state.elevatorStatesMap + (update.state.elevatorId -> update.state))
    initialized(newState)
  }

  // normally we'd read it's state from some sensors
  private def readElevatorPosition(elevatorId: Int) = Floor(0)
}

object ElevatorSystem {
  val GetSatesTimeout: Timeout = 2.seconds

  sealed trait Command

  case class GetStates(replyTo: ActorRef[List[ElevatorState]]) extends Command

  case object Step extends Command

  case class StopAt(stop: Floor, elevatorId: Int) extends Command {
    def toAddStop: AddStop = AddStop(stop)
  }

  case class Pickup(stop: Floor, movementDirection: MovementDirection) extends Command {
    def toAddStop: AddStop = AddStop(stop, Some(movementDirection))
  }

  private case object Draw extends Command

  private case class ElevatorStateResponse(state: ElevatorState) extends Command

  private case class ElevatorSystemState(elevators: List[ActorRef[ElevatorCommand]], optionalDrawer: Option[ActorRef[Drawer.Command]] = None, elevatorStatesMap: Map[Int, ElevatorState] = Map.empty) {
    def elevatorStates: Iterable[ElevatorState] = elevatorStatesMap.values
  }

  def apply(numberOfElevators: Int, numberOfFloors: Int): Behavior[Command] = new ElevatorSystem(numberOfElevators, numberOfFloors, Config.EnableDrawing).init()
}
