package org.example

import Config.{BindingAddress, BindingPort, NumberOfElevators, NumberOfFloors}
import api.ElevatorSystemResources
import system.ElevatorSystem

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    ActorSystem(Main(), "Main")
  }

  def apply(): Behavior[NotUsed] = Behaviors.setup { context =>
    implicit val system: ActorSystem[Nothing] = context.system
    implicit val elevatorSystem: ActorRef[ElevatorSystem.Command] = context.spawn(ElevatorSystem(NumberOfElevators, NumberOfFloors), "elevator-system")
    implicit val ec: ExecutionContextExecutor = system.executionContext

    val bindingFuture = Http().newServerAt(BindingAddress, BindingPort).bind(new ElevatorSystemResources(system, elevatorSystem).routes)
    bindingFuture.onComplete {
      case Success(binding)   => println(s"Successfully bound at: ${binding.localAddress}")
      case Failure(exception) =>
        println(s"Failed to bind. Terminating: $exception")
        system.terminate()
    }
    Behaviors.receiveSignal { case (_, Terminated(_)) => Behaviors.stopped }
  }
}