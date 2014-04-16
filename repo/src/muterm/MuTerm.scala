package muterm

import javafx.{application,event,scene,stage}
import scala.language.reflectiveCalls
import scala.collection.JavaConversions

class MuTerm extends application.Application with ul.GetTextable {
    var primaryStage:javafx.stage.Stage = null
    
    override def start(stg:stage.Stage) {
        Config.load
        ul.GetText.load(Config.localeDir, "messages", "MESSAGES", Config.lang, "en")

        primaryStage = stg
        stg.setTitle(Config.project.capitalize)
        
        stg.setScene(new scene.Scene(MainPane, 300, 250) {
            getStylesheets.add(Config.cssFile)
        })
        stg.setMinWidth(600); stg.setMinHeight(400)
        stg.setX(Config.x); stg.setY(Config.y)
        stg.setWidth(Config.width); stg.setHeight(Config.height)
        
        stg.getScene.getWindow.setOnCloseRequest(new event.EventHandler[stage.WindowEvent] {
            def handle(e:stage.WindowEvent) {
                MainPane.tabs.closeAll
            }
        })

        stg.show()
    }
    
    override def stop() {
        Config.x      = primaryStage.getX
        Config.y      = primaryStage.getY
        Config.width  = primaryStage.getWidth
        Config.height = primaryStage.getHeight
        
        Config.lang   = ul.GetText.tran.lang(MainPane.langBox.getValue)
        
        Config.save
    }
}

object MuTerm {
    def main(args: Array[String]) {
//        val eng = (new javax.script.ScriptEngineManager).getEngineByName("JavaScript")
//        val arr = eng.eval("Java.to([1,2,3,4], 'byte[]')")
//        println(arr)//jdk.nashorn.api.scripting.ScriptUtils.convert(arr, classOf[Array[AnyRef]]))

        application.Application.launch(classOf[MuTerm], args: _*);
    }
}
