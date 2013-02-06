package kr.co.hkcb.tools.chm

import java.io._
import java.nio.charset.CodingErrorAction
import java.util.regex.Pattern

object CompileScala {

  def main(args: Array[String]) {

    val apiVersion = args(0)

    val apiRoot = "docs"
    val indexPage = "package.html"

    val hhpFile = apiVersion + ".hhp"
    val tocFile = apiVersion + ".hhc"
    val indexFile = apiVersion + ".hhk"

    val apiDir = apiRoot + "/" + apiVersion
    val tmpDir = "_modified"

    val caption = apiVersion
    val defaultFile = apiDir + tmpDir + "/" + indexPage
    val homePage = apiDir + tmpDir + "/" + indexPage
    val language = "0x409 English (United States)"
    val etcSettings = "0x61520,,0x104e,[0,0,800,600],,,,,,,0"

    val hhcTool = System.getenv("ProgramFiles") + "/HTML Help Workshop/hhc.exe"

    /**
     * table of contents
     */
    def makeToc {

      val getCurrentDirectoryName = new File(System.getProperty("user.dir")).getName

      def list(path: File): String = {
        var html = ""
        for (file <- path.listFiles) {

          var title = file.toString
            .replace(apiRoot + "\\", "")
            .replace(apiVersion + "\\", "")
            .replace("\\", ".")
            .replace(".html", "")
            .replace("$bang", "!")
            .replace("$eq", "=")
            .replace("$colon", ":")
            .replace("$plus", "+")

          var pageImage = 11

          if (title.contains("$")) {
            pageImage = 10
          }

          title = title
            .replace("$$", ".")
            .replace("$", "")

          val local = file.toURI.toString
            .replaceAll("^.*" + getCurrentDirectoryName + "/", "")
            .replace(apiVersion, apiVersion + tmpDir)

          if (file.isDirectory) {
            html += "<LI>" +
              "<OBJECT type=\"text/sitemap\">\n" +
              "<param name=\"Name\" value=\"" + title + "\">\n" +
              "<param name=\"Local\" value=\"" + local + indexPage + "\">\n" +
              "</OBJECT>" +
              "</LI>\n" +
              "<UL>\n" + list(file) + "</UL>\n"

          } else if (!local.contains(indexPage)) {
            html += "<LI>" +
              "<OBJECT type=\"text/sitemap\">\n" +
              "<param name=\"Name\" value=\"" + title + "\">\n" +
              "<param name=\"Local\" value=\"" + local + "\">\n" +
              "<param name=\"ImageNumber\" value=\"" + pageImage + "\">\n" +
              "</OBJECT>" +
              "</LI>\n"
          }
        }
        html
      }

      val file = new FileWriter(tocFile)

      file.write("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">\n" +
        "<HTML>\n<HEAD>\n<!-- Sitemap 1.0 -->\n</HEAD><BODY>\n")

      file.write("<UL>\n")
      file.write(list(new File(apiDir + "/scala")))
      file.write("</UL>\n")
      file.write("</BODY></HTML>")
      file.close
    }

    /**
     * project
     */
    def makeHhp {

      import scalax.io._

      def list(path: File): String = {
        var txt = ""
        val pattern = Pattern.compile( """<\?xml.+?\?>|<script type="text/javascript">.*</script>""", Pattern.DOTALL)

        implicit val codec = Codec("UTF-8")
        codec.onMalformedInput(CodingErrorAction.REPLACE)
        codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

        for (file <- path.listFiles) {
          if (file.isDirectory) {
            txt += list(file)
          } else {
            println(file)
            txt += file.getCanonicalPath.replace(apiVersion, apiVersion + tmpDir) + "\n"

            val targetDir = new File(file.getParent.replace(apiVersion, apiVersion + tmpDir))

            targetDir.mkdirs

            if (file.toString.contains(".html") && !file.toString.contains(apiVersion + "\\" + indexPage)) {
              val htmlFile = Resource.fromFile(targetDir.getCanonicalPath + "/" + file.getName)
              htmlFile.truncate(0)
              htmlFile.write(pattern.matcher(Resource.fromFile(file).string).replaceAll(""))
            } else {
              Resource.fromFile(file) copyDataTo
                Resource.fromFile(targetDir.getCanonicalPath + "/" + file.getName)
            }
          }
        }
        txt
      }

      val file = new FileWriter(hhpFile)

      file.write("[OPTIONS]\n" +
        "Compatibility=1.1 or later\n" +
        "Compiled file=" + apiVersion + ".chm\n" +
        "Contents file=" + tocFile + "\n" +
        "Default Window=settings\n" +
        "Default topic=" + defaultFile + "\n" +
        "Display compile progress=Yes\n" +
        "Full-text search=Yes\n" +
        "Index file=" + indexFile + "\n" +
        "Language=" + language + "\n" +
        "Title=" + caption + "\n\n" +
        "[WINDOWS]\n" +
        "settings=" +
        "\"" + caption + "\"," +
        "\"" + tocFile + "\"," +
        "\"" + indexFile + "\"," +
        "\"" + defaultFile + "\"," +
        "\"" + homePage + "\"," +
        ",," +
        ",," +
        etcSettings +
        "\n")

      file.write("\n[FILES]\n")
      file.write(list(new File(apiDir)))
      file.close
    }

    /**
     * index
     */
    def makeHhk {

      //import scala.xml._
      import scala.xml.pull._
      import scala.io._

      var isKeyword = false
      var keywordName = ""

      /**
       * index entry
       *
       * {{{
       * <div class="entry">
       * <div class="name">{Name}</div>
       * <div class="occurrences">
       * <a href="{Local}" class="extype" name="scala.collection.mutable.FlatHashTable">FlatHashTable</a>
       * {...}
       * </div>
       * </div>
       * }}}
       */
      def keywords(ev: XMLEvent): String = {
        ev match {
          case EvElemStart(_, tag, attrs, _) => {
            if (tag == "div" && attrs.get("class").mkString == "name") {
              isKeyword = true
            } else if (tag == "a") {
              val local = attrs.get("href").mkString.replace("../", apiDir + tmpDir + "/")
              return "<LI><OBJECT type=\"text/sitemap\">\n" +
                "<param name=\"Name\" value=\"" + keywordName + "\">\n" +
                "<param name=\"Local\" value=\"" + local + "\">\n" +
                "</OBJECT></LI>\n"
            }
          }
          case EvText(text) => {
            if (isKeyword) {
              keywordName = text
              isKeyword = false
            }
          }
          case EvEntityRef(entity) => {
            if (keywordName != "") {
              entity match {
                case "gt" => keywordName += ">"
                case "lt" => keywordName += "<"
                case "nbsp" => keywordName += " "
                case "amp" => keywordName += "&"
                case _ =>
              }
            }
          }
          case _ =>
        }
        return ""
      }

      implicit val codec = Codec("UTF-8")
      codec.onMalformedInput(CodingErrorAction.REPLACE)

      val file = new FileWriter(indexFile)
      file.write("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">\n" +
        "<HTML>\n<HEAD>\n<!-- Sitemap 1.0 -->\n</HEAD><BODY>" +
        "<UL>\n")

      val indexDir = new File(apiDir + "/index")
      for (indexHtml <- indexDir.listFiles) {

        println(indexHtml)

        val indexPages = new XMLEventReader(Source.fromFile(indexHtml))
        for (index <- indexPages) {
          file.write(keywords(index))
        }
      }

      file.write("</UL>\n</BODY></HTML>")
      file.close
    }

    /**
     * chm
     */
    def makeChm {
      import sys.process._

      hhcTool + " " + hhpFile !
    }

    if (!new File(hhcTool).exists) {
      print("#" * 80)
      print("\n\nError!!!\n\n")
      print(" You need to install 'HTML Help Workshop (Htmlhelp.exe)'\n\n")
      print("  http://msdn.microsoft.com/library/windows/desktop/ms669985\n\n")
      println("#" * 80)
      sys.exit(0)
    }

    if (!new File(apiRoot + "/" + apiVersion).exists) {
      print("#" * 80)
      print("\n\nError!!!\n\n")
      print(" You need to download 'Scala API (" + apiVersion + ".zip)'\n")
      print("  and unzip to folder './docs/" + apiVersion + "'\n\n")
      print("  http://www.scala-lang.org/downloads\n\n")
      println("#" * 80)
      sys.exit(0)
    }

    makeToc
    makeHhp
    makeHhk
    makeChm
  }
}