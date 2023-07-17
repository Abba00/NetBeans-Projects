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
import java.awt.List;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author HP
 */
public class ProgramCoordinatorInterface extends javax.swing.JFrame {
    public String filename=null;
    static byte photo[] = null;
    
    private DPFPCapture Reader = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment CaptureFingerPrint = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Checker = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";

    /**
     * Creates new form NewJFrame
     */

        
        
        //time tt=new time();
        //Thread t1=new Thread(tt);
        //t1.start();
        
    

    public void DisplayMsg(String message) {
        jLabel6.setText(message);
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
                        CaptureFingerPrint.clear();
                        FingerPrintFeatureEnrollment = null;
                        FingerPrintFeatureVerification = null;

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
     * Creates new form AddStudent
     */
    public ProgramCoordinatorInterface() {
        initComponents();
        start();
        StartDigitaPersonaEnrollment();
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
        jButton3 = new javax.swing.JButton();
        deptComboBox = new javax.swing.JComboBox<>();
        jButton4 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        emailField = new javax.swing.JTextField();
        fingerLabel = new javax.swing.JLabel();
        phoneField = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        levelComboBox = new javax.swing.JComboBox<>();
        matricField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jLabel15 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel18 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        photoLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        attendancebutton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel1.setText("Add Student");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(225, 13, -1, 46));

        jButton3.setText("Browse");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 140, 91, -1));

        deptComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-Select your Department-", "Computer Science", "Electrical Engineering", "Mechanical Engineering" }));
        deptComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deptComboBoxActionPerformed(evt);
            }
        });
        getContentPane().add(deptComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 190, 194, -1));

        jButton4.setText("Take Photo");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 180, -1, -1));

        jLabel3.setText("Name");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(61, 117, -1, -1));

        jLabel4.setText("Email");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(61, 310, -1, -1));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(375, 88, 12, 430));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 2, 10)); // NOI18N
        jLabel9.setText("Must be university email (i.e. example@pau.edu.ng)");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 336, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Personal Information");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(158, 80, -1, -1));

        jLabel7.setText("Phone Number");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 360, -1, -1));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setText("Fingerprint");
        getContentPane().add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 310, -1, -1));

        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });
        getContentPane().add(nameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 114, 194, -1));
        getContentPane().add(emailField, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 307, 194, -1));

        fingerLabel.setBackground(new java.awt.Color(255, 255, 255));
        fingerLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        fingerLabel.setOpaque(true);
        getContentPane().add(fingerLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 360, 124, 140));
        getContentPane().add(phoneField, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 357, 193, -1));
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 240, 124, -1));

        jLabel14.setText("Date of Birth");
        getContentPane().add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 160, -1, -1));
        getContentPane().add(jDateChooser1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 154, 190, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Courses");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(57, 409, -1, -1));

        jLabel11.setText("Level");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(66, 237, -1, -1));

        levelComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-Select your Level-", "100 Level", "200 Level", "300 Level", "400 Level" }));
        levelComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                levelComboBoxActionPerformed(evt);
            }
        });
        getContentPane().add(levelComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 234, 194, -1));
        getContentPane().add(matricField, new org.netbeans.lib.awtextra.AbsoluteConstraints(109, 269, 193, -1));

        jLabel12.setText("Matric Number");
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 272, -1, -1));

        jScrollPane1.setViewportView(jList1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 432, 123, -1));

        jScrollPane2.setViewportView(jList2);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(205, 432, 120, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("Register");
        getContentPane().add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(243, 409, -1, -1));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 8)); // NOI18N
        jButton1.setText(">");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(147, 478, 51, -1));

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 8)); // NOI18N
        jButton5.setText("Clear");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(147, 504, -1, -1));
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(386, 290, 300, 10));

        jLabel18.setText("Department");
        getContentPane().add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, 70, 20));

        jButton2.setText("Register");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 530, -1, -1));

        jButton6.setText("Clear");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 530, -1, -1));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setText("Student Photo");
        getContentPane().add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(485, 80, -1, -1));
        getContentPane().add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 392, 356, 10));

        photoLabel1.setBackground(new java.awt.Color(255, 255, 255));
        photoLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        photoLabel1.setOpaque(true);
        getContentPane().add(photoLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 110, 124, 123));
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 380, 150, 20));

        attendancebutton.setText("Check Attendance");
        attendancebutton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));
        attendancebutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendancebuttonActionPerformed(evt);
            }
        });
        getContentPane().add(attendancebutton, new org.netbeans.lib.awtextra.AbsoluteConstraints(585, 10, 110, 30));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            jFileChooser1.showOpenDialog(null);
            File f = jFileChooser1.getSelectedFile();
            filename = f.getAbsolutePath();
            String path = f.getAbsolutePath();
            jTextField1.setText(path);
            Image im = Toolkit.getDefaultToolkit().createImage(path);
            im = im.getScaledInstance(fingerLabel.getWidth(), fingerLabel.getHeight(), Image.SCALE_SMOOTH);
            ImageIcon ic = new ImageIcon(im);
            fingerLabel.setIcon(ic);
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

    private void deptComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deptComboBoxActionPerformed
        String[] x = functions.courses(deptComboBox.getSelectedItem().toString());
        jList1.setModel(new DefaultComboBoxModel<>(x));       
        jList2.setModel(new DefaultComboBoxModel());        
    }//GEN-LAST:event_deptComboBoxActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Capture newCapture = new Capture();
        newCapture.show();
        newCapture.setVisible(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameFieldActionPerformed

    private void levelComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_levelComboBoxActionPerformed
        
    }//GEN-LAST:event_levelComboBoxActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String register = jList1.getSelectedValue();
        DefaultComboBoxModel ld1 = (DefaultComboBoxModel)jList1.getModel();
        DefaultComboBoxModel ld2 = (DefaultComboBoxModel)jList2.getModel();
        if (register != null){
            ld2.addElement(register);
            ld1.removeElement(register);
            register = null;
        }else{
            JOptionPane.showMessageDialog(null, "Select a course", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        String[] x = functions.courses(deptComboBox.getSelectedItem().toString());
        jList1.setModel(new DefaultComboBoxModel<>(x));       
        jList2.setModel(new DefaultComboBoxModel()); 
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try{
            //Attributes
            String name = nameField.getText();
            SimpleDateFormat dat = new SimpleDateFormat("yyyy-MM-dd"); String dob = dat.format(jDateChooser1.getDate());
            String dept = deptComboBox.getSelectedItem().toString();
            String level = levelComboBox.getSelectedItem().toString();
            String matric = matricField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            ByteArrayInputStream FingerPrintData = new ByteArrayInputStream(template.serialize());
            Integer FingerPrintSize = template.serialize().length;

            //Informtion Validation
            boolean namecheck = functions.FullNameCheck(name, nameField);
            boolean deptcheck = functions.RoleCheck(deptComboBox);
            boolean levelcheck = functions.RoleCheck(levelComboBox);
            boolean matriccheck = functions.MatricCheck(matric, matricField);
            boolean emailcheck = functions.EmailCheck(email, emailField);
            boolean phonecheck = functions.PhoneCheck(phone, phoneField);
            boolean photocheck = functions.PhotoCheck(photoLabel1);
            boolean coursecheck = functions.CourseCheck(jList2);
            boolean datecheck = functions.DateCheck(jDateChooser1);
            boolean fingercheck =  functions.FingerCheck(fingerLabel);
            String[] courses = new String[jList2.getModel().getSize()];
            for (int i = 0; i < jList2.getModel().getSize(); i++) {
            courses[i] = String.valueOf(jList2.getModel().getElementAt(i));
            }         
            
            System.out.println(courses.length);
            String coursetable = null;
            switch (deptComboBox.getSelectedIndex()) {
                case 1 -> coursetable = "csc";
                case 2 -> coursetable = "eee";
                case 3 -> coursetable = "mee";
                default -> {
                }
            }
            System.out.println(coursetable);
            if ((namecheck == true) && (deptcheck == true) && (emailcheck == true) && (matriccheck == true) && (phonecheck == true) && (photocheck == true) && (levelcheck == true) && (coursecheck == true) && (datecheck == true) && (fingercheck == true)){
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cas","root","$ideWalks3500");
                PreparedStatement ps = con.prepareStatement("insert into students(name, dob, dept, level, matric, email, phone, photo, finger) values(?,?,?,?,?,?,?,?,?)");
                ps.setString(1, name);
                ps.setString(2, dob);
                ps.setString(3, dept);
                ps.setString(4, level);
                ps.setString(5, matric);
                ps.setString(6, email);
                ps.setString(7, phone);
                ps.setBytes(8, photo);
                ps.setBinaryStream(9,  FingerPrintData, FingerPrintSize);
                ps.executeUpdate();
                
                String statement = String.format("insert into %s(name, identity) values(?,?)",coursetable);
                PreparedStatement ps2 = con.prepareStatement(statement);
                ps2.setString(1, name);
                ps2.setString(2, matric);
                ps2.executeUpdate();
                
                for(int a = 0; a < courses.length-1;a++){
                    String course = courses[a];
                    System.out.println(course);
                    PreparedStatement ps3 = con.prepareStatement(String.format("update %s set %s = ? where identity = ?", coursetable, course));
                    ps3.setInt(1, 1);
                    ps3.setString(2, matric);
                    ps3.executeUpdate();
                }
                JOptionPane.showMessageDialog(rootPane, name + " user created");
                con.close();

                nameField.setText(null);
                jDateChooser1.setDate(null);
                deptComboBox.setSelectedIndex(0);
                levelComboBox.setSelectedIndex(0);
                matricField.setText(null);
                emailField.setText(null);
                phoneField.setText(null);
                jTextField1.setText(null);
                photoLabel1.setIcon(null);
                fingerLabel.setIcon(null);
                
            }else{
                System.out.println("not complete");
            }

        }catch(Exception e){
            System.out.println(e);
        }
        start();
        StartDigitaPersonaEnrollment();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        nameField.setText(null);
        jDateChooser1.setDate(null);
        deptComboBox.setSelectedIndex(0);
        levelComboBox.setSelectedIndex(0);
        matricField.setText(null);
        emailField.setText(null);
        phoneField.setText(null);
        jTextField1.setText(null);
        photoLabel1.setIcon(null);
        fingerLabel.setIcon(null);
                
        
    }//GEN-LAST:event_jButton6ActionPerformed

    private void attendancebuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendancebuttonActionPerformed
        DeanInterface Attendance = new DeanInterface();
        Attendance.show();
        Attendance.setVisible(true);
    }//GEN-LAST:event_attendancebuttonActionPerformed

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
            java.util.logging.Logger.getLogger(ProgramCoordinatorInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProgramCoordinatorInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProgramCoordinatorInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProgramCoordinatorInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ProgramCoordinatorInterface().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton attendancebutton;
    private javax.swing.JComboBox<String> deptComboBox;
    private javax.swing.JTextField emailField;
    public static javax.swing.JLabel fingerLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList<String> jList1;
    private javax.swing.JList<String> jList2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JComboBox<String> levelComboBox;
    private javax.swing.JTextField matricField;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField phoneField;
    public static javax.swing.JLabel photoLabel1;
    // End of variables declaration//GEN-END:variables
}
