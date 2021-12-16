package patriker.tasktimer

import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import scalafx.beans.property._
import scalafx.scene.paint.Color

case class Task(project: String, tz: String, taskDate: ZonedDateTime, taskTime: Int, colorBg: String, colorFg: String){
  val projectProp = new StringProperty(this, "project", project)
  val tzProp = new StringProperty(this, "tz", tz)
  val timeProp = new ObjectProperty[Int](this, "taskTime", taskTime)
  val dateProp = new ObjectProperty[ZonedDateTime](this, "taskDate", taskDate)
  val colorProp = new ObjectProperty[(Color,Color)](this, "colors", (Color.web(colorBg), Color.web(colorFg)))
  
  def getDateStr(): String = taskDate.format(Task.dateFormat)
  def getTimeStr(): String = taskDate.format(Task.timeFormat)

  override 
  def toString(): String = 
    taskDate.format(Task.dateFormat) + " (" + tz + ") " + "- Project: " + project + " -  " + taskTime.toString
    + " sec"
}



object Task{
  val dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss")
  val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
  //def apply(p: String, tz: String, date: ZonedDateTime, time: Int): Task =
  //  apply(p, tz, date, time)
  //
  //
  def apply(p: String, tz: String, tzId: String, datetime: String, secs: Int, color1: String, color2: String): Task = {
    val date =
      try
        Right(LocalDateTime.parse(datetime, dateFormat))
      catch
        case e: DateTimeParseException => Left(e)

    val zdate = ZonedDateTime.of(date.getOrElse(LocalDateTime.now()), ZoneId.of(tzId))

    apply(p, tz, zdate, secs, color1, color2)
  }

  def apply(p: String, secs: Int, zone: TaskTimeZone, colorBg: String, colorFg: String): Task = 
    apply(p, zone.getName(), ZonedDateTime.now(zone.getZoneId()), secs, colorBg, colorFg)

  def apply(p: String, secs: Int, zone: TaskTimeZone): Task = 
    apply(p, zone.getName(), ZonedDateTime.now(zone.getZoneId()), secs, "#000", "#000")
}

