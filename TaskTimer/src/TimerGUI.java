package patriker.tasktimer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Taskbar.State;
import java.awt.Toolkit;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.awt.Insets;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.NumberFormatter;


public class TimerGUI{
  
  protected JPanel timePanel;

  private ImageIcon playIcon, restartIcon, pauseIcon, checkIcon, cancelIcon;
  private JButton restartBtn, pauseBtn, cancelBtn; 
  private PlayPauseBtn playBtn;
  private SubmitBtn submitBtn;
  private JFormattedTextField timeText;
  private ArrayList<AbstractButton> timerButtons;

  private ArrayList<ActionListener> startListeners, pauseListeners, restartListeners, submitListeners, cancelListeners, fieldListeners;
  static Color ButtonColor, BackColor;

  public TaskClock clock;

  private static Color GREEN = new Color(149, 255, 147);
  private static Color GREY = new Color(96, 96, 96);
  //Main constructor
  public TimerGUI(MouseListener m){
    
    var sizes = TimerConf.sizes();
    var colors = TimerConf.colors();
    var iconsize = sizes.iconsize();
    var scaling_hints = java.awt.Image.SCALE_SMOOTH;
    
    ButtonColor = Color.decode(colors.buttons());
    BackColor = Color.decode(colors.background());
   

    timePanel = new JPanel();
    timePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
    timePanel.setBackground(BackColor);
    timePanel.addMouseListener(m);

    setupListeners();

    //TIMER_FONTSIZE = sizes.timersize();

    playIcon = new ImageIcon(this.getClass().getResource("/png/play.png"));
    Image newimg = playIcon.getImage().getScaledInstance(iconsize, iconsize, scaling_hints);
    playIcon = new ImageIcon(newimg);

    restartIcon = new ImageIcon(this.getClass().getResource("/png/restart.png"));
    newimg = restartIcon.getImage().getScaledInstance(iconsize, iconsize, scaling_hints);
    restartIcon = new ImageIcon(newimg);
    
    pauseIcon = new ImageIcon(this.getClass().getResource("/png/pause.png"));
    newimg = pauseIcon.getImage().getScaledInstance(iconsize, iconsize, scaling_hints);
    pauseIcon = new ImageIcon(newimg);
   
    checkIcon = new ImageIcon(this.getClass().getResource("/png/check.png"));
    newimg = checkIcon.getImage().getScaledInstance(iconsize, iconsize, scaling_hints);
    checkIcon = new ImageIcon(newimg);

    cancelIcon = new ImageIcon(this.getClass().getResource("/png/cancel2.png"));
    newimg = cancelIcon.getImage().getScaledInstance(iconsize, iconsize, scaling_hints);
    cancelIcon = new ImageIcon(newimg);

    setupText(sizes.timersize());
    setupButtons();


    timeText.addActionListener(new TimeTextListener());

  }

  private void setupListeners(){
    startListeners = new ArrayList<ActionListener>();
    pauseListeners = new ArrayList<ActionListener>();
    restartListeners = new ArrayList<ActionListener>();
    submitListeners = new ArrayList<ActionListener>();
    fieldListeners = new ArrayList<ActionListener>();
    cancelListeners = new ArrayList<ActionListener>();
  }

  private void setupButtons(){

    // Set up Task list window
    var buttonInsets = new Insets(5,5,5,5);

    // Set up timer control buttons
    playBtn = new PlayPauseBtn(playIcon, pauseIcon);
    playBtn.setBackground(ButtonColor);
    playBtn.addActionListener( (ActionEvent ev) -> {
      if(!playBtn.playing){
        playBtn.setPlaying();
        timeText.setEnabled(false);
        cancelBtn.setVisible(true);
        submitBtn.setEnabled(true);
        for(var l : startListeners) l.actionPerformed(new ActionEvent(this, 1, "pressed play"));
      }
      else{ //paused
        playBtn.setPaused();
        timeText.setEnabled(true);
        playBtn.setEnabled(true);
        for(var l : pauseListeners) l.actionPerformed(new ActionEvent(this, 1, "pressed pause"));
      }
    });

    playBtn.setEnabled(false);
    playBtn.setMargin(buttonInsets);
    restartBtn = new JButton(restartIcon);
    restartBtn.setBackground(ButtonColor);

    //TODO: rewrite these anon lambdas to pass in a named private func?
    restartBtn.addActionListener((ActionEvent ev) -> {
        //Probably better to set text in TaskTimer
        //timeText.setText(HelperFunctions.secondsToMinSec(clock.getCountSeconds()));
        timeText.setEnabled(false);
        submitBtn.setEnabled(true);
        cancelBtn.setVisible(true);
        playBtn.setPlaying();
        for(var l : restartListeners) l.actionPerformed(new ActionEvent(this, 2, "pressed restart"));
    });
    restartBtn.setMargin(buttonInsets);

    submitBtn = new SubmitBtn(checkIcon, GREEN);
    //submitBtn.setBackground(ButtonColor);
    submitBtn.addActionListener((ActionEvent ev) -> {
      timeText.setEnabled(true);
      submitBtn.setEnabled(false);
      //submitBtn.setBackground(ButtonColor);
      cancelBtn.setVisible(false);
      playBtn.setPlaying();
      playBtn.setEnabled(false);
      timeText.setText("00:00");
      for (var l : submitListeners) l.actionPerformed(new ActionEvent(this, 3, "pressed submit task"));
    });
    submitBtn.setEnabled(false);
    submitBtn.setMargin(buttonInsets);


    cancelBtn = new JButton(cancelIcon);
    cancelBtn.setBackground(ButtonColor);
    cancelBtn.setVisible(false);
    cancelBtn.setMargin(buttonInsets);

    cancelBtn.addActionListener((ActionEvent e) -> {
      timeText.setText("00:00");
      timeText.setEnabled(true);
      playBtn.setPaused();
      playBtn.setEnabled(false);
      submitBtn.setEnabled(false);
      submitBtn.setBackground(ButtonColor);
      cancelBtn.setVisible(false);
      for(var l : cancelListeners) l.actionPerformed(new ActionEvent(this, 4, "pressed cancel"));
    });

    timerButtons = new ArrayList<AbstractButton>();
    timerButtons.addAll(List.of(playBtn, submitBtn, cancelBtn, restartBtn));
    
    timePanel.add(submitBtn);
    timePanel.add(timeText);
    timePanel.add(playBtn);
    timePanel.add(restartBtn);
    timePanel.add(cancelBtn);

  }

  public void addStartListener(ActionListener a){
    startListeners.add(a);
  }
  public void addPauseListener(ActionListener a){
    pauseListeners.add(a);
  }
  public void addCancelListener(ActionListener a){
    cancelListeners.add(a);
  }
  public void addFieldListener(ActionListener a){
    fieldListeners.add(a);
  }
  public void addSubmitListener(ActionListener a){
    submitListeners.add(a);
  }
  public void addRestartListener(ActionListener a){
    restartListeners.add(a);
  }

  public void fireRestart(){
    restartBtn.doClick();
  }


  private void setupText(int timerFontSize){

    var format = new SimpleDateFormat("mm:ss");
    timeText = new JFormattedTextField(format);
    var f = new Font(Font.MONOSPACED, Font.BOLD, timerFontSize);
    timeText.setFont(f);
    timeText.setText("00:00");
    timeText.setMaximumSize(timeText.getPreferredSize());

  }


  public ClockTickListener getTimerListener(){
    return new TimerListener();
  }
  private class TimerListener implements ClockTickListener{
    public void Tick(int secRemaining){

      timeText.setText(HelperFunctions.secondsToMinSec(secRemaining));
      if (secRemaining % 2 == 0) {
        Color clr = new Color(255, 79, 76);
        timeText.setDisabledTextColor(clr);
      } else {
        timeText.setDisabledTextColor(new Color(169, 169, 169));
      }
    }

    public void finalTick(){
      timeText.setEnabled(true);
      submitBtn.setEnabled(false);
      cancelBtn.setVisible(false);
      playBtn.setEnabled(false);
      submitBtn.setBackground(ButtonColor);
      playBtn.setPaused();
     
    }
  }

  public void setButtonColor(Color c){
    for(var b : timerButtons){
      b.setBackground(c);
    }
  }

  public void setBackground(Color c){
    timePanel.setBackground(c);
  }

  public void setText(String s){
    timeText.setText(s);
    timeText.postActionEvent();
  }

  public String getText(){
    return timeText.getText();
  }

  private class PlayPauseBtn extends JButton{
    public boolean playing = false;

    private ImageIcon pauseIcon, playIcon;

    public PlayPauseBtn(ImageIcon playIc) { super(playIc); }
    public PlayPauseBtn(ImageIcon playIc, ImageIcon pauseIc){ 
      super(playIc);
      pauseIcon = pauseIc;
      playIcon = playIc;
    }

    public void setPlaying(){
      this.setIcon(pauseIcon);
      playing = true;
    }
    public void setPaused(){
      this.setIcon(playIcon);
      playing = false;
    }

  }


  private class SubmitBtn extends JButton{

    private ImageIcon Icon;
    private Color enabledColor;

    public SubmitBtn(ImageIcon submitIc) { super(submitIc); }
    public SubmitBtn(ImageIcon submitIc, Color c) { 
      super(submitIc); 
      enabledColor = c;
      this.setEnabledColor(c);
    }

    public void setEnabledColor(Color c){
      enabledColor = c;
      super.setBackground(c);
    }

    @Override
    public void setEnabled(boolean b){
      if(b && enabledColor != null)
        this.setBackground(enabledColor);
      /*else 
        this.setBackground(GREY);*/
      super.setEnabled(b);
    }

  }


  private class TimeTextListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      var time = HelperFunctions.minSectoSeconds(timeText.getText());
      if (time > 0) {
        playBtn.setEnabled(true);
      }
      else{
        playBtn.setEnabled(false);
      }
      for(var l : fieldListeners) l.actionPerformed(a);
    }
  }

}
