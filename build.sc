import mill._, scalalib._
import mill.modules.Assembly

//Proguard module, might be useful in future(curr. only works scala 2.12)
//import $ivy.`com.lihaoyi::mill-contrib-proguard:$MILL_VERSION`
//import contrib.proguard._

object TaskTimer extends ScalaModule{
  def scalaVersion = "3.0.2"
  def ammoniteVersion = "2.4.0-18-12c9e33e"
  def scalacOptions = Seq("-no-indent")
  def resolutionCustomizer = T.task {
    Some((r: coursier.core.Resolution) =>
      r.withOsInfo(coursier.core.Activation.Os.fromProperties(sys.props.toMap))
    )
  }

  // Add dependency on JavaFX libraries
  val javaFXVersion = "16"
  val scalaFXVersion = "16.0.0-R24"
  val javaFXModules = List("base", "media", "controls", "fxml", "graphics", "swing").map(m => ivy"org.openjfx:javafx-$m:$javaFXVersion")

  def ivyDeps = Agg(
    ivy"com.lihaoyi::os-lib:0.7.8",
    ivy"com.lihaoyi::upickle:1.4.2",
    ivy"org.scalafx::scalafx:$scalaFXVersion",
    ivy"com.github.pureconfig::pureconfig-core:0.16.0",
    ivy"org.kordamp.ikonli:ikonli-javafx:12.2.0",
    ivy"org.kordamp.ikonli:ikonli-antdesignicons-pack:12.2.0",
    ivy"org.kordamp.ikonli:ikonli-openiconic-pack:12.2.0",
    ivy"org.kordamp.ikonli:ikonli-materialdesign2-pack:12.2.0",
    ivy"org.kordamp.ikonli:ikonli-swing:12.2.0",
    ivy"com.formdev:flatlaf:1.6"
    //ivy"org.jfxtras:jmetro:11.6.15"
    //ivy"com.lihaoyi::castor:0.1.8",
  ) ++ javaFXModules

  //Use when excluding javafx from fatjar
  //def compileIvyDeps ={ Agg() ++javaFXModules }

	def forkArgs = Seq("--add-opens=java.desktop/java.awt=ALL-UNNAMED")
	//def forkEnv = Map("--add-opens java.desktop/java.awt=ALL-UNNAMED")

  override def assemblyRules = Seq(
    Assembly.Rule.Exclude("timer.conf"),
    Assembly.Rule.Exclude("sizes.conf"),
    //Assembly.Rule.Exclude("libgstreamer-lite.so"),
    Assembly.Rule.Append("META-INF/services/org.kordamp.ikonli.IkonProvider"), //Fix resolving IkonHandlers
    Assembly.Rule.Append("META-INF/services/org.kordamp.ikonli.IkonHandler")
  )

  override def manifest = super.manifest().add("Class-Path" -> ".")
  //def forkEnv = Map("DISPLAY" -> ":1")
  //def forkArgs = Seq("-XX:TieredStopAtLevel=1")
  //def javacOptions = Seq("--XX:TieredStopAtLevel=1")
}

