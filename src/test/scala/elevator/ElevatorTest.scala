package org.example
package elevator

import elevator.Elevator._

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.example.common.{ElevatorState, Floor, Idle, MovementDirection, Moving, TakingPassengers, Up}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ElevatorTest extends AnyWordSpec with Matchers with BeforeAndAfterAll {
  val testKit: ActorTestKit = ActorTestKit()
  val mockId = 0
  val initialFloor: Floor = Floor(0)
  val initialMovementDirection: MovementDirection = Up

  "Idle elevator" should {

    "Be Idle after initialization" in {
      val elevator = testKit.spawn(Elevator(mockId, List.empty, initialFloor))
      val probe = testKit.createTestProbe[ElevatorState]()

      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual Idle
      stateResponse.floor shouldEqual initialFloor
    }

    "Remain Idle when no stops were added" in {
      val stateWithNoStops = PathManager(initialFloor, initialMovementDirection)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).idle(stateWithNoStops))
      val probe = testKit.createTestProbe[ElevatorState]()

      elevator ! Step
      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual Idle
      stateResponse.floor shouldEqual initialFloor
    }

    "Switch to Moving when a stop on some other floor was added" in {
      val targetFloor = Floor(1)
      val stateWithStopOnUpperFloor = PathManager(initialFloor, initialMovementDirection).addStop(targetFloor)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).idle(stateWithStopOnUpperFloor))
      val probe = testKit.createTestProbe[ElevatorState]()

      elevator ! Step
      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual Moving
      stateResponse.direction shouldEqual initialMovementDirection
      stateResponse.floor shouldEqual initialFloor
    }

    "Switch to Moving and update direction when the only registered stop is in the opposite direction" in {
      val targetFloor = Floor(-1)
      val stateWithStopOnLowerFloor = PathManager(initialFloor, initialMovementDirection).addStop(targetFloor)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).idle(stateWithStopOnLowerFloor))
      val probe = testKit.createTestProbe[ElevatorState]()

      elevator ! Step
      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual Moving
      stateResponse.direction shouldEqual initialMovementDirection.opposite
      stateResponse.floor shouldEqual initialFloor
    }

    "Switch to TakingPassengers when a stop on the same floor was added" in {
      val stateWithStopOnTheSameFloor = PathManager(initialFloor, initialMovementDirection).addStop(initialFloor)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).idle(stateWithStopOnTheSameFloor))
      val probe = testKit.createTestProbe[ElevatorState]()

      elevator ! Step
      elevator ! GetState(probe.ref)

      val stateResponse = probe.receiveMessage()
      stateResponse.stateType shouldEqual TakingPassengers
      stateResponse.direction shouldEqual initialMovementDirection
      stateResponse.floor shouldEqual initialFloor
    }
  }

  "Moving elevator" should {
    val probe = testKit.createTestProbe[ElevatorState]()

    "Move one floor in direction of the next stop and remain in Moving state" in {
      val nextFloor = initialFloor + 1
      val targetFloor = nextFloor + 1
      val stateWithStop = PathManager(initialFloor, initialMovementDirection).addStop(targetFloor)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).moving(stateWithStop))

      elevator ! Step
      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual Moving
      stateResponse.direction shouldEqual initialMovementDirection
      stateResponse.floor shouldEqual nextFloor
    }

    "Switch to TakingPassengers when the elevator should stop at this floor" in {
      val initialFloor = Floor(0)
      val stateWithStopAtTheSameFloor = PathManager(initialFloor, initialMovementDirection).addStop(initialFloor)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).moving(stateWithStopAtTheSameFloor))

      elevator ! Step
      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual TakingPassengers
      stateResponse.direction shouldEqual initialMovementDirection
      stateResponse.floor shouldEqual initialFloor
      stateResponse.nextFloorNumber shouldEqual None
    }
    "Switch to TakingPassengers and with opposite direction when next stop is in the opposite direction" in {
      val initialFloor = Floor(0)
      val stateWithStopAtTheSameFloor = PathManager(initialFloor, initialMovementDirection).addStop(initialFloor, initialMovementDirection.opposite)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).moving(stateWithStopAtTheSameFloor))

      elevator ! Step
      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual TakingPassengers
      stateResponse.direction shouldEqual initialMovementDirection.opposite
      stateResponse.floor shouldEqual initialFloor
      stateResponse.nextFloorNumber shouldEqual None
    }
  }

  "Elevator taking passengers" should {
    val probe = testKit.createTestProbe[ElevatorState]()

    "Switch to Idle when no new stops are present" in {
      val stateWithoutStops = PathManager(initialFloor, initialMovementDirection)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).takingPassengers(stateWithoutStops))

      elevator ! Step
      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual Idle
      stateResponse.direction shouldEqual initialMovementDirection
      stateResponse.floor shouldEqual initialFloor
    }

    "Switch to Moving when the elevator has stops in the current direction" in {
      val stateWithAddedStop = PathManager(initialFloor, initialMovementDirection).addStop(initialFloor + 1)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).takingPassengers(stateWithAddedStop))

      elevator ! Step
      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual Moving
      stateResponse.direction shouldEqual initialMovementDirection
      stateResponse.floor shouldEqual initialFloor
    }

    "Switch to Moving and update direction when the elevator has stops only in the opposite direction" in {
      val stateWithStopInOppositeDirection = PathManager(initialFloor, initialMovementDirection).addStop(initialFloor - 1)
      val elevator = testKit.spawn(new Elevator(mockId, List.empty).takingPassengers(stateWithStopInOppositeDirection))

      elevator ! Step
      elevator ! GetState(probe.ref)
      val stateResponse = probe.receiveMessage()

      stateResponse.stateType shouldEqual Moving
      stateResponse.direction shouldEqual initialMovementDirection.opposite
      stateResponse.floor shouldEqual initialFloor
    }
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()
}
