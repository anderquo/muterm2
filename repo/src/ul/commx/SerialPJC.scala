package ul.commx

import purejavacomm.{CommPort, CommPortIdentifier, SerialPort}
import collection.JavaConverters._

class SerialPJC(name:String) extends SerialBase(name) {
    val port = CommPortIdentifier.getPortIdentifier(name).open("SerialPJC@" + name, 100).asInstanceOf[SerialPort]
    
    port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE)
    
    val inStream = port.getInputStream
    val outStream = port.getOutputStream
    
    override def close = port.close
    
    override def baud = port.getBaudRate
    override def baud_=(newBaud:Int) = port.setSerialPortParams(newBaud, port.getDataBits, port.getStopBits, port.getParity)
    
    override def bits:Int = port.getDataBits match {
        case SerialPort.DATABITS_5 => 5
        case SerialPort.DATABITS_6 => 6
        case SerialPort.DATABITS_7 => 7
        case SerialPort.DATABITS_8 => 8
    }
    override def bits_=(b:Int) = port.setSerialPortParams( port.getBaudRate, b match {
        case 5 => SerialPort.DATABITS_5
        case 6 => SerialPort.DATABITS_6
        case 7 => SerialPort.DATABITS_7
        case 8 => SerialPort.DATABITS_8
    }, port.getStopBits, port.getParity)

    override def stops:Double = port.getStopBits match {
        case SerialPort.STOPBITS_1   => 1.0
        case SerialPort.STOPBITS_1_5 => 1.5
        case SerialPort.STOPBITS_2   => 2.0
    }
    override def stops_=(s:Double) = port.setSerialPortParams( port.getBaudRate, port.getDataBits, s match {
        case 1.0 => SerialPort.STOPBITS_1
        case 1.5 => SerialPort.STOPBITS_1_5
        case 2.0 => SerialPort.STOPBITS_2
    }, port.getParity)

    override def parity:String = port.getParity match {
        case SerialPort.PARITY_NONE  => "n"
        case SerialPort.PARITY_ODD   => "o"
        case SerialPort.PARITY_EVEN  => "e"
        case SerialPort.PARITY_MARK  => "m"
        case SerialPort.PARITY_SPACE => "s"
    }
    override def parity_=(p:String) = port.setSerialPortParams( port.getBaudRate, port.getDataBits, port.getStopBits, p.substring(0, 1).toLowerCase match {
        case "n" => SerialPort.PARITY_NONE
        case "o" => SerialPort.PARITY_ODD
        case "e" => SerialPort.PARITY_EVEN
        case "m" => SerialPort.PARITY_MARK
        case "s" => SerialPort.PARITY_SPACE
    })
    
    override def DTR:Boolean = port.isDTR
    override def DTR_=(state:Boolean) = port.setDTR(state)
    override def RTS:Boolean = port.isRTS
    override def RTS_=(state:Boolean) = port.setRTS(state)
    override def DSR:Boolean = port.isDSR
    override def CTS:Boolean = port.isCTS
    override def DCD:Boolean = port.isCD
    override def RI:Boolean  = port.isRI

    override def available:Int = inStream.available
    
    override def read(n:Int):Seq[Byte] = {
        for (i <- 0 until (if (n == 0) available else math.min(n, available)))
            yield (inStream.read & 0xFF).toByte
    }

    override def write(buf:Seq[Byte]):SerialBase = {
        outStream.write(buf.toArray)
        this
    }
    
    override def flushRX:SerialBase = {
        inStream.skip(available)
        this
    }
    override def flushTX:SerialBase = {
        outStream.flush
        this
    }

    override def break( duration:Int ) = port.sendBreak(duration)
}

object SerialPJC extends SerialBaseMgr {
    
    override def ports:Set[String] = {
        val ps = new collection.mutable.ArrayBuffer[String];
        for (p <- CommPortIdentifier.getPortIdentifiers.asScala) {
            val cpi = p.asInstanceOf[CommPortIdentifier];
            if ((cpi.getPortType == CommPortIdentifier.PORT_SERIAL) &&
                (!cpi.isCurrentlyOwned) &&
                (List("COM","ttyS","ttyUSB","ttyACM","ttyBT","rfcomm","cu.","ttyd","tty.").exists(
                    s => cpi.getName.startsWith(s)
                ))
            ) ps += cpi.getName
        }
        ps.toSet
    }
    
    override def open(name:String):SerialBase = new SerialPJC(name)
}
