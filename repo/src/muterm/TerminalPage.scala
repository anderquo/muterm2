package muterm

import javafx.{application,event,scene,stage,geometry,beans}
import scala.language.reflectiveCalls
import scala.reflect.{BeanProperty}
import scala.collection.JavaConversions
import scala.collection.JavaConverters._
import scala.swing.TabbedPane

class TerminalPage extends scene.control.Tab(Config.tabNoPortSelected) with ul.GetTextable {
    val tab = this
    var port:ul.commx.SerialBase = null
    
    //init Javascript
    object js {
        val engine = (new javax.script.ScriptEngineManager).getEngineByName("JavaScript")
        
        def eval(s:String) = engine.eval(s)
        def evalToStr(s:String, default:String=""):String = try { engine.eval(s).toString } catch { case _:Throwable => default }
        def get(n:String)                            = engine.get(n)
        def getArrayBytes(name:String):Seq[Byte]     = eval("Java.to(" + name + ",'byte[]')").asInstanceOf[Array[Byte]]
        def getArrayStrings(name:String):Seq[String] = eval("Java.to(" + name + ",'java.lang.String[]')").asInstanceOf[Array[String]]
        def put(n:String, v:Any)                     = engine.put(n,v)
        def putObj(obj:Any, to:String)               = { put("obj", obj); eval(to + " = obj;") }

        def reset = eval("reset();")
        def init  = eval("filters.init();")
        
        for (f <- new java.io.File(Config.scriptsDir).listFiles.sorted) {
           if (f.isFile && f.getName.endsWith(".js") && !f.getName.startsWith(".")) {
//               try {
                   println("Loading: " + f.getName)
                   eval( io.Source.fromFile(f, "UTF-8").mkString("") )
//               } catch { case _:Throwable => }
           }
        }
        init

        object filters {
            
            def getTags(io:String) = eval("Java.to(Object.getOwnPropertyNames(filters['" + io + "'].all), 'java.lang.String[]')").asInstanceOf[Array[String]]
            def getAttr(io:String, tag:String, attr:String) = eval("filters['" + io + "'].all['" + tag + "']['" + attr + "']")
            def getAttrs(io:String, attr:String) = (for (tag <- getTags(io)) yield getAttr(io, tag, attr).toString).toArray

            object props {
                def getTags(io:String, tag:String) = eval("Java.to(Object.getOwnPropertyNames(filters['" + io + "'].all." + tag + ".props), 'java.lang.String[]')").asInstanceOf[Array[String]]
                def getAttr(io:String, tag:String, prop:String, attr:String) = eval("filters." + io + ".all." + tag + ".props." + prop + "." + attr)
                def getAttrs(io:String, tag:String, attr:String) = (for (prop <- getTags(io,tag)) yield getAttr(io, tag, prop, attr).toString).toArray
            }
            
//eval("Java.to(filters.getAttrs('" + io + "','" + attr + "'),'java.lang.String[]')").asInstanceOf[Array[String]]
//println("name: " + getAttr("i","chex","name"))
//println("names: " + getAttrs("i","name").mkString(","))
//println("tags: " + getTags("i").mkString(","))
//println("props: " + props.getTags("i","chex").mkString(","))
//println("name: " + props.getAttr("i","chex","enabled","name").toString())
//println("attrs: " + props.getAttrs("i","chex","descr").mkString(","))
           
            object in {
                val tags       = getTags("i")
                val names      = getAttrs("i","name")
                val tagsOpened = new collection.mutable.ArrayBuffer[String]
                
                def indexOf(n:String):Int = if (tags.contains(n)) tags.indexOf(n) else names.indexOf(n)
                def tagOfName(n:String)   = tags(names.indexOf(n))

                var current = -1
                
                val tabs = new scene.control.TabPane {
                    setMinHeight(100)
                }

                def openFilter(tag:String) {
                    if (!tagsOpened.contains(tag)) {
                        tagsOpened += tag
                        
                        //add new tab
                        tabs.getTabs.add( new scene.control.Tab {
                            setOnClosed(new event.EventHandler[event.Event] {
                                def handle(e:event.Event) {
                                    closeFilter(tagOfName(getText))
                                }
                            })
                            setText(names(indexOf(tag))) //caption
                            setContent( new scene.layout.VBox {
                                getChildren.addAll(
                                    //controls/props
                                    new scene.layout.HBox {
                                        for (prop <- props.getTags("i", tag)) {
                                            
                                            props.getAttr("i", tag, prop, "type").toString match {
                                                
                                                //Button
                                                case "button" =>
                                                    getChildren.addAll( new scene.control.Button( props.getAttr("i", tag, prop, "name").toString ) {
                                                        setTooltip(new scene.control.Tooltip( props.getAttr("i", tag, prop, "descr").toString ))
                                                        setOnAction(new event.EventHandler[event.ActionEvent] {
                                                            override def handle(e:event.ActionEvent) {
                                                                eval("filters.i.all." + tag + ".props." + prop + ".update()")
                                                            }
                                                        })
                                                    },
                                                    new scene.control.Separator(geometry.Orientation.VERTICAL))

                                                //String field
                                                case "str" =>
                                                    getChildren.addAll(
                                                        new scene.control.Label(props.getAttr("i", tag, prop, "name").toString + ": "),
                                                        new scene.control.TextField {
                                                            setTooltip(new scene.control.Tooltip( props.getAttr("i", tag, prop, "descr").toString ))
                                                            try { setPrefColumnCount(props.getAttr("i", tag, prop, "width").toString.toDouble.toInt)
                                                            } catch { case _:Throwable => }
                                                            setText(props.getAttr("i", tag, prop, "get()").toString)

                                                            textProperty.addListener(new beans.value.ChangeListener[String]() {
                                                                override def changed(ov:beans.value.ObservableValue[_ <: String], o:String, n:String) {
                                                                    props.getAttr("i", tag, prop, "set('" + n + "')")
                                                                }
                                                            })
                                                        },
                                                        new scene.control.Separator(geometry.Orientation.VERTICAL)
                                                    )
    
                                                //Int field
                                                case "int" =>
                                                    getChildren.addAll(
                                                        new scene.control.Label(props.getAttr("i", tag, prop, "name").toString + ": "),
                                                        new scene.control.TextField {
                                                            setTooltip(new scene.control.Tooltip( props.getAttr("i", tag, prop, "descr").toString ))
                                                            try { setPrefColumnCount(props.getAttr("i", tag, prop, "width").toString.toDouble.toInt)
                                                            } catch { case _:Throwable => }
                                                            setText(props.getAttr("i", tag, prop, "get()").toString)
    
                                                            override def replaceText(start:Int, end:Int, text:String) {
                                                                if (text.matches("[0-9]*")) super.replaceText(start, end, text);
                                                            }
                                                            
                                                            override def replaceSelection(text:String) {
                                                                if (text.matches("[0-9]*")) super.replaceSelection(text);
                                                            }
                                                            
                                                            textProperty.addListener(new beans.value.ChangeListener[String]() {
                                                                override def changed(ov:beans.value.ObservableValue[_ <: String], o:String, n:String) {
                                                                    props.getAttr("i", tag, prop, "set(" + n + ")")
                                                                }
                                                            })
                                                        },
                                                        new scene.control.Separator(geometry.Orientation.VERTICAL)
                                                    )
                                                
                                                //Boolean checkbox
                                                case "bool" =>
                                                    getChildren.addAll(
                                                        new scene.control.CheckBox( props.getAttr("i", tag, prop, "name").toString ) {
                                                            setTooltip(new scene.control.Tooltip( props.getAttr("i", tag, prop, "descr").toString ))
                                                            setSelected( props.getAttr("i", tag, prop, "get()").toString == "true" )
                                                            selectedProperty.addListener(new beans.value.ChangeListener[java.lang.Boolean]() {
                                                                def changed(ov:beans.value.ObservableValue[_ <: java.lang.Boolean], o:java.lang.Boolean, n:java.lang.Boolean) = {
                                                                    props.getAttr("i", tag, prop, "set(" + n + ")")
                                                                }
                                                            })
                                                        },
                                                        new scene.control.Separator(geometry.Orientation.VERTICAL)
                                                    )
                                            }
                                        }
                                    },
                                    
                                    //content
                                    getAttr("i",tag,"pageType").toString match {
                                        case "text" =>
                                            val text = new scene.control.TextArea {
                                                setEditable(false)
                                            }
                                            putObj(text, "filters.i.all." + tag + ".textObj")
                                            text
                                            
                                        case "chart" =>
                                            val _chart = new scene.chart.LineChart[Number,Number]( new scene.chart.NumberAxis, new scene.chart.NumberAxis ) {
                                                setAnimated(false)
                                                setLegendVisible(false)
                                            }
                                            
                                            object chartObj {
                                                @BeanProperty val chart = _chart
                                                
                                                def clear = { javafx.application.Platform.runLater( new Runnable { override def run = {
                                                    try { for (sn <- 0 until chart.getData.size) chart.getData.get(sn).getData.clear
                                                    } catch { case _:Throwable => }
                                                }})}
                                                
                                                def setChartsNum(n:Int) = { javafx.application.Platform.runLater( new Runnable { override def run = {
                                                    try {
                                                        chart.getData.clear
                                                        for (i <- 0 until n) chart.getData.add( new scene.chart.XYChart.Series )
                                                    } catch { case _:Throwable => }
                                                }})}
                                                
                                                def addPoint(n:Int, x:Double, y:Double) = { javafx.application.Platform.runLater( new Runnable { override def run = {
                                                    try { chart.getData.get(n).getData.add( new scene.chart.XYChart.Data(x,y))
                                                    } catch { case _:Throwable => }
                                                }})}
                                                
                                            }

                                            putObj(chartObj, "filters.i.all." + tag + ".chart")
                                            chartObj.setChartsNum(evalToStr("filters.i.all.csv.formatArray.length").toDouble.toInt)
                                            _chart
                                    }
                                )
                            })
                        })
                        tabs.getSelectionModel.selectLast
                    } else {
                        tabs.getSelectionModel.select(indexOf(tag))
                    }
                }
                def openFilter(index:Int) {openFilter(tags(index))}
                
                def closeFilter(tag:String) {
                    if (tagsOpened.contains(tag)) {println("Close:"+tag)
                        tagsOpened -= tag
                    }
                }
                def closeAllFilters = {
                    tabs.getTabs.remove(0, tagsOpened.size)
                    tagsOpened foreach( closeFilter(_) )
                }
            }
            
            object out {
                val tags = getTags("o")
//                val tagsOpened = new collection.mutable.ArrayBuffer[String]
//                val names = tags map ( attr( _ , "name" ))
//
//                def indexOf(n:String):Int = if (tags.contains(n)) tags.indexOf(n) else names.indexOf(n)
//                def tagOf(n:String):String = if (tags.contains(n)) n else tags(names.indexOf(n))
//                
//                def attr(tag:String, n:String):String = eval("filter.i." + tag + "." + n).asInstanceOf[String]
//                var current = -1

//                val tabs = new TabbedPane

//                def open(tag:String) {
//                    tagsOpened += tag
//                }
            }
            
            in.openFilter("text")
        }
    }

    object params {
        def port               = portsCombo.getValue
        def baud               = baudsCombo.getValue
        def bits               = bitsCombo.getValue
        def parity             = paritiesCombo.getValue
        def stops              = stopsCombo.getValue

        def dtr                = dtrCheck.isSelected
        def rts                = rtsCheck.isSelected
        def dsr(value:Boolean) = dsrCheck.setSelected(value)
        def cts(value:Boolean) = ctsCheck.setSelected(value)
        def dcd(value:Boolean) = dcdCheck.setSelected(value)
        def ri(value:Boolean)  = riCheck.setSelected(value)
        
        def rxFormat           = rxFilterFormatCombo.getValue
        var rxTextHexCol       = 0 //Hex text columns
        
        def txInFormat         = txInFormatCombo.getValue
        def txInEnc            = txInEncCombo.getValue
        def txInCr             = txInCrCheck.isSelected
        def txInLf             = txInLfCheck.isSelected
    }

    object pollThread {
        var thread:java.lang.Thread = null
        var threadRun = new java.util.concurrent.atomic.AtomicBoolean(false)
        
        def start = {
            threadRun.set(true)
            thread = new java.lang.Thread( new java.lang.Runnable {
                def run = {
                    while (threadRun.get) {
                        this.synchronized {
                            javafx.application.Platform.runLater(new Runnable() {
                                def run {
                                    params.dsr(port.DSR)
                                    params.cts(port.CTS)
                                    params.dcd(port.DCD)
                                    params.ri(port.RI)
                                }
                            })
                            
                            if (port.available > 0) {
                                val bytes = port.read.toArray
                                var text = ""
//                                if (js.filterIn != null) {

                                if (bytes.length > 0) {
//println("received: " + bytes.mkString(","))
                                    js.put("bytesIn", bytes map ( _ & 0xFF ))
                                    for (tag <- js.filters.in.tagsOpened) {
                                        javafx.application.Platform.runLater(new Runnable() {
                                            def run {
                                                js.eval("filters.i.all." + tag + ".update()")
                                            }
                                        })
                                    }
                                }
//                                } else {
//                                    rxFormatCombo.getValue match {
//                                        case "Text" =>
//                                            text = new String(bytes, rxEncCombo.getValue)
//                                    }
//                                }
//                                rxText.appendText(text)
                            }
                        }
                        Thread.sleep(Config.serialPollPeriod)
                    }
                    port.close
                    port = null
                }
            })
            thread.start
        }
        
        def stop = {
            threadRun.set(false)
        }
    }
    
    val portsCombo = new scene.control.ComboBox[String] {
        getItems.addAll( ( ul.commx.SerialPJC.ports.toSeq.sortWith( _ < _ ) ).asJava )
    }
    
    val baudsCombo:scene.control.ComboBox[String] = new scene.control.ComboBox[String] {
        val combo = this
        getItems.addAll( ( Config.serialBauds map ( _.toString ) ).asJava )
        setEditable(true)
        setValue("9600")
        
        valueProperty.addListener(new beans.value.ChangeListener[String]() {
            def changed(ov:beans.value.ObservableValue[_ <: String], t:String, t1:String) {
                try { port.baud = t1.toInt
                } catch { case _:Throwable => }
            }
        })
    }
    
    val bitsCombo:scene.control.ComboBox[String] = new scene.control.ComboBox[String] {
        val combo = this
        getItems.addAll( Config.serialBits.asJava )
        setValue("8")
        
        valueProperty.addListener(new beans.value.ChangeListener[String]() {
            def changed(ov:beans.value.ObservableValue[_ <: String], t:String, t1:String) {
                try { port.bits = t1.toInt
                } catch { case _:Throwable => }
            }
        })
    }
    
    val paritiesCombo:scene.control.ComboBox[String] = new scene.control.ComboBox[String] {
        val combo = this
        getItems.addAll( Config.serialParities.asJava )
        setValue("none")
        
        valueProperty.addListener(new beans.value.ChangeListener[String] {
            def changed(ov:beans.value.ObservableValue[_ <: String], t:String, t1:String) {
                try { port.parity = t1
                } catch { case _:Throwable => }
            }
        })
    }

    val stopsCombo:scene.control.ComboBox[String] = new scene.control.ComboBox[String] {
        val combo = this
        getItems.addAll( Config.serialStops.asJava )
        setValue("1")

        valueProperty.addListener(new beans.value.ChangeListener[String] {
            def changed(ov:beans.value.ObservableValue[_ <: String], t:String, t1:String) {
                try { port.stops = t1.toDouble
                } catch { case _:Throwable => }
            }
        })
    }

    val dtrCheck = new scene.control.CheckBox("DTR") {
        selectedProperty.addListener(new beans.value.ChangeListener[java.lang.Boolean]() {
            def changed(ov:beans.value.ObservableValue[_ <: java.lang.Boolean], val_old:java.lang.Boolean, val_new:java.lang.Boolean) = {
                try { port.DTR = val_new
                } catch { case _:Throwable => }
            }
        });
    }
    val rtsCheck = new scene.control.CheckBox("RTS") {
        selectedProperty.addListener(new beans.value.ChangeListener[java.lang.Boolean]() {
            def changed(ov:beans.value.ObservableValue[_ <: java.lang.Boolean], val_old:java.lang.Boolean, val_new:java.lang.Boolean) = {
                try { port.RTS = val_new
                } catch { case _:Throwable => }
            }
        });
    }
    val dsrCheck = new scene.control.CheckBox("DSR") {
        setDisable(true)
    }
    val ctsCheck = new scene.control.CheckBox("CTS") {
        setDisable(true)
    }
    val dcdCheck = new scene.control.CheckBox("DCD") {
        setDisable(true)
    }
    val riCheck = new scene.control.CheckBox("RI") {
        setDisable(true)
    }

    val connectButton:scene.control.Button = new scene.control.Button(tr("connect")) {
        val button = this
        setOnAction( new event.EventHandler[event.ActionEvent] {
            override def handle(e:event.ActionEvent) {
                if (port != null) port.close
                try {
                    port = new ul.commx.SerialPJC(params.port) {
                        baud   = params.baud.toInt
                        bits   = params.bits.toInt
                        parity = params.parity
                        stops  = params.stops.toDouble
                        DTR    = params.dtr
                        RTS    = params.rts
                    }
                    tab.setText(params.port)
                    button.setDisable(true)
                    disconnectButton.setDisable(false)
                    pollThread.start
                } catch { case _:Throwable => }
            }
        })
    }
    
    val disconnectButton:scene.control.Button = new scene.control.Button(tr("disconnect")) {
        val button = this
        setDisable(true)
        setOnAction( new event.EventHandler[event.ActionEvent] {
            def handle(e:event.ActionEvent) {
                pollThread.stop
                button.setDisable(true)
                connectButton.setDisable(false)
                tab.setText(Config.tabNoPortSelected)
            }
        })
    }

    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val rxFilterFormatCombo = new scene.control.ComboBox[String] {
        setTooltip(new javafx.scene.control.Tooltip(tr("Select RX text format")))
        getItems.addAll( js.filters.in.names.toList.asJava )
        setValue(js.filters.in.names(0))
    }
    
    val rxFilterOpenButton:scene.control.Button = new scene.control.Button(tr("open")) {
        setTooltip(new javafx.scene.control.Tooltip(tr("Open filter page")))
        setOnAction( new event.EventHandler[event.ActionEvent] {
            def handle(e:event.ActionEvent) {
                js.filters.in.openFilter(rxFilterFormatCombo.getSelectionModel.getSelectedIndex)
            }
        })
    }

    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val txInFormatCombo = new scene.control.ComboBox[String] {
        getItems.addAll( (List("Text", "HEX", "JS") ++ js.filters.out.tags.map( "filter:" + _ ) ).asJava )
        setValue("Text")
        setTooltip(new javafx.scene.control.Tooltip(tr("Select text format")))
    }

    val txInEncCombo = new scene.control.ComboBox[String] {
        getItems.addAll( List("ISO-8859-1","CP1251","KOI8-R","UTF-8").asJava )
        setValue("ISO-8859-1")
        setTooltip(new javafx.scene.control.Tooltip(tr("Select text encoding")))
    }
    
    val txInCrCheck = new scene.control.CheckBox("CR") {
        setTooltip(new javafx.scene.control.Tooltip(tr("Append CR (0x0D) to string")))
    }
    val txInLfCheck = new scene.control.CheckBox("LF") {
        setTooltip(new javafx.scene.control.Tooltip(tr("Append LF (0x0A) to string")))
    }

    val txInClearButton = new scene.control.Button(tr("clear")) {
        setOnAction( new event.EventHandler[event.ActionEvent] {
            def handle(e:event.ActionEvent) {
                txInText.clear
            }
        })
    }
    
    val txInSendButton = new scene.control.Button(tr("send")) {
        setOnAction( new event.EventHandler[event.ActionEvent] {
            def handle(e:event.ActionEvent) {
                val text = txInText.getText
                var bytes:Seq[Byte] = Nil
                txInFormatCombo.getValue match {
                    case "Text" =>
                        bytes = text.getBytes(txInEncCombo.getValue)
                        if (txInCrCheck.isSelected) bytes ++= List(0xD.toByte)
                        if (txInLfCheck.isSelected) bytes ++= List(0xA.toByte)
                    case "HEX" =>
                        try {
                            var t = text.replaceAll(" ", "")
                            t = ( t.split(",").map( s => s.replace("0x", "").replace("0X", "") )
                                .map( s => if ((s.length % 2) == 0) s else "0" + s ) ).mkString("")
                            bytes = javax.xml.bind.DatatypeConverter.parseHexBinary(t)
                        } catch { case _:Throwable => }
                    case "JS" =>
                        try {
                            js.reset
                            js.eval(text)
                            bytes = js.getArrayBytes("bytes")
                        } catch { case _:Throwable => }
                }
                if ((port != null) && bytes.nonEmpty) {
                    port.write(bytes)
                    txOutFormatCombo.getValue match {
                        case "Text" =>
                            txOutText.appendText(text)
                            txOutText.appendText("\n")
                        case "HEX" =>
                            txOutText.appendText(
                                bytes.map( "%02X ".format( _ )).mkString("") + "\n"
                            )
                    }
                }
            }
        })
    }

    val txInText = new scene.control.TextArea {
        setPrefRowCount(4)
        setMinHeight(50)
    }

    val txOutClearButton = new scene.control.Button(tr("clear")) {
        setOnAction( new event.EventHandler[event.ActionEvent] {
            def handle(e:event.ActionEvent) {
                txOutText.clear
            }
        })
    }
    
    val txOutFormatCombo = new scene.control.ComboBox[String] {
        getItems.addAll( List("Text", "HEX").asJava )
        setValue("Text")
        setTooltip(new javafx.scene.control.Tooltip(tr("Select text format")))
    }

    val txOutText = new scene.control.TextArea {
        setEditable(false)
        setWrapText(true)
        setMinHeight(100)
    }
    
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    setContent( new scene.layout.VBox {
        getChildren.addAll(
            new scene.layout.HBox {
                getChildren.addAll(
                    new scene.control.Label(tr("port: ")),
                    portsCombo,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
        
                    new scene.control.Label(tr("baud: ")),
                    baudsCombo,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
        
                    new scene.control.Label(tr("bits: ")),
                    bitsCombo,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
        
                    new scene.control.Label(tr("parity: ")),
                    paritiesCombo,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
        
                    new scene.control.Label(tr("stops: ")),
                    stopsCombo,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
        
                    connectButton,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
        
                    disconnectButton
                )
            },
            new scene.control.Separator(geometry.Orientation.HORIZONTAL),
            new scene.layout.HBox {
                getChildren.addAll(
                    dtrCheck,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    rtsCheck,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    dsrCheck,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    ctsCheck,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    dcdCheck,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    riCheck
                )
            },
            new scene.control.Separator(geometry.Orientation.HORIZONTAL),
            new scene.layout.HBox {
                getChildren.addAll(
                    rxFilterFormatCombo,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    rxFilterOpenButton
                )
            },
            new scene.control.Separator(geometry.Orientation.HORIZONTAL),
            js.filters.in.tabs,
            new scene.control.Separator(geometry.Orientation.HORIZONTAL),
            new scene.layout.HBox {
                getChildren.addAll(
                    txInSendButton,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    txInFormatCombo,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    txInEncCombo,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    txInCrCheck,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    txInLfCheck,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    txInClearButton
                )
            },
            new scene.control.Separator(geometry.Orientation.HORIZONTAL),
            txInText,
            new scene.control.Separator(geometry.Orientation.HORIZONTAL),
            new scene.layout.HBox {
                getChildren.addAll(
                    txOutFormatCombo,
                    new scene.control.Separator(geometry.Orientation.VERTICAL),
                    txOutClearButton
                )
            },
            new scene.control.Separator(geometry.Orientation.HORIZONTAL),
            txOutText
        )
    })
    
    setOnClosed(new event.EventHandler[event.Event] {
        def handle(e:event.Event) {
            js.filters.in.closeAllFilters
            pollThread.stop
        }
    })
    
}
