package patriker.tasktimer;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

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
