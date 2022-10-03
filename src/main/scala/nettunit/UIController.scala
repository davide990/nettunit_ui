package nettunit

import javafx.event.ActionEvent
import javafx.fxml.FXML
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, ListView, TextArea, TextField}
import scalafxml.core.macros.sfxml
import scalaj.http.{Http, HttpRequest}

import java.net.ConnectException

@sfxml
class UIController(private val flowableAddressTextField: TextField,
                   private val flowablePortTextField: TextField,
                   private val deployToFlowableButton: Button,
                   private val convertGoalSPECButton: Button,
                   private val GoalSPECTextArea: TextArea,
                   private val BPMNTextArea: TextArea,
                   private val activePlansListQuery: ListView[String],
                   private val activePlansListSimulate: ListView[String],
                   private val activeTasksListQuery: ListView[String],
                   private val activeTasksListSimulate: ListView[String],
                   private val applyIncidentButton: Button,
                   private val completeTaskButton: Button,
                   private val emergencyTypeField: TextField,
                   private val failTaskButton: Button,
                   private val operatorNameField: TextField,
                   private val nettunitAddressTextField: TextField,
                   private val nettunitPortTextField: TextField,
                   private val planIDField: TextField,
                   private val updateSimulateViewButton: Button,
                   private val updateViewQueryButton: Button) {

  @FXML private[nettunit] def applyEmergencyPlan(event: ActionEvent): Unit = {
    val address = nettunitAddressTextField.getText match {
      case ad if ad.isEmpty => nettunitAddressTextField.getPromptText
      case _ => nettunitAddressTextField.getText
    }
    val port = nettunitPortTextField.getText match {
      case ad if ad.isEmpty => nettunitPortTextField.getPromptText
      case _ => nettunitPortTextField.getText
    }
    val body = s"{\n  \"emergencyPlanID\":\"${planIDField.getText}\",\n  \"empName\":\"${operatorNameField.getText}\",\n  \"requestDescription\":\"${emergencyTypeField.getText}\"\n}"

    try {
      val resultApply = Http(s"http://${address}:${port}/NETTUNIT/incident/apply")
        .postData(body)
        .header("Content-Type", "application/json").asString
      new Alert(AlertType.Information, s"Success").showAndWait()
    } catch {
      case _: ConnectException => new Alert(AlertType.Error, s"unable to connect. Please check if flowable is active").showAndWait()
    }


  }

  @FXML private[nettunit] def onConvertGoalsToBPMN(event: ActionEvent): Unit = {
    val goals = new String(GoalSPECTextArea.getText.getBytes(), "UTF-8")
    val address = flowableAddressTextField.getText match {
      case ad if ad.isEmpty => "localhost"
      case _ => flowableAddressTextField.getText
    }
    val port = flowablePortTextField.getText match {
      case ad if ad.isEmpty => "8081"
      case _ => flowablePortTextField.getText
    }

    val resultApply = Http(s"http://${address}:${port}/Goal2BPMN")
      .header("Content-Type", "text/plain")
      .postData(goals)
      .asString

    BPMNTextArea.setText(resultApply.body)
  }

  @FXML private[nettunit] def onDeployProcessToFlowable(event: ActionEvent): Unit = {
    val address = flowableAddressTextField.getText match {
      case ad if ad.isEmpty => "localhost"
      case _ => flowableAddressTextField.getText
    }
    val port = flowablePortTextField.getText match {
      case ad if ad.isEmpty => "8081"
      case _ => flowablePortTextField.getText
    }
    val resultApply = Http(s"http://${address}:${port}/Deploy")
      .header("Content-Type", "text/xml")
      .postData(BPMNTextArea.getText)
      .asString

    new Alert(AlertType.Error, s"Result: ${resultApply.statusLine}").showAndWait()
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





