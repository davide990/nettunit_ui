package nettunit

import javafx.event.ActionEvent
import javafx.fxml.FXML
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, ListCell, ListView, TextArea, TextField}
import scalafxml.core.macros.sfxml
import scalaj.http.{Http, HttpRequest}

import java.net.ConnectException
import javafx.util.Callback
import net.liftweb.json.{DefaultFormats, parse}

case class Person(firstName: String, lastName: String)

case class TaskDetail(taskID: String, taskName: String, processID: String, taskData: Map[String, Object])

class PersonCellFactory extends Callback[ListView[Person], ListCell[Person]] {
  override def call(listView: ListView[Person]): ListCell[Person] = new ListCell[Person]() {
    def updateItem(person: Person, empty: Boolean): Unit = {
      this.updateItem(person, empty)
      if (empty || person == null) this.setText(null)
      else this.setText(person.firstName + " " + person.lastName)
    }


  }
}

@sfxml
class UIController(private val taskTypeListView: ListView[String],
                   private val taskIDTextField: TextField,
                   private val processIDTextField: TextField,
                   private val MUSAAddressTextField: TextField,
                   private val MUSAPortTextField: TextField,
                   private val deployToFlowableButton: Button,
                   private val convertGoalSPECButton: Button,
                   private val GoalSPECTextArea: TextArea,
                   private val BPMNTextArea: TextArea,
                   private val activePlansTextArea: TextArea,
                   private val activeTasksTextArea: TextArea,
                   private val applyIncidentButton: Button,
                   private val completeTaskButton: Button,
                   private val emergencyTypeField: TextField,
                   private val failTaskButton: Button,
                   private val operatorNameField: TextField,
                   private val flowableAddressTextField: TextField,
                   private val flowablePortTextField: TextField,
                   private val planIDField: TextField,
                   private val updateViewQueryButton: Button) {

  val taskTypes = ObservableBuffer("safety_manager/send_team_to_evaluate")
  taskTypes += "plant_operator/activate_internal_security_plan"
  taskTypes += "commander_fire_brigade/fire_brigade_assessment"
  taskTypes += "prefect/declare_pre_alert_state"
  taskTypes += "ARPA/evaluate_fire_radiant_energy"
  taskTypes += "prefect/declare_alarm_state"
  taskTypeListView.items = taskTypes

  implicit val formats = DefaultFormats

  @FXML private[nettunit] def applyEmergencyPlan(event: ActionEvent): Unit = {
    val address = flowableAddressTextField.getText match {
      case ad if ad.isEmpty => flowableAddressTextField.getPromptText
      case _ => flowableAddressTextField.getText
    }
    val port = flowablePortTextField.getText match {
      case ad if ad.isEmpty => flowablePortTextField.getPromptText
      case _ => flowablePortTextField.getText
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
    val address = MUSAAddressTextField.getText match {
      case ad if ad.isEmpty => "localhost"
      case _ => MUSAAddressTextField.getText
    }
    val port = MUSAPortTextField.getText match {
      case ad if ad.isEmpty => "8081"
      case _ => MUSAPortTextField.getText
    }

    val resultApply = Http(s"http://${address}:${port}/Goal2BPMN")
      .header("Content-Type", "text/plain")
      .postData(goals)
      .asString

    BPMNTextArea.setText(resultApply.body)
  }

  @FXML private[nettunit] def onDeployProcessToFlowable(event: ActionEvent): Unit = {
    val address = MUSAAddressTextField.getText match {
      case ad if ad.isEmpty => "localhost"
      case _ => MUSAAddressTextField.getText
    }
    val port = MUSAPortTextField.getText match {
      case ad if ad.isEmpty => "8081"
      case _ => MUSAPortTextField.getText
    }
    val resultApply = Http(s"http://${address}:${port}/Deploy")
      .header("Content-Type", "text/xml")
      .postData(BPMNTextArea.getText)
      .asString

    new Alert(AlertType.Information, s"Result: ${resultApply.statusLine}").showAndWait()
  }

  @FXML private[nettunit] def completeTask(event: ActionEvent): Unit = {
    val address = flowableAddressTextField.getText match {
      case ad if ad.isEmpty => flowableAddressTextField.getPromptText
      case _ => flowableAddressTextField.getText
    }
    val port = flowablePortTextField.getText match {
      case ad if ad.isEmpty => flowablePortTextField.getPromptText
      case _ => flowablePortTextField.getText
    }

    if (taskTypeListView.getSelectionModel.getSelectedItems.isEmpty) {
      new Alert(AlertType.Information, "no task selected").showAndWait()
      return
    }

    val taskType = taskTypeListView.getSelectionModel.getSelectedItems.get(0)
    val taskID = taskIDTextField.getText

    val requestString = s"http://${address}:${port}/NETTUNIT/${taskType}/${taskID}"
    val resultApply = Http(requestString)
      .postData("")
      .asString

    new Alert(AlertType.Information, s"Result: ${resultApply.statusLine}").showAndWait()
  }

  @FXML private[nettunit] def failTask(event: ActionEvent): Unit = {
    println("fail task")
  }

  @FXML private[nettunit] def updateProcessQueryView(event: ActionEvent): Unit = {
    val address = flowableAddressTextField.getText match {
      case ad if ad.isEmpty => flowableAddressTextField.getPromptText
      case _ => flowableAddressTextField.getText
    }
    val port = flowablePortTextField.getText match {
      case ad if ad.isEmpty => flowablePortTextField.getPromptText
      case _ => flowablePortTextField.getText
    }

    try {
      val resultApply = Http(s"http://${address}:${port}/NETTUNIT/incident_list/")
        .method("GET")
        .asString
      activePlansTextArea.setText(resultApply.body)

      if (!processIDTextField.getText.isEmpty) {
        val resultApply2 = Http(s"http://${address}:${port}/NETTUNIT/task_list/${processIDTextField.getText}")
          .method("GET")
          .asString
        activeTasksTextArea.setText(resultApply2.body)
      }

    } catch {
      case _: ConnectException => new Alert(AlertType.Error, s"unable to connect. Please check if flowable is active").showAndWait()
    }

  }


}





