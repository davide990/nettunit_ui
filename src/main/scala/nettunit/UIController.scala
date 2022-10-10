package nettunit

import JixelAPIInterface.JixelInterface
import JixelAPIInterface.Login.ECOSUsers
import RabbitMQ.Launchers.Jixel.JixelClientTest.jixel
import RabbitMQ.Producer.JixelRabbitMQProducer
import Utils.JixelUtil
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import net.liftweb.json.DefaultFormats
import scalafx.application.Platform
import scalafx.beans.property.{ReadOnlyStringWrapper, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.shape.Circle
import scalafxml.core.macros.sfxml
import scalaj.http.{Http, HttpResponse}

import java.io.{File, FileInputStream}
import java.net.{ConnectException, SocketTimeoutException}
import java.sql.Timestamp
import java.util.{Date, Timer}
import scala.io.Source

case class ServiceTaskView(view: String, fullClassName: String)

@sfxml
class UIController(private val submitServiceTaskFailureButton: Button,
                   private val mareImageView: ImageView,
                   private val jixelImageView: ImageView,
                   private val nettunitHautImageView: ImageView,

                   private val do_crossborder_communication_circle: Circle,
                   private val ensure_presence_of_qualified_personnel_circle: Circle,
                   private val ensure_presence_of_representative_circle: Circle,
                   private val inform_technical_rescue_organisation_alert_circle: Circle,
                   private val inform_technical_rescue_organisation_internal_plan_circle: Circle,
                   private val keep_update_involved_personnel_circle: Circle,
                   private val notify_competent_body_internal_plan_circle: Circle,
                   private val prepare_tech_report_circle: Circle,

                   private val sendTeamImage: ImageView,
                   private val activateInternalPlanImage: ImageView,
                   private val InformRescueInternalPlanImage: ImageView,
                   private val decideResponseTypeImage: ImageView,
                   private val prepareReportImage: ImageView,
                   private val keepUpdateImage: ImageView,
                   private val declarePreAlertImage: ImageView,
                   private val informRescueAlertImage: ImageView,
                   private val evaluateFireRadiantImage: ImageView,
                   private val declareAlarmImage: ImageView,
                   private val notifyCompetentBodiesImage: ImageView,
                   private val ensurePresenceImage: ImageView,
                   private val doCrossBorderImage: ImageView,
                   private val ensureQualifiedPersonnelImage: ImageView,
                   private val adaptationTaskImage: ImageView,

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

  val serviceTasks = ObservableBuffer(ServiceTaskView("do_crossborder_communication", "nettunit.handler.do_crossborder_communication"))
  serviceTasks += ServiceTaskView("ensure_presence_of_qualified_personnel", "nettunit.handler.ensure_presence_of_qualified_personnel")
  serviceTasks += ServiceTaskView("ensure_presence_of_representative", "nettunit.handler.ensure_presence_of_representative")
  serviceTasks += ServiceTaskView("inform_technical_rescue_organisation_alert", "nettunit.handler.inform_technical_rescue_organisation_alert")
  serviceTasks += ServiceTaskView("inform_technical_rescue_organisation_internal_plan", "nettunit.handler.inform_technical_rescue_organisation_internal_plan")
  serviceTasks += ServiceTaskView("keep_update_involved_personnel", "nettunit.handler.keep_update_involved_personnel")
  serviceTasks += ServiceTaskView("notify_competent_body_internal_plan", "nettunit.handler.notify_competent_body_internal_plan")
  serviceTasks += ServiceTaskView("prepare_tech_report", "nettunit.handler.prepare_tech_report")
  serviceTaskListView.items = serviceTasks

  private val SUBMIT_SERVICE_TASK_RESTORE = "Restore activity"
  private val SUBMIT_SERVICE_TASK_FAIL = "Submit failure request"

  serviceTaskListView.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[ServiceTaskView] {
    override def changed(observableValue: ObservableValue[_ <: ServiceTaskView], oldValue: ServiceTaskView, newValue: ServiceTaskView): Unit = {
      setFailActivityButtonStatus(newValue)
    }
  })

  private def setFailActivityButtonStatus(selectedView: ServiceTaskView): Unit = {
    if (failingTaskName.isDefined) {
      if (selectedView.view == failingTaskName.get.view) {
        submitServiceTaskFailureButton.setText(SUBMIT_SERVICE_TASK_RESTORE)
        submitServiceTaskFailureButton.setStyle("-fx-background-color: darkgreen;-fx-text-fill: white;")
        return
      }
    }
    submitServiceTaskFailureButton.setText(SUBMIT_SERVICE_TASK_FAIL)
    submitServiceTaskFailureButton.setStyle("-fx-background-color: darkred;-fx-text-fill: white;")

  }

  serviceTaskListView.cellFactory = {
    a: ListView[ServiceTaskView] => {
      val cell = new ListCell[ServiceTaskView]
      cell.item.onChange { (a, b, newValue) => {


        if (newValue != null) {
          cell.text = newValue.view
          /*if (failingTaskName.isDefined) {
            if (newValue.view == failingTaskName.get.view) {
              cell.setStyle("-fx-background-color: darkred;-fx-text-fill: white;")
            }
          }*/
        }
      }
      }

      cell
    }
  }

  /*
    serviceTaskListView.cellFactory = {
      a: ListView[ServiceTaskView] => {
        val cell = new ListCell[ServiceTaskView]
        cell.item.onChange { (a, b, newValue) => {
          if (newValue != null) {
            cell.text = newValue.view
            if (failingTaskName.isDefined) {
              if (newValue.view == failingTaskName.get.view) {
                cell.setStyle("-fx-background-color: darkred;-fx-text-fill: white;")
              }
            }
          }
        }
        }

        cell
      }
    }*/

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

  val processImage = new Image(new FileInputStream(getClass.getResource("/process.png").getFile))

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

  sendTeamImage.setVisible(false)
  activateInternalPlanImage.setVisible(false)
  InformRescueInternalPlanImage.setVisible(false)
  decideResponseTypeImage.setVisible(false)
  prepareReportImage.setVisible(false)
  keepUpdateImage.setVisible(false)
  declarePreAlertImage.setVisible(false)
  informRescueAlertImage.setVisible(false)
  evaluateFireRadiantImage.setVisible(false)
  declareAlarmImage.setVisible(false)
  notifyCompetentBodiesImage.setVisible(false)
  ensurePresenceImage.setVisible(false)
  doCrossBorderImage.setVisible(false)
  ensureQualifiedPersonnelImage.setVisible(false)
  adaptationTaskImage.setVisible(false)

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
    val connectionString = s"http://$getMUSAAddress:$getMUSAAddressPort/Goal2BPMN"
    val resultApply = Http(connectionString)
      .header("Content-Type", "text/plain")
      .postData(goals)
      .asString
    BPMNTextArea.setText(resultApply.body)
  }

  @FXML private[nettunit] def onDeployProcessToFlowable(event: ActionEvent): Unit = {
    val connectionString = s"http://$getMUSAAddress:$getMUSAAddressPort/Deploy"
    val resultApply = Http(connectionString)
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
    val requestString = s"http://$getFlowableAddress:$getFlowableAddressPort/NETTUNIT/$taskType/$taskID"

    try {
      val resultApply = Http(requestString).postData("").asString
      new Alert(AlertType.Information, s"Result: ${
        resultApply.statusLine
      }").showAndWait()
      //updateProcessImageView()
      updateProcessIconImageViews()
    } catch {
      case _: SocketTimeoutException => new Alert(AlertType.Error, s"Socket timeout").showAndWait()
    }
  }

  @FXML private[nettunit] def failTask(event: ActionEvent): Unit = {
    println("fail task")
  }

  @FXML private[nettunit] def updateProcessQueryView(event: ActionEvent): Unit = {
    try {
      val resultApply = Http(s"http://$getFlowableAddress:$getFlowableAddressPort/NETTUNIT/incident_list/").method("GET").asString
      activePlansTextArea.setText(resultApply.body)

      if (activePlansTextArea.getText == "[]") {
        do_crossborder_communication_circle.setVisible(false)
        ensure_presence_of_qualified_personnel_circle.setVisible(false)
        ensure_presence_of_representative_circle.setVisible(false)
        inform_technical_rescue_organisation_alert_circle.setVisible(false)
        inform_technical_rescue_organisation_internal_plan_circle.setVisible(false)
        keep_update_involved_personnel_circle.setVisible(false)
        notify_competent_body_internal_plan_circle.setVisible(false)
        prepare_tech_report_circle.setVisible(false)
        sendTeamImage.setVisible(false)
        activateInternalPlanImage.setVisible(false)
        InformRescueInternalPlanImage.setVisible(false)
        decideResponseTypeImage.setVisible(false)
        prepareReportImage.setVisible(false)
        keepUpdateImage.setVisible(false)
        declarePreAlertImage.setVisible(false)
        informRescueAlertImage.setVisible(false)
        evaluateFireRadiantImage.setVisible(false)
        declareAlarmImage.setVisible(false)
        notifyCompetentBodiesImage.setVisible(false)
        ensurePresenceImage.setVisible(false)
        doCrossBorderImage.setVisible(false)
        ensureQualifiedPersonnelImage.setVisible(false)
        adaptationTaskImage.setVisible(false)
      }

      if (!processIDTextField.getText.isEmpty) {
        val resultApply2 = Http(s"http://$getFlowableAddress:$getFlowableAddressPort/NETTUNIT/task_list/${processIDTextField.getText}").method("GET").asString
        activeTasksTextArea.setText(resultApply2.body)
      }
    } catch {
      case _: ConnectException => new Alert(AlertType.Error, s"unable to connect. Please check if flowable is active").showAndWait()
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
    val jixelUser = JixelInterface.parseToJixelCredential(JixelInterface.connect(login))
    val ev = JixelUtil.eventFromEventSummary(login, Utils.JixelUtil.getAnyJixelEvent(login));
    val response = jixel.notifyEvent(ev)
    new Alert(AlertType.Information, s"MUSA Response: ${response}").showAndWait()
    processImageView.setImage(processImage)
    sendTeamImage.setImage(pendingIcon)
    sendTeamImage.setVisible(true)
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
        if (serviceTask.view == failingTaskName.get.view) {
          submitUndoServiceTaskFailure(serviceTask)
          new Alert(AlertType.Information, s"Submitted failure request for task: ${serviceTask.view}").showAndWait()
        } else {
          //undo previous task failure request
          submitUndoServiceTaskFailure(failingTaskName.get)
          //request a new failure for selected task
          submitServiceTaskFailure(serviceTask)
          new Alert(AlertType.Information, s"Submitted failure request for task: ${serviceTask.view}").showAndWait()
        }
      case false =>
        val result = submitServiceTaskFailure(serviceTask)
        new Alert(AlertType.Information, s"Submitted failure request for task: ${serviceTask.view}").showAndWait()
    }
    serviceTaskListView.refresh()
    setFailActivityButtonStatus(serviceTask)
  }

  def submitUndoServiceTaskFailure(service: ServiceTaskView): HttpResponse[String] = {
    failingTaskName = Some(service)
    val connectionURL = s"http://$getFlowableAddress:$getFlowableAddressPort/NETTUNIT/undo_fail/${service.fullClassName}"
    Http(connectionURL)
      .header("Content-Type", "text/xml")
      .postData("")
      .asString
  }

  def submitServiceTaskFailure(service: ServiceTaskView): HttpResponse[String] = {
    failingTaskName = Some(service)
    val connectionURL = s"http://$getFlowableAddress:$getFlowableAddressPort/NETTUNIT/fail/${service.fullClassName}"
    Http(connectionURL)
      .header("Content-Type", "text/xml")
      .postData("")
      .asString
  }

  private def updateAdaptationTaskImageView(): Unit = {
    adaptationTaskImage.setVisible(true)
    adaptationTaskImage.setImage(acceptIcon)
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

  private def updateProcessIconImageViews() = taskTypeListView.getSelectionModel.getSelectedItems.get(0) match {
    case "safety_manager/send_team_to_evaluate" => {
      sendTeamImage.setImage(acceptHumanIcon)
      activateInternalPlanImage.setImage(pendingIcon)
      activateInternalPlanImage.setVisible(true)
    }
    case "plant_operator/activate_internal_security_plan" => {
      activateInternalPlanImage.setImage(acceptHumanIcon)
      InformRescueInternalPlanImage.setVisible(true)
      isFailingTask("inform_technical_rescue_organisation_internal_plan") match {
        case true =>
          InformRescueInternalPlanImage.setImage(warningIcon)
          updateAdaptationTaskImageView()
          inform_technical_rescue_organisation_internal_plan_circle.setVisible(true)
        case false =>
          InformRescueInternalPlanImage.setImage(acceptIcon)
          decideResponseTypeImage.setVisible(true)
          decideResponseTypeImage.setImage(pendingIcon)
      }
    }
    case "commander_fire_brigade/decide_response_type" => {
      decideResponseTypeImage.setImage(acceptHumanIcon)

      //prepare tech report && keep update
      isFailingTask("prepare_tech_report") match {
        case true =>
          prepareReportImage.setImage(warningIcon)
          updateAdaptationTaskImageView()
          prepare_tech_report_circle.setVisible(true)
        case false =>
          prepareReportImage.setImage(acceptIcon)
          isFailingTask("keep_update_involved_personnel") match {
            case true =>
              keepUpdateImage.setImage(warningIcon)
              updateAdaptationTaskImageView()
              keep_update_involved_personnel_circle.setVisible(true)
            case false =>
              keepUpdateImage.setImage(acceptIcon)
              declarePreAlertImage.setVisible(true)
              declarePreAlertImage.setImage(pendingIcon)
          }
      }
    }
    case "prefect/declare_pre_alert_state" => {
      declarePreAlertImage.setImage(acceptHumanIcon)

      isFailingTask("inform_technical_rescue_organisation_alert") match {
        case true =>
          informRescueAlertImage.setImage(warningIcon)
          updateAdaptationTaskImageView()
          inform_technical_rescue_organisation_alert_circle.setVisible(true)
        case false =>
          informRescueAlertImage.setImage(acceptIcon)
          evaluateFireRadiantImage.setVisible(true)
          evaluateFireRadiantImage.setImage(pendingIcon)
      }
    }
    case "ARPA/evaluate_fire_radiant_energy" => {
      evaluateFireRadiantImage.setImage(acceptHumanIcon)
      declareAlarmImage.setVisible(true)
      declareAlarmImage.setImage(pendingIcon)
    }
    case "prefect/declare_alarm_state" => {
      declareAlarmImage.setImage(acceptHumanIcon)
      isFailingTask("notify_competent_body_internal_plan") match {
        case true =>
          notifyCompetentBodiesImage.setImage(warningIcon)
          updateAdaptationTaskImageView()
          notify_competent_body_internal_plan_circle.setVisible(true)

        case false =>
          notifyCompetentBodiesImage.setImage(acceptIcon)
          isFailingTask("ensure_presence_of_representative") match {
            case true =>
              ensurePresenceImage.setImage(warningIcon)
              updateAdaptationTaskImageView()
              ensure_presence_of_representative_circle.setVisible(true)
            case false =>
              ensurePresenceImage.setImage(acceptIcon)
              isFailingTask("do_crossborder_communication") match {
                case true =>
                  doCrossBorderImage.setImage(warningIcon)
                  updateAdaptationTaskImageView()
                  do_crossborder_communication_circle.setVisible(true)
                case false =>
                  doCrossBorderImage.setImage(acceptIcon)
                  isFailingTask("ensure_presence_of_qualified_personnel") match {
                    case true =>
                      ensureQualifiedPersonnelImage.setImage(warningIcon)
                      updateAdaptationTaskImageView()
                      ensure_presence_of_qualified_personnel_circle.setVisible(true)
                    case false =>
                      ensureQualifiedPersonnelImage.setImage(acceptIcon)
                  }
              }
          }
      }
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
        val connectionURL = s"http://$getFlowableAddress:$getFlowableAddressPort/NETTUNIT/clearDeployments/"
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





