import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.Tab
import javafx.scene.control.TextField


class UIController {
  @FXML private val activeEmgPlanQuery = null
  @FXML private val activeEmgPlanQueryactiveEmgPlanAct = null
  @FXML private val activeTasksQuery = null
  @FXML private val activeTasksQueryactiveTasksAct = null
  @FXML private val applyNewEmergencyButton = null
  @FXML private val applyPlanID = null
  @FXML private val completeTask = null
  @FXML private val emergencyType = null
  @FXML private val failTask = null
  @FXML private val operatorName = null
  @FXML private val updateAct = null
  @FXML private val updateQuery = null

  @FXML def applyEmergencyPlan(event: ActionEvent): Unit = {
  }

  @FXML def doCompleteTask(event: ActionEvent): Unit = {
  }

  @FXML def doFailTask(event: ActionEvent): Unit = {
  }

  @FXML def doUpdateAct(event: ActionEvent): Unit = {
  }

  @FXML def doUpdateQuery(event: ActionEvent): Unit = {
  }
}

