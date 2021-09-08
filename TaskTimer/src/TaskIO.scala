package patriker.tasktimer

import os._
import javax.swing.DefaultListModel
import scala.util.control.NonFatal
import upickle.default.{read, write, ReadWriter, macroRW, readwriter}
import ScalaTask._

case class SimpleList(val label: String, val tasks: DefaultListModel[String]){}
object TaskIO{

   private val partStr: PartialFunction[ujson.Value, Option[String]] = {
     case ujson.Str(s) => Some(s)
     case _ => None}
   private val partNum: PartialFunction[ujson.Value, Option[Int]] = {
     case ujson.Num(x) => Some(x.toInt)
     case _ => None}
   //implicit def NamesRw: upickle.default.ReadWriter[Name] = upickle.default.macroRW
   implicit val namesRw: ReadWriter[ScalaTask] = readwriter[ujson.Value].bimap[ScalaTask](
     task => ujson.Obj("project" -> task.project, 
       "tz" -> task.tz, 
       "tzId" -> task.taskDate.getZone.toString,
       "date" -> task.getDateStr(),
       "seconds" -> task.taskTime),
     jsval => ScalaTask(
       jsval.obj.get("project").flatMap(partStr).getOrElse("Unknown"),
       jsval.obj.get("tz").flatMap(partStr).getOrElse("Korea - KST"),
       jsval.obj.get("tzId").flatMap(partStr).getOrElse("Asia/Seoul"), 
       jsval.obj.get("date").flatMap(partStr).getOrElse("Not a date"),
       jsval.obj.get("seconds").flatMap(partNum).getOrElse(0))
     )

   def loadTasksJson(filePath: String = "tasks.json"): DefaultListModel[ScalaTask] = { 
     val data = 
       try 
         Right(os.read(os.pwd / filePath))
       catch
         case NonFatal(e) => Left(e)
     var result = new DefaultListModel[ScalaTask]()
     val taskList = upickle.default.read[Seq[ScalaTask]](data.getOrElse("[{}]"))
     taskList.foreach(result.addElement)
     result
   }

   def writeTasksJson(filePath: String = "tasks.json", tasks: Array[ScalaTask]): Unit = {
     val out = upickle.default.write[Seq[ScalaTask]](tasks.toIndexedSeq)
     try{
       os.write(os.pwd / filePath, out)
     }
     catch{
       case NonFatal(e) => os.write(os.pwd / "error.json", "Error writing tasks.json")
     }
     finally{
       println("Could not write any tasks!" + out)
     }
   }

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

