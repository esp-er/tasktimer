package patriker.tasktimer;

import javax.swing.Timer;
import java.util.*;
import java.awt.event.*;


public class TaskClock{

  private Timer timer;
  private List<ClockTickListener> clockListeners = new ArrayList<ClockTickListener>();
  private List<ActionListener> startListeners = new ArrayList<ActionListener>();

  private int timeTotal;
  private int timeRemaining;

  private boolean paused;

  public TaskClock(int t, ClockTickListener l, ActionListener start){
    paused = false;
    timeTotal =  t;
    timeRemaining = timeTotal;
    clockListeners.add(l);
    startListeners.add(start);
    timer = new Timer(1000, new TickListener());
  }

  public TaskClock(int t, ClockTickListener l){
    paused = false;
    timeTotal =  t;
    timeRemaining = timeTotal;
    clockListeners.add(l);
    timer = new Timer(1000, new TickListener());
  }


  public TaskClock(int t, List<ClockTickListener> li){
    paused = false;
    timeTotal =  t;
    timeRemaining = timeTotal;
    clockListeners.addAll(li);
    timer = new Timer(1000, new TickListener());
  }


  public TaskClock(){
    paused=false;
    timeRemaining = 0;
    timeTotal = 0;
    timer = new Timer(1000, new TickListener());
  }

  public int getTimeRemaining(){
    return timeRemaining;
  }
  public int getTimeElapsed(){
    return timeTotal - timeRemaining;
  }
  public int getCountSeconds(){
    return timeTotal;
  }
  public void setCountSeconds(int t){
    timeTotal = t;
    timeRemaining = t;
  }

  public void reset(){
    paused = false;
    timeRemaining = timeTotal;
    timer.stop();
  }

  public void start(){
    paused = false;
    timer.start();
    for(var l : startListeners)
      l.actionPerformed(new ActionEvent(this, 0, "timer start"));
  }

  public void pause(){
    paused = true;
    timer.stop();
  }
  public void stop(){
    timer.stop();
  }
  public boolean isPaused(){
    return paused;
  }

  public void addStartListener(ActionListener l){
    startListeners.add(l);
  }


  private class TickListener implements ActionListener{
    public void actionPerformed(ActionEvent a){
      if(timeRemaining >= 2){
        timeRemaining -= 1;
        for(var ct : clockListeners){
          ct.Tick(timeRemaining);
        }
      }
      else{
        timeRemaining = 0;
        timer.stop();
        for(var ct: clockListeners){
          ct.finalTick();
        }
      }

    }
  }

}
