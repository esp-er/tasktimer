package patriker.tasktimer;

import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

case class ScalaTask(project: String, tz: String, taskDate: ZonedDateTime, taskTime: Int):
    def getDateStr(): String = return taskDate.format(ScalaTask.dateFormat)
    override def toString(): String  = {
        return taskDate.format(ScalaTask.dateFormat) + " (" + tz + ") " + "- Project: " + project + " -  " + Integer.toString(taskTime)
                + " sec"
    }


object ScalaTask:
  val dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss")
  //def apply(p: String, tz: String, date: ZonedDateTime, time: Int): Task =
  //  apply(p, tz, date, time)
  //
  //
  def apply(p: String, tz: String, tzId: String, datetime: String, secs: Int): ScalaTask = {
    val date =
    try
      Right(LocalDateTime.parse(datetime, dateFormat))
    catch
      case e: DateTimeParseException => Left(e)

    val zdate = ZonedDateTime.of(date.getOrElse(LocalDateTime.now()), ZoneId.of(tzId))
    
    apply(p, tz, zdate, secs)
  }

  def apply(p: String, secs: Int, zone: TaskTimeZone): ScalaTask = 
    apply(p, zone.getName(), ZonedDateTime.now(zone.getZoneId()), secs)


