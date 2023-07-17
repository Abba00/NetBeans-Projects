/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cas;

import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Font;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author HP
 */
public class functions {
    public static void ShowPin(JCheckBox jCheckBox, JPasswordField jPasswordfield){
         if(jCheckBox.isSelected()){
            jPasswordfield.setEchoChar((char)0);
        }
        else{
            jPasswordfield.setEchoChar('*');
         }    
    }
    public static boolean UsernameCheck(String username, JTextField jtextField){
        String usernameregex = "^[a-z0-9]+$";
            Pattern pattern4 = Pattern.compile(usernameregex);
            Matcher matcher4 = pattern4.matcher(username);
            if (matcher4.matches()){
                try{    
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection usernamecon = DriverManager.getConnection("jdbc:mysql://localhost:3306/cas","root","$ideWalks3500");
                    PreparedStatement usernameps = usernamecon.prepareStatement("select * from faculty where username=? ");          
                    usernameps.setString(1, username);
                    ResultSet usernamers = usernameps.executeQuery();
                    if(usernamers.next()){
                        JOptionPane.showMessageDialog(null, "Username taken", "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }else if (matcher4.matches()){
                            removePlaceHolder(jtextField);
                            return true;
                    }
                }catch(Exception e){
                    JOptionPane.showMessageDialog(null, " Databse Connection Error", "Error", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Database not connected");
                }
            }else{
                    addPlaceHolder(jtextField);
                    JOptionPane.showMessageDialog(null, "Invalid Username","Error", JOptionPane.ERROR_MESSAGE);
                        return true;
                }           
        return false;
    }  
    public static boolean FullNameCheck(String fullname, JTextField jtextField){
        String nameregex = "^[a-zA-Z ,.'-]+$";
            Pattern pattern1 = Pattern.compile(nameregex);
            Matcher matcher1 = pattern1.matcher(fullname);            
            if (matcher1.matches()) {
                removePlaceHolder(jtextField);
                return true;
            }
            else {
                addPlaceHolder(jtextField);
                JOptionPane.showMessageDialog(null, "Name Invalid", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
    }
    public static boolean RoleCheck(JComboBox jcomboBox){
        if (jcomboBox.getSelectedIndex() == 0){
                addPlaceHolderComboBox(jcomboBox);
                JOptionPane.showMessageDialog(null, "Select valid role", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }else{
                removePlaceHolderComboBox(jcomboBox);
                return true;
            }
    }
    
    public static boolean DeptCheck(JComboBox jcomboBox){
        if (jcomboBox.getSelectedIndex() == 0){
                addPlaceHolderComboBox(jcomboBox);
                JOptionPane.showMessageDialog(null, "Select valid department", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }else{
                removePlaceHolderComboBox(jcomboBox);
                return true;
            }
    }
    public static boolean EmailCheck(String email, JTextField jtextField){
        String emailregex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^,-.]+@pau.edu.ng+$";
            Pattern pattern2 = Pattern.compile(emailregex);
            Matcher matcher2 = pattern2.matcher(email);            
            if (matcher2.matches()) {
                removePlaceHolder(jtextField);
                return true;
            }
            else {
                addPlaceHolder(jtextField);
                JOptionPane.showMessageDialog(null, "Email Invalid", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
    }
    public static boolean PhoneCheck(String phone, JTextField jtextField){
        String phoneregex = "^[0-9]{11}+$";
            Pattern pattern3 = Pattern.compile(phoneregex);
            Matcher matcher3 = pattern3.matcher(phone);
            if (matcher3.matches()){
                removePlaceHolder(jtextField);
                return true;
            }
            else{
                addPlaceHolder(jtextField);
                JOptionPane.showMessageDialog(null, "Invalid Phone Number", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
    }
    public static boolean PinCheck(String PIN, String PIN2, JPasswordField jPasswordField1, JPasswordField jPasswordField2){
        if (PIN.equals(PIN2) && (!PIN.equals("")) && (!PIN2.equals("")) && (PIN.length() == 4)){
                removePlaceHolderPassword(jPasswordField1);
                removePlaceHolderPassword(jPasswordField2);
                return true;
            }else{
                addPlaceHolderPassword(jPasswordField1);
                addPlaceHolderPassword(jPasswordField2);
                JOptionPane.showMessageDialog(null, "Password Mismatch or Length Error", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
    }
    
    public static boolean MatricCheck(String matricno, JTextField jtextField){
        String matricregex = "^[0-9]{10}+$";
            Pattern pattern3 = Pattern.compile(matricregex);
            Matcher matcher3 = pattern3.matcher(matricno);
            if (matcher3.matches()){
                removePlaceHolder(jtextField);
                return true;
            }
            else{
                addPlaceHolder(jtextField);
                JOptionPane.showMessageDialog(null, "Invalid Phone Number", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
    }
    public static boolean DateCheck(JDateChooser jdateChooser){
        if (jdateChooser.getDate() == null){
           JOptionPane.showMessageDialog(null, "Insert date of birth", "Error", JOptionPane.ERROR_MESSAGE); 
           return false;
        }
        else{
           return true; 
        }
    }
    public static boolean PhotoCheck(JLabel photoLabel){
        if (photoLabel.getIcon() == null){
            addPlaceHolderLabel(photoLabel);
            JOptionPane.showMessageDialog(null, "No Photo inserted", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        else{
            removePlaceHolderLabel(photoLabel);
            return true;
        }
    }
    public static boolean FingerCheck(JLabel fingerLabel){
        if (fingerLabel.getIcon() == null){
            addPlaceHolderLabel(fingerLabel);
            JOptionPane.showMessageDialog(null, "No Fingerprint inserted", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        else{
            removePlaceHolderLabel(fingerLabel);
            return true;
        }
    }
    public static boolean CourseCheck(JList list){
        if (list.getModel().getSize() == 0){
            JOptionPane.showMessageDialog(null, "Select your courses", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        else{
            return true;
        }
    }
    public static String Hash(String c) {
        try{
            MessageDigest msgDigest = MessageDigest.getInstance("MD5");
            msgDigest.update((new String(c)).getBytes("UTF8"));
            String passHash = new String(msgDigest.digest());
            return passHash;
        }catch(Exception e){
            return c;
        }
    }        
    public static void addPlaceHolder(JTextField jtextField) {
        Font font = jtextField.getFont();
        font.deriveFont(Font.ITALIC);
        jtextField.setFont(font);
        jtextField.setBackground(Color.RED);
    } 
    public static void removePlaceHolder(JTextField jtextField) {
        Font font = jtextField.getFont();
        font.deriveFont(Font.PLAIN);
        jtextField.setFont(font);
        jtextField.setBackground(Color.WHITE);
    }
    public static void addPlaceHolderLabel(JLabel jLabel) {
        Font font = jLabel.getFont();
        font.deriveFont(Font.ITALIC);
        jLabel.setFont(font);
        jLabel.setBackground(Color.RED);
    }
    public static void removePlaceHolderLabel(JLabel jLabel) {
        Font font = jLabel.getFont();
        font.deriveFont(Font.PLAIN);
        jLabel.setFont(font);
        jLabel.setBackground(Color.WHITE);
    }
    
     public static void addPlaceHolderPassword(JPasswordField jPassword) {
        Font font = jPassword.getFont();
        font.deriveFont(Font.ITALIC);
        jPassword.setFont(font);
        jPassword.setBackground(Color.RED);
    }
     
    public static void removePlaceHolderPassword(JPasswordField jPassword) {
        Font font = jPassword.getFont();
        font.deriveFont(Font.PLAIN|Font.BOLD);
        jPassword.setFont(font);
        jPassword.setBackground(Color.WHITE);
    }
    public static void addPlaceHolderComboBox(JComboBox jComboBox) {
        Font font = jComboBox.getFont();
        font.deriveFont(Font.ITALIC);
        jComboBox.setFont(font);
        jComboBox.setBackground(Color.RED);
    }
    public static void removePlaceHolderComboBox(JComboBox jComboBox) {
        Font font = jComboBox.getFont();
        font.deriveFont(Font.PLAIN);
        jComboBox.setFont(font);
        jComboBox.setBackground(Color.WHITE);
    }
    
    public static String[] courses(String department) {
        if (department == "Computer Science"){
            return new String[] {"csc101", "csc102", "csc201", "csc202", "csc301", "csc302", "csc401", "csc402", "mth101", "mth201", "phy101", "phy201", "chm101"};
        }
        else if (department == "Electrical Engineering"){
            return new String[] {"eee101", "eee102", "eee201", "eee202", "eee301", "eee302", "eee401", "eee402", "mth101", "mth201", "phy101", "phy201", "chm101"};
        }
        else if (department == "Mechanical Engineering"){
            return new String[] {"mee101", "mee102", "mee201", "mee202", "mee301", "mee302", "mee401", "mee402", "mth101", "mth201", "phy101", "phy201", "chm101"};
        }
        return new String[] {"Courses not found"};
    }
    
    public static String[] departments(String learn) {
        if (learn == "Computer Science"){
            return new String[] {"csc101", "csc102", "csc201", "csc202", "csc301", "csc302", "csc401", "csc402"};
        }
        else if (learn == "Electrical Engineering"){
            return new String[] {"eee101", "eee102", "eee201", "eee202", "eee301", "eee302", "eee401", "eee402"};
        }
        else if (learn == "Mechanical Engineering"){
            return new String[] {"mee101", "mee102", "mee201", "mee202", "mee301", "mee302", "mee401", "mee402"};
        }
        else if (learn == "Basic Science"){
            return new String[] {"mth101", "mth201", "phy101", "phy201", "chm101"};      
        }
        return new String[] {"Courses not found"};
    }
    
    public static String[] choose(String learn) {
        if (learn == "Computer Science"){
            return new String[] {"Computer Science"};
        }
        else if (learn == "Electrical Engineering"){
            return new String[] {"Electrical Engineering"};
        }
        else if (learn == "Mechanical Engineering"){
            return new String[] {"Mechanical Engineering"};
        }
        else if (learn == "Basic Science"){
            return new String[] {"Computer Science", "Electrical Engineering", "Mechanical Engineering"};      
        }
        return new String[] {"Courses not found"};
    }
        
    public static void main(String[] args) {
        String test = String.format("%s","hello");
        System.out.println(test);
        
    }
}


