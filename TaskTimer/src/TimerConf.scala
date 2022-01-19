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
case class TimerConfig(project: String, colors: ColorConfig, keypad: List[String], winpos: (Int,Int), taskfile: String) extends AppConf{
  override def toString: String = {
    "type=timer-config \n" +
    "project=" + project.mkString("\"","","\"") + "\n" +
    colors.toString +
    "keypad=" + keypad.map(_.mkString("\"","","\"")).mkString("[ ", ",", " ]\n") +
    "winpos=" + s"[${winpos(0)},${winpos(1)}]\n" +
    "taskfile=" + taskfile + "\n"
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
    case Left(_) => TimerSize(460,600,40,0,0,0)
    }
  }

  val colors = timerSrc match{
    case Right(TimerConfig(_, colors, _,_,_)) => colors
    case _ => ColorConfig("#ffafd3", "#ffdbf5")
  }

  val keypad = timerSrc match{
    case Right(TimerConfig(_,_,keypad,_,_)) => keypad
    case _ => List("60s", "90s", "120s", "180s", "240s", "300s", "360s", "420s", "900s");
  }

  val project = timerSrc match{
    case Right(TimerConfig(project,_,_,_,_)) => project
    case Left(_) => "기본"
  }

  val posx = timerSrc match{
    case Right(TimerConfig(_,_,_,pos,_)) => pos(0)
    case Left(_) => 0
  }

  val posy = timerSrc match{
    case Right(TimerConfig(_,_,_,pos,_)) => pos(1)
    case Left(_) => 0
  }


  val currFile = timerSrc match{
    case Right(TimerConfig(_,_,_,_, file)) => file
    case Left(_) => ""
  }


  val keyArr = new ArrayList[String](keypad.asJava)

  def write(project: String, newKeys: ArrayList[String], background: String, buttons: String, posx:Int=0, posy:Int=0, currFile: String): Unit = {
    val newConf = TimerConfig(project, ColorConfig(background, buttons), newKeys.asScala.toList, (posx, posy), currFile)
    //os.write.over(os.pwd / className / "resources" / "timer.conf", newConf.toString)
    os.write.over(os.pwd /  "timer.conf", newConf.toString)
  }
}
