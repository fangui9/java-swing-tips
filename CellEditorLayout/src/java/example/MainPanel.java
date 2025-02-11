// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.util.EventObject;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(8, 4);
    table.getColumnModel().getColumn(0).setCellEditor(new CustomComponentCellEditor(new JTextField()));
    table.getColumnModel().getColumn(1).setCellEditor(new CustomCellEditor(new JTextField()));
    table.getColumnModel().getColumn(2).setCellEditor(new CustomComponentCellEditor2(new CustomComponent()));
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class CustomCellEditor extends DefaultCellEditor {
  private static final int BUTTON_WIDTH = 20;
  private final JButton button = new JButton();

  protected CustomCellEditor(JTextField field) {
    super(field);
    field.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, BUTTON_WIDTH));
    field.addHierarchyListener(e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c instanceof JTextField && c.isShowing()) {
        // System.out.println("hierarchyChanged: SHOWING_CHANGED");
        JTextField tc = (JTextField) c;
        tc.removeAll();
        tc.add(button);
        Rectangle r = tc.getBounds();
        button.setBounds(r.width - BUTTON_WIDTH, 0, BUTTON_WIDTH, r.height);
        // tc.requestFocusInWindow();
      }
    });
  }

  @Override public Component getComponent() {
    // @see JTable#updateUI()
    SwingUtilities.updateComponentTreeUI(button);
    return super.getComponent();
  }
}

// class CustomComponentCellEditor extends AbstractCellEditor implements TableCellEditor {
class CustomComponentCellEditor extends DefaultCellEditor {
  private final JTextField field;
  private final JPanel panel = new JPanel(new BorderLayout());

  protected CustomComponentCellEditor(JTextField field) {
    super(field);
    this.field = field;
    JButton button = new JButton() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 25;
        return d;
      }
    };
    field.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    panel.add(field);
    panel.add(button, BorderLayout.EAST);
    panel.setFocusable(false);
  }

  // public Object getCellEditorValue() {
  //   return field.getText();
  // }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    // System.out.println("getTableCellEditorComponent");
    field.setText(Objects.toString(value, ""));
    EventQueue.invokeLater(() -> {
      field.setCaretPosition(field.getText().length());
      field.requestFocusInWindow();
    });
    return panel;
  }

  @Override public boolean isCellEditable(EventObject e) {
    // System.out.println("isCellEditable");
    // if (e instanceof KeyEvent) {
    //   // System.out.println("KeyEvent");
    //   EventQueue.invokeLater(() -> {
    //     char kc = ((KeyEvent) e).getKeyChar();
    //     if (!Character.isIdentifierIgnorable(kc)) {
    //       field.setText(field.getText() + kc);
    //     }
    //     field.setCaretPosition(field.getText().length());
    //     // field.requestFocusInWindow();
    //   });
    // }
    EventQueue.invokeLater(() -> {
      if (e instanceof KeyEvent) {
        KeyEvent ke = (KeyEvent) e;
        char kc = ke.getKeyChar();
        // int kc = ke.getKeyCode();
        if (Character.isUnicodeIdentifierStart(kc)) {
          field.setText(field.getText() + kc);
        }
      }
    });
    return super.isCellEditable(e);
  }

  @Override public Component getComponent() {
    return panel;
  }
}

class CustomComponent extends JPanel {
  // static class CustomTextField extends JTextField {
  //   @Override protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
  //     return super.processKeyBinding(ks, e, condition, pressed);
  //   }
  // }
  // public final CustomTextField field = new CustomTextField();
  public final JTextField field = new JTextField();

  protected CustomComponent() {
    super(new BorderLayout());
    // this.setFocusable(false);
    this.add(field);
    JButton button = new JButton();
    this.add(button, BorderLayout.EAST);
  }

  @Override protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
    if (!field.isFocusOwner() && !pressed) {
      field.requestFocusInWindow();
      KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
      EventQueue.invokeLater(() -> kfm.redispatchEvent(field, e));
    }
    return super.processKeyBinding(ks, e, condition, pressed);
    // field.requestFocusInWindow();
    // return field.processKeyBinding(ks, e, condition, pressed);
  }
}

class CustomComponentCellEditor2 extends DefaultCellEditor {
  private final CustomComponent component;

  protected CustomComponentCellEditor2(CustomComponent component) {
    super(component.field);
    this.component = component;
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    component.field.setText(Objects.toString(value, ""));
    return this.component;
  }

  @Override public Component getComponent() {
    return component;
  }
}
