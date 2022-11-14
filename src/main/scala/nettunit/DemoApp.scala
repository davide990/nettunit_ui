package nettunit

import javafx.fxml.FXMLLoader
import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafxml.core.{FXMLView, NoDependencyResolver}

import java.io.IOException

object DemoApp extends JFXApp3 {
  override def start(): Unit = {

    val resource = getClass.getResource("demoUI.fxml")
    if (resource == null) {
      throw new IOException("Cannot load resource: demoUI.fxml")
    }

    val root = FXMLView(resource, NoDependencyResolver)

    stage = new JFXApp3.PrimaryStage() {
      title = "NETTUNIT"
      scene = new Scene(root)
    }
  }
}
