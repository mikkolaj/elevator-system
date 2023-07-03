package org.example
package elevator

import org.example.common.{Floor, MovementDirection, Up}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PathManagerTest extends AnyWordSpec with Matchers {
  val initialDirection: MovementDirection = Up
  val initialFloor: Floor = Floor(0)

  "Path manager's hasNextStops function" should {
    "Return false when there are no stops" in {
      val manager = PathManager(initialFloor, initialDirection)

      manager.hasNextStops shouldEqual false
    }

    "Return true when there are stops in current direction" in {
      val manager = PathManager(initialFloor, initialDirection)

      val updatedManager = manager.addStop(initialFloor)

      updatedManager.hasNextStops shouldEqual true
    }

    "Return true when there are stops in opposite direction" in {
      val manager = PathManager(initialFloor, initialDirection)

      val updatedManager = manager.addStop(initialFloor - 1)

      updatedManager.hasNextStops shouldEqual true
    }
  }

  "Path manager's shouldStopAtThisFloor function" should {

    "Return true after a stop with the same floor number as the current floor has been added" in {
      val manager = PathManager(initialFloor, initialDirection)

      val updatedManager = manager.addStop(initialFloor)

      updatedManager.shouldStopAtThisFloor shouldEqual true
    }

    "Return false when the current floor is not in the stops list" in {
      val manager = PathManager(initialFloor, initialDirection)

      manager.shouldStopAtThisFloor shouldEqual false
    }
  }

  "Path manager" should {

    "Should not move when there are no targets" in {
      val manager = PathManager(initialFloor, initialDirection)

      val updatedManager = manager.moveFloor()

      updatedManager.currentFloor shouldEqual initialFloor
    }

    "Stop at a floor after it reaches it with moveFloor" in {
      val manager = PathManager(initialFloor, initialDirection)

      val updatedManager = manager.addStop(initialFloor + 1).moveFloor()

      updatedManager.shouldStopAtThisFloor shouldEqual true
    }

    "Not swap directions when there are still stops in the current direction" in {
      val manager = PathManager(initialFloor, initialDirection)

      val updatedManager = manager.addStop(initialFloor).updateDirection()

      updatedManager.currentDirection shouldEqual initialDirection
    }

    "Swap directions when there are only stops in the opposite direction" in {
      val manager = PathManager(initialFloor, initialDirection)

      val updatedManager = manager.addStop(initialFloor - 1).updateDirection()

      updatedManager.currentDirection shouldEqual initialDirection.opposite
    }

    "Register a stop only once" in {
      val manager = PathManager(initialFloor, initialDirection)

      val updatedManager = manager.addStop(initialFloor).addStop(initialFloor).stopAtCurrentFloor()

      updatedManager.hasNextStops shouldEqual false
    }

    "Register stops correctly when they are added in order" in {
      val manager = PathManager(initialFloor, initialDirection)

      val managerAfterAddingStops = manager.addStop(initialFloor).addStop(initialFloor + 1)

      managerAfterAddingStops.shouldStopAtThisFloor shouldEqual true

      val updatedManager = managerAfterAddingStops.stopAtCurrentFloor().moveFloor()
      updatedManager.shouldStopAtThisFloor shouldEqual true
    }

    "Register stops correctly when they are out of order" in {
      val manager = PathManager(initialFloor, initialDirection)

      val managerAfterAddingStops = manager.addStop(initialFloor + 1).addStop(initialFloor)

      managerAfterAddingStops.shouldStopAtThisFloor shouldEqual true

      val updatedManager = managerAfterAddingStops.stopAtCurrentFloor().moveFloor()
      updatedManager.shouldStopAtThisFloor shouldEqual true
    }
  }
}
