package nettunit

import javafx.event.ActionEvent
import javafx.fxml.FXML
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, ListView, TextField}
import scalafxml.core.macros.sfxml
import scalaj.http.Http

import java.net.ConnectException

@sfxml
class UIController(private val activePlansListQuery: ListView[String],
                   private val activePlansListSimulate: ListView[String],
                   private val activeTasksListQuery: ListView[String],
                   private val activeTasksListSimulate: ListView[String],
                   private val applyIncidentButton: Button,
                   private val completeTaskButton: Button,
                   private val emergencyTypeField: TextField,
                   private val failTaskButton: Button,
                   private val operatorNameField: TextField,
                   private val planIDField: TextField,
                   private val updateSimulateViewButton: Button,
                   private val updateViewQueryButton: Button) {
  /*@FXML var activePlansListQuery: ListView[String] = _
  @FXML var activePlansListSimulate: ListView[String] = _
  @FXML var activeTasksListQuery: ListView[String] = _
  @FXML var activeTasksListSimulate: Button = _
  @FXML var applyIncidentButton: Button = _
  @FXML var completeTaskButton: Button = _
  @FXML var emergencyTypeField: TextField = _
  @FXML var failTaskButton: Button = _
  @FXML var operatorNameField: TextField = _
  @FXML var planIDField: TextField = _
  @FXML var updateSimulateViewButton: Button = _
  @FXML var updateViewQueryButton: Button = _*/

  @FXML private[nettunit] def applyEmergencyPlan(event: ActionEvent): Unit = {

    val body = s"{\n  \"emergencyPlanID\":\"${planIDField.getText}\",\n  \"empName\":\"${operatorNameField.getText}\",\n  \"requestDescription\":\"${emergencyTypeField.getText}\"\n}"

    try {
      val resultApply = Http(s"http://localhost:8080/NETTUNIT/incident/apply")
        .postData(body)
        .header("Content-Type", "application/json").asString
    } catch {
      case _ : ConnectException => new Alert(AlertType.Error, s"unable to connect. Please check if flowable is active").showAndWait()
      case _ => new Alert(AlertType.Information, s"Success").showAndWait()
    }



    //Caused by: java.net.ConnectException: Connection refused (Connection refused)


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





