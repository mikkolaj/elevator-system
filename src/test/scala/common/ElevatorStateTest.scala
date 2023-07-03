package org.example
package common

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ElevatorStateTest extends AnyWordSpec with Matchers {
  "Elevator state" should {
    val initialFloor = Floor(5)
    val emptyStopsRegistry = StopsRegistry(directionsToStopsMap(Nil, Nil))
    val mockId = 1

    "Report correct distance starting from Idle" in {
      val state = new ElevatorState(mockId, initialFloor, Down, Idle, emptyStopsRegistry)

      state.distanceTo(initialFloor, Down) shouldEqual 1
    }
    "Report correct distance starting from Moving" in {
      val state = new ElevatorState(mockId, initialFloor, Down, Moving, emptyStopsRegistry)

      state.distanceTo(initialFloor, Down) shouldEqual 1
    }
    "Report correct distance starting from TakingPassengers" in {
      val state = new ElevatorState(mockId, initialFloor, Down, TakingPassengers, emptyStopsRegistry)

      state.distanceTo(initialFloor, Down) shouldEqual 0
    }
    "Report correct distance to floor when floor is the only one in current direction" in {
      val desiredFloor = Floor(4)
      val state = new ElevatorState(mockId, initialFloor, Down, Idle, emptyStopsRegistry)

      state.distanceTo(desiredFloor, Down) shouldEqual 3
    }
    "Report correct distance to floor when floor is the only one in opposite direction" in {
      val desiredFloor = Floor(4)
      val state = new ElevatorState(mockId, initialFloor, Up, Idle, emptyStopsRegistry)

      state.distanceTo(desiredFloor, Down) shouldEqual 3
    }
    "Report correct distance to floor when floor is in the current direction and after registered current direction stops" in {
      val desiredFloor = Floor(2)
      val stopsRegistry = StopsRegistry(directionsToStopsMap(Nil, List(Floor(5), Floor(3))))
      val state = new ElevatorState(mockId, initialFloor, Down, Idle, stopsRegistry)

      state.distanceTo(desiredFloor, Down) shouldEqual 8
    }
    "Report correct distance to floor when floor is in the opposite direction and after registered current direction stops" in {
      val desiredFloor = Floor(2)
      val stopsRegistry = StopsRegistry(directionsToStopsMap(Nil, List(Floor(5), Floor(3))))
      val state = new ElevatorState(mockId, initialFloor, Down, Idle, stopsRegistry)

      state.distanceTo(desiredFloor, Up) shouldEqual 8
    }
    "Report correct distance to floor when floor is the same as the one when the direction was changed" in {
      val desiredFloor = Floor(3)
      val stopsRegistry = StopsRegistry(directionsToStopsMap(Nil, List(Floor(5), Floor(3))))
      val state = new ElevatorState(mockId, initialFloor, Down, Idle, stopsRegistry)

      state.distanceTo(desiredFloor, Up) shouldEqual 6
    }
    "Report correct distance to floor when floor is in the opposite direction and after both current and opposite direction registered stops" in {
      val desiredFloor = Floor(5)
      val stopsRegistry = StopsRegistry(directionsToStopsMap(List(Floor(2)), List(Floor(5), Floor(3))))
      val state = new ElevatorState(mockId, initialFloor, Down, Idle, stopsRegistry)

      state.distanceTo(desiredFloor, Up) shouldEqual 12
    }
    "Report correct distance to floor when floor is in the same direction and after both current and opposite direction registered stops" in {
      val desiredFloor = Floor(6)
      val stopsRegistry = StopsRegistry(directionsToStopsMap(List(Floor(2), Floor(7)), List(Floor(5), Floor(3))))
      val state = new ElevatorState(mockId, initialFloor, Down, Idle, stopsRegistry)

      state.distanceTo(desiredFloor, Down) shouldEqual 18
    }
  }

  private def directionsToStopsMap(upStops: List[Floor], downStops: List[Floor]): Map[MovementDirection, List[Floor]] = Map(
    Up -> upStops,
    Down -> downStops
  )
}
