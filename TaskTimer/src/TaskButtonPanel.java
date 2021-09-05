package patriker.tasktimer;

import javax.swing.*;
import java.util.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Font.*;
import java.util.ArrayList;


public class TaskButtonPanel{

	private static final int NUM_BUTTONS = 9;
	private static final int BUTTON_FONTSIZE = 18;


	private TaskButton[] buttonArr = new TaskButton[NUM_BUTTONS];
	private Color buttonColor;
	private Color backgroundColor; 
  private boolean compact = false;
	public JPanel buttonPanel;
	
	TaskButtonPanel(ArrayList<String> buttons, TaskButtonListener listener){
		buttonPanel = new JPanel(new GridLayout(3,3));
		buttonPanel.setBackground(backgroundColor);
		
		Font f = new Font(Font.MONOSPACED, Font.BOLD, BUTTON_FONTSIZE);
		
		buttonColor = TaskTimer.ButtonColor;
		backgroundColor = TaskTimer.BackColor;

    
    var i = 0;
		for(String b : buttons){
			buttonArr[i] = new TaskButton(b, i, buttonColor, listener,f);
			buttonPanel.add(buttonArr[i].btn);
      i++;
		}
	}

	public void setEnabled(boolean value){
		for(TaskButton b : buttonArr){
			b.btn.setEnabled(value);	
		}
	}

  public void setCompact(boolean b){
    if(b == true){
      var i = 0;
      for(TaskButton button : buttonArr){
        if(i > 2){
          buttonPanel.remove(button.btn);
        }
        i += 1;
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
		buttonPanel.setBackground(c);
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
