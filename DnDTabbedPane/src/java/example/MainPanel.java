// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DnDTabbedPane sub = new DnDTabbedPane();
    sub.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    sub.addTab("Title aa", new JLabel("aaa"));
    sub.addTab("Title bb", new JScrollPane(new JTree()));
    sub.addTab("Title cc", new JScrollPane(new JTextArea("123412341234\n46746745\n245342\n")));

    DnDTabbedPane tab = new DnDTabbedPane();
    tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tab.addTab("JTree 00", new JScrollPane(new JTree()));
    tab.addTab("JLabel 01", new JLabel("Test"));
    tab.addTab("JTable 02", new JScrollPane(new JTable(20, 3)));
    tab.addTab("JTextArea 03", new JScrollPane(new JTextArea("111111111\n2222222222\n")));
    tab.addTab("JLabel 04", new JLabel("<html>33333333333<br>13412341234123446745"));
    tab.addTab("null 05", null);
    tab.addTab("JTabbedPane 06", sub);
    tab.addTab("Title 000000000000000007", new JScrollPane(new JTree()));

    add(makeCheckBoxPanel(tab), BorderLayout.NORTH);
    add(tab);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeCheckBoxPanel(DnDTabbedPane tabs) {
    JCheckBox check1 = new JCheckBox("Tab Ghost", true);
    check1.addActionListener(e -> tabs.hasGhost = check1.isSelected());

    JCheckBox check2 = new JCheckBox("Top", true);
    check2.addActionListener(e -> {
      boolean f = ((JCheckBox) e.getSource()).isSelected();
      tabs.setTabPlacement(f ? SwingConstants.TOP : SwingConstants.RIGHT);
    });

    JCheckBox check3 = new JCheckBox("SCROLL_TAB_LAYOUT", true);
    check3.addActionListener(e -> {
      boolean f = ((JCheckBox) e.getSource()).isSelected();
      tabs.setTabLayoutPolicy(f ? JTabbedPane.SCROLL_TAB_LAYOUT : JTabbedPane.WRAP_TAB_LAYOUT);
    });

    JCheckBox check4 = new JCheckBox("Debug Paint", true);
    check4.addActionListener(e -> tabs.isPaintScrollArea = check4.isSelected());

    JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p1.add(check1);
    p1.add(check2);

    JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p2.add(check3);
    p2.add(check4);

    JPanel p = new JPanel(new BorderLayout());
    p.add(p1, BorderLayout.NORTH);
    p.add(p2, BorderLayout.SOUTH);
    return p;
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

class DnDTabbedPane extends JTabbedPane {
  private static final int LINE_SIZE = 3;
  private static final int RWH = 20;
  private static final int BUTTON_SIZE = 30; // XXX 30 is magic number of scroll button size

  private final GhostGlassPane glassPane = new GhostGlassPane(this);
  protected int dragTabIndex = -1;

  // For Debug: >>>
  protected boolean hasGhost = true;
  protected boolean isPaintScrollArea = true;
  // <<<

  protected Rectangle rectBackward = new Rectangle();
  protected Rectangle rectForward = new Rectangle();

  private void clickArrowButton(String actionKey) {
    JButton forwardButton = null;
    JButton backwardButton = null;
    for (Component c : getComponents()) {
      if (c instanceof JButton) {
        if (Objects.isNull(forwardButton)) {
          forwardButton = (JButton) c;
        } else if (Objects.isNull(backwardButton)) {
          backwardButton = (JButton) c;
        }
      }
    }
    JButton button = "scrollTabsForwardAction".equals(actionKey) ? forwardButton : backwardButton;
    Optional.ofNullable(button)
        .filter(JButton::isEnabled)
        .ifPresent(JButton::doClick);

    // // ArrayIndexOutOfBoundsException
    // Optional.ofNullable(getActionMap())
    //   .map(am -> am.get(actionKey))
    //   .filter(Action::isEnabled)
    //   .ifPresent(a -> a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, 0, 0)));
    // // ActionMap map = getActionMap();
    // // if (Objects.nonNull(map)) {
    // //   Action action = map.get(actionKey);
    // //   if (Objects.nonNull(action) && action.isEnabled()) {
    // //     action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, 0, 0));
    // //   }
    // // }
  }

  public void autoScrollTest(Point glassPt) {
    Rectangle r = getTabAreaBounds();
    if (isTopBottomTabPlacement(getTabPlacement())) {
      rectBackward.setBounds(r.x, r.y, RWH, r.height);
      rectForward.setBounds(r.x + r.width - RWH - BUTTON_SIZE, r.y, RWH + BUTTON_SIZE, r.height);
    } else {
      rectBackward.setBounds(r.x, r.y, r.width, RWH);
      rectForward.setBounds(r.x, r.y + r.height - RWH - BUTTON_SIZE, r.width, RWH + BUTTON_SIZE);
    }
    rectBackward = SwingUtilities.convertRectangle(getParent(), rectBackward, glassPane);
    rectForward = SwingUtilities.convertRectangle(getParent(), rectForward, glassPane);
    if (rectBackward.contains(glassPt)) {
      clickArrowButton("scrollTabsBackwardAction");
    } else if (rectForward.contains(glassPt)) {
      clickArrowButton("scrollTabsForwardAction");
    }
  }

  protected DnDTabbedPane() {
    super();
    glassPane.setName("GlassPane");
    new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new TabDropTargetListener(), true);
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_COPY_OR_MOVE, new TabDragGestureListener());
  }

  protected int getTargetTabIndex(Point glassPt) {
    Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, this);
    Point d = isTopBottomTabPlacement(getTabPlacement()) ? new Point(1, 0) : new Point(0, 1);
    return IntStream.range(0, getTabCount()).filter(i -> {
      Rectangle r = getBoundsAt(i);
      r.translate(-r.width * d.x / 2, -r.height * d.y / 2);
      return r.contains(tabPt);
    }).findFirst().orElseGet(() -> {
      int count = getTabCount();
      Rectangle r = getBoundsAt(count - 1);
      r.translate(r.width * d.x / 2, r.height * d.y / 2);
      return r.contains(tabPt) ? count : -1;
    });
//     for (int i = 0; i < getTabCount(); i++) {
//       Rectangle r = getBoundsAt(i);
//       r.translate(-r.width * d.x / 2, -r.height * d.y / 2);
//       if (r.contains(tabPt)) {
//         return i;
//       }
//     }
//     Rectangle r = getBoundsAt(getTabCount() - 1);
//     r.translate(r.width * d.x / 2, r.height * d.y / 2);
//     return r.contains(tabPt) ? getTabCount() : -1;
  }

  protected void convertTab(int prev, int next) {
    if (next < 0 || prev == next) {
      // This check is needed if tab content is null.
      return;
    }
    final Component cmp = getComponentAt(prev);
    final Component tab = getTabComponentAt(prev);
    final String title = getTitleAt(prev);
    final Icon icon = getIconAt(prev);
    final String tip = getToolTipTextAt(prev);
    final boolean isEnabled = isEnabledAt(prev);
    int tgtIndex = prev > next ? next : next - 1;
    remove(prev);
    insertTab(title, icon, cmp, tip, tgtIndex);
    setEnabledAt(tgtIndex, isEnabled);
    // When you drag'n'drop a disabled tab, it finishes enabled and selected.
    // pointed out by dlorde
    if (isEnabled) {
      setSelectedIndex(tgtIndex);
    }
    // I have a component in all tabs (JLabel with an X to close the tab)
    // and when i move a tab the component disappear.
    // pointed out by Daniel Dario Morales Salas
    setTabComponentAt(tgtIndex, tab);
  }

  protected void initTargetLine(int next) {
    boolean isSideNeighbor = next < 0 || dragTabIndex == next || next - dragTabIndex == 1;
    if (isSideNeighbor) {
      glassPane.setTargetRect(0, 0, 0, 0);
      return;
    }
    Optional.ofNullable(getBoundsAt(Math.max(0, next - 1))).ifPresent(boundsRect -> {
      final Rectangle r = SwingUtilities.convertRectangle(this, boundsRect, glassPane);
      int a = Math.min(next, 1); // a = (next == 0) ? 0 : 1;
      if (isTopBottomTabPlacement(getTabPlacement())) {
        glassPane.setTargetRect(r.x + r.width * a - LINE_SIZE / 2, r.y, LINE_SIZE, r.height);
      } else {
        glassPane.setTargetRect(r.x, r.y + r.height * a - LINE_SIZE / 2, r.width, LINE_SIZE);
      }
    });
  }

  protected void initGlassPane(Point tabPt) {
    getRootPane().setGlassPane(glassPane);
    if (hasGhost) {
      Component c = Optional.ofNullable(getTabComponentAt(dragTabIndex))
          .orElseGet(() -> new JLabel(getTitleAt(dragTabIndex)));
      Dimension d = c.getPreferredSize();
      BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = image.createGraphics();
      SwingUtilities.paintComponent(g2, c, glassPane, 0, 0, d.width, d.height);
      g2.dispose();
      glassPane.setImage(image);
      // Rectangle rect = getBoundsAt(dragTabIndex);
      // BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      // Graphics2D g2 = image.createGraphics();
      // paint(g2);
      // g2.dispose();
      // if (rect.x < 0) {
      //   rect.translate(-rect.x, 0);
      // }
      // if (rect.y < 0) {
      //   rect.translate(0, -rect.y);
      // }
      // if (rect.x + rect.width > image.getWidth()) {
      //   rect.width = image.getWidth() - rect.x;
      // }
      // if (rect.y + rect.height > image.getHeight()) {
      //   rect.height = image.getHeight() - rect.y;
      // }
      // glassPane.setImage(image.getSubimage(rect.x, rect.y, rect.width, rect.height));
      // // rect.x = Math.max(0, rect.x); // rect.x < 0 ? 0 : rect.x;
      // // rect.y = Math.max(0, rect.y); // rect.y < 0 ? 0 : rect.y;
      // // image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
      // // glassPane.setImage(image);
    }
    Point glassPt = SwingUtilities.convertPoint(this, tabPt, glassPane);
    glassPane.setPoint(glassPt);
    glassPane.setVisible(true);
  }

  protected Rectangle getTabAreaBounds() {
    Rectangle tabbedRect = getBounds();
    // XXX: Rectangle compRect = getSelectedComponent().getBounds();
    // pointed out by daryl. NullPointerException: i.e. addTab("Tab", null)
    // Component comp = getSelectedComponent();
    // int idx = 0;
    // while (Objects.isNull(comp) && idx < getTabCount()) {
    //   comp = getComponentAt(idx++);
    // }

    Rectangle compRect = Optional.ofNullable(getSelectedComponent())
        .map(Component::getBounds)
        .orElseGet(Rectangle::new);
    // // TEST:
    // Rectangle compRect = Optional.ofNullable(getSelectedComponent())
    //   .map(Component::getBounds)
    //   .orElseGet(() -> IntStream.range(0, getTabCount())
    //     .mapToObj(this::getComponentAt)
    //     .map(Component::getBounds)
    //     .findFirst()
    //     .orElseGet(Rectangle::new));
    int tabPlacement = getTabPlacement();
    if (isTopBottomTabPlacement(tabPlacement)) {
      tabbedRect.height = tabbedRect.height - compRect.height;
      if (tabPlacement == BOTTOM) {
        tabbedRect.y += compRect.y + compRect.height;
      }
    } else {
      tabbedRect.width = tabbedRect.width - compRect.width;
      if (tabPlacement == RIGHT) {
        tabbedRect.x += compRect.x + compRect.width;
      }
    }
    // if (tabPlacement == TOP) {
    //   tabbedRect.height = tabbedRect.height - compRect.height;
    // } else if (tabPlacement == BOTTOM) {
    //   tabbedRect.y = tabbedRect.y + compRect.y + compRect.height;
    //   tabbedRect.height = tabbedRect.height - compRect.height;
    // } else if (tabPlacement == LEFT) {
    //   tabbedRect.width = tabbedRect.width - compRect.width;
    // } else if (tabPlacement == RIGHT) {
    //   tabbedRect.x = tabbedRect.x + compRect.x + compRect.width;
    //   tabbedRect.width = tabbedRect.width - compRect.width;
    // }
    tabbedRect.grow(2, 2);
    return tabbedRect;
  }

  public static boolean isTopBottomTabPlacement(int tabPlacement) {
    return tabPlacement == SwingConstants.TOP || tabPlacement == SwingConstants.BOTTOM;
  }
}

class TabTransferable implements Transferable {
  private static final String NAME = "test";
  private static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
  private final Component tabbedPane;

  protected TabTransferable(Component tabbedPane) {
    this.tabbedPane = tabbedPane;
  }

  @Override public Object getTransferData(DataFlavor flavor) {
    return tabbedPane;
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {FLAVOR};
  }

  @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
    return NAME.equals(flavor.getHumanPresentableName());
  }
}

class TabDragSourceListener implements DragSourceListener {
  @Override public void dragEnter(DragSourceDragEvent e) {
    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
  }

  @Override public void dragExit(DragSourceEvent e) {
    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    // glassPane.setTargetRect(0, 0, 0, 0);
    // glassPane.setPoint(new Point(-1000, -1000));
    // glassPane.repaint();
  }

  @Override public void dragOver(DragSourceDragEvent e) {
    // Point glassPt = e.getLocation();
    // JComponent glassPane = (JComponent) e.getDragSourceContext();
    // SwingUtilities.convertPointFromScreen(glassPt, glassPane);
    // int targetIdx = getTargetTabIndex(glassPt);
    // if (getTabAreaBounds().contains(glassPt) && targetIdx >= 0
    //     && targetIdx != dragTabIndex && targetIdx != dragTabIndex + 1) {
    //   e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    //   glassPane.setCursor(DragSource.DefaultMoveDrop);
    // } else {
    //   e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    //   glassPane.setCursor(DragSource.DefaultMoveNoDrop);
    // }
  }

  @Override public void dragDropEnd(DragSourceDropEvent e) {
    // dragTabIndex = -1;
    // glassPane.setVisible(false);
  }

  @Override public void dropActionChanged(DragSourceDragEvent e) {
    /* not needed */
  }
}

class TabDragGestureListener implements DragGestureListener {
  private final DragSourceListener handler = new TabDragSourceListener();

  @Override public void dragGestureRecognized(DragGestureEvent e) {
    Optional.ofNullable(e.getComponent())
        .filter(DnDTabbedPane.class::isInstance)
        .map(DnDTabbedPane.class::cast)
        .filter(t -> t.getTabCount() > 1)
        .ifPresent(t -> startDrag(e, t));
  }

  private void startDrag(DragGestureEvent e, DnDTabbedPane tabs) {
    Point tabPt = e.getDragOrigin();
    int idx = tabs.indexAtLocation(tabPt.x, tabPt.y);
    int selIdx = tabs.getSelectedIndex();
    // When a tab runs rotation occurs, a tab that is not the target is dragged.
    // pointed out by Arjen
    boolean isTabRunsRotated = !(tabs.getUI() instanceof MetalTabbedPaneUI)
        && tabs.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT
        && idx != selIdx;
    tabs.dragTabIndex = isTabRunsRotated ? selIdx : idx;
    if (tabs.dragTabIndex >= 0 && tabs.isEnabledAt(tabs.dragTabIndex)) {
      tabs.initGlassPane(tabPt);
      try {
        e.startDrag(DragSource.DefaultMoveDrop, new TabTransferable(tabs), handler);
      } catch (InvalidDnDOperationException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }
}

class TabDropTargetListener implements DropTargetListener {
  private static final Point HIDDEN_POINT = new Point(0, -1000);

  private static Optional<GhostGlassPane> getGhostGlassPane(Component c) {
    return Optional.ofNullable(c).filter(GhostGlassPane.class::isInstance).map(GhostGlassPane.class::cast);
  }

  @Override public void dragEnter(DropTargetDragEvent e) {
    getGhostGlassPane(e.getDropTargetContext().getComponent()).ifPresent(glassPane -> {
      // DnDTabbedPane tabbedPane = glassPane.tabbedPane;
      Transferable t = e.getTransferable();
      DataFlavor[] f = e.getCurrentDataFlavors();
      if (t.isDataFlavorSupported(f[0])) { // && tabbedPane.dragTabIndex >= 0) {
        e.acceptDrag(e.getDropAction());
      } else {
        e.rejectDrag();
      }
    });
  }

  @Override public void dragExit(DropTargetEvent e) {
    // Component c = e.getDropTargetContext().getComponent();
    // System.out.println("DropTargetListener#dragExit: " + c.getName());
    getGhostGlassPane(e.getDropTargetContext().getComponent()).ifPresent(glassPane -> {
      // XXX: glassPane.setVisible(false);
      glassPane.setPoint(HIDDEN_POINT);
      glassPane.setTargetRect(0, 0, 0, 0);
      glassPane.repaint();
    });
  }

  @Override public void dropActionChanged(DropTargetDragEvent e) {
    /* not needed */
  }

  @Override public void dragOver(DropTargetDragEvent e) {
    Component c = e.getDropTargetContext().getComponent();
    getGhostGlassPane(c).ifPresent(glassPane -> {
      Point glassPt = e.getLocation();

      DnDTabbedPane tabbedPane = glassPane.tabbedPane;
      tabbedPane.initTargetLine(tabbedPane.getTargetTabIndex(glassPt));
      tabbedPane.autoScrollTest(glassPt);

      glassPane.setPoint(glassPt);
      glassPane.repaint();
    });
  }

  @Override public void drop(DropTargetDropEvent e) {
    Component c = e.getDropTargetContext().getComponent();
    getGhostGlassPane(c).ifPresent(glassPane -> {
      DnDTabbedPane tabbedPane = glassPane.tabbedPane;
      Transferable t = e.getTransferable();
      DataFlavor[] f = t.getTransferDataFlavors();
      int prev = tabbedPane.dragTabIndex;
      int next = tabbedPane.getTargetTabIndex(e.getLocation());
      if (t.isDataFlavorSupported(f[0]) && prev != next) {
        tabbedPane.convertTab(prev, next);
        e.dropComplete(true);
      } else {
        e.dropComplete(false);
      }
      glassPane.setVisible(false);
      // tabbedPane.dragTabIndex = -1;
    });
  }
}

class GhostGlassPane extends JComponent {
  private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
  public final DnDTabbedPane tabbedPane;
  private final Rectangle lineRect = new Rectangle();
  private final Color lineColor = new Color(0, 100, 255);
  private final Point location = new Point();
  private transient BufferedImage draggingGhost;

  protected GhostGlassPane(DnDTabbedPane tabbedPane) {
    super();
    this.tabbedPane = tabbedPane;
    setOpaque(false);
    // [JDK-6700748] Cursor flickering during D&D when using CellRendererPane with validation - Java Bug System
    // https://bugs.openjdk.java.net/browse/JDK-6700748
    // setCursor(null);
  }

  public void setTargetRect(int x, int y, int width, int height) {
    lineRect.setBounds(x, y, width, height);
  }

  public void setImage(BufferedImage draggingImage) {
    this.draggingGhost = draggingImage;
  }

  public void setPoint(Point pt) {
    this.location.setLocation(pt);
  }

  @Override public void setVisible(boolean v) {
    super.setVisible(v);
    if (!v) {
      setTargetRect(0, 0, 0, 0);
      setImage(null);
    }
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setComposite(ALPHA);
    if (tabbedPane.isPaintScrollArea && tabbedPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
      g2.setPaint(Color.RED);
      g2.fill(tabbedPane.rectBackward);
      g2.fill(tabbedPane.rectForward);
    }
    if (draggingGhost != null) {
      double xx = location.getX() - draggingGhost.getWidth(this) / 2d;
      double yy = location.getY() - draggingGhost.getHeight(this) / 2d;
      g2.drawImage(draggingGhost, (int) xx, (int) yy, this);
    }
    g2.setPaint(lineColor);
    g2.fill(lineRect);
    g2.dispose();
  }
}
