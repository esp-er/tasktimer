package patriker.tasktimer;

import java.awt.event.*;
interface TaskButtonListener{
	void buttonClicked(int seconds,int btnIndex);
	void rightClick(int btnIndex, MouseEvent e);
}
