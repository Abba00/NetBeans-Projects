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
import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author HP
 */
public final class AdminInterface extends javax.swing.JFrame {
     
    private DPFPCapture Reader = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment CaptureFingerPrint = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Checker = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    public String filename=null;
    static byte photo[] = null;
    
    /**
     * Creates new form NewJFrame
     */

        
        
        //time tt=new time();
        //Thread t1=new Thread(tt);
        //t1.start();
        
    

    public void DisplayMsg(String message) {
        jLabel14.setText(message);
    }

    protected void StartDigitaPersonaEnrollment() {
        Reader.addDataListener(new DPFPDataAdapter() {

            public void dataAcquired(final DPFPDataEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      
                        DisplayMsg("Capturing FingerPrint");
                        FingerCaptureProcess(e.getSample());
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
       // FingerPrintFeatureVerification = extractFingerPrintCharacteristic(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        if (FingerPrintFeatureEnrollment != null) {
            try {

                CaptureFingerPrint.addFeatures(FingerPrintFeatureEnrollment);
                Image image;
                image = CreateImageFingerprint(sample);
                DrawFingerPrint(image);
               DisplayMsg("Tap The FingerPrint Sensor Again");

            } catch (DPFPImageQualityException ex) {

            } finally {

                switch (CaptureFingerPrint.getTemplateStatus()) {
                    case TEMPLATE_STATUS_READY:
                        stop();
                        setTemplate(CaptureFingerPrint.getTemplate());
                        DisplayMsg("FingerPrint Captured");
                        JOptionPane.showMessageDialog(rootPane, "FingerPrint Captured");

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
        fingerLabel.setIcon(new ImageIcon(
                image.getScaledInstance(fingerLabel.getWidth(), fingerLabel.getHeight(), Image.SCALE_DEFAULT)));
        repaint();
    }

    public void start() {
        Reader.startCapture();
        DisplayMsg("FingerPrint is Connected");
           
    }

    public void setTemplate(DPFPTemplate FingerPrintTemplate) {
        CaptureFingerPrint.getTemplate();
        template = FingerPrintTemplate;
        DPFPTemplate old = FingerPrintTemplate;
        FingerPrintTemplate = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, FingerPrintTemplate);
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
    
    /**
     * Creates new form AddUser
     */
    public AdminInterface() {    
        initComponents(); 
        StartDigitaPersonaEnrollment();
        start();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        emailField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        phoneField = new javax.swing.JTextField();
        pinCheckBox = new javax.swing.JCheckBox();
        roleComboBox = new javax.swing.JComboBox<>();
        pinField = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        pinField2 = new javax.swing.JPasswordField();
        jLabel8 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        photoLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        fingerLabel = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        roleComboBox1 = new javax.swing.JComboBox<>();
        attendancebutton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        getContentPane().setLayout(null);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel1.setText("Add User");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(210, 30, 240, 46);

        jLabel4.setText("Email");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(60, 240, 60, 16);

        jButton1.setText("Register");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1);
        jButton1.setBounds(248, 496, 75, 25);

        jLabel5.setText("PIN");
        getContentPane().add(jLabel5);
        jLabel5.setBounds(70, 340, 50, 16);

        jButton2.setText("Clear");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2);
        jButton2.setBounds(335, 496, 59, 25);

        jLabel6.setText("Re-type PIN");
        getContentPane().add(jLabel6);
        jLabel6.setBounds(30, 380, 90, 16);

        jLabel9.setFont(new java.awt.Font("Segoe UI", 2, 10)); // NOI18N
        jLabel9.setText("Must be university email (i.e. example@pau.edu.ng)");
        getContentPane().add(jLabel9);
        jLabel9.setBounds(110, 260, 227, 14);

        jLabel7.setText("Phone Number");
        getContentPane().add(jLabel7);
        jLabel7.setBounds(20, 420, 110, 16);

        jLabel10.setText("Username");
        getContentPane().add(jLabel10);
        jLabel10.setBounds(40, 290, 80, 16);

        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });
        getContentPane().add(nameField);
        nameField.setBounds(110, 124, 194, 22);
        getContentPane().add(emailField);
        emailField.setBounds(110, 240, 194, 22);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 2, 10)); // NOI18N
        jLabel11.setText("Only lowercase letters and numbers(i.e. user01)");
        getContentPane().add(jLabel11);
        jLabel11.setBounds(110, 310, 211, 14);
        getContentPane().add(phoneField);
        phoneField.setBounds(110, 420, 144, 22);

        pinCheckBox.setText("Show PIN");
        pinCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pinCheckBoxActionPerformed(evt);
            }
        });
        getContentPane().add(pinCheckBox);
        pinCheckBox.setBounds(180, 330, 87, 25);

        roleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-Select your Role-", "Program Coordinator", "Dean", "Head of Department", "Lecturer", "Admin" }));
        roleComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                roleComboBoxActionPerformed(evt);
            }
        });
        getContentPane().add(roleComboBox);
        roleComboBox.setBounds(110, 164, 141, 22);
        getContentPane().add(pinField);
        pinField.setBounds(110, 340, 47, 22);

        jLabel3.setText("Name");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(60, 130, 60, 16);
        getContentPane().add(pinField2);
        pinField2.setBounds(110, 380, 47, 22);

        jLabel8.setText("Role");
        getContentPane().add(jLabel8);
        jLabel8.setBounds(70, 170, 50, 16);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 2, 10)); // NOI18N
        jLabel12.setText("Your pin must be 4 digits");
        getContentPane().add(jLabel12);
        jLabel12.setBounds(110, 360, 110, 14);
        getContentPane().add(usernameField);
        usernameField.setBounds(110, 290, 193, 22);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        getContentPane().add(jSeparator1);
        jSeparator1.setBounds(340, 98, 12, 342);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Personal Information");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(123, 90, 140, 16);

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setText("Fingerprint");
        getContentPane().add(jLabel13);
        jLabel13.setBounds(450, 290, 70, 16);

        photoLabel1.setBackground(new java.awt.Color(255, 255, 255));
        photoLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        photoLabel1.setOpaque(true);
        getContentPane().add(photoLabel1);
        photoLabel1.setBounds(370, 130, 124, 123);

        jButton3.setText("Browse");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3);
        jButton3.setBounds(510, 170, 90, 25);

        jButton4.setText("Take Photo");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4);
        jButton4.setBounds(510, 210, 91, 25);
        getContentPane().add(jTextField1);
        jTextField1.setBounds(370, 260, 130, 22);

        fingerLabel.setBackground(new java.awt.Color(255, 255, 255));
        fingerLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        fingerLabel.setOpaque(true);
        getContentPane().add(fingerLabel);
        fingerLabel.setBounds(370, 320, 120, 140);
        getContentPane().add(jLabel14);
        jLabel14.setBounds(500, 350, 110, 20);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("Biometrics");
        getContentPane().add(jLabel15);
        jLabel15.setBounds(450, 90, 60, 16);

        jLabel16.setText("Department");
        getContentPane().add(jLabel16);
        jLabel16.setBounds(30, 200, 70, 20);

        roleComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-Select Department-", "Computer Science", "Electrical Engineering", "Mechanical Engineering", "Basic Science", "SST Management" }));
        roleComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                roleComboBox1ActionPerformed(evt);
            }
        });
        getContentPane().add(roleComboBox1);
        roleComboBox1.setBounds(110, 200, 151, 22);

        attendancebutton.setText("Check Attendance");
        attendancebutton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));
        attendancebutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendancebuttonActionPerformed(evt);
            }
        });
        getContentPane().add(attendancebutton);
        attendancebutton.setBounds(490, 20, 140, 27);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try{
            //Attributes
            String name = nameField.getText();
            String role = roleComboBox.getSelectedItem().toString();
            String department = roleComboBox1.getSelectedItem().toString();
            String email = emailField.getText();
            String username = usernameField.getText();
            String pin = pinField.getText();
            String pin2 = pinField2.getText();
            String phone = phoneField.getText();
            ByteArrayInputStream FingerPrintData = new ByteArrayInputStream(template.serialize());
            Integer FingerPrintSize = template.serialize().length;
            
            
            //Hash Pin
            String hpin = functions.Hash(pin);
            
            //Informtion Validation
            boolean namecheck = functions.FullNameCheck(name, nameField);
            boolean rolecheck = functions.RoleCheck(roleComboBox);
            boolean deptcheck = functions.DeptCheck(roleComboBox1);
            boolean emailcheck = functions.EmailCheck(email, emailField);
            boolean usernamecheck = functions.UsernameCheck(username, usernameField);
            boolean pincheck = functions.PinCheck(pin, pin2, pinField, pinField2);
            boolean phonecheck = functions.PhoneCheck(phone, phoneField);
            boolean fingercheck = functions.FingerCheck(fingerLabel);
            boolean photocheck = functions.PhotoCheck(photoLabel1);
                     
            if ((namecheck == true) && (rolecheck == true) && (deptcheck == true) && (emailcheck == true) && (pincheck == true) && (phonecheck == true) && (usernamecheck == true) && (photocheck == true) && (fingercheck = true)){     
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cas","root","$ideWalks3500");
                PreparedStatement ps = con.prepareStatement("insert into faculty(name, role, department, email, username, pin, phone, photo, finger) values(?,?,?,?,?,?,?,?,?)");
                ps.setString(1, name);
                ps.setString(2, role);
                ps.setString(3, department);
                ps.setString(4, email);
                ps.setString(5, username);
                ps.setString(6, hpin);
                ps.setString(7, phone);
                ps.setBytes(8, photo);
                ps.setBinaryStream(9,  FingerPrintData, FingerPrintSize);
                int rs  = ps.executeUpdate();
                
                JOptionPane.showMessageDialog(rootPane, name + " user created");
                con.close();
  
                
                nameField.setText(null);
                emailField.setText(null);
                usernameField.setText(null);
                phoneField.setText(null);
                pinField.setText(null);
                pinField2.setText(null);
                roleComboBox.setSelectedIndex(0);
                roleComboBox1.setSelectedIndex(0);
                photoLabel1.setIcon(null);
                jTextField1.setText(null);
                fingerLabel.setIcon(null);
                
                
            }else{
                JOptionPane.showMessageDialog(rootPane, "Database Issue");
            }
                   
        }catch(Exception e){
            System.out.println(e);
        }
        start();
        StartDigitaPersonaEnrollment();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        nameField.setText(null);
        emailField.setText(null);
        usernameField.setText(null);
        phoneField.setText(null);
        pinField.setText(null);
        pinField2.setText(null);
        roleComboBox.setSelectedIndex(0);
        photoLabel1.setIcon(null);
        jTextField1.setText(null);
        fingerLabel.setIcon(null);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void pinCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pinCheckBoxActionPerformed
        functions.ShowPin(pinCheckBox, pinField);
        functions.ShowPin(pinCheckBox, pinField2);
    }//GEN-LAST:event_pinCheckBoxActionPerformed

    private void roleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roleComboBoxActionPerformed
    }//GEN-LAST:event_roleComboBoxActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameFieldActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            jFileChooser1.showOpenDialog(null);
            File f = jFileChooser1.getSelectedFile();
            filename = f.getAbsolutePath();
            String path = f.getAbsolutePath();
            jTextField1.setText(path);
            Image im = Toolkit.getDefaultToolkit().createImage(path);
            im = im.getScaledInstance(photoLabel1.getWidth(), photoLabel1.getHeight(), Image.SCALE_SMOOTH);
            ImageIcon ic = new ImageIcon(im);
            photoLabel1.setIcon(ic);
            File image = new File(filename);
            FileInputStream fis = new FileInputStream(image);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] Byte = new byte[1024];

            for (int i; (i = fis.read(Byte)) != -1;) {
                baos.write(Byte, 0, i);
            }
            photo = baos.toByteArray();

        } catch (Exception e) {

        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        CaptureFaculty newCapture = new CaptureFaculty();
        newCapture.show();
        newCapture.setVisible(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void roleComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roleComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_roleComboBox1ActionPerformed

    private void attendancebuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendancebuttonActionPerformed
        DeanInterface Attendance = new DeanInterface();
        Attendance.show();
        Attendance.setVisible(true);
    }//GEN-LAST:event_attendancebuttonActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        
    }//GEN-LAST:event_formWindowOpened

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered

    }//GEN-LAST:event_formMouseEntered

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        
    }//GEN-LAST:event_formWindowActivated

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
            java.util.logging.Logger.getLogger(AdminInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminInterface().setVisible(true);
                
            }
            
        });
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton attendancebutton;
    private javax.swing.JTextField emailField;
    public static javax.swing.JLabel fingerLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField phoneField;
    public static javax.swing.JLabel photoLabel1;
    private javax.swing.JCheckBox pinCheckBox;
    private javax.swing.JPasswordField pinField;
    private javax.swing.JPasswordField pinField2;
    private javax.swing.JComboBox<String> roleComboBox;
    private javax.swing.JComboBox<String> roleComboBox1;
    private javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
