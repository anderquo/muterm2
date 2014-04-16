package ul.commx

import java.io.{InputStream, OutputStream}
import collection.mutable.{ArrayBuffer}

abstract class SerialBase(name:String) extends ul.StrAttrs {
    /// closes port
    def close = {}
    
    /// set port configuration
    def init(baud:Int, bits:Int, stops:Double, parity:String): SerialBase = this
    
    override def strKeys = Set("name","available","baud","bits","stops","parity","dtr","dsr","rts","cts","dcd","ri")
    /// set string parameter
    def strSet(k:String, v:String) = {
        try k match {
            case "baud"   => baud   = v.toInt
            case "bits"   => bits   = v.toInt
            case "stops"  => stops  = v.toDouble
            case "parity" => parity = v
            case "dtr"    => DTR = List("true","1").contains(v.toLowerCase)
            case "rts"    => RTS = List("true","1").contains(v.toLowerCase)
        } catch { case _:Throwable => }
    }
    /// get string parameter
    def strGet(k:String):String = {
        k match {
            case "name"      => name
            case "available" => available.toString
            case "baud"      => baud.toString
            case "bits"      => bits.toString
            case "stops"     => stops.toString
            case "parity"    => parity
            case "dtr"       => if (DTR) "1" else "0"
            case "rts"       => if (RTS) "1" else "0"
            case "dsr"       => if (DSR) "1" else "0"
            case "cts"       => if (CTS) "1" else "0"
            case "dcd"       => if (DCD) "1" else "0"
            case "ri"        => if (RI)  "1" else "0"
            case _           => ""
        }
    }
    
    /// get/set baud rate
    def baud:Int = 0
    def baud_=(newBaud:Int) = {}
    
    /// get/set data bits
    def bits:Int = 0
    def bits_=(newBits:Int) = {}

    /// get/set stop bits
    def stops:Double = 0
    def stops_=(newStops:Double) = {}

    /// get/set parity
    def parity:String = "n"
    def parity_=(newParity:String) = {}
    
    /// get/set port signals
    def DTR:Boolean = false
    def DTR_=(state:Boolean) = {}
    def RTS:Boolean = false
    def RTS_=(state:Boolean) = {}
    def DSR:Boolean = false
    def CTS:Boolean = false
    def DCD:Boolean = false
    def RI:Boolean  = false

    /// returns number of available bytes in RX buffer
    def available:Int
    
    /// waits for n input bytes for t milliseconds
    /// returns true if n bytes received, false if timeout occured
    def availWait( n:Int, t:Int ):Boolean = {
        var to = t
        while ( available < n && to > 0 ) {
            Thread.sleep(1)
            to -= 1
        }
        available >= n
    }
    
    /// waits for n input bytes for t * 0.1 milliseconds
    /// returns Array of n received bytes, null if timeout occured
    def readWait( n:Int, t:Int ): Seq[Byte] = {
        if (availWait(n, t)) read(n) else Nil
    }
    
    /// read n bytes from RX buffer to array (available number if n == 0)
    def read(n:Int):Seq[Byte] = Nil
    def read:Seq[Byte] = read(available)

    /// write bytes
    def write(buf:Seq[Byte]):SerialBase = this
    /// write string
    def write(s:String, enc:String="ISO-8859-1"):SerialBase = write(s.getBytes(enc)) 
    
    /// flush RX/TX buffers
    def flushRX:SerialBase = this
    def flushTX:SerialBase = this
    def flush:SerialBase = { flushRX; flushTX; this }

    def break( duration:Int ) = {}
}

class SerialBaseMgr {
    /// returns list of available ports
    def ports:Set[String] = Set.empty[String]
    
    /// open port by name
    def open(name:String):SerialBase = null

}
