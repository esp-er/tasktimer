package patriker.tasktimer

//import scalafx.stage.Stage
import scalafx.scene.Scene
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.control.{TableView, TableColumn, TableCell}
import scalafx.scene.layout.{VBox, HBox}
import scalafx.Includes._
import scalafx.scene.paint._
import scalafx.scene.shape.{Rectangle, Circle}
import scalafx.scene.input._
import scalafx.scene.control.SelectionMode.Multiple
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.beans.property._
import scalafx.scene.control.TextField
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ContextMenu, MenuItem }
import scalafx.print.PrintColor
import scalafx.beans.binding.BooleanBinding
import scalafx.beans.binding.Bindings
import scalafx.geometry.Pos
import scalafx.geometry.Insets
import scalafx.scene.control.ScrollPane

import scala.language.postfixOps

import java.awt.Font
import java.awt.Dimension


import javax.swing.ScrollPaneConstants
import javafx.collections.ObservableList
import javafx.embed.swing.JFXPanel
import javafx.beans.property.{SimpleIntegerProperty => JIntProp}
import javafx.beans.property.{SimpleDoubleProperty => JDoubleProp}
//import javafx.beans.property.{StringProperty => JStringProp}
import javafx.beans.property.SimpleStringProperty
import java.time.ZonedDateTime

import org.kordamp.ikonli.javafx.FontIcon
//import jfxtras.styles.jmetro.{JMetro, Style}

object TableIcons{
  //val clock = FontIcon("mdi2c-clock:16")
  lazy val date = new FontIcon("mdi2c-calendar-month:16")
  lazy val clock = new FontIcon("mdi2t-timelapse:16")
  lazy val color = new FontIcon("mdi2p-palette:16")
  lazy val zone = new FontIcon("mdi2w-web:16")
}

object TableStyle{
  def fontsize = "-fx-font-size : 14"
}

object viewHelper{
  def getLabel(n: Int, total: Int): String = {
    if (total > 0){
      val hrstr = HelperFunctions.hrStr(total)
      val minstr = HelperFunctions.minStr(total)
      val secstr = total.toString + " 초"

      " :: " + HelperFunctions.sectoHrMinSec(total) + " ㅣ" + hrstr + "ㅣ"
      + " ㅣ" + minstr + "ㅣ"
      + " ㅣ" + secstr + "ㅣ:: "
    }
    else{
      "No tasks completed."
    }
  }
}

class TaskTable{
  val panelWidth = new JDoubleProp(400.0)
  val panelHeight = new JDoubleProp(600.0)
  val totalWorkProp = new JIntProp(0)
  val totalTasksProp = new JIntProp(0)
  val totalDeletedProp = new JIntProp(0)
  val numSelectedProp = new JIntProp(0)
  val labelStr = new StringProperty("Empty")
  private val highlightColor = new SimpleStringProperty("-fx-accent: #2862eb; -fx-focus-color: #2862eb")


  def setAccentColor(hex: String) = highlightColor.setValue(
            //s"-fx-accent: ${hex}; -fx-focus-color: ${hex}")
            s"-fx-accent: derive(${hex}, -4%); -fx-focus-color: ${hex}")
  
  //private val taskBuffer = ObservableBuffer[Task](TaskIO.loadTaskList()*)
  private var taskBuffer = ObservableBuffer[Task]()
  taskBuffer.onChange{
    val sum = taskBuffer.foldLeft(0)(_ + _.taskTime)
    totalWorkProp.setValue(sum)
    totalTasksProp.setValue(taskBuffer.length)
    labelStr.setValue(viewHelper.getLabel(taskBuffer.length, sum))
  }
  

  private var deletedBuffer = ObservableBuffer[Task]()
  deletedBuffer.onChange{
    totalDeletedProp.setValue(deletedBuffer.length)
  }
  
  def loadItems(pwd: Boolean = true, fileName: String) = {
    val tasks = TaskIO.loadTasksJson(curdir=pwd, filePath=fileName)
    taskBuffer ++= tasks
  }

  def deselect() = taskList.selectionModel().clearSelection()

  def createNew(): Unit = { 
    taskBuffer.removeRange(0, taskBuffer.length) 
    deletedBuffer.removeRange(0, deletedBuffer.length)
  }

  def addItem(task: Task): Unit = { 
    taskBuffer.prepend(task)
    taskList.selectionModel().clearSelection()
  }

  def removeItem(): Unit = {
    val toDel = taskList.selectionModel().getSelectedIndices.toList
    deletedBuffer.clear()
    toDel.foreach(i => deletedBuffer.add(this.taskBuffer.get(i)))
    
    def rem(li: Seq[Integer], acc: Int = 0): Unit =
      li match{
        case Nil => ()
        case Seq(head, tail*) => this.taskBuffer.remove(head - acc); rem(tail, acc + 1) 
      }
    rem(toDel)

    taskList.selectionModel().clearSelection()
  }

  def restoreItems(): Unit = {
    deletedBuffer.foreach(taskBuffer.add(_))
    taskBuffer.sort((x,y) => x.taskDate isAfter y.taskDate)
    deletedBuffer.clear()
  }
  
  def getLength(): Int = taskBuffer.length

  def getTaskArray(): Array[Task] = taskBuffer.toArray[Task]

  private val sampleContextMenu = new ContextMenu {
    items ++= Seq(
      new MenuItem("MenuItemA") {
        onAction = e => println(s"${e.eventType} occurred on Menu Item A")
      },
      new MenuItem("MenuItemB") {
        onAction = e => println(s"${e.eventType} occurred on Menu Item B")
      }
      )
  }

  private val tableItemMenu = new ContextMenu {
    items ++= Seq(
      new MenuItem("Remove") {
        onAction = e => println(s"${e.eventType} occurred on Menu Item A")
      }
      )
  }

    //TODO: Fix this mutable variable workaround
    var taskList: TableView[Task] = null
    def taskTableView() = new TableView[Task](taskBuffer) {
      style = TableStyle.fontsize
      prefHeight <== panelHeight
      prefWidth <== panelWidth
      //columnResizePolicy = TableView.ConstrainedResizePolicy
      selectionModel().selectionMode = Multiple
      selectionModel().selectedItems.onChange{
        val sel = selectionModel().selectedIndices.length
        numSelectedProp.set(sel)
        if(sel > 1){
          val sum = selectionModel().selectedItems.foldLeft(0)(_ + _.taskTime)
          totalWorkProp.set(sum)
          totalTasksProp.set(sel)
        }
        else{
          val sum = taskBuffer.foldLeft(0)(_ + _.taskTime)
          totalWorkProp.set(sum)
          totalTasksProp.set(taskBuffer.length)
        }
      }
      //editable = true
      //contextMenu = sampleContextMenu
      columns ++=  Seq(
        new TableColumn[Task, ZonedDateTime]{
          //contextMenu = sampleContextMenu
          //editable = true
          text = "#"
          sortable = false
          cellValueFactory = _.value.dateProp
          //graphic = TableIcons.clock
          cellFactory = {
            (_ : TableColumn[Task, ZonedDateTime]) => new TableCell[Task, ZonedDateTime]{
              item.onChange{
                val idx = taskBuffer.size - this.index.value
                text = if idx > 0 then idx.toString else ""
              }
            }
          }
        },
        new TableColumn[Task, Int]{
          //contextMenu = sampleContextMenu
          //editable = true
          text = ""
          graphic = TableIcons.clock
          cellValueFactory = _.value.timeProp
          cellFactory = {
            (_ : TableColumn[Task, Int]) => new TableCell[Task, Int]{
              alignment = Pos.Center
              item.onChange{ (_,_,newTime) =>
              text = if newTime != 0 then newTime.toString + "s" else ""}
            }
          }
        },
        new TableColumn[Task, String] {
          //contextMenu = sampleContextMenu
          editable = true
          text = "Project"
          cellValueFactory = {
            _.value.projectProp
          }
          cellFactory = {
            (x: TableColumn[Task,String]) => new TableCell[Task, String]{
              item.onChange{ (projectObs,_,_) =>
                contextMenu = sampleContextMenu
                text = projectObs.value
              }
            }
          }
        },
        new TableColumn[Task, ZonedDateTime] {
          contextMenu = tableItemMenu
          graphic = TableIcons.date
          text = "Date"
          cellValueFactory = {_.value.dateProp }
          cellFactory = {
            (x: TableColumn[Task,ZonedDateTime]) => new TableCell[Task, ZonedDateTime]{
              item.onChange{ (_,_,newDate) =>
                contextMenu = sampleContextMenu
                text = 
                  if(newDate != null)
                    if(newDate.getZone == ZonedDateTime.now.getZone)
                      if(newDate.getDayOfYear == ZonedDateTime.now.getDayOfYear)
                        "오늘 - " + newDate.format(Task.timeFormat)
                      else if(newDate.getDayOfYear == ZonedDateTime.now.minusDays(1).getDayOfYear)
                        "어제 - " + newDate.format(Task.timeFormat)
                      else
                        newDate.format(Task.dateFormat)
                      else
                        newDate.format(Task.dateFormat)
                      else
                        null
              }
            }
          }
        },
        new TableColumn[Task, (Color, Color)]{
          text = ""
          sortable = false
          graphic = TableIcons.color 
          // What should be used as the value of the cell
          cellValueFactory = _.value.colorProp
          cellFactory = { (x: TableColumn[Task, (Color, Color)]) =>
            new TableCell[Task, (Color, Color)] {
              alignment = Pos.Center
              text = ""
              item.onChange { (_, _, newColor) =>
                graphic = if (newColor != null){
                  val circ1 = Circle(radius = 6, fill = newColor._1)
                  val circ2 = Circle(radius = 6, fill = newColor._2)
                  new HBox(){alignment = Pos.Center
                  children = Seq(circ1, circ2); spacing = 1.0}
                }
                else{
                  null
                }
              }
            }
          }
        },
        new TableColumn[Task, String]{
          contextMenu = tableItemMenu
          text = "Zone"
          graphic = TableIcons.zone
          cellValueFactory = {_.value.tzProp}
          cellFactory = {
            (x: TableColumn[Task,String]) => new TableCell[Task, String]{
              item.onChange{ (tzObs,_,_) =>
                contextMenu = sampleContextMenu
                text = tzObs.value
              }
            }
          }
        }
        )
    }

  def setHeight(v: Double) = panelHeight.setValue(v)
  def setWidth(v: Double) = panelWidth.setValue(v)

  def init(fxPanel: JFXPanel, startTime: Long, viewMode: Boolean = false) = {
    Platform.runLater{
      taskList = taskTableView()
      lazy val taskLabel = new TextField(){
                text <== labelStr
                alignment = Pos.Center
                focusTraversable = false
                editable = false
                padding = Insets(4)}
      var cont = if(viewMode == true) Seq(taskLabel, taskList) else Seq(taskList)
      val taskScene = new Scene {
          stylesheets += this.getClass.getResource("/css/flatbee.css").toExternalForm
          content = new VBox(){
            style <== highlightColor 
            alignment = Pos.Center
            prefHeight <== panelHeight
            maxHeight <== panelHeight
            maxWidth <== panelWidth
            prefWidth <== panelWidth
            children = cont 
          }
        }
      println("Javafx start in: " + (System.currentTimeMillis() - startTime))
      fxPanel.setScene(taskScene)
      //new JMetro(Style.LIGHT).setScene(taskScene)
      //println(fxPanel.getSize.width + " " + fxPanel.getSize.height)
    }
  }
}
