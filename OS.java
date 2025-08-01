// OS.java
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OS {
    private static JFrame desktop;
    private static Kernel kernel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            kernel = new Kernel();
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        desktop = new JFrame("JavaOS Desktop");
        desktop.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        desktop.setSize(1000, 650);
        desktop.setLayout(null);
        desktop.getContentPane().setBackground(new Color(30, 30, 30));

        // Menü çubuğu
        JMenuBar menuBar = new JMenuBar();
        JMenu startMenu = new JMenu("Start");

        JMenuItem terminalItem   = new JMenuItem("Terminal");
        JMenuItem notepadItem    = new JMenuItem("Not Defteri");
        JMenuItem paintItem      = new JMenuItem("Paint");
        JMenuItem calcItem       = new JMenuItem("Calculator");
        JMenuItem internetItem   = new JMenuItem("Internet");
        JMenuItem simpleBrowserItem = new JMenuItem("Simple Browser");
        JMenuItem githubItem = new JMenuItem("GitHub Info");
        JMenuItem aboutItem      = new JMenuItem("About JavaOS");

        startMenu.add(terminalItem);
        startMenu.add(notepadItem);
        startMenu.add(paintItem);
        startMenu.add(calcItem);
        startMenu.add(internetItem);
        startMenu.add(simpleBrowserItem);
        startMenu.add(githubItem);
        startMenu.addSeparator();
        startMenu.add(aboutItem);

        menuBar.add(startMenu);
        desktop.setJMenuBar(menuBar);

        terminalItem.addActionListener(e -> openTerminal());
        notepadItem.addActionListener(e -> openNotepad());
        paintItem.addActionListener(e -> openPaint());
        calcItem.addActionListener(e -> openCalculator());
        internetItem.addActionListener(e -> openInternet());
        simpleBrowserItem.addActionListener(e -> openSimpleBrowser());
        githubItem.addActionListener(e -> openGitHubInfo());
        aboutItem.addActionListener(e -> openAbout());

        JLabel label = new JLabel("<html><span style='color:#DDD;'>JavaOS v0.5 — Başlat menüsünden uygulama aç.</span></html>");
        label.setBounds(10, 35, 600, 25);
        desktop.add(label);

        desktop.setVisible(true);
    }

    // ==================== Uygulamalar ====================

    private static void openTerminal() {
        if (kernel.windowManager.contains("terminal")) {
            kernel.windowManager.get("terminal").toFront();
            return;
        }
        Kernel.MiniWindow term = new Kernel.MiniWindow("Terminal", 450, 300, desktop, kernel.windowManager, "terminal");

        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setBackground(Color.BLACK);
        output.setForeground(Color.GREEN);
        output.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(output);
        JTextField input = new JTextField();

        input.addActionListener(e -> {
            String line = input.getText().trim();
            input.setText("");
            output.append("JavaOS> " + line + "\n");
            if (line.isEmpty()) return;
            String[] parts = line.split("\\s+");
            String cmdName = parts[0];
            String[] cmdArgs = new String[parts.length - 1];
            System.arraycopy(parts, 1, cmdArgs, 0, cmdArgs.length);

            Kernel.Command cmd = kernel.commandRegistry.get(cmdName);
            if (cmd != null) {
                try {
                    String res = cmd.execute(cmdArgs);
                    output.append(res + "\n");
                } catch (Exception ex) {
                    output.append("Komut hatası: " + ex.getMessage() + "\n");
                }
            } else {
                output.append("Bilinmeyen komut: " + cmdName + "\n");
            }
            output.setCaretPosition(output.getDocument().getLength());
        });

        term.setContent(new BorderLayout());
        term.addContent(scroll, BorderLayout.CENTER);
        term.addContent(input, BorderLayout.SOUTH);
        term.showOn(desktop, kernel.windowManager.count());
    }

    private static void openNotepad() {
        if (kernel.windowManager.contains("notepad")) {
            kernel.windowManager.get("notepad").toFront();
            return;
        }
        Kernel.MiniWindow note = new Kernel.MiniWindow("Not Defteri", 360, 340, desktop, kernel.windowManager, "notepad");
        JTextArea area = new JTextArea();
        JScrollPane scroll = new JScrollPane(area);
        note.setContent(new BorderLayout());
        note.addContent(scroll, BorderLayout.CENTER);
        note.showOn(desktop, kernel.windowManager.count());
    }

    private static void openPaint() {
        if (kernel.windowManager.contains("paint")) {
            kernel.windowManager.get("paint").toFront();
            return;
        }
        Kernel.MiniWindow paint = new Kernel.MiniWindow("Paint", 400, 300, desktop, kernel.windowManager, "paint");

        JPanel canvas = new JPanel() {
            Image img = new BufferedImage(400, 260, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = (Graphics2D) img.getGraphics();
            {
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, 400, 260);
                g2.setColor(Color.BLACK);
            }
            Point last = null;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        last = e.getPoint();
                    }
                    public void mouseReleased(java.awt.event.MouseEvent e) {
                        last = null;
                    }
                });
                addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    public void mouseDragged(java.awt.event.MouseEvent e) {
                        if (last != null) {
                            g2.drawLine(last.x, last.y, e.getX(), e.getY());
                            last = e.getPoint();
                            repaint();
                        }
                    }
                });
            }
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img, 0, 0, null);
            }
        };
        paint.setContent(new BorderLayout());
        paint.addContent(canvas, BorderLayout.CENTER);
        paint.showOn(desktop, kernel.windowManager.count());
    }

    private static void openCalculator() {
        if (kernel.windowManager.contains("calc")) {
            kernel.windowManager.get("calc").toFront();
            return;
        }
        Kernel.MiniWindow calc = new Kernel.MiniWindow("Calculator", 250, 250, desktop, kernel.windowManager, "calc");
        JTextField display = new JTextField();
        display.setEditable(false);
        JPanel buttons = new JPanel(new GridLayout(4, 4, 5, 5));
        String[] labels = {"7","8","9","/","4","5","6","*","1","2","3","-","0",".","=","+"};
        for (String lab : labels) {
            JButton b = new JButton(lab);
            b.addActionListener(e -> {
                String t = display.getText();
                if (lab.equals("=")) {
                    try {
                        Object val = new javax.script.ScriptEngineManager().getEngineByName("JavaScript").eval(t);
                        display.setText(val.toString());
                    } catch (Exception ex) {
                        display.setText("ERR");
                    }
                } else {
                    display.setText(t + lab);
                }
            });
            buttons.add(b);
        }
        calc.setContent(new BorderLayout());
        calc.addContent(display, BorderLayout.NORTH);
        calc.addContent(buttons, BorderLayout.CENTER);
        calc.showOn(desktop, kernel.windowManager.count());
    }

    private static void openInternet() {
        if (kernel.windowManager.contains("internet")) {
            kernel.windowManager.get("internet").toFront();
            return;
        }
        Kernel.MiniWindow browser = new Kernel.MiniWindow("Internet", 700, 500, desktop, kernel.windowManager, "internet");

        JPanel top = new JPanel(new BorderLayout(5,5));
        JTextField urlField = new JTextField("https://example.com");
        JButton go = new JButton("Git");
        top.add(urlField, BorderLayout.CENTER);
        top.add(go, BorderLayout.EAST);

        JEditorPane view = new JEditorPane();
        view.setEditable(false);
        view.setContentType("text/html");
        JScrollPane scroll = new JScrollPane(view);

        Runnable loadUrl = () -> {
            try {
                String urlText = urlField.getText().trim();
                if (!urlText.startsWith("http")) {
                    urlText = "http://" + urlText;
                }
                view.setPage(urlText);
            } catch (Exception ex) {
                view.setText("<html><body><h3>Yüklenemedi:</h3><pre>" + ex.getMessage() + "</pre></body></html>");
            }
        };

        go.addActionListener(e -> new Thread(loadUrl).start());
        urlField.addActionListener(e -> new Thread(loadUrl).start());

        new Thread(loadUrl).start();

        JPanel container = new JPanel(new BorderLayout());
        container.add(top, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);

        browser.setContent(new BorderLayout());
        browser.addContent(container, BorderLayout.CENTER);
        browser.showOn(desktop, kernel.windowManager.count());
    }

    private static void openSimpleBrowser() {
        if (kernel.windowManager.contains("simplebrowser")) {
            kernel.windowManager.get("simplebrowser").toFront();
            return;
        }
        Kernel.MiniWindow browser = new Kernel.MiniWindow("Simple Browser", 650, 450, desktop, kernel.windowManager, "simplebrowser");

        JPanel top = new JPanel(new BorderLayout(5,5));
        JTextField urlField = new JTextField("example.com");
        JButton go = new JButton("Git");
        top.add(urlField, BorderLayout.CENTER);
        top.add(go, BorderLayout.EAST);

        JTextArea display = new JTextArea();
        display.setEditable(false);
        display.setLineWrap(true);
        display.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(display);

        go.addActionListener(e -> fetchAndRender(urlField.getText().trim(), display));
        urlField.addActionListener(e -> fetchAndRender(urlField.getText().trim(), display));

        fetchAndRender(urlField.getText().trim(), display);

        JPanel container = new JPanel(new BorderLayout());
        container.add(top, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);

        browser.setContent(new BorderLayout());
        browser.addContent(container, BorderLayout.CENTER);
        browser.showOn(desktop, kernel.windowManager.count());
    }

    private static void fetchAndRender(String rawUrl, JTextArea display) {
        display.setText("Yükleniyor: " + rawUrl + "...\n");
        new Thread(() -> {
            try {
                String urlText = rawUrl;
                if (!urlText.startsWith("http://") && !urlText.startsWith("https://")) {
                    urlText = "http://" + urlText;
                }
                HttpURLConnection con = (HttpURLConnection) new URL(urlText).openConnection();
                con.setRequestProperty("User-Agent", "JavaOS-SimpleBrowser/1.0");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                int code = con.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                in.close();
                String html = sb.toString();

                String textOnly = stripHtmlTags(html);
                String links = extractLinks(html);

                SwingUtilities.invokeLater(() -> {
                    display.setText("HTTP " + code + "\n\n");
                    display.append(textOnly.length() > 5000 ? textOnly.substring(0, 5000) + "...(truncated)\n\n" : textOnly + "\n\n");
                    if (!links.isEmpty()) {
                        display.append("Bulunan linkler:\n" + links);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> display.setText("Hata: " + ex.getMessage()));
            }
        }).start();
    }

    private static String stripHtmlTags(String html) {
        html = html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", "");
        html = html.replaceAll("<[^>]+>", "");
        html = html.replaceAll("&nbsp;", " ");
        html = html.replaceAll("&amp;", "&");
        html = html.replaceAll("&lt;", "<");
        html = html.replaceAll("&gt;", ">");
        return html.trim();
    }

    private static String extractLinks(String html) {
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile("href\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        while (m.find()) {
            sb.append(m.group(1)).append("\n");
        }
        return sb.toString();
    }

    private static void openGitHubInfo() {
        if (kernel.windowManager.contains("github")) {
            kernel.windowManager.get("github").toFront();
            return;
        }
        Kernel.MiniWindow win = new Kernel.MiniWindow("GitHub Kullanıcı", 500, 300, desktop, kernel.windowManager, "github");

        JPanel top = new JPanel(new BorderLayout(5,5));
        JTextField userField = new JTextField("octocat");
        JButton fetch = new JButton("Getir");
        top.add(new JLabel("GitHub kullanıcı:"), BorderLayout.WEST);
        top.add(userField, BorderLayout.CENTER);
        top.add(fetch, BorderLayout.EAST);

        JTextArea output = new JTextArea();
        output.setEditable(false);
        JScrollPane scroll = new JScrollPane(output);

        fetch.addActionListener(e -> {
            String user = userField.getText().trim();
            output.setText("Yükleniyor...\n");
            new Thread(() -> {
                try {
                    String apiUrl = "https://api.github.com/users/" + user;
                    HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
                    con.setRequestProperty("User-Agent", "JavaOS-APIClient");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    int code = con.getResponseCode();
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) sb.append(line);
                    in.close();
                    String json = sb.toString();

                    String login = findJsonField(json, "login");
                    String name = findJsonField(json, "name");
                    String repos = findJsonField(json, "public_repos");
                    String followers = findJsonField(json, "followers");

                    SwingUtilities.invokeLater(() -> {
                        output.setText("");
                        output.append("Login: " + login + "\n");
                        output.append("Name: " + name + "\n");
                        output.append("Public Repos: " + repos + "\n");
                        output.append("Followers: " + followers + "\n");
                        output.append("Raw HTTP Code: " + code + "\n");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> output.setText("Hata: " + ex.getMessage()));
                }
            }).start();
        });

        JPanel container = new JPanel(new BorderLayout());
        container.add(top, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);

        win.setContent(new BorderLayout());
        win.addContent(container, BorderLayout.CENTER);
        win.showOn(desktop, kernel.windowManager.count());
    }

    private static String findJsonField(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*\"?([^\"," + "}]*)");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        return "(yok)";
    }

    private static void openAbout() {
        JOptionPane.showMessageDialog(desktop,
                "JavaOS v0.5\n" +
                "Basit Java ile masaüstü ve pencere sistemi\n" +
                "Yapan: Deniz Asaf\n" +
                "Tarih: 2025\n" +
                "https://github.com/denizasaf",
                "Hakkında",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
