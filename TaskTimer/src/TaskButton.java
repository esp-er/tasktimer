package patriker.tasktimer;

import javax.swing.*;
import java.util.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

public class TaskButton{
	public JButton btn; 
	private int seconds;
	private int btnIndex;
	private List<TaskButtonListener> listeners;

	TaskButton(){
	}

	TaskButton(String s, int index, TaskButtonListener listener){
		btnIndex = index;
		listeners = new ArrayList<TaskButtonListener>();
		listeners.add(listener);
		btn = new JButton(s);
		s = s.replace("s","");
		seconds= Integer.parseInt(s);
		btn.addActionListener(new ButtonListener());
		btn.addMouseListener(new ButtonRightListener());
	}

	TaskButton(String s, int index, Color c, TaskButtonListener listener, Font f){
		this(s,index,c,listener);
		btn.setFont(f);
	}
	TaskButton(String s, int index, Color c, TaskButtonListener listener){
		this(s,index,listener);
		btn.setBackground(c);
	}

	public int getSeconds(){
		return seconds;
	}

	public void setSeconds(String label){
		String s = label.replace("s","");
		seconds = Integer.parseInt(s);
		btn.setText(s);
	}

	public void setSeconds(int s){
		seconds = s;
		String label = Integer.toString(s) + "s";
		btn.setText(label);
	}

	public void setColor(Color clr){
		btn.setBackground(clr);
	}

	public void setTextColor(Color clr){
		btn.setForeground(clr);
	}

	public void setFont(Font f){
		btn.setFont(f);
	}

	public class ButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			for(TaskButtonListener l : listeners){
				l.buttonClicked(seconds, btnIndex);
			}
		}
	}

	public class ButtonRightListener implements MouseListener{
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) { }
		public void mouseClicked(MouseEvent e){ }

	   @Override
	   public void mousePressed(MouseEvent e) {
		  if (e.getButton() == MouseEvent.BUTTON3) {
        for(TaskButtonListener l : listeners){
          l.rightClick(btnIndex, e);
        }
		  }
	   }
	}
}
