import java.net.URL
import scala.xml.XML
import org.xml.sax.InputSource
import scala.xml.parsing.NoBindingFactoryAdapter
import org.ccil.cowan.tagsoup.jaxp._
import java.net.HttpURLConnection
import scala.xml.Node
import java.io._

val trenner = "}"

object HTML {
  lazy val adapter = new NoBindingFactoryAdapter
  lazy val parser = (new SAXFactoryImpl).newSAXParser

  def load(url: URL, headers: Map[String, String] = Map.empty): Node = {
    val conn = url.openConnection().asInstanceOf[HttpURLConnection]
    for ((k, v) <- headers)
      conn.setRequestProperty(k, v)
    val source = new InputSource(conn.getInputStream)
    source.setEncoding("ISO-8859-1");
    adapter.loadXML(source, parser)
  }
}

def dreistellig(in: String) = {
 val neu = in.trim
 if (neu.length<2) "00" + neu
 else if (neu.length<3) "0" + neu
 else neu
}

implicit def toSafeDouble(st: String) = scala.util.Try { st.toDouble }.getOrElse(Double.NaN)

var media: List[String] = List();
val mediadir = "./img-see/" //last slash mandatory

def saveImage(path: String): String = {
  val index = path.lastIndexOf("/");
  val newfilename = path.substring(index + 1);
  if (!media.contains(newfilename.toString)) media = media :+ newfilename.toString
  return newfilename
}

def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
  val p = new java.io.PrintWriter(f)
  try { op(p) } finally { p.close() }
}

def ladeUndTu(page: String, Tags: String) = {

  System.setProperty("jsse.enableSNIExtension", "false"); // ssl validation failed
  val site = new URL(page)

  val content = HTML.load(site)

  def processImg(liste: Seq[scala.xml.Node]): Seq[scala.xml.Node] = {
    def process(node: scala.xml.Node) = node.label match {
      case "img" => <img src={ saveImage(node \@ "src") }/>; //strip everything and process image path
//      case "br" => new scala.xml.Text("") //empty dummy
      case _ => node
    }

    liste.map(x => process(x))
  }

  for (frage <- content \\ "div" \ "ol") {
    val fragenummer = frage \@ "start";
    val frageoptionen = frage \\ "li" \\ "ol" \ "li"
    val antwortencsv = frageoptionen.map(x=>processImg(x.child)).map( _.mkString("")).mkString(trenner) // alle Frageoptionen zusammen gefaltet
    val fragentext_elems = processImg((frage \ "li")(0).child.filterNot(_.label == "ol"))
    val fragentext = fragentext_elems.map(_.toString).filterNot(_.length < 2).mkString("") // alles bis auf die "ol" mit den Antwortmöglichkeiten
    println("S"+dreistellig(fragenummer) + trenner + "(" + dreistellig(fragenummer) + ") " + fragentext + trenner + antwortencsv + trenner + Tags)
  }

}

/* nimmt alle Media-Einträge und benennt die Datein fortlaufend nur mit einer Nummer
  macht danach ein JSON-Object in der Form "id": "filename" und daraus ein großes Array
*/
def renameMediaAndGetMediaFile(media: List[String]) {
  val korrekt = media.toArray.zipWithIndex
  for ((filename, id) <- korrekt) {
    var file = new File(mediadir + filename);
    // File (or directory) with new name
    var file2 = new File(mediadir + id);
    file.renameTo(file2);
  }

  //build json-like string for media file
  val jsonlike: String = korrekt.map({ case (x, y) => "\"" + y.toString + "\": \"" + x.toString + "\"" }).reduceLeft((x, y) => x + "," + y)

  printToFile(new File("media")) { p =>
    p.println("{" + jsonlike + "}")
  }
}

ladeUndTu("https://elwis.de/Freizeitschifffahrt/fuehrerscheininformationen/Fragenkatalog-See/Basisfragen/index.html", "SBF-See-Basisfragen")
ladeUndTu("https://elwis.de/Freizeitschifffahrt/fuehrerscheininformationen/Fragenkatalog-See/See/index.html", "SBF-See-Spezifische-Fragen")

renameMediaAndGetMediaFile(media)

