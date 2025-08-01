// Kernel.java
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Kernel: Komut kaydı/düzeneği ve pencere yöneticisi + mini pencere sınıfı.
 */
public class Kernel {
    public final CommandRegistry commandRegistry = new CommandRegistry();
    public final WindowManager windowManager = new WindowManager();

    public Kernel() {
        registerDefaultCommands();
    }

    private void registerDefaultCommands() {
        commandRegistry.register("help", args -> {
            return "Kullanılabilir komutlar: help, echo, time, clear, sum";
        });

        commandRegistry.register("echo", args -> {
            return String.join(" ", args);
        });

        commandRegistry.register("time", args -> {
            return "Şu an: " + ZonedDateTime.now();
        });

        commandRegistry.register("sum", args -> {
            int sum = 0;
            for (String a : args) {
                try {
                    sum += Integer.parseInt(a);
                } catch (NumberFormatException e) {
                    return "Geçersiz sayı: " + a;
                }
            }
            return "Toplam: " + sum;
        });
    }

    // Komut arayüzü
    public interface Command {
        String execute(String[] args) throws Exception;
    }

    public static class CommandRegistry {
        private final Map<String, Command> commands = new HashMap<>();

        public void register(String name, Command cmd) {
            commands.put(name.toLowerCase(), cmd);
        }

        public Command get(String name) {
            return commands.get(name.toLowerCase());
        }

        public java.util.Set<String> list() {
            return commands.keySet();
        }
    }

    /**
     * Pencere yöneticisi: MiniWindow'ları kaydeder.
     */
    public static class WindowManager {
        private final Map<String, MiniWindow> openWindows = new HashMap<>();

        public void add(String key, MiniWindow w) {
            openWindows.put(key.toLowerCase(), w);
        }

        public MiniWindow get(String key) {
            return openWindows.get(key.toLowerCase());
        }

        public boolean contains(String key) {
            return openWindows.containsKey(key.toLowerCase());
        }

        public void remove(String key) {
            openWindows.remove(key.toLowerCase());
        }

        public java.util.Collection<MiniWindow> all() {
            return openWindows.values();
        }

        public int count() {
            return openWindows.size();
        }
    }

    /**
     * Küçük taşınabilir pencere sınıfı.
     */
    public static class MiniWindow extends JPanel {
        private final JPanel titleBar = new JPanel();
        private final JLabel titleLabel = new JLabel();
        private final JButton closeBtn = new JButton("x");
        private final JPanel contentPane = new JPanel();
        private Point dragOffset;
        private final int width, height;
        private final WindowManager ownerManager;
        private final String key;

        public MiniWindow(String title, int w, int h, JFrame desktop, WindowManager manager, String key) {
            this.width = w;
            this.height = h;
            this.ownerManager = manager;
            this.key = key.toLowerCase();
            setLayout(new BorderLayout());
            setBorder(new LineBorder(Color.GRAY, 2));
            setSize(w, h);
            setOpaque(true);
            setBackground(new Color(50, 50, 50));
            titleBar.setLayout(new BorderLayout());
            titleBar.setPreferredSize(new Dimension(w, 25));
            titleBar.setBackground(new Color(70, 70, 70));
            titleLabel.setText(" " + title);
            titleLabel.setForeground(Color.WHITE);
            titleBar.add(titleLabel, BorderLayout.WEST);

            closeBtn.setBorder(null);
            closeBtn.setOpaque(false);
            closeBtn.setContentAreaFilled(false);
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setFocusable(false);
            closeBtn.setPreferredSize(new Dimension(45, 25));
            titleBar.add(closeBtn, BorderLayout.EAST);
            add(titleBar, BorderLayout.NORTH);

            contentPane.setLayout(new BorderLayout());
            add(contentPane, BorderLayout.CENTER);

            // Taşıma
            titleBar.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    dragOffset = e.getPoint();
                }
            });
            titleBar.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    Point loc = getLocation();
                    loc.translate(e.getX() - dragOffset.x, e.getY() - dragOffset.y);
                    setLocation(loc);
                }
            });

            // Kapat
            closeBtn.addActionListener(e -> close(desktop));

            // Öne çıkar
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    toFront();
                }
            });
        }

        public void setContent(LayoutManager lm) {
            contentPane.setLayout(lm);
            removeAll();
            add(titleBar, BorderLayout.NORTH);
            add(contentPane, BorderLayout.CENTER);
        }

        public void addContent(Component comp, Object constraints) {
            contentPane.add(comp, constraints);
        }

        public void showOn(JFrame desktop, int offsetIndex) {
            desktop.getLayeredPane().add(this, JLayeredPane.PALETTE_LAYER);
            setLocation(50 + offsetIndex * 30, 70 + offsetIndex * 30);
            setVisible(true);
            setSize(width, height);
            ownerManager.add(key, this);
        }

        public void close(JFrame desktop) {
            int result = JOptionPane.showConfirmDialog(this, "Pencereyi kapatmak istediğine emin misin?", "Kapatılıyor", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) return;
            Container parent = getParent();
            if (parent != null) {
                parent.remove(this);
                parent.repaint();
            }
            ownerManager.remove(key);
        }

        public void toFront() {
            Container p = getParent();
            if (p != null) {
                p.setComponentZOrder(this, 0);
                repaint();
            }
        }
    }
}
