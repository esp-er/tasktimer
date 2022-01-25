package patriker.tasktimer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Taskbar.State;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.Insets;
import java.awt.geom.*;
import java.awt.Frame;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.sound.sampled.*;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
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
import javax.swing.JOptionPane;
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

import javafx.embed.swing.JFXPanel;
import patriker.tasktimer.HelperFunctions;

import org.kordamp.ikonli.antdesignicons.AntDesignIconsFilledIkonHandler;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlinedIkonHandler;
import org.kordamp.ikonli.openiconic.OpeniconicIkonHandler;
import org.kordamp.ikonli.swing.FontIcon;


public class TaskTimer {


  /* Default variables */
  private int BUTTON_FONTSIZE = 32;
  private int ICONSIZE = 32;
  private int TIMER_FONTSIZE = 40;
  private int TASK_FONTSIZE = 18;
  private int NAME_FONTSIZE = 20;
  private int WINSIZE_X;
  private int WINSIZE_Y;
  private static Color RED = new Color(240, 27, 12);
  private static Color GREY = new Color(96, 96, 96);
  private static Color GREEN = new Color(149, 255, 147);

  Taskbar taskbar = null;
  Taskbar.State taskbarState = Taskbar.State.OFF;

  private TaskClock clock;

  private ImageIcon exitDialogIcon, pinIcon, checkIcon, editIcon, renameIcon, appIcon;

  private JButton taskDelButton, loadButton, saveButton, newButton, undoButton;

  private JToggleButton editNameButton, goalButton, autoRepeatButton;
  private ArrayList<AbstractButton> buttonList;

  private JPanel workPanel, saveDelPanel, bottomPanel, topPanel, workNamePanel, pinPanel, timeZonePanel;
  private Insets bottomButtonsInsets;

  private JProgressBar goalProgress;

  private Clip aClip;
  private JFrame frame;
  private JTextField workLabel;
  private JTextField workName;
  private JComboBox unitPicker, timeZonePicker;
  static Color ButtonColor;
  static Color BackColor;
  private JPopupMenu keypadPopup, backgroundPopup;
  private TaskTimeZone currTimeZone;

  private KeyPad keypad;
  private TimerGUI timergui;

  private static int lastKey;
  private static long enterMain; //for DEBUG info

  //Constants
  private int keyHeight = 98;
  private float tablePadX = 0f;
  private float tablePadY = 36.0f;

  //ScalaFX TaskTable
  private TaskTable table;

  private String currFile = "";

  public static void main(String[] args) {
    enterMain = System.currentTimeMillis();
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        TaskTimer timerApp = new TaskTimer();
        if(args.length == 1){
          var absPath = args[0];
          timerApp.viewMode(absPath);
        }
        else{
          timerApp.start();
        }
      }
    });
  }

  public void viewMode(String file){
    appIcon = new ImageIcon(this.getClass().getResource("/png/appicon_96.png"));

    var f = new JFrame(file);
    var time = System.currentTimeMillis();
    var fxPanel = new JFXPanel();
    var viewTable = new TaskTable();
    f.add(fxPanel);
    fxPanel.repaint();
    viewTable.init(fxPanel,time,true);
    viewTable.loadItems(false, file);
    f.setIconImage(appIcon.getImage());
    f.setSize(500,600);
    viewTable.setHeight(565);
    viewTable.setWidth(500);
    //var s = Toolkit.getDefaultToolkit().getScreenSize();
    //f.setLocation((int)s.getWidth() / 2 -250, (int)s.getHeight()/2 -280);

    f.setVisible(true);
    f.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e){
        System.exit(0);
      }
    });

    //set up jfxpanel resizing
    f.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent componentEvent) {
        var dim = f.getSize();
        viewTable.setHeight(Double.valueOf(dim.height) - 35.0);
        viewTable.setWidth(Double.valueOf(dim.width)-16.0);
      }
    });

  }

  private void preInit(){
    FlatLightLaf.setup(); //Setup flat theme
    var sizes = TimerConf.sizes();
    var colors = TimerConf.colors();
    WINSIZE_X = sizes.width();
    WINSIZE_Y = sizes.height();
    BUTTON_FONTSIZE = sizes.buttonsize();
    TIMER_FONTSIZE = sizes.timersize();
    TASK_FONTSIZE = sizes.tasksize();
    ICONSIZE = sizes.iconsize();

    //Windows needs needs extra padding for the FX table
    if(HelperFunctions.getOS().startsWith("windows")){
      tablePadX = tablePadX + 16.0f;
    }

    ButtonColor = Color.decode(colors.buttons());
    BackColor = Color.decode(colors.background());
    exitDialogIcon = new ImageIcon(this.getClass().getResource("/png/sadcat.png"));
    // Set up Window Pin icon
    var scaling = java.awt.Image.SCALE_SMOOTH;
    pinIcon = new ImageIcon(this.getClass().getResource("/png/pin.png"));
    Image newimg = pinIcon.getImage().getScaledInstance(ICONSIZE / 2, ICONSIZE / 2, scaling);
    pinIcon = new ImageIcon(newimg);
    // Set up Edit Name and Toggled Edit button
    editIcon = new ImageIcon(this.getClass().getResource("/png/edit2.png"));
    newimg = editIcon.getImage().getScaledInstance(ICONSIZE / 2, ICONSIZE / 2, scaling);
    editIcon = new ImageIcon(newimg);

    renameIcon = new ImageIcon(this.getClass().getResource("/png/rename.png"));
    newimg = renameIcon.getImage().getScaledInstance(ICONSIZE / 2, ICONSIZE / 2, scaling);
    renameIcon = new ImageIcon(newimg);

    var keys = TimerConf.keyArr();
    keypad = new KeyPad(keys, new KeypadListener());
    keypad.setButtonColor(ButtonColor);

    // Init clock
    clock = new TaskClock();

    timergui = new TimerGUI(new panelMouseListener());
    timergui.addStartListener(new StartTimerListener());
    timergui.addPauseListener(new PauseTimerListener());
    timergui.addRestartListener(new RestartListener());
    timergui.addSubmitListener(new SubmitTaskListener());
    timergui.addCancelListener((ActionEvent e) -> {
      clock.reset();
      keypad.setEnabled(true);
      if(taskbar != null){
        taskbarState = State.OFF;
        taskbar.setWindowProgressValue(frame, 0);
        taskbar.setWindowProgressState(frame, taskbarState);
      }
    });
    timergui.addFieldListener(new TimeTextListener());

    //SETUP AUDIO
    try {
      AudioInputStream aStream = AudioSystem.getAudioInputStream(this.getClass().getResource("/audio/alarm.wav"));
      AudioFormat format = aStream.getFormat();
      DataLine.Info info = new DataLine.Info(Clip.class, format);
      aClip = (Clip) AudioSystem.getLine(info);
      aClip.open(aStream);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // Set up right-click popup-menu
    ImageIcon editBtnIcon = new ImageIcon(this.getClass().getResource("/png/timer.png"));
    newimg = editBtnIcon.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
    editBtnIcon = new ImageIcon(newimg);
    keypadPopup = new JPopupMenu();
    JMenuItem menuitem = new JMenuItem("Edit Button", editBtnIcon);
    menuitem.addActionListener(new EditButtonListener());
    keypadPopup.add(menuitem);
    editBtnIcon = new ImageIcon(this.getClass().getResource("/png/colors.png"));
    newimg = editBtnIcon.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
    editBtnIcon = new ImageIcon(newimg);
    menuitem = new JMenuItem("Edit Button Color", editBtnIcon);
    menuitem.addActionListener(new KeypadColorListener());
    keypadPopup.add(menuitem);

    editBtnIcon = new ImageIcon(this.getClass().getResource("/png/colors.png"));
    newimg = editBtnIcon.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
    editBtnIcon = new ImageIcon(newimg);
    JMenuItem menuitem2 = new JMenuItem("Edit Background Color", editBtnIcon);
    backgroundPopup = new JPopupMenu();

    menuitem2.addActionListener((ActionEvent e) -> {
      Color newColor = JColorChooser.showDialog(null, "Choose a color", BackColor);
      if (newColor != null) {
        BackColor = newColor;

        var p = new Point(0,0);
        if(frame != null && frame.isVisible())
          p = frame.getLocationOnScreen();

        TimerConf.write(workName.getText(), keypad.getButtonList(), HelperFunctions.getHex(BackColor), HelperFunctions.getHex(ButtonColor), p.x, p.y, currFile);

        timergui.timePanel.setBackground(newColor);
        keypad.setBackground(newColor);
        workPanel.setBackground(newColor);
        saveDelPanel.setBackground(newColor);
        workNamePanel.setBackground(newColor);
        pinPanel.setBackground(newColor);
        timeZonePanel.setBackground(newColor);
        goalProgress.setBackground(newColor);
        table.setAccentColor(HelperFunctions.getHex(newColor));
      }
    });
    backgroundPopup.add(menuitem2);

  }

  private boolean continueDialog(String pName, String fPath){
    String ObjButtons[] = { "계속 하기", "새 리스트 만들기" };
    JLabel text = new JLabel("저장된 " + pName + " (위치 : " + fPath + "  ) " + "\n" +
        "계속 할거예요? 새로 시작할거예요? ");
    text.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TASK_FONTSIZE));
    int PromptResult = JOptionPane.showOptionDialog(null, text, "New List",
        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ObjButtons,
        ObjButtons[0]);
    if (PromptResult == JOptionPane.NO_OPTION || PromptResult == JOptionPane.CLOSED_OPTION) {
      return false;
    }
    else{
      return true;
    }
    /*
       workName.setText("기본");
       editNameButton.setSelected(true);
       table.createNew();
       }*/

  }
  //TODO: Split this long function into several (related) parts
  public void start() {
    preInit();
    var time = System.currentTimeMillis();
		
    JFrame.setDefaultLookAndFeelDecorated(true);
    frame = new JFrame("TaskTimer - " + TimerConf.project());
    appIcon = new ImageIcon(this.getClass().getResource("/png/appicon_96.png"));
    frame.setIconImage(appIcon.getImage());
    //Remove windows in build
    frame.addKeyListener(new KeyboardListener());
    JPanel buttonPanel = new JPanel(new GridLayout(3, 4));
    topPanel = new JPanel();

    //Test Taskbar task progress
    if (Taskbar.isTaskbarSupported()) {
      taskbar = Taskbar.getTaskbar();
      taskbar.setWindowProgressState(frame, taskbarState);
    }
    Font f = new Font(Font.SANS_SERIF, Font.PLAIN, TASK_FONTSIZE);

    // Set up Total Work Label
    workLabel = new JTextField("No tasks completed.", SwingConstants.CENTER);
    workLabel.setHorizontalAlignment(SwingConstants.CENTER);
    workLabel.setEditable(false);
    workLabel.setBackground(new Color(255,255,255));
    workLabel.setFont(f);

    // Progress Goals Toggle button
    ImageIcon goalsIcon = new ImageIcon(this.getClass().getResource("/png/goal.png"));
    ImageIcon goalsIconDialog = new ImageIcon(
    goalsIcon.getImage().getScaledInstance(ICONSIZE, ICONSIZE, java.awt.Image.SCALE_SMOOTH));
    goalsIcon = new ImageIcon(goalsIcon.getImage().getScaledInstance(ICONSIZE / 2, ICONSIZE / 2, java.awt.Image.SCALE_SMOOTH));
    goalButton = new JToggleButton(goalsIcon);
    goalButton.setPreferredSize(new Dimension(28, 28));
    goalButton.addActionListener((ActionEvent e) -> {
      if (goalButton.isSelected()) {
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(60);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        // formatter.setCommitsOnValidEdit(true);
        JFormattedTextField minutes = new JFormattedTextField(formatter);
        minutes.setColumns(2);
        minutes.setValue(0);
        NumberFormatter hformatter = new NumberFormatter(NumberFormat.getInstance());
        hformatter.setFormat(formatter.getFormat());
        hformatter.setMaximum(999);
        JFormattedTextField hours = new JFormattedTextField(hformatter);
        hours.setColumns(2);
        hours.setValue(1);
        JPanel goalDialog = new JPanel();
        goalDialog.add(hours);
        goalDialog.add(new JLabel("시간"));
        goalDialog.add(Box.createHorizontalStrut(15)); // a spacer
        goalDialog.add(minutes);
        goalDialog.add(new JLabel("분"));
        int result = JOptionPane.showConfirmDialog(null, goalDialog, "목표 입력해주세요", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE, goalsIconDialog);
        if (result == JOptionPane.OK_OPTION) {
          goalProgress.setVisible(true);
          goalProgress.setMaximum((int) hours.getValue() * 60 * 60 + (int) minutes.getValue() * 60);
        } else {
          goalProgress.setVisible(false);
          goalButton.setSelected(false);
        }
      } else {
        goalProgress.setVisible(false);
      }
    });
    // Set up Progress Bar
    UIManager.put("ProgressBar.selectionForeground", Color.black);
    UIManager.put("ProgressBar.selectionBackground", Color.black);
    goalProgress = new JProgressBar(0, 100);
    goalProgress.setPreferredSize(new Dimension(goalProgress.getPreferredSize().width, 25));
    goalProgress.addChangeListener((ChangeEvent l) -> {
      if (goalProgress.getPercentComplete() >= 1.0) {
        goalProgress.setForeground(GREEN);
        goalProgress.setString("축하합니다! 목표 완성!!");
      } else {
        goalProgress.setForeground(ButtonColor);
        goalProgress.setString(null);
      }
    });
    goalProgress.setStringPainted(true);
    goalProgress.setVisible(false);
    goalProgress.setBackground(BackColor);
    goalProgress.setForeground(ButtonColor);

    editNameButton = new JToggleButton(editIcon);
    // Set up work & window name label
    workName = new JTextField(TimerConf.project());
    workName.setEditable(false);
    workName.setFocusable(false);

    workName.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
    workName.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
      workName.setColumns(workName.getColumns());
      frame.setTitle("TaskTimer - " + workName.getText());
      frame.revalidate();
    });
    workName.addActionListener((ActionEvent e) -> {
      workName.setEditable(false);
      editNameButton.setSelected(false);
      frame.requestFocus();
      var p = new Point(0,0);
      if(frame != null && frame.isVisible()) p = frame.getLocationOnScreen();
      TimerConf.write(workName.getText(), keypad.getButtonList(), HelperFunctions.getHex(BackColor), HelperFunctions.getHex(ButtonColor), p.x, p.y, currFile);
    });
    editNameButton.setBackground(ButtonColor);
    editNameButton.setSelectedIcon(renameIcon);
    editNameButton.setPreferredSize(new Dimension(28, 28));
    editNameButton.addActionListener((ActionEvent e) -> {
      if (editNameButton.isSelected()) {
        // editNameButton.setBackground(new Color(149, 255, 147));
        workName.setFocusable(true);
        workName.setEditable(true);
        workName.requestFocus();
        frame.revalidate();
      } else {
        workName.setFocusable(false);
        workName.setEditable(false);
        editNameButton.setSelected(false);
        frame.requestFocus();
        var p = new Point(0,0);
        if(frame != null && frame.isVisible()) p = frame.getLocationOnScreen();
        TimerConf.write(workName.getText(), keypad.getButtonList(), HelperFunctions.getHex(BackColor), HelperFunctions.getHex(ButtonColor), p.x, p.y, currFile);
      }
    });

    // Create time unit picker right of Work Label
    String[] timeUnits = { "Hr", "Min", "Sec" };
    unitPicker = new JComboBox(timeUnits);
    unitPicker.setPreferredSize(new Dimension(68, 30));
    unitPicker.addActionListener((ActionEvent e) -> updateTotalWorkLabel(getTotalWork(), getTotalTasks()));
    // TimeZone Picker (Left of Total Work Label)
    // URL timezoneURL = this.getClass().getResource("./timezones.txt");
    ZonesReader z = new ZonesReader();
    timeZonePicker = new JComboBox(z.getZones());
    timeZonePicker.setPreferredSize(new Dimension(110, 30));
    var ff = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    timeZonePicker.setFont(ff);
    timeZonePicker.addActionListener((ActionEvent e) -> currTimeZone = (TaskTimeZone) timeZonePicker.getSelectedItem());
    currTimeZone = (TaskTimeZone) timeZonePicker.getItemAt(0);

    // Set up Delete and Save task Buttons
    var antIcons = new AntDesignIconsOutlinedIkonHandler();
    var antIconsFilled = new AntDesignIconsFilledIkonHandler();
    taskDelButton = new JButton(FontIcon.of(antIcons.resolve("anto-delete"), 16, GREY));
    //
    taskDelButton.setBackground(ButtonColor);
    taskDelButton.addActionListener((ActionEvent e) -> {
      table.removeItem();
      var taskarr = table.getTaskArray();
      TaskIO.autoSave(workName.getText(), getTotalWorkStr(), taskarr);
      TaskIO.writeTasksJson(true, workName.getText() + ".mim", taskarr);
    });
        //taskDelButton.setEnabled(false);
    undoButton = new JButton(FontIcon.of(antIcons.resolve("anto-rollback"), 16));
    undoButton.setToolTipText("삭제 되돌리기");
    undoButton.setBackground(GREEN);

    undoButton.addActionListener((ActionEvent e) -> {
      table.restoreItems();
      Task[] taskArr = table.getTaskArray();
      TaskIO.autoSave(workName.getText(), getTotalWorkStr(), taskArr);
      var cur = TaskIO.writeTasksJson(true, workName.getText() + ".mim", taskArr);
      currFile = cur;
    });
    undoButton.setVisible(false);


    // Set up Window Pin buttons
    var pinButton = new JToggleButton(pinIcon, false);
    pinButton.addItemListener((ItemEvent ev) -> {
      if (ev.getStateChange() == ItemEvent.SELECTED) 
        frame.setAlwaysOnTop(true);
      else if (ev.getStateChange() == ItemEvent.DESELECTED) 
        frame.setAlwaysOnTop(false);
    });
    pinButton.setBackground(ButtonColor);

    //Set up auto repeat button
    autoRepeatButton = new JToggleButton(FontIcon.of(antIcons.resolve("anto-sync"), 16));
    var pinPanelBtnDims = pinButton.getPreferredSize();
    autoRepeatButton.setText("R");
    autoRepeatButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
    autoRepeatButton.setHorizontalTextPosition(JButton.CENTER);
    autoRepeatButton.setVerticalTextPosition(JButton.CENTER);
    autoRepeatButton.setPreferredSize(pinPanelBtnDims);
    autoRepeatButton.setMargin(new Insets(0, 0, 0, 0));

    var transpButton = new JToggleButton(FontIcon.of(antIcons.resolve("anto-eye"), 16));
		transpButton.addItemListener((ItemEvent e) ->{
      if(e.getStateChange() == ItemEvent.SELECTED){
        frame.setOpacity(0.7f);
      }
      else{
        frame.setOpacity(1f);
      }
    });

    transpButton.setToolTipText("Toggle Transparency");

    newButton = new JButton(FontIcon.of(antIconsFilled.resolve("antf-file"), 16));
    newButton.setBackground(ButtonColor);
    //TODO: add English/Korean strings to a property file and fetch string according to lang setting
    newButton.setToolTipText("새 태스크 리스트 시작");
    newButton.addActionListener((ActionEvent e) -> {
      if (table.getLength() > 0) {
        String ObjButtons[] = { "Yes", "No" };
        JLabel text = new JLabel("새로운 리스트 시작하겠습니까? (저장 안된 내용 삭제됩니다)");
        text.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TASK_FONTSIZE));
        int PromptResult = JOptionPane.showOptionDialog(null, text, "New List",
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ObjButtons,
            ObjButtons[0]);
        if (PromptResult == JOptionPane.NO_OPTION || PromptResult == JOptionPane.CLOSED_OPTION) {
        }
        else{
          workName.setEditable(true);
          workName.requestFocus();
          workName.setText("기본");
          editNameButton.setSelected(true);
          table.createNew();
        }
    }
    });

    // Set up load list button
    loadButton = new JButton(FontIcon.of(antIconsFilled.resolve("antf-folder-open"), 16));
    loadButton.setBackground(ButtonColor);
    loadButton.addActionListener(new LoadListListener());
    loadButton.setToolTipText("파일 열기");
    loadButton.setEnabled(false);


    // Set up save list button
    saveButton = new JButton(FontIcon.of(antIconsFilled.resolve("antf-save"), 16));
    saveButton.setBackground(ButtonColor);
    saveButton.setToolTipText("파일 저장");
    saveButton.addActionListener(new SaveListListener());

    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

    // Add buttons to arrayList for easy access
    buttonList = new ArrayList<AbstractButton>();
    buttonList.addAll(List.of(loadButton, saveButton, pinButton, autoRepeatButton, transpButton,
                      editNameButton, goalButton));

    topPanel.add(timergui.timePanel);

    JPanel keypadPanel = new JPanel(new BorderLayout());
    keypadPanel.setBackground(BackColor);

    keypadPanel.add(BorderLayout.CENTER, keypad.panel);
    topPanel.add(keypadPanel);

    workPanel = new JPanel();
    workPanel.setBackground(BackColor);
    workPanel.setLayout(new BorderLayout());
    workPanel.add(goalButton, BorderLayout.LINE_START);
    workPanel.add(workLabel, BorderLayout.CENTER);
    workPanel.add(unitPicker, BorderLayout.LINE_END);
    workPanel.addMouseListener(new tempDeselectListener());
    topPanel.add(workPanel);
    topPanel.add(goalProgress);

    setBackgroundAllButtons(ButtonColor);

    // Set up bottom panel
    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BorderLayout());
    bottomButtonsInsets = new Insets(2,4,2,4);
    saveDelPanel = new JPanel();
    FlowLayout flow = new FlowLayout(FlowLayout.CENTER);
    saveDelPanel.setLayout(flow);

    workNamePanel = new JPanel();
    workNamePanel.setBackground(BackColor);
    workNamePanel.add(workName);
    workNamePanel.add(editNameButton);

    pinPanel = new JPanel();
    flow = new FlowLayout(FlowLayout.TRAILING);
    pinPanel.setLayout(flow);
    saveDelPanel.setBackground(BackColor);

    undoButton.setMargin(bottomButtonsInsets);
    taskDelButton.setMargin(bottomButtonsInsets);
    newButton.setMargin(bottomButtonsInsets);
    loadButton.setMargin(bottomButtonsInsets);
    saveButton.setMargin(bottomButtonsInsets);

    saveDelPanel.add(undoButton);
    saveDelPanel.add(taskDelButton);
    saveDelPanel.add(Box.createHorizontalStrut(30));
    saveDelPanel.add(newButton);
    saveDelPanel.add(loadButton);
    saveDelPanel.add(saveButton);

    timeZonePanel = new JPanel();
    timeZonePanel.setBackground(BackColor);
    timeZonePanel.add(timeZonePicker);
    //timeZonePanel.add(taskDelButton);

    pinPanel.setBackground(BackColor);
    pinPanel.add(autoRepeatButton);
    pinPanel.add(transpButton);
    pinPanel.add(pinButton);
    bottomPanel.addMouseListener(new panelMouseListener());
    bottomPanel.add(BorderLayout.WEST, timeZonePanel);
    bottomPanel.add(BorderLayout.CENTER, saveDelPanel);
    bottomPanel.add(BorderLayout.EAST, pinPanel);
    bottomPanel.add(BorderLayout.SOUTH, workNamePanel);

    // Lay out contents on the frame
    frame.getContentPane().add(BorderLayout.NORTH, topPanel);
    frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);

    //set up jfxpanel resizing
    frame.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent componentEvent) {
        refreshTableSize();
      }
    });

    keypad.toggleCompact.addActionListener((ActionEvent e) -> {
      var dim = frame.getSize();
      var topHeight = topPanel.getSize().height;
      var bottomHeight = bottomPanel.getSize().height;
      var compactExtraHeight  = keypad.panel.getSize().height / 2 + 8;
      if(table != null)
        if(keypad.isCompact())
            table.setHeight(Double.valueOf(dim.height) - topHeight + compactExtraHeight - bottomHeight - tablePadY);
        else
            refreshTableSize();
    });
    keypad.toggleHide.addActionListener((ActionEvent e) -> {
      var dim = frame.getSize();
      var topHeight = topPanel.getSize().height;
      var bottomHeight = bottomPanel.getSize().height;
      var compactExtraHeight  = keypad.panel.getSize().height / 2 + 8;
      if(table != null){
        if(keypad.panel.isVisible())
          table.setHeight(Double.valueOf(dim.height) - topHeight + keyHeight - bottomHeight - tablePadY);
        else if(keypad.isCompact())
          table.setHeight(Double.valueOf(dim.height) - topHeight + compactExtraHeight - bottomHeight - tablePadY);
        else
          refreshTableSize();
      }
      
    });
    // Set up frame properties
    frame.setSize(WINSIZE_X, WINSIZE_Y);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    var dim = frame.getSize();
    var topHeight = topPanel.getSize().height;
    var bottomHeight = bottomPanel.getSize().height;

    updateTotalWorkLabel(getTotalWork(), getTotalTasks());
    //frame.pack();
    frame.setLocation(TimerConf.posx(), TimerConf.posy());
    frame.setVisible(true);
		try {
			undecorate(frame); //Change it after frame is visible
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}


    var fxPanel = new JFXPanel();
    table = new TaskTable();
    table.init(fxPanel, time, false);
    table.setAccentColor(HelperFunctions.getHex(BackColor));
    frame.getContentPane().add(BorderLayout.CENTER, fxPanel);

        table.totalWorkProp().addListener( (obs, oldval, newval) -> {
      updateTotalWorkLabel(newval.intValue(), getTotalTasks());
    });
    table.totalTasksProp().addListener( (obs, oldval, newval) -> {
      updateTotalWorkLabel(getTotalWork(), newval.intValue());
    });

    table.totalDeletedProp().addListener( (obs, oldx, newx) -> {
      if(newx.intValue() > 0){
        undoButton.setText(newx.intValue() + "");
        undoButton.setEnabled(true);
        undoButton.setVisible(true);
        undoButton.setMargin(new Insets(2,8,2,8));
      }
      else{
        undoButton.setEnabled(false);
        undoButton.setMargin(bottomButtonsInsets);
        undoButton.setVisible(false);
      }
    });



    table.numSelectedProp().addListener( (obs, oldx, newx) -> {
      var iconSize = 18;
      if (newx.intValue() == 1){
        int width =
            taskDelButton.getFontMetrics(taskDelButton.getFont()).stringWidth(String.valueOf("1"));
        taskDelButton.setIcon(FontIcon.of(antIcons.resolve("anto-delete"), iconSize));
        taskDelButton.setText("");
        taskDelButton.setMargin(bottomButtonsInsets);
        taskDelButton.setEnabled(true);
      }
      else if (newx.intValue() > 1){
        int width =
            taskDelButton.getFontMetrics(taskDelButton.getFont()).stringWidth(String.valueOf(newx.intValue()));
        taskDelButton.setIcon(FontIcon.of(antIcons.resolve("anto-delete"), iconSize));
        taskDelButton.setMargin(new Insets(2,8,2,8));
        taskDelButton.setText(newx.intValue() + "");
        taskDelButton.setEnabled(true);
      }
      else{
        taskDelButton.setIcon(FontIcon.of(antIcons.resolve("anto-delete"), iconSize, GREY));
        taskDelButton.setEnabled(false);
        taskDelButton.setMargin(bottomButtonsInsets);
        taskDelButton.setText("");
      }
    });

    try {
      setUIFont(new javax.swing.plaf.FontUIResource(Font.SANS_SERIF, Font.PLAIN, 18));
    } catch (Exception e) {}
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowActivated(WindowEvent we) {
        if(taskbar != null && taskbarState == State.ERROR){
          taskbarState = State.OFF;
          taskbar.setWindowProgressState(frame, taskbarState);
        }
      }
      @Override
      public void windowClosing(WindowEvent we) {
        if (table.getLength() > 0) {
          String ObjButtons[] = { "Yes", "No" };
          JLabel text = new JLabel("Do you want to save TASK LIST!?");
          text.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TASK_FONTSIZE));
          int PromptResult = JOptionPane.showOptionDialog(null, text, "Task Timer",
              JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, exitDialogIcon, ObjButtons,
              ObjButtons[0]);
          if (PromptResult == JOptionPane.NO_OPTION) {
          var p = new Point(0,0);
          if(frame != null && frame.isVisible()) p = frame.getLocationOnScreen();
            TimerConf.write(workName.getText(), keypad.getButtonList(), HelperFunctions.getHex(BackColor), HelperFunctions.getHex(ButtonColor), p.x, p.y, currFile);
            System.exit(0);
          }
          else if(PromptResult == JOptionPane.CLOSED_OPTION){
            //Return to main window!
          }
          else {
            JFileChooser fileSave = new JFileChooser();
            fileSave.setMinimumSize(new Dimension(700, 1000));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Timer File", "mim");
            fileSave.addChoosableFileFilter(filter);
            fileSave.setFileFilter(filter);
            String defaultFile = (workName.getText() == "") ? "fileToSave" : workName.getText();
            var choice = fileSave.showSaveDialog(frame);
            if (choice == JFileChooser.APPROVE_OPTION){
              fileSave.setSelectedFile(new File(defaultFile));
              Task[] taskArr = table.getTaskArray();
              TaskIO.writeTasksJson(false, fileSave.getSelectedFile().getAbsolutePath() + ".mim", taskArr);
              //TaskIO.saveTasks(fileSave.getSelectedFile().getAbsolutePath() + ".txt", getTotalWorkStr(), taskArr);
              System.exit(0);
            }
          }
        } else {
          System.exit(0);
        }
      }
    });
    //Tooltip font
    //

    refreshTableSize();
    UIManager.put("ToolTip.font", new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN, 12));
  }

  private void refreshTableSize(){
    if(table != null){
      var dim = frame.getSize();
      var topHeight = topPanel.getSize().height;
      var bottomHeight = bottomPanel.getSize().height;

      table.setHeight(Double.valueOf(dim.height) - topHeight - bottomHeight - tablePadY);
      table.setWidth(Double.valueOf(dim.width - tablePadX));
    }
  }




  public static void setUIFont(javax.swing.plaf.FontUIResource f) {
    java.util.Enumeration keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = UIManager.get(key);
      if (value instanceof javax.swing.plaf.FontUIResource)
        UIManager.put(key, f);
    }
  }

  public class KeyboardListener implements KeyListener {
    public void keyPressed(KeyEvent e){}
    //TODO: REMOVE IN WINDOWS BUILD!
    public void keyReleased(KeyEvent e) {
      if(e.getKeyCode()== KeyEvent.VK_ESCAPE)
        System.exit(0);
    }
    public void keyTyped(KeyEvent e) {
    }
  }

  public class ClockListener implements ClockTickListener {

    public void Tick(int secRemaining) {
      long progress = Math.round((double)clock.getTimeRemaining() / clock.getCountSeconds() * 100);
      if(taskbar != null){
        taskbarState = State.NORMAL;
        taskbar.setWindowProgressValue(frame, (int) progress);
        taskbar.setWindowProgressState(frame, taskbarState);
      }
    }

    public void finalTick() {
            var t = Task.apply(workName.getText(), clock.getCountSeconds(), currTimeZone, HelperFunctions.getHex(BackColor), HelperFunctions.getHex(ButtonColor));
      table.addItem(t);
      if(taskbar != null){
        taskbarState = State.ERROR;
        taskbar.setWindowProgressValue(frame, -1);
        taskbar.setWindowProgressState(frame, taskbarState);
      }

      if (!autoRepeatButton.isSelected()) {
        timergui.setText("00:00");
        keypad.setEnabled(true);
        frame.requestFocus();
      } 
      else {
        timergui.fireRestart();
      }
      //AutoSave
      Task[] taskArr = table.getTaskArray();
      TaskIO.autoSave(workName.getText(), getTotalWorkStr(), taskArr);
      String cur = TaskIO.writeTasksJson(true, workName.getText() + ".mim", taskArr);
      //currFile = cur;
      aClip.stop();
      aClip.setFramePosition(0);
      aClip.setMicrosecondPosition(0);
      aClip.start();

    }
  }


  public class SaveListListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      /*
        try{
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          FlatLaf.updateUI();
        }
        catch (Exception e1){
          e1.printStackTrace();
        }*/
        JFileChooser fileSave = new JFileChooser();
        fileSave.setMinimumSize(new Dimension(700, 1000));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Timer file", "mim");
        fileSave.addChoosableFileFilter(filter);
        fileSave.setFileFilter(filter);
        String defaultFile = (workName.getText() == "") ? "fileToSave" : workName.getText();
        fileSave.setSelectedFile(new File(defaultFile));
        var choice = fileSave.showSaveDialog(frame);
        if (choice == JFileChooser.APPROVE_OPTION){
          File tmp = fileSave.getSelectedFile();
          Task[] taskArr = table.getTaskArray();
          TaskIO.writeTasksJson(false, tmp.getAbsolutePath() + ".mim", taskArr);
        }
        //try{UIManager.setLookAndFeel(new FlatLightLaf());}
        //catch(Exception e1){ e1.printStackTrace();}
        //TaskIO.saveTasks(tmp.getAbsolutePath() + ".txt", getTotalWorkStr(), taskArr);
    }
  }
  public class TimeTextListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      var time = HelperFunctions.minSectoSeconds(timergui.getText());
      if (time > 0) {
        //TODO: Add timerguis listener
        clock = new TaskClock(time, List.of(timergui.getTimerListener(), new ClockListener()));
      }
      frame.requestFocus();
      table.deselect();
      //timergui.setText(HelperFunctions.secondsToMinSec(time));
      aClip.stop();
    }
  }

  public class StartTimerListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
        clock.start();
        keypad.setEnabled(false);
        table.deselect();
        aClip.stop();
        if(taskbar != null){
          if(taskbarState != State.PAUSED){
            taskbarState = State.NORMAL;
            taskbar.setWindowProgressState(frame, taskbarState);
            taskbar.setWindowProgressValue(frame, 100);
          }
        }
    }
  }

  public class PauseTimerListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      clock.pause();
      if(taskbar != null){
        taskbarState = State.PAUSED;
        taskbar.setWindowProgressState(frame, taskbarState);
      }
      keypad.setEnabled(true);
    }
  }

  public class RestartListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      if(clock.getCountSeconds() != 0){
        int s = clock.getCountSeconds();
        clock.stop();
        clock = new TaskClock(s, List.of(timergui.getTimerListener(), new ClockListener()));
        timergui.setText(HelperFunctions.secondsToMinSec(clock.getCountSeconds()));
        clock.start();
        keypad.setEnabled(false);
        table.deselect();
        aClip.stop();
      }
    }
  }

  public class SubmitTaskListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      clock.stop();
      aClip.stop();
      keypad.setEnabled(true);
      // Add new task to list
      table.addItem(Task.apply(workName.getText(), clock.getCountSeconds(), currTimeZone, HelperFunctions.getHex(BackColor), HelperFunctions.getHex(ButtonColor)));
      if(autoRepeatButton.isSelected())
        autoRepeatButton.setSelected(false);

      //AutoSave functionality
      Task[] taskArr = table.getTaskArray();
      TaskIO.autoSave(workName.getText(), getTotalWorkStr(), taskArr);
      currFile = TaskIO.writeTasksJson(true, workName.getText() + ".mim", taskArr);
      //goalProgress.setValue((int) getTotalWorkTime());
      frame.requestFocus();
      frame.revalidate();
      frame.repaint();
      if(taskbar != null){
        taskbarState = State.OFF;
        taskbar.setWindowProgressState(frame, taskbarState);
      }
    }
  }

  public class KeypadListener implements TaskButtonListener {
    public void buttonClicked(int seconds, int i) {
        timergui.setText(HelperFunctions.secondsToMinSec(seconds));
    }

    public void rightClick(int i, MouseEvent e) {
      keypadPopup.show(e.getComponent(), e.getX(), e.getY());
      lastKey = i;
    }
  }

  public class EditButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      String newButtonCount = JOptionPane.showInputDialog("Edit Button # seconds", keypad.getButtonSecs(lastKey));
      if (newButtonCount != null) {
        try {
          int sec = Integer.parseInt(newButtonCount);
          keypad.setButtonSecs(lastKey, sec);
        } catch (NumberFormatException e) {
        } finally {
          var p = new Point(0,0);
          if(frame != null && frame.isVisible()) p = frame.getLocationOnScreen();
          TimerConf.write(workName.getText(), keypad.getButtonList(), HelperFunctions.getHex(BackColor), HelperFunctions.getHex(ButtonColor), p.x, p.y, currFile);
        }
      }
    }
  }

  public class KeypadColorListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent a) {
      Color newColor = JColorChooser.showDialog(null, "Choose a color", keypad.getButtonColor());
      if (newColor != null) {
        ButtonColor = newColor;
        keypad.setButtonColor(newColor);
        setBackgroundAllButtons(newColor);
        var p = new Point(0,0);
        if(frame != null && frame.isVisible()) p = frame.getLocationOnScreen();
        TimerConf.write(workName.getText(), keypad.getButtonList(), HelperFunctions.getHex(BackColor), HelperFunctions.getHex(ButtonColor), p.x, p.y, currFile);
      }
    }
  }

	//Note that we use reflection "hack" here
  private static void undecorate(Frame frame) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    Field undecoratedField = Frame.class.getDeclaredField("undecorated");
    undecoratedField.setAccessible(true);
    undecoratedField.set(frame, true);
  }

  private class panelMouseListener implements MouseListener {
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON3) {
        backgroundPopup.show(e.getComponent(), e.getX(), e.getY());
      }
      else if(e.getButton() == MouseEvent.BUTTON1) {
        table.deselect();
      }
    }
  }
  private class tempDeselectListener extends panelMouseListener{
    @Override
    public void mousePressed(MouseEvent e) {
      if(e.getButton() == MouseEvent.BUTTON1)
        table.deselect();
    }
  }

  private void setBackgroundAllButtons(Color newColor) {
    for (AbstractButton b : buttonList)
      b.setBackground(newColor);
    taskDelButton.setBackground(newColor);
    newButton.setBackground(newColor);
    goalProgress.setForeground(newColor);
    if(timergui != null) timergui.setButtonColor(newColor);
  }
  private void updateTotalWorkLabel(int total, int numTasks) {
    String s = switch ((String) unitPicker.getSelectedItem()) {
      case "Hr" -> String.format("%.3f", HelperFunctions.sectoHr(total)) + " 시간";
      case "Min" -> String.format("%.1f", HelperFunctions.sectoMin(total)) + " 분";
      case "Sec" -> Integer.toString(total) + " 초";
      default -> "d";
    };
    if (total > 0) {
      workLabel.setText(numTasks + "개" + " - " + HelperFunctions.sectoHrMinSec(total) + " - " + "" + s);
    } else {
      workLabel.setText("No tasks completed.");
    }
  }

  private int getTotalWork(){
    if(this.table != null) return table.getTotalWork(); else return 0;
  }

  private int getTotalTasks(){
    if(this.table != null) return table.getTotalTasks(); else return 0;
  }

  private String getTotalWorkStr(){
    var total = getTotalWork();
    if (total > 0){
      var hrstr = String.format("%.3f", HelperFunctions.sectoHr((int) total)) + " 시간";
      var minstr = String.format("%.1f", HelperFunctions.sectoMin((int) total)) + " 분";
      var secstr = Integer.toString((int) total) + " 초";
      return "총: " + table.getLength() + "개" + " - "
                                + HelperFunctions.sectoHrMinSec((int) total) + " ("
                                + " |" + hrstr + "| "
                                + " |" + minstr + "| "
                                + " |" + secstr + "| )";

    }
    else{
      return "No tasks completed.";
    }
  }

}
