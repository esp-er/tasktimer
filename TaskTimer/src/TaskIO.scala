package patriker.tasktimer

import os._
import javax.swing.DefaultListModel
import scala.util.control.NonFatal
import upickle.default.{read, write, ReadWriter, macroRW, readwriter}

case class SimpleList(val label: String, val tasks: DefaultListModel[String]){}

object TaskIO {
   private val partStr: PartialFunction[ujson.Value, Option[String]] = {
     case ujson.Str(s) => Some(s)
     case _ => None}
   private val partNum: PartialFunction[ujson.Value, Option[Int]] = {
     case ujson.Num(x) => Some(x.toInt)
     case _ => None}
   //implicit def NamesRw: upickle.default.ReadWriter[Name] = upickle.default.macroRW
   implicit val namesRw: ReadWriter[Task] = readwriter[ujson.Value].bimap[Task](
     task => ujson.Obj("project" -> task.project, 
       "tz" -> task.tz, 
       "tzId" -> task.taskDate.getZone.toString,
       "date" -> task.getDateStr(),
       "seconds" -> task.taskTime,
       "colorBg" -> task.colorBg,
       "colorFg" -> task.colorFg),
     jsval => Task(
       jsval.obj.get("project").flatMap(partStr).getOrElse("Unknown"),
       jsval.obj.get("tz").flatMap(partStr).getOrElse("Korea - KST"),
       jsval.obj.get("tzId").flatMap(partStr).getOrElse("Asia/Seoul"), 
       jsval.obj.get("date").flatMap(partStr).getOrElse("Not a date"),
       jsval.obj.get("seconds").flatMap(partNum).getOrElse(0),
       jsval.obj.get("colorBg").flatMap(partStr).getOrElse("#000"),
       jsval.obj.get("colorFg").flatMap(partStr).getOrElse("#000"))
   )

   def loadTaskList(filePath: String): List[Task] = { 
     val data = 
       try 
         Right(os.read(os.Path(filePath)))
       catch
         case NonFatal(e) => Left(e)
     val taskList = upickle.default.read[Seq[Task]](data.getOrElse("[{}]"))
     taskList.filter(_.taskTime!=0).toList
   }


   def loadTasksJson(curdir: Boolean = true, filePath: String = "tasks.mim"): Seq[Task] = { 
     val fp =
       if (curdir)
         os.pwd / filePath
       else
         os.Path(filePath)
     println(fp)
     val data = 
       try 
         Right(os.read(fp))
       catch
         case NonFatal(e) => Left(e)

     val taskList = upickle.default.read[Seq[Task]](data.getOrElse("[{}]"))
     taskList.filter(_.taskTime != 0)
   }

   def writeTasksJson(curdir: Boolean = true, filePath: String = "tasks.mim", tasks: Array[Task]): String = {
     val out = upickle.default.write[Seq[Task]](tasks.toIndexedSeq)
     val fp =
       if (curdir)
         os.pwd / "SAVE" / filePath
       else
         os.Path(filePath)
     try{
       os.write.over(fp, out)
       fp.toString
     }
     catch{
       case NonFatal(e) => os.write.over(os.pwd / "error.json", "Error writing " + filePath)
       ""
     }
     fp.toString
   }

   def loadTasks(filePath: String): SimpleList = {
     var lines = IndexedSeq[String]()
     var result = SimpleList("File Not Read", new DefaultListModel[String]())
     try{
       lines = os.read.lines(os.Path(filePath))
     }catch{
       case NonFatal(ex) => lines = lines :+ "#Error reading"
     }
     finally{
       val labelOpt = lines.dropWhile(!_.startsWith(" ---")).headOption
       val label: String = labelOpt.getOrElse("Unknown File Format")
       result = SimpleList(label, new DefaultListModel[String]())
       lines.filter(_.startsWith("#")).foreach{
         t => result.tasks.addElement(t)
       }
     }
     result
   }

   def saveTasksTxt(filePath: String, workLabel: String, tasks: Array[Task]): Unit = {
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
        val list = os.list(os.pwd / saveDir / dirName).filter((x:Path) => x.last.startsWith((projName)) && !(x.last.endsWith(".json")))
          if(list.length == 0)
            os.pwd / saveDir / dirName / fileName
          else
            list.sortWith((p1,p2) => os.stat(p1).mtime.toInstant.isAfter(os.stat(p2).mtime.toInstant)).head
      }

      if(tasks.length == 1){
        val outFil = determineOutFile()
        if(!os.exists(os.pwd / saveDir / dirName / fileName)){
          try
            os.write.over(os.pwd / saveDir / dirName / fileName, outStr)
          catch 
            case NonFatal(ex) => println("Could not backup file!")
          val jsonName = projName + ".json"
          writeTasksJson(false, (os.pwd / saveDir / dirName / jsonName).toString, tasks)
        }
        else{
          val filNum = outFil.last match { //FIX this to include arbitrary number of - in file
            case s"$name-$m-$m2-$num.txt" => if(num.exists(!_.isDigit)) 1 else num.toInt + 1
            case s"$name-$m-$num.txt" => if(num.exists(!_.isDigit)) 1 else num.toInt + 1
            case s"$name-$num.txt" => if(num.exists(!_.isDigit)) 1 else num.toInt + 1
            case _ => 1
          }
          val f = projName + "-" + filNum.toString() + ".txt"
          val fjson = projName + "-" + filNum.toString() + ".json"
          try
            os.write.over(os.pwd / saveDir / dirName / f, outStr) 
          catch 
            case NonFatal(ex) => println("Could not backup file!")

          os.write.over(os.pwd / saveDir / dirName / f, outStr) 
          writeTasksJson(false, (os.pwd / saveDir / dirName / fjson).toString(), tasks)
        }
      }
      else{
        try
          os.write.over(determineOutFile(), outStr)
        catch 
          case NonFatal(ex) => println("Could not backup file!")

        val fjson = determineOutFile().toString.dropRight(4) + ".json"

        writeTasksJson(false, fjson, tasks)

      }
    }
  }
}

