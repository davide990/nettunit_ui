package nettunit

import JixelAPIInterface.Login.ECOSUsers
import RabbitMQ.JixelEvent
import RabbitMQ.Launchers.Jixel.JixelClientTest.jixel
import RabbitMQ.Producer.JixelRabbitMQProducer
import RabbitMQ.Serializer.JixelEventJsonSerializer
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import net.liftweb.json.Extraction.decompose
import net.liftweb.json._
import scalafx.collections.ObservableBuffer
import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{Clipboard, ClipboardContent}
import scalafxml.core.macros.sfxml
import scalaj.http.{Http, HttpResponse}

import java.io.{ByteArrayInputStream, FileInputStream}
import java.net.{ConnectException, SocketTimeoutException}
import java.sql.Timestamp
import javax.imageio.ImageIO

case class ServiceTaskView(label: String, fullClassName: String)

case class UserTaskDetail(taskID: String, taskName: String, processID: String)

case class ProcessInstanceDetail(name: String, processInstanceID: String, processDefinitionName: String, processDefinitionVersion: Int)

@sfxml
class UIController(private val processStatusListView: ListView[UserTaskDetail],
                   private val flowableRadioButton: RadioButton,
                   private val activitiRadioButton: RadioButton,
                   private val activePlansListView: ListView[ProcessInstanceDetail],
                   private val activeTasksListView: ListView[UserTaskDetail],


                   private val submitServiceTaskFailureButton: Button,
                   private val mareImageView: ImageView,
                   private val jixelImageView: ImageView,
                   private val nettunitHautImageView: ImageView,

                   private val serviceTaskListView: ListView[ServiceTaskView],
                   private val actInstanceHITableView: TableView[FlowableActInstHistoricRecord],
                   private val actInstHi_idColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_revColumn: TableColumn[FlowableActInstHistoricRecord, Int],
                   private val actInstHi_proc_def_idColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_act_idColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_act_nameColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_act_typeColumn: TableColumn[FlowableActInstHistoricRecord, String],
                   private val actInstHi_start_timeColumn: TableColumn[FlowableActInstHistoricRecord, Option[Timestamp]],
                   private val actInstHi_end_timeColumn: TableColumn[FlowableActInstHistoricRecord, Option[Timestamp]],
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
                   private val taskInstHi_end_timeColumn: TableColumn[FlowableTaskInstHistoricRecord, Option[Timestamp]],
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
                   private val processInstHi_end_timeColumn: TableColumn[FlowableProcessInstanceHistoricRecord, Option[Timestamp]],
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

                   private val processIDTextField: TextField,
                   private val MUSAAddressTextField: TextField,
                   private val MUSAPortTextField: TextField,
                   private val deployToFlowableButton: Button,
                   private val convertGoalSPECButton: Button,
                   private val GoalSPECTextArea: TextArea,
                   private val BPMNTextArea: TextArea,

                   private val applyIncidentButton: Button,
                   private val completeTaskButton: Button,
                   private val emergencyTypeField: TextField,
                   private val failTaskButton: Button,
                   private val operatorNameField: TextField,
                   private val flowableAddressTextField: TextField,
                   private val flowablePortTextField: TextField,
                   private val planIDField: TextField) {

  //necessary for parsing json
  implicit val formats = DefaultFormats

  val serviceTasks = ObservableBuffer(ServiceTaskView("do_crossborder_communication", "nettunit.handler.do_crossborder_communication"))
  serviceTasks += ServiceTaskView("ensure_presence_of_qualified_personnel", "nettunit.handler.ensure_presence_of_qualified_personnel")
  serviceTasks += ServiceTaskView("ensure_presence_of_representative", "nettunit.handler.ensure_presence_of_representative")
  serviceTasks += ServiceTaskView("inform_technical_rescue_organisation_alert", "nettunit.handler.inform_technical_rescue_organisation_alert")
  serviceTasks += ServiceTaskView("inform_technical_rescue_organisation_internal_plan", "nettunit.handler.inform_technical_rescue_organisation_internal_plan")
  serviceTasks += ServiceTaskView("keep_update_involved_personnel", "nettunit.handler.keep_update_involved_personnel")
  serviceTasks += ServiceTaskView("notify_competent_body_internal_plan", "nettunit.handler.notify_competent_body_internal_plan")
  serviceTasks += ServiceTaskView("prepare_tech_report", "nettunit.handler.prepare_tech_report")
  serviceTaskListView.items = serviceTasks

  activePlansListView.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[ProcessInstanceDetail] {
    override def changed(observableValue: ObservableValue[_ <: ProcessInstanceDetail], oldValue: ProcessInstanceDetail, newValue: ProcessInstanceDetail): Unit = {
      updateTaskListButtonClick(null)
      updateCompletedTasks(null)
    }
  })

  processStatusListView.cellFactory = {
    a: ListView[UserTaskDetail] => {
      val cell = new ListCell[UserTaskDetail]
      cell.item.onChange { (a, b, newValue) => {
        if (newValue != null) {
          cell.text = newValue.taskName
          if (failingTaskName.isDefined) {
            if (failingTaskName.get.label == newValue) {
              cell.style = ".list-cell {\n    -fx-text-fill: white; /* 5 */\n    -fx-background-radius: 4 4 4 4;\n    -fx-border-radius: 2 2 2 2;\n    -fx-background-color: darkred;\n}"
            }
          }
          //cell.style = ".list-cell {\n    -fx-text-fill: black; /* 5 */\n    -fx-background-radius: 4 4 4 4;\n    -fx-border-radius: 2 2 2 2;\n    -fx-background-color: #FFFFBF;\n}"
        } else {
          cell.text = null
          cell.style = null
        }
      }
      }
      cell
    }
  }

  private val SUBMIT_SERVICE_TASK_RESTORE = "Restore activity"
  private val SUBMIT_SERVICE_TASK_FAIL = "Submit failure request"

  serviceTaskListView.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[ServiceTaskView] {
    override def changed(observableValue: ObservableValue[_ <: ServiceTaskView], oldValue: ServiceTaskView, newValue: ServiceTaskView): Unit = {
      setFailActivityButtonStatus(newValue)
    }
  })

  serviceTaskListView.cellFactory = {
    a: ListView[ServiceTaskView] => {
      val cell = new ListCell[ServiceTaskView]
      cell.item.onChange { (a, b, newValue) => {
        if (newValue != null) {
          cell.text = newValue.label
        } else {
          cell.text = null
        }
      }
      }
      cell
    }
  }

  activeTasksListView.cellFactory = {
    p: ListView[UserTaskDetail] => {
      val cell = new ListCell[UserTaskDetail]
      cell.item.onChange { (_, _, str) =>
        if (str != null) {
          cell.text = s"${str.taskName} [${str.taskID}]"
        } else {
          cell.text = null
        }
      }
      cell
    }
  }

  activePlansListView.cellFactory = {
    p: ListView[ProcessInstanceDetail] => {
      val cell = new ListCell[ProcessInstanceDetail]
      cell.item.onChange { (_, _, str) =>
        if (str != null) {
          cell.text = s"${str.processDefinitionName} [version: ${str.processDefinitionVersion}; ID: ${str.processInstanceID}]"
        } else {
          cell.text = null
          cell.style = null
        }
      }
      cell
    }
  }

  private def setFailActivityButtonStatus(selectedView: ServiceTaskView): Unit = {
    if (failingTaskName.isDefined) {
      if (selectedView.label == failingTaskName.get.label) {
        submitServiceTaskFailureButton.setText(SUBMIT_SERVICE_TASK_RESTORE)
        submitServiceTaskFailureButton.setStyle("-fx-background-color: darkgreen;-fx-text-fill: white;")
        return
      }
    }
    submitServiceTaskFailureButton.setText(SUBMIT_SERVICE_TASK_FAIL)
    submitServiceTaskFailureButton.setStyle("-fx-background-color: darkred;-fx-text-fill: white;")
  }

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

  //val processImage = new Image(new FileInputStream(getClass.getResource("/process.png").getFile))
  val cooperationTransfrontaliereImage = new Image(new FileInputStream(getClass.getResource("/banners/nettunitHaut.png").getFile))
  nettunitHautImageView.setImage(cooperationTransfrontaliereImage)
  mareImageView.setImage(new Image(new FileInputStream(getClass.getResource("/banners/slide-1.1.png").getFile)))
  jixelImageView.setImage(new Image(new FileInputStream(getClass.getResource("/jixel.png").getFile)))
  nettunitImageView.setImage(new Image(new FileInputStream(getClass.getResource("/nettunit.png").getFile)))

  val acceptIconFile = getClass.getResource("/icons/accept.png").getFile
  val acceptHumanIconFile = getClass.getResource("/icons/settings.png").getFile
  val pendingIconFile = getClass.getResource("/icons/pending.png").getFile
  val warningIconFile = getClass.getResource("/icons/warning.png").getFile

  val acceptIcon = new Image(new FileInputStream(acceptIconFile))
  val acceptHumanIcon = new Image(new FileInputStream(acceptHumanIconFile))
  val pendingIcon = new Image(new FileInputStream(pendingIconFile))
  val warningIcon = new Image(new FileInputStream(warningIconFile))

  var failingTaskName: Option[ServiceTaskView] = None

  //String containing the BPMN string
  var flowableReadyBPMNString = ""
  //String containing the BPMN string modified to be as graphical bpmn into eclipse with flowable plugin editor
  var eclipseBPMNEditorReadyBPMNString = ""

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
    } catch {
      case _: ConnectException => new Alert(AlertType.Error, s"unable to connect. Please check if flowable is active").showAndWait()
    }


  }

  @FXML private[nettunit] def onConvertGoalsToBPMN(event: ActionEvent): Unit = {
    val goals = new String(GoalSPECTextArea.getText.getBytes(), "UTF-8")
    val connectionString = s"http://${getMUSAAddress()}:${getMUSAAddressPort()}/Goal2BPMN"
    val resultApply = Http(connectionString)
      .header("Content-Type", "text/plain")
      .postData(goals)
      .asString

    flowableReadyBPMNString = resultApply.body
    eclipseBPMNEditorReadyBPMNString = resultApply.body
    eclipseBPMNEditorReadyBPMNString = eclipseBPMNEditorReadyBPMNString.replace("flowable:executionListener", "activiti:executionListener")
    eclipseBPMNEditorReadyBPMNString = eclipseBPMNEditorReadyBPMNString.replace("flowable:class", "activiti:class")

    flowableRadioButton.isSelected match {
      case true => BPMNTextArea.setText(flowableReadyBPMNString)
      case false => BPMNTextArea.setText(eclipseBPMNEditorReadyBPMNString)
    }

    println("ok")
  }

  def getUserTaskList(goals: String): Unit = {
    val connectionString2 = s"http://${getMUSAAddress()}:${getMUSAAddressPort()}/UserTasks"
    val resultApply2 = Http(connectionString2)
      .header("Content-Type", "text/plain")
      .postData(goals)
      .asString
  }

  def getServiceTaskList(goals: String): Unit = {
    val connectionString2 = s"http://${getMUSAAddress()}:${getMUSAAddressPort()}/ServiceTasks"
    val resultApply2 = Http(connectionString2)
      .header("Content-Type", "text/plain")
      .postData(goals)
      .asString
  }

  @FXML private[nettunit] def onDeployProcessToFlowable(event: ActionEvent): Unit = {
    val connectionString = s"http://${getMUSAAddress()}:${getMUSAAddressPort()}/Deploy"
    val resultApply = Http(connectionString)
      .header("Content-Type", "text/xml")
      .postData(BPMNTextArea.getText)
      .asString
    new Alert(AlertType.Information, s"Result: $resultApply.statusLine").showAndWait()
  }

  @FXML private[nettunit] def completeTask(event: ActionEvent): Unit = {
    if (activeTasksListView.getSelectionModel.getSelectedItems.isEmpty) {
      new Alert(AlertType.Information, "no task selected").showAndWait()
      return
    }
    val selectedTask = activeTasksListView.getSelectionModel.getSelectedItems.get(0)
    val taskEndPoint = getNETTUNITCapabilityMatching(selectedTask.taskName)
    val taskID = selectedTask.taskID
    println(s"request complete task for ID [$taskID]")
    val requestString = s"http://${getFlowableAddress()}:${getFlowableAddressPort()}/NETTUNIT/$taskEndPoint/$taskID"
    try {
      val resultApply = Http(requestString).postData("").asString
      new Alert(AlertType.Information, s"Result: ${
        resultApply.statusLine
      }").showAndWait()
    } catch {
      case _: SocketTimeoutException => new Alert(AlertType.Error, s"Socket timeout").showAndWait()
    }

    updateTaskListButtonClick(null)
    updateCompletedTasks(null)
  }

  def updateCompletedTasks(event: ActionEvent): Unit = {
    if (activePlansListView.getSelectionModel.getSelectedItems.isEmpty) {
      return
    }
    val selectedProcess = activePlansListView.getSelectionModel.getSelectedItems.get(0).processInstanceID
    val requestStringUpdate = s"http://${getFlowableAddress()}:${getFlowableAddressPort()}/NETTUNIT/completed_tasks/$selectedProcess"

    try {
      val resultApply = Http(requestStringUpdate).method("GET").asString
      val ll = parse(resultApply.body)
      processStatusListView.getItems.clear()
      val childrenName = (ll \\ "taskName").children
      val childrenID = (ll \\ "taskID").children
      val childrenProcessID = (ll \\ "processID").children
      for (i <- 0 until childrenName.size) {
        val taskName = childrenName(i).asInstanceOf[JString].s
        val taskID = childrenID(i).asInstanceOf[JString].s
        val processID = childrenProcessID(i).asInstanceOf[JString].s
        val ut = UserTaskDetail(taskID, taskName, processID)
        processStatusListView.getItems.add(ut)
      }
    } catch {
      case _: SocketTimeoutException => new Alert(AlertType.Error, s"Socket timeout").showAndWait()
    }

    updateDiagram(null)
  }

  @FXML private[nettunit] def flowableRadioButtonCheck(event: ActionEvent): Unit = {
    flowableRadioButton.setSelected(true)
    activitiRadioButton.setSelected(false)
    deployToFlowableButton.setDisable(false)
    BPMNTextArea.setText(flowableReadyBPMNString)
  }

  @FXML private[nettunit] def activityRadioButtonCheck(event: ActionEvent): Unit = {
    flowableRadioButton.setSelected(false)
    activitiRadioButton.setSelected(true)
    BPMNTextArea.setText(eclipseBPMNEditorReadyBPMNString)
    deployToFlowableButton.setDisable(true)

  }

  @FXML private[nettunit] def failTask(event: ActionEvent): Unit = {
    println("fail task")
  }

  @FXML private[nettunit] def updateTaskListButtonClick(event: ActionEvent): Unit = {
    if (activePlansListView.getSelectionModel.getSelectedItems.isEmpty) {
      return
    }
    activeTasksListView.getItems.clear()
    val selectedProcess = activePlansListView.getSelectionModel.getSelectedItems.get(0).processInstanceID
    if (!selectedProcess.isEmpty) {
      val resultApply2 = Http(s"http://${getFlowableAddress()}:${getFlowableAddressPort()}/NETTUNIT/task_list/${selectedProcess}").method("GET").asString
      val ll = parse(resultApply2.body)

      val childrenName = (ll \\ "taskName").children
      val childrenID = (ll \\ "taskID").children
      val childrenProcessID = (ll \\ "processID").children
      for (i <- 0 until childrenName.size) {
        val taskName = childrenName(i).asInstanceOf[JString].s
        val taskID = childrenID(i).asInstanceOf[JString].s
        val processID = childrenProcessID(i).asInstanceOf[JString].s
        val ut = UserTaskDetail(taskID, taskName, processID)
        activeTasksListView.getItems.add(ut)
      }
    }
  }

  def getNETTUNITCapabilityMatching(capName: String): String = capName match {
    case "Send team to evaluate" => "safety_manager/send_team_to_evaluate"
    case "Activate internal plan" => "plant_operator/activate_internal_security_plan"
    case "Decide response type" => "commander_fire_brigade/decide_response_type"
    case "Declare pre-alert state" => "prefect/declare_pre_alert_state"
    case "Evaluate fire radiant energy" => "ARPA/evaluate_fire_radiant_energy"
    case "Declare alarm state" => "prefect/declare_alarm_state"
  }

  @FXML private[nettunit] def updateProcessListButtonClick(event: ActionEvent): Unit = {
    val request = s"http://${getFlowableAddress()}:${getFlowableAddressPort()}/NETTUNIT/incident_list_new/"
    val resultApply = Http(request).method("GET").asString
    val ll = parse(resultApply.body)
    processStatusListView.getItems.clear()
    activePlansListView.getItems.clear()
    val childrenProcessName = (ll \\ "name").children
    val childrenProcessInstanceID = (ll \\ "processInstanceID").children
    val childrenProcessDefName = (ll \\ "processDefinitionName").children
    val childrenProcessDefVersion = (ll \\ "processDefinitionVersion").children

    if (childrenProcessDefName.isEmpty) {
      processImageView.setImage(null)
    }

    for (i <- 0 until childrenProcessName.size) {
      val processName = childrenProcessName(i).asInstanceOf[JString].s
      val processInstanceID = childrenProcessInstanceID(i).asInstanceOf[JString].s
      val processDefName = childrenProcessDefName(i).asInstanceOf[JString].s
      val processDefVersion = childrenProcessDefVersion(i).asInstanceOf[JInt].num

      val details = ProcessInstanceDetail(processName, processInstanceID, processDefName, processDefVersion.intValue)
      activePlansListView.getItems.add(details)
    }
  }

  private def getFlowableAddressPort(): String = flowablePortTextField.getText match {
    case ad if ad.isEmpty => flowablePortTextField.getPromptText
    case _ => flowablePortTextField.getText
  }

  private def getFlowableAddress(): String = flowableAddressTextField.getText match {
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
    val event_3760 = "{\n  \"id\": 3760,\n  \"description\": \"descrizione\",\n  \"casualties\": null,\n  \"caller_name\": null,\n  \"caller_phone\": null,\n  \"incident_interface_fire\": false,\n  \"incident_distance\": null,\n  \"incident_id\": null,\n  \"headline\": \"oggetto\",\n  \"date\": \"2022-10-26T09:48:21+0200\",\n  \"completable\": false,\n  \"public\": false,\n  \"incident_subtype\": null,\n  \"incident_severity\": {\n    \"id\": 4,\n    \"description\": \"Minor\",\n    \"_locale\": \"en-GB\",\n    \"cap_description_value\": \"Minor\"\n  },  \n  \"incident_urgency\": {\n    \"id\": 3,\n    \"description\": \"Past\",\n    \"_locale\": \"en-GB\",\n    \"cap_description_value\": \"Past\"\n  },\n  \"incident_status\": {\n    \"id\": 1,\n    \"description\": \"Reported\",\n    \"_locale\": \"en-GB\",\n    \"description_name\": \"Event status\",\n    \"description_value\": \"Reported\",\n    \"description_category\": \"Event\",\n    \"additional_parameters\": []\n  },\n  \"incident_type\": {\n    \"id\": 100,\n    \"description\": \"Alluvione\",\n    \"color\": null,\n    \"icon\": null,\n    \"app_enabled\": false,\n    \"order\": null,\n    \"deleted\": null,\n    \"default_value\": true,\n    \"category\": \"Met\",\n    \"interoperability_incident_type_id\": 2,\n    \"post_emergency\": 0,\n    \"_locale\": \"en-GB\",\n    \"description_name\": \"Event type\",\n    \"description_value\": \"Alluvione\",\n    \"description_category\": \"Event\",\n    \"additional_parameters\": [],\n    \"subtypes\": \"\",\n    \"cap_type_description\": \"Flood\"\n  },  \n  \"incident_msgtype\": {\n    \"id\": 5,\n    \"enum_value\": \"Pre-operational\",\n    \"_locale\": \"en-GB\",\n    \"description_name\": \"Event communication type\",\n    \"description_value\": \"Pre-operational\",\n    \"description_category\": \"Event\",\n    \"additional_parameters\": [],\n    \"cap_description_value\": null\n  },\n  \"controllable_object\": {\n    \"id\": 75389,\n    \"created\": \"2022-10-26T09:48:21+0200\",\n    \"modified\": \"2022-10-26T09:48:21+0200\",\n    \"organisation_id\": 2242,\n    \"controllable_object_type_id\": 1,\n    \"last_activity_entry_id\": 16311,\n    \"check_code\": \"d422c98670a3cbf55b04077fad191b95\",\n    \"emergency_scenario_id\": null,\n    \"emergency_scenario_activity\": 0,\n    \"create_user_id\": 9,\n    \"edit_user_id\": 9,\n    \"edit_organisation_id\": 2242,\n    \"deleted\": null,\n    \"locations\": [\n      {\n        \"id\": 57242,\n        \"description\": \"Via Provinciale, Monreale, Palermo, Sicilia\",\n        \"geotype\": \"marker\",\n        \"coordinates\": \"13.22522 38.04631\",\n        \"is_strategic_positions\": null,\n        \"code\": null,\n        \"controllable_object_id\": 75389,\n        \"feature_collection\": null,\n        \"road\": \"Via Provinciale\",\n        \"town\": \"Monreale\",\n        \"county\": \"Palermo\",\n        \"postcode\": \"90046\",\n        \"state\": \"Sicilia\",\n        \"country\": \"\",\n        \"loggable_object_id\": 23903,\n        \"coordinates_ne\": \"38.04631 13.22522\",\n        \"latitude\": \"38.04630972668314\",\n        \"longitude\": \"13.22521524877168\"\n      }\n    ],\n    \"additional_parameter_value\": [],\n    \"creator_with_organisation\": \"Giovanni Francesco Luigi Catania (IES Solutions SRL)\",\n    \"additional_parameters\": [],\n    \"additional_parameters_for_list\": [],\n    \"attachment_file_names\": {\n      \"d67e1dc241ae67d9963e6214cddc233a\": \"Scheda Evento Emer2.pdf\"\n    },\n    \"attachment_url_accesses\": {\n      \"\": \"Scheda Evento Emer2.pdf\"\n    },\n    \"creator_organisation\": \"IES - IES Solutions SRL\"\n  },\n  \"weblink\": \"https://lambda.dev.ies.solutions/incidents/view/3760\",\n  \"recipients\": [],\n  \"updates\": 0,\n  \"entity_type\": \"Event\",\n  \"entity_description\": \"3760 - oggetto\",\n  \"instructions\": null\n}"
    val parsedJixelEvent = JixelEventJsonSerializer.fromJson(event_3760).asInstanceOf[JixelEvent]

    processStatusListView.getItems.clear()

    val response = jixel.notifyEvent(parsedJixelEvent)
    new Alert(AlertType.Information, s"MUSA Response: ${response}").showAndWait()

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
    val serviceTask = serviceTaskListView.getSelectionModel.getSelectedItems.get(0)

    failingTaskName.isDefined match {
      case true =>
        if (serviceTask.label == failingTaskName.get.label) {
          submitUndoServiceTaskFailure(serviceTask)
          failingTaskName = None
          new Alert(AlertType.Information, s"Submitted failure request for task: ${serviceTask.label}").showAndWait()
        } else {
          //undo previous task failure request
          submitUndoServiceTaskFailure(failingTaskName.get)
          //request a new failure for selected task
          submitServiceTaskFailure(serviceTask)
          new Alert(AlertType.Information, s"Submitted failure request for task: ${serviceTask.label}").showAndWait()
        }
      case false =>
        val result = submitServiceTaskFailure(serviceTask)
        new Alert(AlertType.Information, s"Submitted failure request for task: ${serviceTask.label}").showAndWait()
    }
    serviceTaskListView.refresh()
    setFailActivityButtonStatus(serviceTask)
  }

  def submitUndoServiceTaskFailure(service: ServiceTaskView): Unit = {
    //First tell to flowable
    failingTaskName = Some(service)
    val connectionURL = s"http://${getFlowableAddress()}:${getFlowableAddressPort()}/NETTUNIT/undo_fail/${service.fullClassName}"
    Http(connectionURL)
      .header("Content-Type", "text/xml")
      .postData("").asString

    //Then to musa, so that next plan definition can include the restored capability
    val connectionString = s"http://${getMUSAAddress()}:${getMUSAAddressPort()}/RestoreCapability"
    Http(connectionString)
      .header("Content-Type", "text/plain")
      .postData(service.fullClassName).asString
  }

  def submitServiceTaskFailure(service: ServiceTaskView): HttpResponse[String] = {
    failingTaskName = Some(service)
    val connectionURL = s"http://${getFlowableAddress()}:${getFlowableAddressPort()}/NETTUNIT/fail/${service.fullClassName}"
    Http(connectionURL)
      .header("Content-Type", "text/xml")
      .postData("")
      .asString
  }

  def isFailingTask(task: String): Boolean = {
    if (!failingTaskName.isDefined) {
      return false
    }
    failingTaskName.get match {
      case task => true
      case _ => false
    }
  }

  /**/

  @FXML private[nettunit] def clearProcessDefButtonClick(event: ActionEvent): Unit = {
    // Create and show confirmation alert
    val alert = new Alert(AlertType.Confirmation) {
      title = "Confirmation Dialog"
      headerText = "Delete all previous process definitions."
      contentText = "Are you ok with this?"
    }

    val result = alert.showAndWait()

    // React to user's selectioon
    result match {
      case Some(ButtonType.OK) => {
        val connectionURL = s"http://${getFlowableAddress()}:${getFlowableAddressPort()}/NETTUNIT/clearDeployments/"
        val resultApply = Http(connectionURL)
          .header("Content-Type", "text/xml")
          .postData("")
          .asString

        new Alert(AlertType.Information, s"All done.").showAndWait()
      }
      case _ =>
    }


  }

  @FXML private[nettunit] def updateDiagram(event: ActionEvent): Unit = {
    if (activePlansListView.getSelectionModel.getSelectedItems.isEmpty) {
      return
    }
    val selectedProcess = activePlansListView.getSelectionModel.getSelectedItems.get(0)

    val requestStringUpdate = s"http://${getFlowableAddress()}:${getFlowableAddressPort()}/NETTUNIT/get_diagram/"
    val processInstanceDetailJSON = prettyRender(decompose(selectedProcess))

    try {
      val resultApply = Http(requestStringUpdate)
        .header("Content-Type", "application/json")
        .postData(processInstanceDetailJSON).asBytes
      val is = new ByteArrayInputStream(resultApply.body)
      val bi = ImageIO.read(is)
      val im = SwingFXUtils.toFXImage(bi, null)
      processImageView.setFitWidth(im.getWidth)
      processImageView.setFitHeight(im.getHeight)
      processImageView.setImage(im)


    } catch {
      case _: SocketTimeoutException => new Alert(AlertType.Error, s"Socket timeout").showAndWait()
    }
  }

  @FXML private[nettunit] def copyProcessToClipboardButtonClick(event: ActionEvent): Unit = {
    val clipboard = Clipboard.systemClipboard
    val content = new ClipboardContent
    content.putString(BPMNTextArea.getText)
    clipboard.setContent(content)
  }

  @FXML private[nettunit] def onRemoveProcessInstanceButtonClick(event: ActionEvent): Unit = {
    if (activePlansListView.getSelectionModel.getSelectedItems.isEmpty) {
      new Alert(AlertType.Information, "No plan selected").showAndWait()
      return
    }

    // Create and show confirmation alert
    val alert = new Alert(AlertType.Confirmation) {
      title = "Confirmation Dialog"
      headerText = "Delete the selected process instance?"
      contentText = "Are you ok with this?"
    }

    val result = alert.showAndWait()

    // React to user's selectioon
    result match {
      case Some(ButtonType.OK) => {
        val selectedProcess = activePlansListView.getSelectionModel.getSelectedItems.get(0).processInstanceID
        val connectionURL = s"http://${getFlowableAddress()}:${getFlowableAddressPort()}/NETTUNIT/removeProcessInstance/$selectedProcess"
        val resultApply = Http(connectionURL)
          .header("Content-Type", "text/xml")
          .postData("")
          .asString

        new Alert(AlertType.Information, s"All done.").showAndWait()
      }
      case _ =>
    }
  }


}





