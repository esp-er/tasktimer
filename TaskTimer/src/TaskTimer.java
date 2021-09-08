package patriker.tasktimer;

import java.util.ArrayList;
import java.io.File;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.Math.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.AbstractButton;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JProgressBar;
import javax.swing.JComponent;
import javax.swing.event.*;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import javax.imageio.ImageIO;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Taskbar.State;
import javax.swing.WindowConstants;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.time.format.*;
import java.time.temporal.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.kordamp.ikonli.swing.FontIcon;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonProvider;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlinedIkonHandler;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsFilledIkonHandler;
import org.kordamp.ikonli.openiconic.OpeniconicIkonHandler;


public class TaskTimer {


  /* Default variables */
  private int BUTTON_FONTSIZE = 32;
  private int ICONSIZE = 32;
  private int TIMER_FONTSIZE = 40;
  private int TASK_FONTSIZE = 18;
  private int NAME_FONTSIZE = 20;
  private int WINSIZE_X = 460;
  private int WINSIZE_Y = 700;
  private static Color GREY = new Color(219, 219, 219);
  private static Color RED = new Color(240, 27, 12);
  private static Color GREEN = new Color(149, 255, 147);

  Taskbar taskbar = null;
  Taskbar.State taskbarState = Taskbar.State.OFF;

  private JFormattedTextField timeText;
  private TaskClock clock;

  private ImageIcon playIcon, restartIcon, pauseIcon, exitDialogIcon, pinIcon, checkIcon, editIcon, renameIcon,
          cancelIcon;
  private JButton restartBtn, playBtn, pauseBtn, taskDelButton, loadButton, saveButton, checkButton, cancelButton;
  private JToggleButton editNameButton, goalButton;
  private ArrayList<AbstractButton> buttonList;
  private JCheckBox autoRepeat;
  private JPanel timePanel, cancelRepeatPanel, workPanel, saveDelPanel, bottomPanel, workNamePanel, pinPanel,
          timeZonePanel;

  private JProgressBar goalProgress;

  // TODO: add all panels to a common data structure(for easy manipul.)
  private Clip aClip;
  private DefaultListModel<Task> taskList;
  private JList taskWindow;
  private JFrame frame;
  private JTextField workLabel;
  private JTextField workName;
  private JComboBox unitPicker, timeZonePicker;
  static Color ButtonColor;
  static Color BackColor;
  private JPopupMenu keypadPopup, backgroundPopup;
  private TaskTimeZone currTimeZone;

  private TaskButtonPanel keypad;

  private static int lastKey;

  public static void main(String[] args) {

    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        TaskTimer timerApp = new TaskTimer();
        timerApp.go();
      }
    });
  }

  public TaskTimer() {
    // read window and button size info(scala)
    var sizes = TimerConf.sizes();
    var colors = TimerConf.colors();
    WINSIZE_X = sizes.width();
    WINSIZE_Y = sizes.height();
    BUTTON_FONTSIZE = sizes.buttonsize();
    TIMER_FONTSIZE = sizes.timersize();
    TASK_FONTSIZE = sizes.tasksize();
    ICONSIZE = sizes.iconsize();

    // Construct new task list
    taskList = new DefaultListModel<Task>();

    // Task GUI
    taskWindow = new JList(taskList);

    // Button data
    ButtonColor = Color.decode(colors.buttons());
    BackColor = Color.decode(colors.background());

    // set up "Play button"
    playIcon = new ImageIcon(this.getClass().getResource("/png/play.png"));
    Image newimg = playIcon.getImage().getScaledInstance(ICONSIZE, ICONSIZE, java.awt.Image.SCALE_SMOOTH);
    playIcon = new ImageIcon(newimg);
    // set up "restart icon"
    restartIcon = new ImageIcon(this.getClass().getResource("/png/restart.png"));
    newimg = restartIcon.getImage().getScaledInstance(ICONSIZE, ICONSIZE, java.awt.Image.SCALE_SMOOTH);
    restartIcon = new ImageIcon(newimg);
    // set up "pause icon"
    pauseIcon = new ImageIcon(this.getClass().getResource("/png/pause.png"));
    newimg = pauseIcon.getImage().getScaledInstance(ICONSIZE, ICONSIZE, java.awt.Image.SCALE_SMOOTH);
    pauseIcon = new ImageIcon(newimg);

    // Set up "Check" (Task Finish) Button
    checkIcon = new ImageIcon(this.getClass().getResource("/png/check.png"));
    newimg = checkIcon.getImage().getScaledInstance(ICONSIZE, ICONSIZE, java.awt.Image.SCALE_SMOOTH);
    checkIcon = new ImageIcon(newimg);

    // Cancel Task Button
    cancelIcon = new ImageIcon(this.getClass().getResource("/png/cancel.png"));
    newimg = cancelIcon.getImage().getScaledInstance(12, 12, java.awt.Image.SCALE_SMOOTH);
    cancelIcon = new ImageIcon(newimg);

    // set up Exit dialog icon
    exitDialogIcon = new ImageIcon(this.getClass().getResource("/png/sadcat.png"));
    // Set up Window Pin icon
    pinIcon = new ImageIcon(this.getClass().getResource("/png/pin.png"));
    newimg = pinIcon.getImage().getScaledInstance(ICONSIZE / 2, ICONSIZE / 2, java.awt.Image.SCALE_SMOOTH);
    pinIcon = new ImageIcon(newimg);
    // Set up Edit Name and Toggled Edit button
    editIcon = new ImageIcon(this.getClass().getResource("/png/edit2.png"));
    newimg = editIcon.getImage().getScaledInstance(ICONSIZE / 2, ICONSIZE / 2, java.awt.Image.SCALE_SMOOTH);
    editIcon = new ImageIcon(newimg);

    renameIcon = new ImageIcon(this.getClass().getResource("/png/rename.png"));
    newimg = renameIcon.getImage().getScaledInstance(ICONSIZE / 2, ICONSIZE / 2, java.awt.Image.SCALE_SMOOTH);
    renameIcon = new ImageIcon(newimg);

    var keys = TimerConf.keyArr();
    keypad = new TaskButtonPanel(keys, new KeypadListener());
    keypad.setButtonColor(ButtonColor);

    // Init clock
    clock = new TaskClock();

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

    // Set up background popup menu(background color)
    editBtnIcon = new ImageIcon(this.getClass().getResource("/png/colors.png"));
    newimg = editBtnIcon.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
    editBtnIcon = new ImageIcon(newimg);
    JMenuItem menuitem2 = new JMenuItem("Edit Background Color", editBtnIcon);
    backgroundPopup = new JPopupMenu();
    menuitem2.addActionListener((ActionEvent e) -> {
      Color newColor = JColorChooser.showDialog(null, "Choose a color", BackColor);
      if (newColor != null) {
        BackColor = newColor;
        TimerConf.write(workName.getText(), keypad.getButtonList(), getHex(BackColor), getHex(ButtonColor),
            "TaskTimer");
        // TODO: move this to new function
        timePanel.setBackground(newColor);
        workPanel.setBackground(newColor);
        saveDelPanel.setBackground(newColor);
        workNamePanel.setBackground(newColor);
        cancelRepeatPanel.setBackground(newColor);
        autoRepeat.setBackground(newColor);
        pinPanel.setBackground(newColor);
        timeZonePanel.setBackground(newColor);
        goalProgress.setBackground(newColor);
      }
    });
    backgroundPopup.add(menuitem2);
    // Open audio file
    try {
      AudioInputStream aStream = AudioSystem.getAudioInputStream(this.getClass().getResource("/audio/alarm.wav"));
      AudioFormat format = aStream.getFormat();
      DataLine.Info info = new DataLine.Info(Clip.class, format);
      aClip = (Clip) AudioSystem.getLine(info);
      aClip.open(aStream);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void go() {

    // set up Application main ImageIcon
    frame = new JFrame("TaskTimer - " + TimerConf.project());
    ImageIcon appIcon = new ImageIcon(this.getClass().getResource("/png/appicon_96.png"));
    frame.setIconImage(appIcon.getImage());
    frame.addKeyListener(new KeyboardListener());
    timePanel = new JPanel();
    timePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
    JPanel buttonPanel = new JPanel(new GridLayout(3, 4));
    JPanel topPanel = new JPanel();


    //Test Taskbar task progress
    if (Taskbar.isTaskbarSupported()) {
      taskbar = Taskbar.getTaskbar();
      taskbar.setWindowProgressState(frame, taskbarState);
    }

    // Set up Task list window
    ListSelectionModel listSelectionModel = taskWindow.getSelectionModel();
    listSelectionModel.addListSelectionListener(new TaskListListener());

    // Set up timer control buttons
    playBtn = new JButton(playIcon);
    playBtn.setBackground(ButtonColor);
    playBtn.addActionListener(new StartTimerListener());
    playBtn.setEnabled(false);
    pauseBtn = new JButton(pauseIcon);
    pauseBtn.setBackground(ButtonColor);
    pauseBtn.addActionListener(new PauseTimerListener());
    restartBtn = new JButton(restartIcon);
    restartBtn.setBackground(ButtonColor);
    restartBtn.addActionListener(new RestartListener());
    checkButton = new JButton(checkIcon);
    checkButton.setBackground(ButtonColor);
    checkButton.addActionListener(new SubmitTaskListener());
    checkButton.setEnabled(false);

    Font f = new Font(Font.SANS_SERIF, Font.PLAIN, TASK_FONTSIZE);
    // Set up total work Label
    workLabel = new JTextField("No tasks completed.", SwingConstants.CENTER);
    workLabel.setHorizontalAlignment(SwingConstants.CENTER);
    workLabel.setEditable(false);
    // workLabel.setPreferredSize(new Dimension(480,30));
    // workLabel.setMinimumSize(new Dimension(200,30));
    workLabel.setFont(f);

    // Progress Goals Toggle button
    ImageIcon goalsIcon = new ImageIcon(this.getClass().getResource("/png/goal.png"));
    ImageIcon goalsIconDialog = new ImageIcon(
    goalsIcon.getImage().getScaledInstance(ICONSIZE, ICONSIZE, java.awt.Image.SCALE_SMOOTH));
    goalsIcon = new ImageIcon(
        goalsIcon.getImage().getScaledInstance(ICONSIZE / 2, ICONSIZE / 2, java.awt.Image.SCALE_SMOOTH));
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

    // Set up work & window name label
    workName = new JTextField(TimerConf.project());
    workName.setEditable(false);
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
      TimerConf.write(workName.getText(), keypad.getButtonList(), getHex(BackColor), getHex(ButtonColor),
          "TaskTimer");
    });
    editNameButton = new JToggleButton(editIcon);
    editNameButton.setBackground(ButtonColor);
    editNameButton.setSelectedIcon(renameIcon);
    editNameButton.setPreferredSize(new Dimension(28, 28));
    editNameButton.addActionListener((ActionEvent e) -> {
      if (editNameButton.isSelected()) {
        // editNameButton.setBackground(new Color(149, 255, 147));
        workName.setEditable(true);
        workName.requestFocus();
        frame.revalidate();
      } else {
        workName.setEditable(false);
        editNameButton.setSelected(false);
        frame.requestFocus();
        TimerConf.write(workName.getText(), keypad.getButtonList(), getHex(BackColor), getHex(ButtonColor),
            "TaskTimer");
      }
    });

    // Create time unit picker right of Work Label
    String[] timeUnits = { "Hr.", "Min.", "Sec." };
    unitPicker = new JComboBox(timeUnits);
    unitPicker.setPreferredSize(new Dimension(60, 30));
    unitPicker.addActionListener((ActionEvent e) -> updateTotalWorkLabel());
    // TimeZone Picker (Left of Total Work Label)
    // URL timezoneURL = this.getClass().getResource("./timezones.txt");
    ZonesReader z = new ZonesReader();
    timeZonePicker = new JComboBox(z.getZones());
    timeZonePicker.setPreferredSize(new Dimension(120, 30));
    timeZonePicker
      .addActionListener((ActionEvent e) -> currTimeZone = (TaskTimeZone) timeZonePicker.getSelectedItem());
    currTimeZone = (TaskTimeZone) timeZonePicker.getItemAt(0);

    // Set up Delete and Save task Buttons
    //FontIcon delFontIcon = new FontIcon("antf-delete:32");
    //
    var antIcons = new AntDesignIconsOutlinedIkonHandler();
    var antIconsFilled = new AntDesignIconsFilledIkonHandler();
    var openIcons = new OpeniconicIkonHandler();
    taskDelButton = new JButton(FontIcon.of(antIcons.resolve("anto-delete"), 18));
    //taskDelButton.setFont(f);
    taskDelButton.addActionListener(new TaskDeleteListener());
    taskDelButton.setBackground(ButtonColor);
    taskDelButton.setEnabled(false);

    // Set up Window Pin buttons
    JToggleButton pinButton = new JToggleButton(pinIcon, false);
    pinButton.addItemListener(new PinListener());
    pinButton.setBackground(ButtonColor);


    // Set up load list button
    loadButton = new JButton(FontIcon.of(antIconsFilled.resolve("antf-folder-open"), 18));
    loadButton.setBackground(ButtonColor);
    loadButton.addActionListener(new LoadListListener());


    // Set up save list button
    saveButton = new JButton(FontIcon.of(antIconsFilled.resolve("antf-save"), 18));
    saveButton.setBackground(ButtonColor);
    saveButton.addActionListener(new SaveListListener());

    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
    DateFormat format = new SimpleDateFormat("mm:ss");
    timeText = new JFormattedTextField(format);
    f = new Font(Font.MONOSPACED, Font.BOLD, TIMER_FONTSIZE);
    timeText.setFont(f);
    timeText.setText("00:00");
    timeText.addActionListener(new TimeTextListener());

    // Cancel Task Button
    cancelButton = new JButton(cancelIcon);
    cancelButton.setBackground(RED);
    cancelButton.setVisible(false);

    cancelButton.addActionListener((ActionEvent e) -> {
      clock.reset();
      timeText.setText("00:00");
      timeText.setEnabled(true);
      keypad.setEnabled(true);
      swapButtons(playBtn);
      playBtn.setEnabled(true);
      checkButton.setEnabled(false);
      checkButton.setBackground(ButtonColor);
      cancelButton.setVisible(false);
      if(taskbar != null){
        taskbarState = State.OFF;
        taskbar.setWindowProgressValue(frame, 0);
        taskbar.setWindowProgressState(frame, taskbarState);
      }
    });

    // Add buttons to arrayList for easy access
    buttonList = new ArrayList<AbstractButton>();
    buttonList.add(playBtn);
    buttonList.add(checkButton);
    buttonList.add(restartBtn);
    buttonList.add(pauseBtn);
    buttonList.add(loadButton);
    buttonList.add(saveButton);
    buttonList.add(taskDelButton);
    buttonList.add(pinButton);
    buttonList.add(editNameButton);
    buttonList.add(goalButton);

    // Auto-repeat checkbox
    autoRepeat = new JCheckBox("Repeat", false);
    autoRepeat.setBackground(BackColor);
    // repeatButton.setPreferredSize(new Dimension(64,64));
    // Vertical Layout for cancel and repeat buttons
    cancelRepeatPanel = new JPanel();
    cancelRepeatPanel.setBackground(BackColor);
    cancelRepeatPanel.setLayout(new BoxLayout(cancelRepeatPanel, BoxLayout.Y_AXIS));
    cancelRepeatPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
    cancelRepeatPanel.add(cancelButton);
    cancelRepeatPanel.add(Box.createVerticalStrut(2));
    cancelRepeatPanel.add(autoRepeat);
    cancelRepeatPanel.setPreferredSize(new Dimension(96, 64));
    // add the topmost time display and buttons
    timePanel.add(checkButton);
    timePanel.add(timeText);
    timePanel.add(playBtn);
    timePanel.add(restartBtn);
    timePanel.add(cancelRepeatPanel);
    timePanel.setBackground(BackColor);
    timePanel.addMouseListener(new panelMouseListener());
    topPanel.add(timePanel);
    timeText.setMaximumSize(timeText.getPreferredSize());

    JPanel keypadPanel = new JPanel(new BorderLayout());
    keypadPanel.setBackground(BackColor);
    var downArrow = FontIcon.of(antIcons.resolve("anto-caret-down"),16);
    var rightArrow = FontIcon.of(antIcons.resolve("anto-caret-right"),16);
    JButton toggleKeypad = new JButton(downArrow);
    toggleKeypad.setHorizontalAlignment(SwingConstants.LEFT);
    toggleKeypad.setBackground(BackColor);
    toggleKeypad.setRolloverEnabled(false);
    toggleKeypad.setContentAreaFilled(false);
    toggleKeypad.setBorder(BorderFactory.createEmptyBorder(2,4,2,2));
    toggleKeypad.setFocusPainted(false);
    toggleKeypad.addActionListener((ActionEvent e) -> {
      if(keypad.buttonPanel.isVisible()){
        keypad.buttonPanel.setVisible(false);
        toggleKeypad.setIcon(rightArrow);
      }
      else{
        keypad.buttonPanel.setVisible(true);
        toggleKeypad.setIcon(downArrow);
      }
    });

    var rowIcon = FontIcon.of(openIcons.resolve("oi-expand-up"),16);
    var gridIcon = FontIcon.of(openIcons.resolve("oi-grid-three-up"),16);
    JButton toggleCompact= new JButton(rowIcon);
    toggleCompact.setHorizontalAlignment(SwingConstants.RIGHT);
    toggleCompact.setBackground(BackColor);
    toggleCompact.setRolloverEnabled(false);
    toggleCompact.setContentAreaFilled(false);
    toggleCompact.setBorder(BorderFactory.createEmptyBorder(2,2,2,8));
    toggleCompact.setFocusPainted(false);
    toggleCompact.addActionListener((ActionEvent e) -> {
      if(!keypad.isCompact()){
        keypad.setCompact(true);
        toggleCompact.setIcon(gridIcon);
      }
      else{
        keypad.setCompact(false);
        toggleCompact.setIcon(rowIcon);
      }
    });

    JPanel togglePane = new JPanel(new BorderLayout());
    togglePane.setBackground(BackColor);
    togglePane.add(BorderLayout.WEST, toggleKeypad);
    togglePane.add(BorderLayout.EAST, toggleCompact);
    keypadPanel.add(BorderLayout.NORTH, togglePane);
    keypadPanel.add(BorderLayout.CENTER, keypad.buttonPanel);
    topPanel.add(keypadPanel);

    workPanel = new JPanel();
    workPanel.setBackground(BackColor);
    workPanel.setLayout(new BorderLayout());
    workPanel.add(goalButton, BorderLayout.LINE_START);
    workPanel.add(workLabel, BorderLayout.CENTER);
    workPanel.add(unitPicker, BorderLayout.LINE_END);
    topPanel.add(workPanel);
    topPanel.add(goalProgress);

    setBackgroundAllButtons(ButtonColor);

    // Set up task list
    JScrollPane scroll = new JScrollPane(taskWindow);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    // Set up bottom panel
    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BorderLayout());
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
    saveDelPanel.add(loadButton);
    saveDelPanel.add(taskDelButton);
    saveDelPanel.add(saveButton);

    timeZonePanel = new JPanel();
    timeZonePanel.setBackground(BackColor);
    timeZonePanel.add(timeZonePicker);

    pinPanel.setBackground(BackColor);
    pinPanel.add(pinButton);
    bottomPanel.addMouseListener(new panelMouseListener());
    bottomPanel.add(BorderLayout.WEST, timeZonePanel);
    bottomPanel.add(BorderLayout.CENTER, saveDelPanel);
    bottomPanel.add(BorderLayout.EAST, pinPanel);
    bottomPanel.add(BorderLayout.SOUTH, workNamePanel);

    // change task list font
    f = new Font(Font.SANS_SERIF, Font.PLAIN, TASK_FONTSIZE);
    taskWindow.setFont(f);

    // Lay out contents on the frame
    frame.getContentPane().add(BorderLayout.NORTH, topPanel);
    frame.getContentPane().add(BorderLayout.CENTER, scroll);
    frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);

    // Set up frame properties
    frame.setSize(WINSIZE_X, WINSIZE_Y);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.setVisible(true);
    TaskIO.loadTasksJson("test.json");

    try {
      setUIFont(new javax.swing.plaf.FontUIResource(Font.SANS_SERIF, Font.PLAIN, 22));
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
        if (taskList.getSize() > 0) {
          String ObjButtons[] = { "Yes", "No" };
          JLabel text = new JLabel("Do you want to save TASK LIST!?");
          text.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TASK_FONTSIZE));
          int PromptResult = JOptionPane.showOptionDialog(null, text, "Task Timer",
              JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, exitDialogIcon, ObjButtons,
              ObjButtons[0]);
          if (PromptResult == JOptionPane.NO_OPTION) {
            System.exit(0);
          } else {
            JFileChooser fileSave = new JFileChooser();
            fileSave.setMinimumSize(new Dimension(700, 1000));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text file", "txt");
            fileSave.addChoosableFileFilter(filter);
            fileSave.setFileFilter(filter);
            String defaultFile = (workName.getText() == "") ? "fileToSave" : workName.getText();
            fileSave.showSaveDialog(frame);
            fileSave.setSelectedFile(new File(defaultFile));
            Task[] taskArr = Arrays.copyOf(taskList.toArray(), taskList.toArray().length, Task[].class);
            TaskIO.saveTasks(fileSave.getSelectedFile().getAbsolutePath() + ".txt", getTotalWorkStr(), taskArr);
            System.exit(0);
          }
        } else {
          System.exit(0);
        }
      }
    });

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

  private String sectoHrMinSec(int seconds) {
    int hours = seconds / 60 / 60;
    int minutes = (seconds / 60) - (hours * 60);
    int secs = seconds % 60;
    return String.format("%02d:%02d:%02d", hours, minutes, secs);
  }

  private float sectoHr(int seconds) {
    int hours = seconds / 60 / 60;
    int secs = 60 * ((seconds / 60) - (hours * 60)) + (seconds % 60);
    float frac = (float) hours + (((float) secs) / (float) 3600.0);
    return frac;
  }

  private float sectoMin(int seconds) {
    return (float) (seconds / 60.0);
  }

  private String secondsToMinSec(int seconds) {
    int minutes = seconds / 60;
    int secs = seconds - (minutes * 60);
    return String.format("%02d:%02d", minutes, secs);
  }

  private int minSectoSeconds(String s) {
    String[] split = s.split(":");
    int mins = Integer.parseInt(split[0]);
    int secs = Integer.parseInt(split[1]);

    return (mins * 60) + secs;
  }

  private void swapButtons(JButton b) {
    timePanel.removeAll();
    timePanel.add(checkButton);
    timePanel.add(timeText);
    timePanel.add(b);
    timePanel.add(restartBtn);
    timePanel.add(cancelRepeatPanel);
    timePanel.revalidate();
    timePanel.repaint();
  }

  public class PinListener implements ItemListener {
    public void itemStateChanged(ItemEvent ev) {
      if (ev.getStateChange() == ItemEvent.SELECTED) {
        frame.setAlwaysOnTop(true);
      } else if (ev.getStateChange() == ItemEvent.DESELECTED) {
        frame.setAlwaysOnTop(false);
      }
    }
  }

  public class KeyboardListener implements KeyListener {
    public void keyPressed(KeyEvent e){}
    public void keyReleased(KeyEvent e) {
      // if(e.getKeyCode()== KeyEvent.VK_SPACE)
      // restartBtn.doClick();
    }
    public void keyTyped(KeyEvent e) {
    }
  }

  public class ClockListener implements ClockTickListener {
    public void Tick() {
      long progress = Math.round((double)clock.getTimeRemaining() / clock.getCountSeconds() * 100);
      if(taskbar != null){
        taskbarState = State.NORMAL;
        taskbar.setWindowProgressValue(frame, (int) progress);
        taskbar.setWindowProgressState(frame, taskbarState);
      }
      timeText.setText(secondsToMinSec(clock.getTimeRemaining()));
      if (clock.getTimeRemaining() % 2 == 0) {
        Color clr = new Color(255, 79, 76);
        timeText.setDisabledTextColor(clr);
      } else {
        timeText.setDisabledTextColor(new Color(169, 169, 169));
      }
    }

    public void finishTick() {
      aClip.setFramePosition(0);
      aClip.start();
      var t = new Task(workName.getText(), clock.getCountSeconds(), currTimeZone);
      //var st = ScalaTask.apply(workName.getText(), clock.getCountSeconds(), currTimeZone);
      taskList.add(0, t);
      if(taskbar != null){
        taskbarState = State.ERROR;
        taskbar.setWindowProgressValue(frame, -1);
        taskbar.setWindowProgressState(frame, taskbarState);
      }
      if (!autoRepeat.isSelected()) {
        timeText.setEnabled(true);
        checkButton.setEnabled(false);
        cancelButton.setVisible(false);
        playBtn.setEnabled(false);
        checkButton.setBackground(ButtonColor);
        swapButtons(playBtn);
        timeText.setText("00:00");
        keypad.setEnabled(true);
        frame.requestFocus();
      } else {
        int s = clock.getCountSeconds();
        clock.stop();
        clock = new TaskClock(s, new ClockListener());
        clock.start();
        swapButtons(pauseBtn);
        timeText.setText(secondsToMinSec(clock.getCountSeconds()));
        timeText.setEnabled(false);
        checkButton.setEnabled(true);
        checkButton.setBackground(GREEN);
        keypad.setEnabled(false);
      }
      // Add new task to list
      updateTotalWorkLabel();
      //AutoSave
      Task[] taskArr = Arrays.copyOf(taskList.toArray(), taskList.toArray().length, Task[].class);
      TaskIO.autoSave(workName.getText(), getTotalWorkStr(), taskArr);

      frame.revalidate();
      frame.repaint();
    }
  }

  public class TaskDeleteListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      int i = taskWindow.getSelectedIndex();
      taskList.remove(i);
      updateTotalWorkLabel();
      goalProgress.setValue((int) getTotalWorkTime());
      frame.revalidate();
      frame.repaint();
    }
  }

  public class SaveListListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      int numTasks = taskList.getSize();
      if (numTasks > 0) {
        JFileChooser fileSave = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text file", "txt");
        fileSave.addChoosableFileFilter(filter);
        fileSave.setFileFilter(filter);
        String defaultFile = (workName.getText() == "") ? "fileToSave" : workName.getText();
        fileSave.setSelectedFile(new File(defaultFile));
        fileSave.showSaveDialog(frame);
        File tmp = fileSave.getSelectedFile();
        Task[] taskArr = Arrays.copyOf(taskList.toArray(), taskList.toArray().length, Task[].class);
        TaskIO.saveTasks(tmp.getAbsolutePath() + ".txt", getTotalWorkStr(), taskArr);
      }
    }
  }

  public class LoadListListener implements ActionListener{
    public void actionPerformed(ActionEvent a){
        JFileChooser fileLoadDiag = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text file", "txt");
        fileLoadDiag.addChoosableFileFilter(filter);
        fileLoadDiag.setFileFilter(filter);
        fileLoadDiag.showOpenDialog(frame);
        File inFile = fileLoadDiag.getSelectedFile();
        var list = TaskIO.loadTasks(inFile.getAbsolutePath());

        var loadTasks = new JList(list.tasks());
        var scroll = new JScrollPane(loadTasks);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //scroll.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 6));

        var f = new JFrame();
        f.setBackground(BackColor);
        var dia = new JDialog(f, "Open Tasks: " + inFile.getAbsolutePath(),true);
        dia.setBackground(BackColor);
        dia.setLayout(new BorderLayout());

        var diaWidth = WINSIZE_X;
        var diaHeight = WINSIZE_Y;
        dia.setSize(diaWidth,diaHeight);

        var fon = new Font(Font.SANS_SERIF, Font.PLAIN, TASK_FONTSIZE);
        var totalLabel = new JTextField(list.label(), SwingConstants.CENTER);
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        var filNameLabel = new JLabel("파일: " + inFile.getAbsolutePath());
        filNameLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        f.add(filNameLabel);
        totalLabel.setFont(fon);
        totalLabel.setEditable(false);
        loadTasks.setFont(fon);

        var antIconsFilled = new AntDesignIconsFilledIkonHandler();
        var close = new JButton(FontIcon.of(antIconsFilled.resolve("antf-close-square"), 18));
        var closePan = new JPanel(new FlowLayout());
        closePan.add(close);
        close.setBackground(ButtonColor);
        close.addActionListener((ActionEvent e) -> dia.dispose());

        dia.add(BorderLayout.NORTH, totalLabel);
        dia.add(BorderLayout.CENTER, scroll);
        dia.add(BorderLayout.SOUTH, closePan);
        dia.setVisible(true);
    }
  }

  public class TaskListListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting() == false) {
        if (taskWindow.getSelectedIndex() == -1) {
          taskDelButton.setEnabled(false);
        } else {
          taskDelButton.setEnabled(true);
        }
      }
    }
  }

  public class TimeTextListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      if (clock.isPaused()) {
        swapButtons(playBtn);
      }
      int time = 0;
      time = minSectoSeconds(timeText.getText());
      if (time > 0) {
        clock = new TaskClock(time, new ClockListener());
        playBtn.setEnabled(true);
      }

      frame.requestFocus();
      timeText.setText(secondsToMinSec(time));
      aClip.stop();
    }
  }

  public class StartTimerListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      if (clock.getCountSeconds() > 0) {
        clock.start();
        timeText.setEnabled(false);
        keypad.setEnabled(false);
        cancelButton.setVisible(true);
        swapButtons(pauseBtn);
        checkButton.setEnabled(true);
        checkButton.setBackground(GREEN);
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
  }

  public class PauseTimerListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      clock.pause();
      if(taskbar != null){
        taskbarState = State.PAUSED;
        taskbar.setWindowProgressState(frame, taskbarState);
      }
      timeText.setEnabled(true);
      keypad.setEnabled(true);
      swapButtons(playBtn);
      playBtn.setEnabled(true);
    }
  }

  public class RestartListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      if(clock.getCountSeconds() != 0){
        int s = clock.getCountSeconds();
        clock.stop();
        clock = new TaskClock(s, new ClockListener());
        clock.start();
        swapButtons(pauseBtn);
        timeText.setText(secondsToMinSec(clock.getCountSeconds()));
        timeText.setEnabled(false);
        checkButton.setEnabled(true);
        checkButton.setBackground(GREEN);
        cancelButton.setVisible(true);
        keypad.setEnabled(false);
        aClip.stop();
      }
    }
  }

  public class SubmitTaskListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      clock.stop();
      aClip.stop();
      timeText.setEnabled(true);
      checkButton.setEnabled(false);
      checkButton.setBackground(ButtonColor);
      cancelButton.setVisible(false);
      swapButtons(playBtn);
      playBtn.setEnabled(false);
      timeText.setText("00:00");
      // Add new task to list
      taskList.add(0, new Task(workName.getText(), clock.getCountSeconds(), currTimeZone));
      //TODO: pass the taskList to Scala Task JSON Writer (and write)
      updateTotalWorkLabel();
      //AutoSave
      Task[] taskArr = Arrays.copyOf(taskList.toArray(), taskList.toArray().length, Task[].class);
      TaskIO.autoSave(workName.getText(), getTotalWorkStr(), taskArr);
      goalProgress.setValue((int) getTotalWorkTime());
      keypad.setEnabled(true);
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
      clock = new TaskClock(seconds, new ClockListener());
      timeText.setText(secondsToMinSec(seconds));
      playBtn.setEnabled(true);
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
          TimerConf.write(workName.getText(), keypad.getButtonList(), getHex(BackColor), getHex(ButtonColor),
              "TaskTimer");
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
        TimerConf.write(workName.getText(), keypad.getButtonList(), getHex(BackColor), getHex(ButtonColor),
            "TaskTimer");
      }
    }
  }

  public class panelMouseListener implements MouseListener {
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON3) {
        backgroundPopup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  private void setBackgroundAllButtons(Color newColor) {
    for (AbstractButton b : buttonList)
      b.setBackground(newColor);
    goalProgress.setForeground(newColor);
  }
  private void updateTotalWorkLabel() {
    int total = (int) getTotalWorkTime();
    String s = switch ((String) unitPicker.getSelectedItem()) {
      case "Hr." -> String.format("%.3f", sectoHr(total)) + " 시간";
      case "Min." -> String.format("%.1f", sectoMin(total)) + " 분";
      case "Sec." -> Integer.toString(total) + " 초";
      default -> "d";
    };
    if (total > 0) {
      workLabel.setText(taskList.getSize() + "개" + " - " + sectoHrMinSec(total) + " - " + "" + s);
    } else {
      workLabel.setText("No tasks completed.");
    }
  }

  private String getTotalWorkStr(){
    long total = getTotalWorkTime();
    if (total > 0){
      var hrstr = String.format("%.3f", sectoHr((int) total)) + " 시간";
      var minstr = String.format("%.1f", sectoMin((int) total)) + " 분";
      var secstr = Integer.toString((int) total) + " 초";
      return "총: " + taskList.getSize() + "개" + " - "
                                + sectoHrMinSec((int) total) + " ("
                                + " |" + hrstr + "| "
                                + " |" + minstr + "| "
                                + " |" + secstr + "| )";

    }
    else{
      return "No tasks completed.";
    }
  }

  // Returns total work time in seconds
  private long getTotalWorkTime() {
    int numTasks = taskList.getSize();
    long totalWork = 0;
    for (int i = 0; i < numTasks; i++) {
      totalWork += taskList.getElementAt(i).taskTime;
    }
    return totalWork;
  }

  private String getHex(java.awt.Color c) {
    return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
  }

  @FunctionalInterface
  public interface SimpleDocumentListener extends DocumentListener {
    void update(DocumentEvent e);

    @Override
    default void insertUpdate(DocumentEvent e) {
      update(e);
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
      update(e);
    }

    @Override
    default void changedUpdate(DocumentEvent e) {
      update(e);
    }
  }
}
