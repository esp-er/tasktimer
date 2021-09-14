package patriker.tasktimer

import scala.collection.JavaConverters._
import java.util.ArrayList
import pureconfig._
import pureconfig.generic.derivation.default._
import os._

sealed trait SizeConf derives ConfigReader
case class TimerSize(
  width: Int,
  height: Int,
  timersize: Int,
  tasksize: Int,
  buttonsize: Int,
  iconsize: Int
) extends SizeConf


sealed trait AppConf derives ConfigReader
case class TimerConfig(project: String, colors: ColorConfig, keypad: List[String]) extends AppConf{
  override def toString: String = {
    "type=timer-config \n" +
    "project=" + project.mkString("\"","","\"") + "\n" + 
    colors.toString + 
    "keypad=" + keypad.map(_.mkString("\"","","\"")).mkString("[ ", ",", " ]")
  }
}

case class ColorConfig(background: String, buttons: String){
  override def toString: String = { 
    "colors.buttons=" + "\"" + buttons + "\"\n" +
    "colors.background=" + "\"" + background + "\"\n"
  }
}

object TimerConf{
  private val sizeSrc = ConfigSource.resources("sizes.conf").load[SizeConf]
  private val timerSrc = ConfigSource.resources("timer.conf").load[AppConf]


  val sizes = {
    sizeSrc match{
    case Right(c @ TimerSize(_,_,_,_,_,_)) => c
    case Left(_) => TimerSize(0,0,0,0,0,0)
    }
  }

  val colors = timerSrc match{
    case Right(TimerConfig(_, colors, _)) => colors
    case _ => ColorConfig("#ffafd3", "#ffdbf5")
  }

  val keypad = timerSrc match{
    case Right(TimerConfig(_,_,keypad)) => keypad
    case _ => List.fill(9)("0s")
  }

  val project = timerSrc match{
    case Right(TimerConfig(project,_,_)) => project
    case Left(_) => "기본"
  }

  val keyArr = new java.util.ArrayList[String](keypad.asJava)

  def write(project: String, newKeys: java.util.ArrayList[String], background: String, buttons: String, className: String = "TaskTimer" ): Unit = {
    val newConf = TimerConfig(project, ColorConfig(background, buttons), newKeys.asScala.toList) 
    //os.write.over(os.pwd / className / "resources" / "timer.conf", newConf.toString)
    os.write.over(os.pwd /  "timer.conf", newConf.toString)
  }
}
