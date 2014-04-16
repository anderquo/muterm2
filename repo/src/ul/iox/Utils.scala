package ul.iox

object Utils {

    def loadString(f:java.io.File, enc:String):String = {
        try {
            return scala.io.Source.fromFile(f, enc).mkString;
        } catch {
            case _:Throwable => return "";
        }
    }
    def loadString(fName:String, enc:String):String =
        loadString(new java.io.File(fName), enc);
    
    def loadBytes(f:java.io.File):Seq[Byte] = {
        try {
            val in = new java.io.FileInputStream(f);
            (for (i <- 0 until in.available) yield (in.read.toByte)).toArray
        } catch { case _:Throwable => Nil}
    }
    def loadBytes(fName:String):Seq[Byte] =
        loadBytes(new java.io.File(fName));

    def saveString(f:java.io.File, enc:String, s:String):Unit = {
        try {
            val out = new java.io.BufferedWriter( new java.io.OutputStreamWriter(new java.io.FileOutputStream(f), enc) );
            out.write(s);
            out.close;
        } catch { case _:Throwable => }
    }
    def saveString(fName:String, enc:String, s:String):Unit =
        saveString(new java.io.File(fName), enc, s);

    def saveBytes(f:java.io.File, buf:Seq[Byte]):Unit = {
        try {
            val out = new java.io.FileOutputStream(f);
            val bs = new java.io.ByteArrayOutputStream();
            bs.write(buf.toArray);
            bs.writeTo(out);
            out.close; bs.close;
        } catch { case _:Throwable => }
    }
    def saveBytes(fName:String, buf:Seq[Byte]):Unit =
        saveBytes(new java.io.File(fName), buf);
}
