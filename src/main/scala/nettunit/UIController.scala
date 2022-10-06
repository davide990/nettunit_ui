package nettunit

import JixelAPIInterface.JixelInterface
import JixelAPIInterface.Login.ECOSUsers
import RabbitMQ.Launchers.Jixel.JixelClientTest.jixel
import RabbitMQ.Producer.JixelRabbitMQProducer
import Utils.JixelUtil
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import net.liftweb.json.DefaultFormats
import scalafx.application.Platform
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafxml.core.macros.sfxml
import scalaj.http.Http

import java.io.{File, FileInputStream}
import java.net.ConnectException
import java.sql.Timestamp
import java.util.{Date, Timer}
import scala.io.Source

@sfxml
class UIController(private val serviceTaskListView: ListView[String],
                   private val actInstanceHITableView: TableView[FlowableActInstHistoricRecord],
                   private val actInstHi_idColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_revColumn: TableColumn[FlowableActInstHistoricRecord, Int],
                   private val actInstHi_proc_def_idColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_act_idColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_act_nameColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_act_typeColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_start_timeColumn: TableColumn[FlowableActInstHistoricRecord, Timestamp],
                   private val actInstHi_end_timeColumn: TableColumn[FlowableActInstHistoricRecord, Timestamp],
                   private val actInstHi_durationColumn: TableColumn[FlowableActInstHistoricRecord, Int],
                   private val actInstHi_delete_reasonColumn: TableColumn[FlowableActInstHistoricRecord, String],

                   private val taskInstanceHITableView: TableView[FlowableTaskInstHistoricRecord],
                   private val taskInstHi_idColumn: TableColumn[FlowableTaskInstHistoricRecord, String],
                   private val taskInstHi_revColumn: TableColumn[FlowableTaskInstHistoricRecord, Int],
                   private val taskInstHi_proc_def_idColumn: TableColumn[FlowableTaskInstHistoricRecord, String],
                   private val taskInstHi_task_def_keyColumn: TableColumn[FlowableTaskInstHistoricRecord, String],
                   private val taskInstHi_proc_inst_idColumn: TableColumn[FlowableTaskInstHistoricRecord, String],
                   private val taskInstHi_nameColumn: TableColumn[FlowableTaskInstHistoricRecord, String],
                   private val taskInstHi_start_timeColumn: TableColumn[FlowableTaskInstHistoricRecord, Timestamp],
                   private val taskInstHi_end_timeColumn: TableColumn[FlowableTaskInstHistoricRecord, Timestamp],
                   private val taskInstHi_durationColumn: TableColumn[FlowableTaskInstHistoricRecord, Int],
                   private val taskInstHi_delete_reasonColumn: TableColumn[FlowableTaskInstHistoricRecord, String],
                   private val taskInstHi_priorityColumn: TableColumn[FlowableTaskInstHistoricRecord, Int],
                   private val taskInstHi_last_updated_timeColumn: TableColumn[FlowableTaskInstHistoricRecord, Timestamp],

                   private val processInstanceHITableView: TableView[FlowableProcessInstanceHistoricRecord],
                   private val processInstHi_idColumn: TableColumn[FlowableProcessInstanceHistoricRecord, String],
                   private val processInstHi_revColumn: TableColumn[FlowableProcessInstanceHistoricRecord, Int],
                   private val processInstHi_proc_inst_idColumn: TableColumn[FlowableProcessInstanceHistoricRecord, String],
                   private val processInstHi_proc_def_idColumn: TableColumn[FlowableProcessInstanceHistoricRecord, String],
                   private val processInstHi_start_timeColumn: TableColumn[FlowableProcessInstanceHistoricRecord, Timestamp],
                   private val processInstHi_end_timeColumn: TableColumn[FlowableProcessInstanceHistoricRecord, Timestamp],
                   private val processInstHi_durationColumn: TableColumn[FlowableProcessInstanceHistoricRecord, Int],
                   private val processInstHi_start_user_idColumn: TableColumn[FlowableProcessInstanceHistoricRecord, String],
                   private val processInstHi_start_act_idColumn: TableColumn[FlowableProcessInstanceHistoricRecord, String],
                   private val processInstHi_end_act_idColumn: TableColumn[FlowableProcessInstanceHistoricRecord, String],
                   private val processInstHi_delete_reasonColumn: TableColumn[FlowableProcessInstanceHistoricRecord, String],

                   private val processDefTableView: TableView[FlowableProcessDefRecord],
                   private val processDef_IDColumn: TableColumn[FlowableProcessDefRecord, String],
                   private val processDef_RevColumn: TableColumn[FlowableProcessDefRecord, Int],
                   private val processDef_Name: TableColumn[FlowableProcessDefRecord, String],
                   private val processDef_Key: TableColumn[FlowableProcessDefRecord, String],
                   private val processDef_Version: TableColumn[FlowableProcessDefRecord, String],
                   private val processDef_DeploymentID: TableColumn[FlowableProcessDefRecord, String],
                   private val processDef_TenantID: TableColumn[FlowableProcessDefRecord, String],
                   private val nettunitImageView: ImageView,
                   private val processImageView: ImageView,
                   private val sendJixelEventButton: Button,
                   private val planImageView: ImageView,
                   private val taskTypeListView: ListView[String],
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
  taskTypes += "commander_fire_brigade/decide_response_type"
  taskTypes += "prefect/declare_pre_alert_state"
  taskTypes += "ARPA/evaluate_fire_radiant_energy"
  taskTypes += "prefect/declare_alarm_state"
  taskTypeListView.items = taskTypes

  val serviceTasks = ObservableBuffer("do_crossborder_communication")
  serviceTasks += "ensure_presence_of_qualified_personnel"
  serviceTasks += "ensure_presence_of_representative"
  serviceTasks += "inform_technical_rescue_organisation_alert"
  serviceTasks += "inform_technical_rescue_organisation_internal_plan"
  serviceTasks += "keep_update_involved_personnel"
  serviceTasks += "notify_competent_body_internal_plan"
  serviceTasks += "prepare_tech_report"
  serviceTaskListView.items = serviceTasks

  processDef_IDColumn.cellValueFactory = _.value.id
  processDef_RevColumn.cellValueFactory = _.value.rev
  processDef_Name.cellValueFactory = _.value.name
  processDef_Key.cellValueFactory = _.value.key
  processDef_Version.cellValueFactory = _.value.version
  processDef_DeploymentID.cellValueFactory = _.value.deployment_id
  processDef_TenantID.cellValueFactory = _.value.tenant_id

  processInstHi_idColumn.cellValueFactory = _.value.id
  processInstHi_revColumn.cellValueFactory = _.value.rev
  processInstHi_proc_inst_idColumn.cellValueFactory = _.value.proc_inst_id
  processInstHi_proc_def_idColumn.cellValueFactory = _.value.proc_def_id
  processInstHi_start_timeColumn.cellValueFactory = _.value.start_time
  processInstHi_end_timeColumn.cellValueFactory = _.value.end_time
  processInstHi_durationColumn.cellValueFactory = _.value.duration
  processInstHi_start_user_idColumn.cellValueFactory = _.value.start_user_id
  processInstHi_start_act_idColumn.cellValueFactory = _.value.start_act_id
  processInstHi_end_act_idColumn.cellValueFactory = _.value.end_act_id
  processInstHi_delete_reasonColumn.cellValueFactory = _.value.delete_reason

  taskInstHi_idColumn.cellValueFactory = _.value.id
  taskInstHi_revColumn.cellValueFactory = _.value.rev
  taskInstHi_proc_def_idColumn.cellValueFactory = _.value.proc_def_id
  taskInstHi_task_def_keyColumn.cellValueFactory = _.value.task_def_key
  taskInstHi_proc_inst_idColumn.cellValueFactory = _.value.proc_inst_id
  taskInstHi_nameColumn.cellValueFactory = _.value.name
  taskInstHi_start_timeColumn.cellValueFactory = _.value.start_time
  taskInstHi_end_timeColumn.cellValueFactory = _.value.end_time
  taskInstHi_durationColumn.cellValueFactory = _.value.duration
  taskInstHi_delete_reasonColumn.cellValueFactory = _.value.delete_reason
  taskInstHi_priorityColumn.cellValueFactory = _.value.priority
  taskInstHi_last_updated_timeColumn.cellValueFactory = _.value.last_updated_time

  actInstHi_idColumn.cellValueFactory = _.value.id
  actInstHi_revColumn.cellValueFactory = _.value.rev
  actInstHi_proc_def_idColumn.cellValueFactory = _.value.proc_def_id
  actInstHi_act_idColumn.cellValueFactory = _.value.act_id
  actInstHi_act_nameColumn.cellValueFactory = _.value.act_name
  actInstHi_act_typeColumn.cellValueFactory = _.value.act_type
  actInstHi_start_timeColumn.cellValueFactory = _.value.start_time
  actInstHi_end_timeColumn.cellValueFactory = _.value.end_time
  actInstHi_durationColumn.cellValueFactory = _.value.duration
  actInstHi_delete_reasonColumn.cellValueFactory = _.value.delete_reason

  val processStatusIdle = getClass.getResource("/infographic-1.png").getFile
  val processsSendTeamIdle = getClass.getResource("/infographic-2.png").getFile
  val processActivateInternalPlanIdle = getClass.getResource("/infographic-3.png").getFile
  val processDecideResponsePlanIdle = getClass.getResource("/infographic-4.png").getFile
  val processDeclarePreAlertIdle = getClass.getResource("/infographic-5.png").getFile
  val processEvaluateFireRadiantEnIdle = getClass.getResource("/infographic-6.png").getFile
  val processDeclareAlarmIdle = getClass.getResource("/infographic-7.png").getFile
  val processComplete = getClass.getResource("/infographic-8.png").getFile

  nettunitImageView.setImage(new Image(new FileInputStream(getClass.getResource("/nettunit.png").getFile), 500, 50, false, true))

  val login = ECOSUsers.davide_login


  private def imageFromResource(name: String) =
    new ImageView(new Image(getClass.getClassLoader.getResourceAsStream(name)))

  @FXML private[nettunit] def applyEmergencyPlan(event: ActionEvent): Unit = {
    val address = flowableAddressTextField.getText match {
      case ad if ad.isEmpty => flowableAddressTextField.getPromptText
      case _ => flowableAddressTextField.getText
    }
    val port = flowablePortTextField.getText match {
      case ad if ad.isEmpty => flowablePortTextField.getPromptText
      case _ => flowablePortTextField.getText
    }

    val body = s"{\n  \"emergencyPlanID\":\"$planIDField.getText\",\n  \"empName\":\"$operatorNameField.getText\",\n  \"requestDescription\":\"$emergencyTypeField.getText\"\n}"

    try {
      val resultApply = Http(s"http://$address:$port/NETTUNIT/incident/apply")
        .postData(body)
        .header("Content-Type", "application/json").asString
      new Alert(AlertType.Information, s"Success").showAndWait()
      processImageView.setImage(new Image(new FileInputStream(processsSendTeamIdle)))
    } catch {
      case _: ConnectException => new Alert(AlertType.Error, s"unable to connect. Please check if flowable is active").showAndWait()
    }


  }

  @FXML private[nettunit] def onConvertGoalsToBPMN(event: ActionEvent): Unit = {
    val goals = new String(GoalSPECTextArea.getText.getBytes(), "UTF-8")
    val resultApply = Http(s"http://$getMUSAAddress():$getMUSAAddressPort()/Goal2BPMN")
      .header("Content-Type", "text/plain")
      .postData(goals)
      .asString
    BPMNTextArea.setText(resultApply.body)
  }

  @FXML private[nettunit] def onDeployProcessToFlowable(event: ActionEvent): Unit = {
    val resultApply = Http(s"http://$getMUSAAddress():$getMUSAAddressPort()/Deploy")
      .header("Content-Type", "text/xml")
      .postData(BPMNTextArea.getText)
      .asString
    new Alert(AlertType.Information, s"Result: $resultApply.statusLine").showAndWait()
  }

  @FXML private[nettunit] def completeTask(event: ActionEvent): Unit = {
    if (taskTypeListView.getSelectionModel.getSelectedItems.isEmpty) {
      new Alert(AlertType.Information, "no task selected").showAndWait()
      return
    }

    val taskType = taskTypeListView.getSelectionModel.getSelectedItems.get(0)
    val taskID = taskIDTextField.getText
    val requestString = s"http://$getFlowableAddress():$getFlowableAddressPort()/NETTUNIT/$taskType/$taskID"
    val resultApply = Http(requestString).postData("").asString
    new Alert(AlertType.Information, s"Result: ${
      resultApply.statusLine
    }").showAndWait()

    updateProcessImageView()
  }

  @FXML private[nettunit] def failTask(event: ActionEvent): Unit = {
    println("fail task")
  }

  @FXML private[nettunit] def updateProcessQueryView(event: ActionEvent): Unit = {
    try {
      val resultApply = Http(s"http://$getFlowableAddress():$getFlowableAddressPort()/NETTUNIT/incident_list/").method("GET").asString
      activePlansTextArea.setText(resultApply.body)

      if (activePlansTextArea.getText == "[]") {
        processImageView.setImage(new Image(new FileInputStream(processStatusIdle)))
      }

      if (!processIDTextField.getText.isEmpty) {
        val resultApply2 = Http(s"http://$getFlowableAddress():$getFlowableAddressPort()/NETTUNIT/task_list/${processIDTextField.getText}").method("GET").asString
        activeTasksTextArea.setText(resultApply2.body)
      }
    } catch {
      case _: ConnectException => new Alert(AlertType.Error, s"unable to connect. Please check if flowable is active").showAndWait()
    }
  }

  private def getFlowableAddress(): String = flowablePortTextField.getText match {
    case ad if ad.isEmpty => flowablePortTextField.getPromptText
    case _ => flowablePortTextField.getText
  }

  private def getFlowableAddressPort(): String = flowableAddressTextField.getText match {
    case ad if ad.isEmpty => flowableAddressTextField.getPromptText
    case _ => flowableAddressTextField.getText
  }

  private def getMUSAAddress(): String = MUSAAddressTextField.getText match {
    case ad if ad.isEmpty => MUSAAddressTextField.getPromptText
    case _ => MUSAAddressTextField.getText
  }

  private def getMUSAAddressPort(): String = MUSAPortTextField.getText match {
    case ad if ad.isEmpty => MUSAPortTextField.getPromptText
    case _ => MUSAPortTextField.getText
  }

  @FXML private[nettunit] def onSendJixelEventButtonClick(event: ActionEvent): Unit = {
    jixel = new JixelRabbitMQProducer
    val jixelUser = JixelInterface.parseToJixelCredential(JixelInterface.connect(login))
    val ev = JixelUtil.eventFromEventSummary(login, Utils.JixelUtil.getAnyJixelEvent(login));
    val response = jixel.notifyEvent(ev)
    new Alert(AlertType.Information, s"MUSA Responde: ${response}").showAndWait()
    processImageView.setImage(new Image(new FileInputStream(processsSendTeamIdle)))
  }

  @FXML private[nettunit] def processDefUpdateButtonClick(event: ActionEvent): Unit = {
    val processDefList = FlowableDBQuery.findAllProcessDef()
    val toAdd = processDefList.filter(pd => !processDefTableView.getItems.contains(pd))
    toAdd.foreach(pd => processDefTableView.getItems.add(pd))
  }

  @FXML private[nettunit] def processInstanceHIButtonClick(event: ActionEvent): Unit = {
    val processInstanceHIList = FlowableDBQuery.findAllProcessInstancesHistoric()
    val toAdd = processInstanceHIList.filter(pd => !processInstanceHITableView.getItems.contains(pd))
    toAdd.foreach(pd => processInstanceHITableView.getItems.add(pd))
  }

  @FXML private[nettunit] def taskInstanceHIButtonClick(event: ActionEvent): Unit = {
    val taskList = FlowableDBQuery.findAllTaskInstHistoric()
    val toAdd = taskList.filter(pd => !taskInstanceHITableView.getItems.contains(pd))
    toAdd.foreach(pd => taskInstanceHITableView.getItems.add(pd))
  }

  @FXML private[nettunit] def actInstanceHIButtonClick(event: ActionEvent): Unit = {
    val actList = FlowableDBQuery.findAllActivitiesInstHistoric()
    val toAdd = actList.filter(pd => !actInstanceHITableView.getItems.contains(pd))
    toAdd.foreach(pd => actInstanceHITableView.getItems.add(pd))
  }

  @FXML private[nettunit] def submitServiceTaskFailureButtonClick(event: ActionEvent): Unit = {

    print("ok")
  }

  private def updateProcessImageView() = taskTypeListView.getSelectionModel.getSelectedItems.get(0) match {
    case "safety_manager/send_team_to_evaluate" => processImageView.setImage(new Image(new FileInputStream(processActivateInternalPlanIdle)))
    case "plant_operator/activate_internal_security_plan" => processImageView.setImage(new Image(new FileInputStream(processDecideResponsePlanIdle)))
    case "commander_fire_brigade/decide_response_type" => processImageView.setImage(new Image(new FileInputStream(processDeclarePreAlertIdle)))
    case "prefect/declare_pre_alert_state" => processImageView.setImage(new Image(new FileInputStream(processEvaluateFireRadiantEnIdle)))
    case "ARPA/evaluate_fire_radiant_energy" => processImageView.setImage(new Image(new FileInputStream(processDeclareAlarmIdle)))
    case "prefect/declare_alarm_state" => processImageView.setImage(new Image(new FileInputStream(processComplete)))
  }


}





