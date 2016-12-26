/*
 * GUI.java
 *
 * Created on 16-Apr-2009, 17:27:19
 */

package browsermonkey;

import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import browsermonkey.utility.BrowserMonkeyLogger;
import java.awt.Point;
import java.net.URLEncoder;
import java.util.logging.*;

/**
 * GUI class, handles the GUI for the browser. Contains code to allow user to
 * open files, search and zoom and connects them to the relevant other classes.
 * @author Paul Calcraft
 */
public class GUI extends javax.swing.JFrame {
    /** Creates new form GUI */
    public GUI() {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {}

        initComponents();

        documentScrollPanel.getVerticalScrollBar().setUnitIncrement(25);
        documentScrollPanel.getHorizontalScrollBar().setUnitIncrement(25);
        
        documentPanel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                panelChanged();
            }
        });

        BrowserMonkeyLogger.addAlertHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel() == Level.INFO) {
                    statusBar.setText(record.getMessage());
                    statusBar.repaint();
                }
                else if (record.getLevel() == Level.WARNING) {
                    JOptionPane.showMessageDialog(GUI.this, record.getMessage(), "Notice", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            @Override
            public void flush() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void close() throws SecurityException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        top = new javax.swing.JPanel();
        addressLabel = new javax.swing.JLabel();
        addressField = new javax.swing.JTextField();
        goButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        zoomLabel = new javax.swing.JLabel();
        zoomOutButton = new javax.swing.JButton();
        zoomLevelLabel = new javax.swing.JLabel();
        zoomInButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        searchLabel = new javax.swing.JLabel();
        statusBar = new javax.swing.JLabel();
        documentScrollPanel = new javax.swing.JScrollPane();
        documentPanel = new browsermonkey.render.DocumentPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BrowserMonkey");
        setMinimumSize(top.getMinimumSize());

        top.setMinimumSize(top.getPreferredSize());

        addressLabel.setText("Address:");

        addressField.setText("welcome.html");
        addressField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressFieldActionPerformed(evt);
            }
        });

        goButton.setText("Go");
        goButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goButtonActionPerformed(evt);
            }
        });

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        zoomLabel.setText("Zoom:");

        zoomOutButton.setText("-");
        zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutButtonActionPerformed(evt);
            }
        });

        zoomLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        zoomLevelLabel.setText("100%");

        zoomInButton.setText("+");
        zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInButtonActionPerformed(evt);
            }
        });

        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });

        searchLabel.setText("Search:");

        javax.swing.GroupLayout topLayout = new javax.swing.GroupLayout(top);
        top.setLayout(topLayout);
        topLayout.setHorizontalGroup(
            topLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(topLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addressLabel)
                    .addComponent(zoomLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(topLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(topLayout.createSequentialGroup()
                        .addComponent(zoomOutButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zoomLevelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zoomInButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topLayout.createSequentialGroup()
                        .addComponent(addressField, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(goButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton)))
                .addContainerGap())
        );
        topLayout.setVerticalGroup(
            topLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(topLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addressLabel)
                    .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)
                    .addComponent(goButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(topLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zoomLabel)
                    .addComponent(zoomOutButton)
                    .addComponent(zoomLevelLabel)
                    .addComponent(zoomInButton)
                    .addComponent(searchLabel)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addGap(11, 11, 11))
        );

        statusBar.setText("Ready");
        statusBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.setMinimumSize(new java.awt.Dimension(0, 0));

        documentScrollPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        documentScrollPanel.setViewportView(documentPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
            .addComponent(documentScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
            .addComponent(top, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(top, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(documentScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
        String address = addressField.getText();
        if (address.startsWith("g "))
            address = "http://www.google.co.uk/m/search?q="+URLEncoder.encode(address.substring(2));
        else if (address.startsWith("t ")) {
            // Don't modify address, used for testing.
        }
        else {
            File f = new File(address);
            if (!f.exists() && !address.startsWith("http://") && !address.startsWith("file:/"))
                address = "http://" + address;
        }
        loadFile(address);
    }//GEN-LAST:event_goButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(addressField.getText()));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        String path = fc.getSelectedFile().getAbsolutePath();
        loadFile(path);
    }//GEN-LAST:event_browseButtonActionPerformed

    private void addressFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressFieldActionPerformed
        goButtonActionPerformed(null);
    }//GEN-LAST:event_addressFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        documentPanel.setSearch(searchField.getText());
}//GEN-LAST:event_searchButtonActionPerformed

    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
        zoom(1);
}//GEN-LAST:event_zoomInButtonActionPerformed

    private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
        zoom(-1);
}//GEN-LAST:event_zoomOutButtonActionPerformed

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        searchButtonActionPerformed(null);
    }//GEN-LAST:event_searchFieldActionPerformed

    //private int zoomLevel = 100;
    private int zoomLevelIndex = 5;
    private static final int[] zoomLevels = new int[] {
        50, 60, 70, 80, 90,
        100,
        125, 150, 175, 200, 250, 300
    };

    private void zoom(int levelChange) {
        int newIndex = zoomLevelIndex+levelChange;

        if (newIndex < 0 || newIndex >= zoomLevels.length)
            return;

        documentPanel.setZoomLevel(zoomLevels[newIndex]/100f);
        zoomLevelIndex = newIndex;
        zoomLevelLabel.setText(zoomLevels[newIndex]+"%");
        zoomOutButton.setEnabled(newIndex > 0);
        zoomInButton.setEnabled(newIndex < zoomLevels.length-1);
    }

    private void panelChanged() {
        addressField.setText(documentPanel.getAddress());
        addressField.setCaretPosition(addressField.getText().length());
        addressField.repaint();
        documentScrollPanel.getViewport().setViewPosition(new Point(0, 0));
        if (documentPanel.getTitle() == null)
            setTitle("BrowserMonkey");
        else
            setTitle(documentPanel.getTitle()+" - BrowserMonkey");
    }

    /**
     * Used to load a file with a given path.
     * @param path File path to load the file from
     */
    public void loadFile(String path){
        documentPanel.load(path, true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addressField;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JButton browseButton;
    private browsermonkey.render.DocumentPanel documentPanel;
    private javax.swing.JScrollPane documentScrollPanel;
    private javax.swing.JButton goButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JLabel statusBar;
    private javax.swing.JPanel top;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JLabel zoomLabel;
    private javax.swing.JLabel zoomLevelLabel;
    private javax.swing.JButton zoomOutButton;
    // End of variables declaration//GEN-END:variables

}