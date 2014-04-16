package muterm

import javafx.{application,event,scene,stage,geometry}

object MainPane extends scene.layout.VBox with ul.GetTextable {

    val langBox = new scene.control.ComboBox[String] {
        getItems.addAll( scala.collection.JavaConversions.asJavaCollection( ul.GetText.langs ))
        setValue(Config.lang)
    }

    val toolBar = new scene.layout.HBox {
        getChildren.addAll(
            new scene.control.Button(tr("Open")) {
                setOnAction( new event.EventHandler[event.ActionEvent] {
                    override def handle(e:event.ActionEvent) {
                        tabs.all.getTabs.add(new TerminalPage)
                        tabs.all.getSelectionModel.selectLast
                    }
                })
            },
            new scene.control.Separator(geometry.Orientation.VERTICAL),
            langBox
        )
    }
    
    object tabs {
        val all = new scene.control.TabPane
        def closeAll = {
            for (t <- scala.collection.JavaConversions.collectionAsScalaIterable(all.getTabs)) {
                t.asInstanceOf[TerminalPage].pollThread.stop
            }
        }
    }
    
    getChildren.addAll(toolBar, new scene.control.Separator(javafx.geometry.Orientation.HORIZONTAL), tabs.all)
}
