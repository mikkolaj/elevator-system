package org.example
package api

import elevator.dtos
import system.ElevatorSystem
import system.dtos.{PickupDto, StopAtDto}

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

class ElevatorSystemResources(system: ActorSystem[Nothing], elevatorSystem: ActorRef[ElevatorSystem.Command]) {
  implicit val scheduler: Scheduler = system.scheduler

  private def elevatorRoutes: Route = pathPrefix("elevator" / IntNumber) { elevatorId =>
    path("stopAt") {
      post {
        entity(as[StopAtDto]) { entity =>
          elevatorSystem ! entity.toStopAt(elevatorId)
          complete(202, "Accepted")
        }
      }
    }
  }

  private def systemRoutes = pathPrefix("system") {
    path("states") {
      get {
        implicit val timeout: Timeout = ElevatorSystem.GetSatesTimeout
        val futureResponse = elevatorSystem.ask(ref => ElevatorSystem.GetStates(ref))
        onSuccess(futureResponse) { response =>
          complete(dtos.ElevatorStatesDto(response.map(_.toDto)))
        }
      }
    } ~ path("step") {
      post {
        elevatorSystem ! ElevatorSystem.Step
        complete(202, "Accepted")
      }
    } ~ path("pickup") {
      post {
        entity(as[PickupDto]) { entity =>
          elevatorSystem ! entity.toPickup
          complete(202, "Accepted")
        }
      }
    }
  }

  def routes: Route = concat(
    elevatorRoutes,
    systemRoutes
  )
}
