package ul.iox

object CSV {

    def parse(s:String, sep:String=";"):Seq[Seq[Double]] = {
        val rows = s.replace("\r\n", "\n").split("\n")
        var rowWidth = 0
        val buf = new collection.mutable.ArrayBuffer[collection.mutable.ArrayBuffer[Double]]
        rows foreach { r =>
            val rs = r.split(sep)
            if (rowWidth == 0) rowWidth = rs.length
            val bufRow = collection.mutable.ArrayBuffer.fill[Double](rowWidth)(Double.NaN)
            try {
                for (i <- 0 until rs.length)
                    bufRow(i) = java.lang.Double.parseDouble(rs(i))
                buf += bufRow
            } catch { case _:Throwable => }
        }
        buf
    }
    
    def parseFile(name:String, sep:String=";", enc:String="ISO-8859-1"):Seq[Seq[Double]] = {
        try {
            parse(scala.io.Source.fromFile(name, enc).getLines.mkString("\n"), sep)
        } catch { case _:Throwable => Nil }
    }
}
