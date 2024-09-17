/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.iradraconis.jl.wallpapers;

/**
 *
 * @author Maximilian Steinert
 */
import com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import java.util.Comparator;
import java.nio.file.attribute.BasicFileAttributes;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import java.util.List;

public class JLWallpapers {

    private static String file_path_wallpaper;
    private static String file_path_installation;
    private static JLabel statusLabel;
    private static JProgressBar progressBar;

    public static void main(String[] args) {
        try {
            FlatMonokaiProIJTheme.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("jL-Wallpapers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        JButton button1 = new JButton("Wallpaper Ordner auswählen");
        button1.setAlignmentX(Component.CENTER_ALIGNMENT);
        button1.addActionListener(e -> getWallpaperFolder());
        frame.add(Box.createVerticalStrut(15));  
        frame.add(button1);
        frame.add(Box.createVerticalStrut(15));  

        JButton button2 = new JButton("j-lawyer Client Ordner auswählen");
        button2.setAlignmentX(Component.CENTER_ALIGNMENT);
        button2.addActionListener(e -> getInstallationFolderPath());
        frame.add(button2);
        frame.add(Box.createVerticalStrut(10));  

        JButton button3 = new JButton("Start");
        button3.setAlignmentX(Component.CENTER_ALIGNMENT);
        button3.addActionListener(e -> doYourWork());
        frame.add(button3);
        frame.add(Box.createVerticalStrut(10));  

        statusLabel = new JLabel("             ");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        frame.add(statusLabel);
        frame.add(Box.createVerticalStrut(10));  

        progressBar = new JProgressBar(0, 100); 
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        frame.add(progressBar);

        frame.pack();
        frame.setSize(280, 230); 
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        getOs();
    }

    private static void doYourWork() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String sourceZip = file_path_installation + File.separator + "j-lawyer-client.jar";
                String tempDir = new File(file_path_installation).getAbsolutePath() + File.separator + "temp";
                String pictFolder = tempDir + File.separator + "themes/default/backgroundsrandom";
                String targetZipFolder = tempDir;
                String zipFile = "j-lawyer-client";

                setStatus("Entpacke die Daten zum " + targetZipFolder + " Ordner...");
                progressBar.setValue(10); // 10% Fortschritt

                // Entpacken der ZIP-Datei
                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceZip))) {
                    ZipEntry zipEntry = zis.getNextEntry();
                    while (zipEntry != null) {
                        File destFile = new File(targetZipFolder, zipEntry.getName());
                        if (zipEntry.isDirectory()) {
                            if (!destFile.exists()) {
                                destFile.mkdirs();
                            }
                        } else {
                            new File(destFile.getParent()).mkdirs();
                            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                        zipEntry = zis.getNextEntry();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    setStatus("Datei nicht gefunden: " + e.getMessage());
                    return null;
                }

                // Löschen der alten Wallpaper
                setStatus("Lösche alte Wallpaper...");
                progressBar.setValue(20); // 20% Fortschritt
                try {
                    Files.walk(Paths.get(pictFolder))
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                    setStatus("Fehler beim Löschen der alten Wallpaper: " + e.getMessage());
                    return null;
                }

                // Kopieren der neuen Wallpaper
                setStatus("Kopiere neue Wallpaper...");
                progressBar.setValue(40); // 40% Fortschritt
                try {
                    // Überprüfen und Erstellen des Zielverzeichnisses, falls es nicht existiert
                    if (!Files.exists(Paths.get(pictFolder))) {
                        Files.createDirectories(Paths.get(pictFolder));
                    }
                    
                    Files.walk(Paths.get(file_path_wallpaper)).filter(Files::isRegularFile).forEach(source -> {
                        Path dest = Paths.get(pictFolder, source.getFileName().toString());

                        // Umwandeln in absolute Pfade
                        source = source.toAbsolutePath();
                        dest = dest.toAbsolutePath();

                        try {
                            processAndCopyImage(source, dest);
                        } catch (IOException e) {
                            e.printStackTrace();
                            setStatus("Fehler beim Bearbeiten und Kopieren der Wallpaper: " + e.getMessage());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    setStatus("Fehler beim Durchlaufen des Wallpaper-Ordners: " + e.getMessage());
                    return null;
                }

                // Re-Komprimierung der Dateien in eine ZIP-Datei
                setStatus("Komprimiere Dateien...");
                progressBar.setValue(60); // 60% Fortschritt
                try {
                    Path sourceFolderPath = Paths.get(targetZipFolder);
                    File archiveFile = new File(zipFile + ".zip");

                    try (FileOutputStream fos = new FileOutputStream(archiveFile);
                         ZipOutputStream zos = new ZipOutputStream(fos)) {

                        Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                                Path relFile = sourceFolderPath.relativize(file);
                                ZipEntry entry = new ZipEntry(relFile.toString());
                                zos.putNextEntry(entry);

                                byte[] bytes = Files.readAllBytes(file);
                                zos.write(bytes, 0, bytes.length);
                                zos.closeEntry();

                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                                zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(dir).toString() + "/"));
                                zos.closeEntry();
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    }

                    // Umbenennen der ZIP-Datei in .jar
                    Files.move(archiveFile.toPath(), new File("j-lawyer-client.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);

                } catch (IOException e) {
                    e.printStackTrace();
                    setStatus("Fehler beim Komprimieren: " + e.getMessage());
                    return null;
                }

                // Verschieben der .jar-Datei in den Zielordner
                setStatus("Verschiebe .jar-Datei...");
                progressBar.setValue(80); // 80% Fortschritt
                try {
                    Path jarPath = Paths.get("j-lawyer-client.jar");
                    Path destinationPath = Paths.get(file_path_installation, "j-lawyer-client.jar");
                    Files.move(jarPath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                    setStatus("Datei nicht gefunden oder keine Berechtigung: " + e.getMessage());
                    return null;
                }

                // Restliche Aufräumarbeiten
                setStatus("Räume auf...");
                progressBar.setValue(90); // 90% Fortschritt
                try {
                    Files.walk(Paths.get(targetZipFolder))
                         .sorted(Comparator.reverseOrder())
                         .map(Path::toFile)
                         .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                    setStatus("Fehler beim Aufräumen: " + e.getMessage());
                    return null;
                }

                setStatus("Fertig!");
                progressBar.setValue(100); // 100% Fortschritt
                return null;
            }

            @Override
            protected void process(List<Void> chunks) {
                // Hier wird die GUI aktualisiert. Der Code wird auf dem Event Dispatch Thread ausgeführt.
                progressBar.setValue(progressBar.getValue() + 10); // Erhöht den Wert um 10
            }

            @Override
            protected void done() {
                try {
                    get();
                    setStatus("Fertig!");
                    progressBar.setValue(100);
                } catch (Exception e) {
                    e.printStackTrace();
                    setStatus("Ein Fehler ist aufgetreten: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private static BufferedImage reduceBrightness(BufferedImage original, float factor) {
        BufferedImage darkened = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics2D g2d = darkened.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver.derive(factor));
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        return darkened;
    }

    private static BufferedImage applyMediumBlur(BufferedImage image) {
        float oneFortyNinth = 1.0f / 49.0f;
        float[] blurKernel = {
            oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth,
            oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth,
            oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth,
            oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth,
            oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth,
            oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth,
            oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth,
            oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth, oneFortyNinth
        };

        Map<RenderingHints.Key, Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ConvolveOp op = new ConvolveOp(new Kernel(7, 7, blurKernel), ConvolveOp.EDGE_NO_OP, new RenderingHints(hints));
        return op.filter(image, null);
    }

    private static void processAndCopyImage(Path source, Path dest) throws IOException {
        BufferedImage original = ImageIO.read(source.toFile());
        if (original == null) {
            throw new IOException("Bild konnte nicht gelesen werden: " + source);
        }
        BufferedImage darkened = reduceBrightness(original, 0.4f);
        BufferedImage blurred = applyMediumBlur(darkened);

        // Temporäre Datei verwenden
        File tempFile = File.createTempFile("processed-", ".tmp");
        ImageIO.write(blurred, "jpg", tempFile);
        Files.copy(tempFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        tempFile.delete();
    }

    private static void getWallpaperFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = chooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file_path_wallpaper = chooser.getSelectedFile().getAbsolutePath();
            setStatus("Wallpaper Ordner: " + file_path_wallpaper);
        }
    }

    private static void getInstallationFolderPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = chooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file_path_installation = chooser.getSelectedFile().getAbsolutePath();
            setStatus("Installation Ordner: " + file_path_installation);
        }
    }

    private static void setStatus(String message) {
        statusLabel.setText(message);
    }

    // Ordner sind unterschiedlich aufgebaut, daher wird das Betriebssystem ermittelt
    private static void getOs() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.out.println("Das Betriebssystem ist Windows");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            System.out.println("Das Betriebssystem ist Unix-basiert");
        } else {
            System.out.println("Das Betriebssystem konnte nicht erkannt werden");
        }
    }


}
