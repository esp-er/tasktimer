package patriker.tasktimer

import os._
import javax.swing.DefaultListModel
import scala.util.control.NonFatal


case class SimpleList(val label: String, val tasks: DefaultListModel[String]){}
object TaskIO{
  /*
  def writeTask(filename: String, t: Task) = {
    val u = ujson.Obj("project" -> t.project, "timezone" -> t.tz, "date" -> t.getDateStr(), "time" -> t.taskTime)
    println(ujson.write(u))
  }*/

 def loadTasks(filePath: String): SimpleList = {
   var lines = IndexedSeq[String]()
   var result = SimpleList("File Not Read", new DefaultListModel[String]())
   try
    lines = os.read.lines(os.Path(filePath))
   catch
    case NonFatal(ex) => lines = lines :+ "#Error reading"
   finally
    val labelOpt = lines.dropWhile(!_.startsWith(" ---")).headOption
    val label: String = labelOpt.getOrElse("Unknown File Format")
    result = SimpleList(label, new DefaultListModel[String]())
    lines.filter(_.startsWith("#")).foreach{
      t => result.tasks.addElement(t)
    }

   result
 }

  def saveTasks(filePath: String, workLabel: String, tasks: Array[Task]): Unit = {
    val tasksOut = for((t,num) <- tasks.zipWithIndex) yield "#" + ((tasks.length-num)) + " " + t
    val outStr = "--- " + workLabel + "--- \n\n" + tasksOut.mkString("\n") + "\n"
    try
      os.write.over(os.Path(filePath), outStr)
    catch 
      case NonFatal(ex) => println("Could Not Write file!")
  }

  def autoSave(projName: String, workLabel: String, tasks: Array[Task]): Unit = {
    if(tasks.length >= 1){
      val saveDir = "backup"
      val tasksOut = for((t,num) <- tasks.zipWithIndex) yield "#" + ((tasks.length-num)) + " " + t
      val outStr = "자동저장:\n --- " + workLabel + "--- \n\n" + tasksOut.mkString("\n") + "\n"
      val startDate = tasks(0).getDateStr()
      val dirName = startDate.take(10).replace("/", "-")
      val fileName = projName + ".txt"
      if (!os.exists(os.pwd / saveDir / dirName)){
        os.makeDir.all(os.pwd / saveDir / dirName)
      }

      def determineOutFile(): os.Path = {
          val list = os.list(os.pwd / saveDir / dirName).filter(_.last.startsWith((projName)))
          if(list.length == 0)
            os.pwd / saveDir / dirName / fileName
          else
            list.sortWith((p1,p2) => os.stat(p1).mtime.toInstant.isAfter(os.stat(p2).mtime.toInstant)).head
      }

      if(tasks.length == 1){
        val outFil = determineOutFile()
        if(!os.exists(os.pwd / saveDir / dirName / fileName))
          try
            os.write.over(os.pwd / saveDir / dirName / fileName, outStr)
          catch 
            case NonFatal(ex) => println("Could not backup file!")
        else{
          val filNum = outFil.last match { //FIX this to include arbitrary number of - in file
            case s"$name-$m-$m2-$num.txt" => if(num.exists(!_.isDigit)) 1 else num.toInt + 1
            case s"$name-$m-$num.txt" => if(num.exists(!_.isDigit)) 1 else num.toInt + 1
            case s"$name-$num.txt" => if(num.exists(!_.isDigit)) 1 else num.toInt + 1
            case _ => 1
          }
          val f = projName + "-" + filNum.toString() + ".txt"
          try
            os.write.over(os.pwd / saveDir / dirName / f, outStr) 
          catch 
            case NonFatal(ex) => println("Could not backup file!")
        }
      }
      else{
        try
          os.write.over(determineOutFile(), outStr)
        catch 
          case NonFatal(ex) => println("Could not backup file!")
      }
    }
  }
}

