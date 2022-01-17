package patriker.tasktimer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Taskbar.State;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.awt.Insets;
import java.awt.geom.*;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;


public class Timer{

  private ImageIcon playIcon, restartIcon, pauseIcon, checkIcon, cancelIcon;
  private JButton restartBtn, playBtn, pauseBtn, checkButton, cancelButton; 

  protected JPanel timePanel;
  private ArrayList<AbstractButton> timerButtons;

  //Main constructor
  public Timer(){
    var sizes = TimerConf.sizes();
    var colors = TimerConf.colors();
    var iconsize = sizes.iconsize();
    var scaling_hints = java.awt.Image.SCALE_SMOOTH;

    //WINSIZE_X = sizes.width();
    //WINSIZE_Y = sizes.height();
    //BUTTON_FONTSIZE = sizes.buttonsize();
    //TIMER_FONTSIZE = sizes.timersize();
    //TASK_FONTSIZE = sizes.tasksize();
    //iconsize = sizes.iconsize();

    ButtonColor = Color.decode(colors.buttons());
    BackColor = Color.decode(colors.background());
   
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


  }
  


}
