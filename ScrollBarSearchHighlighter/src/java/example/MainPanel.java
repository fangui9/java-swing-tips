// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private static final HighlightPainter HIGHLIGHT = new DefaultHighlightPainter(Color.YELLOW);
  private static final String PATTERN = "Swing";
  private static final String TEXT = String.join("\n",
      "Trail: Creating a GUI with JFC/Swing",
      "Lesson: Learning Swing by Example",
      "This lesson explains the concepts you need to",
      " use Swing components in building a user interface.",
      " First we examine the simplest Swing application you can write.",
      " Then we present several progressively complicated examples of creating",
      " user interfaces using components in the javax.swing package.",
      " We cover several Swing components, such as buttons, labels, and text areas.",
      " The handling of events is also discussed,",
      " as are layout management and accessibility.",
      " This lesson ends with a set of questions and exercises",
      " so you can test yourself on what you've learned.",
      "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html");

  private MainPanel() {
    super(new BorderLayout());

    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setText(TEXT + TEXT + TEXT);

    JScrollBar scrollbar = new JScrollBar(Adjustable.VERTICAL);
    // JScrollBar scrollbar = new JScrollBar(Adjustable.VERTICAL) {
    //   @Override public Dimension getPreferredSize() {
    //     Dimension d = super.getPreferredSize();
    //     d.width += 4; // getInsets().left;
    //     return d;
    //   }
    // };
    if (scrollbar.getUI() instanceof WindowsScrollBarUI) {
      scrollbar.setUI(new WindowsHighlightScrollBarUI(textArea));
    } else {
      scrollbar.setUI(new MetalHighlightScrollBarUI(textArea));
    }
    scrollbar.setUnitIncrement(10);

    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setVerticalScrollBar(scrollbar);

    JLabel label = new JLabel(new HighlightIcon(textArea, scrollbar));
    // label.setBorder(BorderFactory.createLineBorder(Color.RED));
    scroll.setRowHeaderView(label);

    // // [JDK-6826074] JScrollPane does not revalidate the component hierarchy after scrolling
    // // https://bugs.openjdk.java.net/browse/JDK-6826074
    // // Affected Versions: 6u12, 6u16, 7
    // // Fixed Versions: 7 (b134)
    // JViewport vp = new JViewport() {
    //   @Override public void setViewPosition(Point p) {
    //     super.setViewPosition(p);
    //     revalidate();
    //   }
    // };
    // vp.setView(new JLabel(new HighlightIcon(textArea, scrollbar)));
    // scroll.setRowHeader(vp);

    JCheckBox check = new JCheckBox("LineWrap");
    check.addActionListener(e -> textArea.setLineWrap(((JCheckBox) e.getSource()).isSelected()));

    JButton highlight = new JButton("highlight");
    highlight.addActionListener(e -> {
      setHighlight(textArea, PATTERN);
      repaint();
    });

    JButton clear = new JButton("clear");
    clear.addActionListener(e -> {
      textArea.getHighlighter().removeAllHighlights();
      scroll.repaint();
    });

    Box box = Box.createHorizontalBox();
    box.add(check);
    box.add(Box.createHorizontalGlue());
    box.add(highlight);
    box.add(Box.createHorizontalStrut(2));
    box.add(clear);

    add(box, BorderLayout.SOUTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void setHighlight(JTextComponent jtc, String pattern) {
    Highlighter highlighter = jtc.getHighlighter();
    highlighter.removeAllHighlights();
    Document doc = jtc.getDocument();
    try {
      String text = doc.getText(0, doc.getLength());
      Matcher matcher = Pattern.compile(pattern).matcher(text);
      int pos = 0;
      while (matcher.find(pos) && !matcher.group().isEmpty()) {
        int start = matcher.start();
        int end = matcher.end();
        highlighter.addHighlight(start, end, HIGHLIGHT);
        pos = end;
      }
    } catch (BadLocationException | PatternSyntaxException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(jtc);
    }
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

class HighlightIcon implements Icon {
  private static final Color THUMB_COLOR = new Color(0, 0, 255, 50);
  private final Rectangle thumbRect = new Rectangle();
  private final JTextComponent textArea;
  private final JScrollBar scrollbar;

  protected HighlightIcon(JTextComponent textArea, JScrollBar scrollbar) {
    this.textArea = textArea;
    this.scrollbar = scrollbar;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    // Rectangle rect = textArea.getBounds();
    // Dimension sbSize = scrollbar.getSize();
    // Insets sbInsets = scrollbar.getInsets();
    // double sy = (sbSize.height - sbInsets.top - sbInsets.bottom) / rect.getHeight();
    int top = scrollbar.getInsets().top;
    BoundedRangeModel range = scrollbar.getModel();
    double sy = range.getExtent() / (double) (range.getMaximum() - range.getMinimum());
    AffineTransform at = AffineTransform.getScaleInstance(1d, sy);
    Highlighter highlighter = textArea.getHighlighter();

    // paint Highlight
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.RED);
    try {
      for (Highlighter.Highlight hh : highlighter.getHighlights()) {
        Rectangle r = textArea.modelToView(hh.getStartOffset());
        Rectangle s = at.createTransformedShape(r).getBounds();
        int h = 2; // Math.max(2, s.height - 2);
        g2.fillRect(0, top + s.y, getIconWidth(), h);
      }
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }

    // paint Thumb
    if (scrollbar.isVisible()) {
      // JViewport vport = Objects.requireNonNull(
      //     (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, textArea));
      // Rectangle thumbRect = vport.getBounds();
      thumbRect.height = range.getExtent();
      thumbRect.y = range.getValue(); // vport.getViewPosition().y;
      g2.setColor(THUMB_COLOR);
      Rectangle s = at.createTransformedShape(thumbRect).getBounds();
      g2.fillRect(0, top + s.y, getIconWidth(), s.height);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 4;
  }

  @Override public int getIconHeight() {
    int ih = scrollbar.getHeight();
    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, textArea);
    if (c instanceof JViewport) {
      ih = c.getHeight();
    }
    return ih;
  }
}

class WindowsHighlightScrollBarUI extends WindowsScrollBarUI {
  private final JTextComponent textArea;

  protected WindowsHighlightScrollBarUI(JTextComponent textArea) {
    super();
    this.textArea = textArea;
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    super.paintTrack(g, c, trackBounds);

    Rectangle rect = textArea.getBounds();
    double sy = trackBounds.getHeight() / rect.getHeight();
    AffineTransform at = AffineTransform.getScaleInstance(1d, sy);
    Highlighter highlighter = textArea.getHighlighter();
    g.setColor(Color.YELLOW);
    try {
      for (Highlighter.Highlight hh : highlighter.getHighlights()) {
        Rectangle r = textArea.modelToView(hh.getStartOffset());
        Rectangle s = at.createTransformedShape(r).getBounds();
        int h = 2; // Math.max(2, s.height - 2);
        g.fillRect(trackBounds.x, trackBounds.y + s.y, trackBounds.width, h);
      }
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }
}

class MetalHighlightScrollBarUI extends MetalScrollBarUI {
  private final JTextComponent textArea;

  protected MetalHighlightScrollBarUI(JTextComponent textArea) {
    super();
    this.textArea = textArea;
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    super.paintTrack(g, c, trackBounds);

    Rectangle rect = textArea.getBounds();
    double sy = trackBounds.getHeight() / rect.getHeight();
    AffineTransform at = AffineTransform.getScaleInstance(1d, sy);
    Highlighter highlighter = textArea.getHighlighter();
    g.setColor(Color.YELLOW);
    try {
      for (Highlighter.Highlight hh : highlighter.getHighlights()) {
        Rectangle r = textArea.modelToView(hh.getStartOffset());
        Rectangle s = at.createTransformedShape(r).getBounds();
        int h = 2; // Math.max(2, s.height - 2);
        g.fillRect(trackBounds.x, trackBounds.y + s.y, trackBounds.width, h);
      }
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }
}
