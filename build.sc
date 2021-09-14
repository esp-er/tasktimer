import mill._, scalalib._
import mill.modules.Assembly



object TaskTimer extends ScalaModule{
  def scalaVersion = "3.0.1"
  def ivyDeps = Agg(
    ivy"com.lihaoyi::os-lib:0.7.8",
    //ivy"com.lihaoyi::castor:0.1.8",
    ivy"com.lihaoyi::upickle:1.4.0",
    ivy"org.kordamp.ikonli:ikonli-antdesignicons-pack:12.2.0",
    ivy"org.kordamp.ikonli:ikonli-openiconic-pack:12.2.0",
    ivy"org.kordamp.ikonli:ikonli-swing:12.2.0",
    ivy"com.github.pureconfig::pureconfig-core:0.16.0"
  )


  override def assemblyRules = Seq(
    Assembly.Rule.Exclude("timer.conf"),
    Assembly.Rule.Exclude("sizes.conf"),
    Assembly.Rule.Append("META-INF/services/org.kordamp.ikonli.IkonProvider"), //Fix resolving IkonHandlers
    Assembly.Rule.Append("META-INF/services/org.kordamp.ikonli.IkonHandler")
  )

  override def manifest = super.manifest().add("Class-Path" -> ".")
  //def forkEnv = Map("DISPLAY" -> ":0.0")
  //def forkArgs = Seq("--XX:TieredStopAtLevel=1")
  //def javacOptions = Seq("--XX:TieredStopAtLevel=1")
}

