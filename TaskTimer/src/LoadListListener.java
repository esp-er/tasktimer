package patriker.tasktimer;

import java.awt.event.*;  
import javax.swing.event.*;  

public class LoadListListener implements ActionListener{
    public void actionPerformed(ActionEvent a){
      /*
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
        */
    }
  }


