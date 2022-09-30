import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafxml.core.{FXMLView, NoDependencyResolver}

import java.io.IOException


object DemoApp extends JFXApp3 {
  override def start(): Unit = {

    val resource = getClass.getResource("demoUI.fxml")
    if (resource == null) {
      throw new IOException("Cannot load resource: AdoptionForm.fxml")
    }

    val root = FXMLView(resource, NoDependencyResolver)

    stage = new JFXApp3.PrimaryStage() {
      title = "FXML GridPane Demo"
      scene = new Scene(root)

    }
  }
}
