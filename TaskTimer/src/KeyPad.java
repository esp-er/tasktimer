package patriker.tasktimer;

import javax.swing.*;
import java.util.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Font.*;
import java.awt.event.*;
import java.util.ArrayList;

import org.kordamp.ikonli.antdesignicons.AntDesignIconsFilledIkonHandler;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlinedIkonHandler;
import org.kordamp.ikonli.openiconic.OpeniconicIkonHandler;
import org.kordamp.ikonli.swing.FontIcon;

public class KeyPad{

  private static final int NUM_BUTTONS = 9;
  private static final int BUTTON_FONTSIZE = 16;


  private TaskButton[] buttonArr = new TaskButton[NUM_BUTTONS];
  private Color buttonColor;
  private Color backgroundColor;
  private boolean compact = false;
  public JPanel panel;
  private JPanel togglePanel;
  private JPanel buttonPanel;
  public JToggleButton toggleCompact, toggleHide;


  //Main Constructor
  KeyPad(ArrayList<String> buttons, TaskButtonListener listener){
    panel = new JPanel(new BorderLayout());
    buttonPanel = new JPanel(new GridLayout(3,3));
    panel.setBackground(backgroundColor);

    Font f = new Font(Font.SANS_SERIF, Font.BOLD, BUTTON_FONTSIZE);

    buttonColor = TaskTimer.ButtonColor;
    backgroundColor = TaskTimer.BackColor;

    var i = 0;
    for(String b : buttons){
      buttonArr[i] = new TaskButton(b, i, buttonColor, listener,f);
      buttonPanel.add(buttonArr[i].btn);
      i++;
    }

    togglePanel = setupTogglePanel();
    togglePanel.setBackground(backgroundColor);

    panel.add(togglePanel, BorderLayout.NORTH);
    panel.add(buttonPanel, BorderLayout.CENTER);
  }

  private JPanel setupTogglePanel(){
    var resultPanel = new JPanel(new BorderLayout());

    var openIcons = new OpeniconicIkonHandler();
    var antIcons = new AntDesignIconsOutlinedIkonHandler();


    var downArrow = FontIcon.of(antIcons.resolve("anto-caret-down"),16);
    var rightArrow = FontIcon.of(antIcons.resolve("anto-caret-right"),16);
    toggleHide = new JToggleButton(downArrow);
    toggleHide.setHorizontalAlignment(SwingConstants.LEFT);
    toggleHide.setBackground(backgroundColor);
    toggleHide.setRolloverEnabled(false);
    toggleHide.setContentAreaFilled(false);
    toggleHide.setBorder(BorderFactory.createEmptyBorder(2,4,2,2));
    toggleHide.setFocusPainted(false);

    toggleHide.addItemListener((ItemEvent ev) -> {
      if (ev.getStateChange() == ItemEvent.SELECTED) {
        toggleHide.setIcon(rightArrow);
        buttonPanel.setVisible(false);
      }
      else{
        toggleHide.setIcon(downArrow);
        buttonPanel.setVisible(true);
      }
    });


    var rowIcon = FontIcon.of(openIcons.resolve("oi-expand-up"),16);
    var gridIcon = FontIcon.of(openIcons.resolve("oi-grid-three-up"),16);

    toggleCompact = new JToggleButton(rowIcon);
    toggleCompact.setHorizontalAlignment(SwingConstants.RIGHT);
    toggleCompact.setBackground(backgroundColor);
    toggleCompact.setRolloverEnabled(false);
    toggleCompact.setContentAreaFilled(false);
    toggleCompact.setBorder(BorderFactory.createEmptyBorder(2,2,2,8));
    toggleCompact.setFocusPainted(false);
    toggleCompact.addItemListener((ItemEvent ev) -> {
      if (ev.getStateChange() == ItemEvent.SELECTED) {
        toggleCompact.setIcon(gridIcon);
        setCompact(true);
      }
      else{
        toggleCompact.setIcon(rowIcon);
        setCompact(false);
      }
    });



    resultPanel.add(toggleHide, BorderLayout.WEST);
    resultPanel.add(toggleCompact, BorderLayout.EAST);

    return resultPanel;

  }

  public void setEnabled(boolean value){
    for(TaskButton b : buttonArr){
      b.btn.setEnabled(value);
    }
  }

  public void setCompact(boolean b){
    if(b){
      for(int i = 3; i < NUM_BUTTONS; i++){
        buttonPanel.remove(buttonArr[i].btn);
      }
      buttonPanel.setLayout(new GridLayout(1,3));
      buttonPanel.revalidate();
      buttonPanel.repaint();
      compact = true;
    }
    else{
      buttonPanel.removeAll();
      buttonPanel.setLayout(new GridLayout(3,3));
      for(TaskButton button : buttonArr){
        buttonPanel.add(button.btn);
      }
      buttonPanel.revalidate();
      buttonPanel.repaint();
      compact = false;
    }
  }

  public boolean isCompact(){
    return compact;
  }


  public Color getButtonColor(){
    return buttonColor;
  }

  public void setColor(Color c){
    panel.setBackground(c);
  }

  public void setButtonColor(Color c){
    buttonColor = c;
    for(TaskButton b : buttonArr){
      b.setColor(c);
    }
  }

  public void setButtonTextColor(Color c){
    for(TaskButton b : buttonArr){
      b.setTextColor(c);
    }
  }

  public void setButtonSecs(int i, int s){
    buttonArr[i].setSeconds(s);
  }

  public int getButtonSecs(int i){
    return buttonArr[i].getSeconds();
  }

  public ArrayList<String> getButtonList(){
    var a = new ArrayList<String>();
    for (TaskButton b : buttonArr){
      a.add(b.btn.getText());
    }
    return a;
  }
}
