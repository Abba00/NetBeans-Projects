/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package cas;

import cas.signIn;
import static cas.signIn.TEMPLATE_PROPERTY;
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
import com.mysql.cj.jdbc.result.ResultSetMetaData;
import java.awt.Image;
import static java.lang.Thread.sleep;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HP
 */
public class LecturerInterface extends javax.swing.JFrame {
    private DPFPCapture Reader = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment CaptureFingerPrint = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Checker = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    public void DisplayMsg(String message) {
        capture.setText(message);
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
        fingerLabel.setIcon(new ImageIcon(
                image.getScaledInstance(fingerLabel.getWidth(), fingerLabel.getHeight(), Image.SCALE_DEFAULT)));
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
            PreparedStatement ps = con.prepareStatement("SELECT * FROM students");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                byte templateBuffer[] = rs.getBytes(9);
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                setTemplate(referenceTemplate);
                DPFPVerificationResult result = Checker.verify(FingerPrintFeatureVerification, getTemplate());

                if (result.isVerified()){
                    found = true;
                    String retreived_matric = rs.getString(5);
                    String retreived_name = rs.getString(1);
                        if (jTable1.getRowCount() == 0){ 
                            DefaultTableModel MODEL = (DefaultTableModel) jTable1.getModel();
                            Object addrow [] = {retreived_matric, retreived_name};
                            MODEL.addRow(addrow);
                        }else{
                            for (int i = 0; i < jTable1.getRowCount(); i++) {
                                String left = jTable1.getValueAt(i, 0).toString();
                                String right = jTable1.getValueAt(i, 1).toString();
                                if ((left.equals(retreived_matric)) && (right.equals(retreived_name))){
                                    JOptionPane.showMessageDialog(rootPane, "Student already signed in");
                                    
                                    break;
                                }else{
                                    DefaultTableModel MODEL = (DefaultTableModel) jTable1.getModel();
                                    Object addrow [] = {retreived_matric, retreived_name};
                                    MODEL.addRow(addrow);
                                    break;
                                }
                        
                    }
                        
                }
                    
                                                    
            }            
            con.close();
                    break;
                }                       
            if (!found) {
                JOptionPane.showMessageDialog(rootPane, "No user found");
            }

        } catch (Exception e) {
            
        }
    }
    /**
     * Creates new form signIn
     */

    /**
     * Creates new form TakeAttendance
     */
    public LecturerInterface() {
        initComponents();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        fingerLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        attendancebutton = new javax.swing.JButton();
        attendancestatus = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        capture = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        getContentPane().setLayout(null);

        jButton1.setText("Take Attendance");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1);
        jButton1.setBounds(130, 120, 130, 25);

        jButton2.setText("Stop Attendance");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2);
        jButton2.setBounds(280, 120, 121, 25);

        fingerLabel.setBackground(new java.awt.Color(255, 255, 255));
        fingerLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        fingerLabel.setOpaque(true);
        getContentPane().add(fingerLabel);
        fingerLabel.setBounds(50, 180, 120, 160);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Matric No", "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(200, 180, 240, 160);

        jButton3.setText("Add");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3);
        jButton3.setBounds(370, 350, 70, 25);

        jButton5.setText("Clear Attendance");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5);
        jButton5.setBounds(120, 400, 130, 25);

        jButton6.setText("Send Attendance");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6);
        jButton6.setBounds(260, 400, 130, 25);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-Choose Department-" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        getContentPane().add(jComboBox1);
        jComboBox1.setBounds(120, 70, 150, 22);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-Choose Course-" }));
        getContentPane().add(jComboBox2);
        jComboBox2.setBounds(280, 70, 130, 22);

        attendancebutton.setText("Check Attendance");
        attendancebutton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));
        attendancebutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendancebuttonActionPerformed(evt);
            }
        });
        getContentPane().add(attendancebutton);
        attendancebutton.setBounds(380, 10, 140, 27);
        getContentPane().add(attendancestatus);
        attendancestatus.setBounds(210, 150, 120, 16);

        jButton4.setText("Remove");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4);
        jButton4.setBounds(450, 270, 77, 25);
        getContentPane().add(jTextField1);
        jTextField1.setBounds(260, 350, 100, 30);

        jLabel2.setText("Matric No");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(200, 350, 70, 30);

        capture.setText("jLabel1");
        getContentPane().add(capture);
        capture.setBounds(50, 350, 120, 16);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if ((jComboBox1.getSelectedIndex() == 0) || (jComboBox2.getSelectedIndex() == 0)){
            JOptionPane.showMessageDialog(rootPane, "Select department and course");
        }else{
            attendancestatus.setText("Attendance Open");     
            StartDigitaPersonaRetrieve();
            stop();
            start();
        }
        
        
        
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        String matno = jTextField1.getText();
        String name = "o";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cas", "root", "$ideWalks3500");
            //
            PreparedStatement ps2 = con.prepareStatement("select * from students where matric = ?");
            ps2.setString(1, matno);
            ResultSet rs = ps2.executeQuery();
            if(rs.next()){
                name = rs.getString(1);
            }
            DefaultTableModel MODEL = (DefaultTableModel) jTable1.getModel();
            Object addrow [] = {matno, name};
            MODEL.addRow(addrow);        
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Matriculation Number unidentified", "Error", JOptionPane.ERROR_MESSAGE);  
        }
        
        
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        DefaultTableModel td = (DefaultTableModel) jTable1.getModel();
        int rowCount = td.getRowCount();
                while (rowCount != 0){
                    td.removeRow(rowCount-1);
                    rowCount -= 1;
                }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        try{
        int rowcount = jTable1.getRowCount();
        String dept = jComboBox1.getSelectedItem().toString();
        String course = jComboBox2.getSelectedItem().toString();
        switch (dept){
                case "Computer Science" ->  dept = "csc";
                case "Electrical Engineering" -> dept = "eee";
                case "Mechanical Engineering" -> dept  = "mee";
                //case "Basic Science" -> table = "bsc";
                default -> {
                }
            }
        if (rowcount != 0){
            Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cas","root","$ideWalks3500");
                PreparedStatement ps2 = con.prepareStatement(String.format("update %s set %s = %s + 1 where identity = ?",dept, course,course));
                ps2.setString(1, "lecturer");
                ps2.executeUpdate();
                for(int i = 0; i < rowcount; i++){
                PreparedStatement ps = con.prepareStatement(String.format("update %s set %s = %s + 1 where identity = ?",dept, course, course));
                ps.setString(1, jTable1.getValueAt(i, 0).toString());
                ps.executeUpdate();           
                }
                JOptionPane.showMessageDialog(rootPane, "Attendance Recorded");
                con.close();
        }else{
            JOptionPane.showMessageDialog(null, "Empty Table", "Error", JOptionPane.ERROR_MESSAGE);
        }
        }catch(Exception e ){
            System.out.println(e);
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        String table = "";
        String department = signIn.retreived_dept;
        System.out.println(functions.choose(department)[0]);
        String [] deptlist = functions.choose(department);      
        for (int i =0; i < deptlist.length;i++){
            jComboBox1.addItem(deptlist[i]);
        }
        table = department;
        switch (table){
                case "Computer Science" ->  table = "csc";
                case "Electrical Engineering" -> table = "eee";
                case "Mechanical Engineering" -> table  = "mee";
                //case "Basic Science" -> table = "bsc";
                default -> {
                }
            }
        System.out.println(table);
        try{             
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cas","root","$ideWalks3500");
            PreparedStatement ps = con.prepareStatement(String.format("select * from %s where identity = ?",table));
            ps.setString(1, "lecturer");
            ResultSet rs2 = ps.executeQuery();           
            if(rs2.next()== false){
                JOptionPane.showMessageDialog(null, "You have not been assigned courses in this Department", "Error", JOptionPane.ERROR_MESSAGE);  
            }else{
                ResultSetMetaData rsmd = (ResultSetMetaData) rs2.getMetaData();
                    for(int i = 3; i < rsmd.getColumnCount(); i++){
                        if (rs2.getInt(i) > 0){
                            jComboBox2.addItem(rsmd.getColumnName(i));                         
                        }
                    }
                }
            con.close();
            
        }catch(Exception e){
            System.out.println("error" + e.getMessage());
        }
    }//GEN-LAST:event_formWindowOpened

    private void attendancebuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendancebuttonActionPerformed
       courseattendance att = new courseattendance();
       att.show();
       att.setVisible(true);
    }//GEN-LAST:event_attendancebuttonActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        attendancestatus.setText("Attendance Closed");
        stop();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         try {
            DefaultTableModel td = (DefaultTableModel)jTable1.getModel();
            int row = jTable1.getSelectedRow();
            td.removeRow(row);
        }
        catch(Exception e){
            DefaultTableModel td = (DefaultTableModel)jTable1.getModel();
            if (td.getRowCount() == 0){
                JOptionPane.showMessageDialog(rootPane, "Empty table");
            }
        }
    }//GEN-LAST:event_jButton4ActionPerformed

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
            java.util.logging.Logger.getLogger(LecturerInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LecturerInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LecturerInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LecturerInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LecturerInterface().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton attendancebutton;
    private javax.swing.JLabel attendancestatus;
    private javax.swing.JLabel capture;
    public static javax.swing.JLabel fingerLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
