package nettunit

import javafx.event.ActionEvent
import javafx.fxml.FXML
import scalafxml.core.macros.sfxml

@sfxml
class UIController {
  @FXML private val activePlansListQuery = null
  @FXML private val activePlansListSimulate = null
  @FXML private val activeTasksListQuery = null
  @FXML private val activeTasksListSimulate = null
  @FXML private val applyIncidentButton = null
  @FXML private val completeTaskButton = null
  @FXML private val emergencyTypeField = null
  @FXML private val failTaskButton = null
  @FXML private val operatorNameField = null
  @FXML private val planIDField = null
  @FXML private val updateSimulateViewButton = null
  @FXML private val updateViewQueryButton = null

  @FXML private[nettunit] def applyEmergencyPlan(event: ActionEvent): Unit = {
    println("apply plan")
  }

  @FXML private[nettunit] def completeTask(event: ActionEvent): Unit = {
    println("complete task")
  }

  @FXML private[nettunit] def failTask(event: ActionEvent): Unit = {
    println("fail task")
  }

  @FXML private[nettunit] def updateProcessQueryView(event: ActionEvent): Unit = {
    println("update view query")
  }

  @FXML private[nettunit] def updateSimulatedTaskView(event: ActionEvent): Unit = {
    println("update view simulate")
  }
}





