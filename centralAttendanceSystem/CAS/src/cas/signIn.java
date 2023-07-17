/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package cas;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.awt.Image;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author HP
 */
public class signIn extends javax.swing.JFrame {
    private DPFPCapture Reader = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment CaptureFingerPrint = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Checker = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    String retreived_username;
    public static String retreived_role;
    public static String retreived_dept = "Computer Science";
    

    /**
     * Creates new form Retrieve
     */

    public void DisplayMsg(String message) {
        jLabel8.setText(message);
    }

    protected void StartDigitaPersonaRetrieve() {
        Reader.addDataListener(new DPFPDataAdapter() {

            public void dataAcquired(final DPFPDataEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DisplayMsg("Capturing FingerPrint");
                        FingerCaptureProcess(e.getSample());
                        try {
                            IdentifyFingerPrint();
                            CaptureFingerPrint.clear();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        });

        Reader.addReaderStatusListener(new DPFPReaderStatusAdapter() {

            public void readerConnected(final DPFPReaderStatusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        DisplayMsg("The FingerPrint Sensor is Connected");
                    }
                });
            }

            public void readerDisconnected(final DPFPReaderStatusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DisplayMsg("The FingerPrint Sensor is disconnected");
                    }
                });
            }
        });

        Reader.addSensorListener(new DPFPSensorAdapter() {

            public void fingerTouched(final DPFPSensorEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DisplayMsg("Reading FingerPrint");
                    }
                });
            }

            public void fingerRemoved(final DPFPSensorEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        DisplayMsg("Place your Finger on the FingerPrint Scanner");
                    }
                });
            }
        });

        Reader.addErrorListener(new DPFPErrorAdapter() {
            public void errorReader(final DPFPErrorEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DisplayMsg("Error: " + e.getError());
                    }
                });
            }
        });
    }

    public DPFPFeatureSet FingerPrintFeatureEnrollment;
    public DPFPFeatureSet FingerPrintFeatureVerification;

    public DPFPFeatureSet extractFingerPrintCharacteristic(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            return null;
        }
    }

    public void FingerCaptureProcess(DPFPSample sample) {

        FingerPrintFeatureEnrollment = extractFingerPrintCharacteristic(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
        FingerPrintFeatureVerification = extractFingerPrintCharacteristic(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        if (FingerPrintFeatureEnrollment != null) {
            try {

                CaptureFingerPrint.addFeatures(FingerPrintFeatureEnrollment);
                Image image;
                image = CreateImageFingerprint(sample);
                DrawFingerPrint(image);
                DisplayMsg("Done Capturing");

            } catch (DPFPImageQualityException ex) {

            } finally {

                switch (CaptureFingerPrint.getTemplateStatus()) {
                    case TEMPLATE_STATUS_READY:
                        stop();
                        setTemplate(CaptureFingerPrint.getTemplate());
                        DisplayMsg("FingerPrint Captured");

                        break;

                    case TEMPLATE_STATUS_FAILED:
                        CaptureFingerPrint.clear();
                        stop();

                        setTemplate(null);
                        start();
                        break;
                }
            }
        }

    }

    public void DrawFingerPrint(Image image) {
        jLabel11.setIcon(new ImageIcon(
                image.getScaledInstance(jLabel11.getWidth(), jLabel11.getHeight(), Image.SCALE_DEFAULT)));
        repaint();
    }

    public void start() {
        Reader.startCapture();
        DisplayMsg("FingerPrint is Connected");
    }

    public void setTemplate(DPFPTemplate templat) {
        template = templat;
        DPFPTemplate old = templat;
        templat = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }

    public Image CreateImageFingerprint(DPFPSample sample) {
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }

    public void stop() {
        Reader.stopCapture();
        DisplayMsg("Done Capturing");
    }

    public DPFPTemplate getTemplate() {
        return template;
    }

    public void IdentifyFingerPrint() {
        boolean found = false;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cas", "root", "$ideWalks3500");
            PreparedStatement ps = con.prepareStatement("SELECT * FROM faculty");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                byte templateBuffer[] = rs.getBytes(9);
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                setTemplate(referenceTemplate);
                DPFPVerificationResult result = Checker.verify(FingerPrintFeatureVerification, getTemplate());

                if (result.isVerified()){
                    found = true;
                    retreived_username = rs.getString(5);
                    retreived_role = rs.getString(2);
                    System.out.println(retreived_role);
                    JOptionPane.showMessageDialog(null, "Login Successful", "Login", JOptionPane.PLAIN_MESSAGE);
                    switch(retreived_role){
                        case "Program Coordinator" -> {
                            ProgramCoordinatorInterface one = new ProgramCoordinatorInterface();
                            one.show();
                            one.setVisible(true);
                        }
                        case "Dean" -> {
                            DeanInterface two = new DeanInterface();
                            two.show();
                            two.setVisible(true);
                        }
                        case "Head of Department" -> {
                            HODInterface three = new HODInterface();
                            three.show();
                            three.setVisible(true);
                        }
                        case "Lecturer" -> {
                            LecturerInterface four = new LecturerInterface();
                            four.show();
                            four.setVisible(true); 
                        }
                        case "Admin" -> {
                            AdminInterface five = new AdminInterface();
                            five.show();
                            five.setVisible(true);                            
                        }
                        default -> {
                            LecturerInterface four = new LecturerInterface();
                            four.show();
                            four.setVisible(true);
                        }
                    }
                    break;
                }           
            }
            if (!found) {
                JOptionPane.showMessageDialog(rootPane, "No user found");
            }
            jTextField2.setText(null);
            JPasswordField2.setText(null);
            jLabel11.setIcon(null);

        } catch (Exception e) {
            
        }
    }
    /**
     * Creates new form signIn
     */
    public signIn() {
        initComponents();
        //StartDigitaPersonaRetrieve();
        //stop();
        //start();
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel7 = new javax.swing.JLabel();
        JPasswordField2 = new javax.swing.JPasswordField();
        jCheckBox2 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        jLabel7.setText("PIN");
        getContentPane().add(jLabel7);
        jLabel7.setBounds(80, 133, 28, 16);
        getContentPane().add(JPasswordField2);
        JPasswordField2.setBounds(113, 130, 56, 22);

        jCheckBox2.setText("Show PIN");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });
        getContentPane().add(jCheckBox2);
        jCheckBox2.setBounds(180, 130, 87, 17);

        jButton2.setText("Login");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2);
        jButton2.setBounds(180, 170, 63, 25);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Classroom Attendance System");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(70, 50, 290, 25);

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField2);
        jTextField2.setBounds(113, 101, 140, 22);

        jLabel10.setText("Username");
        getContentPane().add(jLabel10);
        jLabel10.setBounds(47, 101, 61, 22);
        getContentPane().add(jLabel8);
        jLabel8.setBounds(270, 170, 0, 0);

        jLabel11.setBackground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("jLabel11");
        jLabel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel11.setOpaque(true);
        getContentPane().add(jLabel11);
        jLabel11.setBounds(270, 80, 80, 90);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        if(jCheckBox2.isSelected()){
            JPasswordField2.setEchoChar((char)0);
        }
        else{
            JPasswordField2.setEchoChar('*');
        }
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try{
            String username = jTextField2.getText();
            String pin = JPasswordField2.getText();
            String hpin = functions.Hash(pin);
            String ppin = "";

            boolean usernamecheck, pincheck;

            //Username Check
            String usernameregex = "^[a-z0-9]+$";
            Pattern pattern4 = Pattern.compile(usernameregex);
            Matcher matcher4 = pattern4.matcher(username);
            if (matcher4.matches()){
                usernamecheck = true;
            }
            else{
                usernamecheck = false;
                JOptionPane.showMessageDialog(null, "Invalid Username Type","Error", JOptionPane.ERROR_MESSAGE);
            }

            //Pin Check
            if ((!pin.equals("")) && (pin.length() == 4)){
                pincheck = true;
            }else{
                pincheck = false;
                JOptionPane.showMessageDialog(null, "Invalid Username Type", "Error", JOptionPane.ERROR_MESSAGE);
            }

            if ((usernamecheck == true) && (pincheck == true) ){
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cas","root","$ideWalks3500");
                PreparedStatement ps = con.prepareStatement("select * from faculty where username=? ");
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    retreived_username = rs.getString(5);
                    retreived_role = rs.getString(2);
                    retreived_dept = rs.getString(3);
                    
                    ppin = rs.getString(6);
                }else{
                    JOptionPane.showMessageDialog(null, "Invalid Login Credentials", "Error", JOptionPane.ERROR_MESSAGE);
                }
                if (username.equals(retreived_username) && (ppin.equals(hpin))){
                    JOptionPane.showMessageDialog(null, "Login Successful", "Login", JOptionPane.PLAIN_MESSAGE);
                    switch(retreived_role){
                        case "Program Coordinator" -> {
                            ProgramCoordinatorInterface one = new ProgramCoordinatorInterface();
                            one.show();
                            one.setVisible(true);
                        }
                        case "Dean" -> {
                            DeanInterface two = new DeanInterface();
                            two.show();
                            two.setVisible(true);
                        }
                        case "Head of Department" -> {
                            HODInterface three = new HODInterface();
                            three.show();
                            three.setVisible(true);
                        }
                        case "Lecturer" -> {
                            LecturerInterface four = new LecturerInterface();
                            four.show();
                            four.setVisible(true);
                        }
                        case "Admin" -> {
                            AdminInterface five = new AdminInterface();
                            five.show();
                            five.setVisible(true);                            
                        }
                        default -> {
                            LecturerInterface four = new LecturerInterface();
                            four.show();
                            four.setVisible(true);
                        }
                    }
                    
                }
                else{
                    JOptionPane.showMessageDialog(null, "Invalid Login Credentials", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            jTextField2.setText(null);
            JPasswordField2.setText(null);
            jLabel11.setIcon(null);

        }catch (Exception e){
            System.out.println(e);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(signIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(signIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(signIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(signIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new signIn().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField JPasswordField2;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
