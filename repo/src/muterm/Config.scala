package muterm

import scala.reflect.BeanProperty

object Config extends ul.Props {
    
    val project    = "muterm2"
    val projectDir = System.getProperty("user.dir") + java.io.File.separator
    
    val localeDir  = projectDir + "locale"

    val cssDir     = projectDir + "css"
    val cssFile    = "file:" + cssDir + java.io.File.separator + project + ".css"
    
    val scriptsDir = projectDir + "scripts"

    val tabNoPortSelected = "----"
    
    val serialBauds = List(300,600,1200,1800,2400,4000,4800,7200,9600,14400,16000,19200,28800,38400,51200,56000,57600,64000,
        76800,115200,128000,153600,230400,250000,256000,460800,500000,576000,921000,1000000,3000000)
    val serialParities = List("none","even","odd","mark","space")
    val serialStops = List("1", "1.5", "2")
    val serialBits = List("5", "6", "7", "8")
    
    val serialPollPeriod = 100
    
    val rxTextHexCols = 16

    @BeanProperty var x        = 10.0
    @BeanProperty var y        = 10.0
    @BeanProperty var width    = 600.0
    @BeanProperty var height   = 400.0
    
    @BeanProperty var lang     = "en"

    props.attrs ++= List(
        new ul.PropAttr("x"),
        new ul.PropAttr("y"),
        new ul.PropAttr("width"),
        new ul.PropAttr("height"),
        
        new ul.PropAttr("lang")
    )
 
    def save(fn:String):Unit = {
        val out = new java.io.BufferedWriter(
            new java.io.OutputStreamWriter(
            new java.io.FileOutputStream(fn), "UTF-8")
        );
        out.write(props.toConf)
        out.close
    };
    def save:Unit = save("./" + project + ".conf")
    
    def load(fn:String):Unit = {
        try { props.fromConf(io.Source.fromFile(fn, "UTF-8").mkString(""))
        } catch { case _:Throwable => }
    }
    def load:Unit = load("./" + project + ".conf")
}
