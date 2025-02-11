// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel {
  public void start(JFrame frame) {
    String path = "example/splash.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image img = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);

    JWindow splashScreen = createSplashScreen(frame, new ImageIcon(img));
    splashScreen.setVisible(true);

    new Thread(() -> {
      try {
        // dummy long task
        Thread.sleep(6000);
        EventQueue.invokeAndWait(() -> {
          showFrame(frame);
          // hideSplash();
          splashScreen.setVisible(false);
          splashScreen.dispose();
        });
      } catch (InterruptedException | InvocationTargetException ex) {
        ex.printStackTrace();
        splashScreen.setVisible(false);
        splashScreen.dispose();
        Thread.currentThread().interrupt();
      }
    }).start();
  }

  private static Component makeUI() {
    JLabel label = new JLabel("Draggable Label (@title@)");
    DragWindowListener dwl = new DragWindowListener();
    label.addMouseListener(dwl);
    label.addMouseMotionListener(dwl);
    label.setOpaque(true);
    label.setForeground(Color.WHITE);
    label.setBackground(Color.BLUE);
    label.setBorder(BorderFactory.createEmptyBorder(5, 16 + 5, 5, 2));

    JButton button = new JButton("Exit");
    button.addActionListener(e -> {
      JComponent c = (JComponent) e.getSource();
      Window frame = (Window) c.getTopLevelAncestor();
      // frame.dispose();
      // System.exit(0);
      // frame.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
      frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(button);

    JPanel p = new JPanel(new BorderLayout());
    p.add(label, BorderLayout.NORTH);
    p.add(box, BorderLayout.SOUTH);
    p.add(new JLabel("Alt+Space => System Menu"));
    return p;
  }

  private static Image makeMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }

  public static JWindow createSplashScreen(Frame frame, ImageIcon img) {
    DragWindowListener dwl = new DragWindowListener();

    JLabel label = new JLabel(img);
    label.addMouseListener(dwl);
    label.addMouseMotionListener(dwl);

    JWindow window = new JWindow(frame);
    window.getContentPane().add(label);
    window.pack();
    window.setLocationRelativeTo(null);
    return window;
  }

  public static void showFrame(JFrame frame) {
    frame.getContentPane().add(makeUI());
    frame.setMinimumSize(new Dimension(100, 100));
    frame.setSize(320, 240);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  // public static JMenuBar createMenuBar() {
  //   JMenuBar menuBar = new JMenuBar();
  //   JMenu menu = new JMenu("FFF");
  //   menu.setMnemonic(KeyEvent.VK_F);
  //   menuBar.add(menu);
  //
  //   JMenuItem menuItem = new JMenuItem("NNN");
  //   menuItem.setMnemonic(KeyEvent.VK_N);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   menuItem = new JMenuItem("MMM");
  //   menuItem.setMnemonic(KeyEvent.VK_M);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   menuItem = new JMenuItem("UUU");
  //   menuItem.setMnemonic(KeyEvent.VK_U);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   menuItem = new JMenuItem("III");
  //   menuItem.setMnemonic(KeyEvent.VK_I);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   return menuBar;
  // }

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
    JFrame frame = new JFrame();
    frame.setUndecorated(true);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    // frame.getContentPane().add(new MainPanel(frame));
    // frame.setMinimumSize(new Dimension(100, 100));
    // frame.setSize(320, 240);
    // frame.setLocationRelativeTo(null);
    // frame.setVisible(true);
    new MainPanel().start(frame);
  }
}

class DragWindowListener extends MouseAdapter {
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      startPt.setLocation(e.getPoint());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = SwingUtilities.getRoot(e.getComponent());
    if (c instanceof Window && SwingUtilities.isLeftMouseButton(e)) {
      Point pt = c.getLocation();
      c.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();

    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;

    g2.setColor(Color.WHITE);
    g2.fillRect(x, y, w, h);

    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(x + gap, y + gap, x + w - gap, y + h - gap);
    g2.drawLine(x + gap, y + h - gap, x + w - gap, y + gap);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}
