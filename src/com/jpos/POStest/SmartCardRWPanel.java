//------------------------------------------------------------------------------
//
// This software is provided "AS IS".  360Commerce MAKES NO
// REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE,
// EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
// WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NON-INFRINGEMENT. 360Commerce shall not be liable for
// any damages suffered as a result of using, modifying or distributing this
// software or its derivatives. Permission to use, copy, modify, and distribute
// the software and its documentation for any purpose is hereby granted.
//
// SmartCardRWPanel.java - The SmartCardRW panel of POStest
//
//------------------------------------------------------------------------------
package com.jpos.POStest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import jpos.*;
import jpos.events.*;


public class SmartCardRWPanel extends Component implements StatusUpdateListener, ActionListener {
    
	private static final long serialVersionUID = 454997701164051870L;

	protected MainButtonPanel mainButtonPanel;
    
    private SmartCardRW smartCardRW;
    
    private String defaultLogicalName = "defaultSmartCardRW";
    
    
    private JCheckBox deviceEnabledCB;
    private JCheckBox freezeEventsCB;
    
    private boolean ver_19_complient = false;
    private boolean ver_18_complient = false;
    
    private java.util.Timer updateStatusTimer;
    StatusTimerUpdateTask updateStatusTask;
    
    public SmartCardRWPanel() {
        smartCardRW = new SmartCardRW();
        updateStatusTimer = new java.util.Timer(true);
        updateStatusTask =  new StatusTimerUpdateTask();
        updateStatusTimer.schedule(updateStatusTask, 200, 200);
    }
    
    public Component make() {
        
        JPanel mainPanel = new JPanel(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        //        MethodListener methodListener = new MethodListener();
        
        mainButtonPanel = new MainButtonPanel(this,defaultLogicalName);
        mainPanel.add(mainButtonPanel);
        
        JPanel buttonPanel = new JPanel();
        
        buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        
        mainPanel.add(buttonPanel);
        
        
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));
        
        JPanel propPanel = new JPanel();
        propPanel.setLayout(new BoxLayout(propPanel, BoxLayout.Y_AXIS));
        
        
        deviceEnabledCB = new JCheckBox("Device enabled");
        propPanel.add(deviceEnabledCB);
        freezeEventsCB = new JCheckBox("Freeze events");
        propPanel.add(freezeEventsCB);
        propPanel.add(Box.createVerticalGlue());
        subPanel.add(propPanel);
        
        deviceEnabledCB.setEnabled(false);
        freezeEventsCB.setEnabled(false);
        
        CheckBoxListener cbListener = new CheckBoxListener();
        deviceEnabledCB.addItemListener(cbListener);
        freezeEventsCB.addItemListener(cbListener);
        
        
        
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Not yet implemented.");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusPanel.add(label);
        statusPanel.add(Box.createHorizontalGlue());
        
        
        subPanel.add(statusPanel);
        
        mainPanel.add(subPanel);
        mainPanel.add(Box.createVerticalGlue());
        
        return mainPanel;
    }
    
    public void statusUpdateOccurred(StatusUpdateEvent sue) {
        System.out.println("SmartCardRW received status update event.");
    }
    
    
    /** Listens to the method buttons. */
    
    public void actionPerformed(ActionEvent ae) {
        mainButtonPanel.action(ae);
        String logicalName = mainButtonPanel.getLogicalName();
        if(ae.getActionCommand().equals("open")){
            try{
                if(logicalName.equals("")){
                    logicalName = defaultLogicalName;
                }
                smartCardRW.addStatusUpdateListener(this);
                
                smartCardRW.open(logicalName);
                deviceEnabledCB.setEnabled(false);
                freezeEventsCB.setEnabled(true);
                int version = smartCardRW.getDeviceServiceVersion();
                if(version >= 1009000) {
                    ver_19_complient = true;
                    ver_18_complient = true;
                }
                if(version >= 1008000) {
                    ver_18_complient = true;
                }
            }catch(JposException e){
                JOptionPane.showMessageDialog(null, "Failed to open \""+logicalName+"\"\nException: "+ e.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
                System.err.println("Jpos exception " + e);
            }
        } else if(ae.getActionCommand().equals("claim")){
            try{
                smartCardRW.claim(0);
                
                deviceEnabledCB.setEnabled(true);
                freezeEventsCB.setEnabled(true);
            }catch(JposException e){
                JOptionPane.showMessageDialog(null, "Failed to claim \""+logicalName+"\"\nException: "+ e.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
                System.err.println("Jpos exception " + e);
            }
        } else if(ae.getActionCommand().equals("release")){
            try{
                smartCardRW.release();
                
                deviceEnabledCB.setEnabled(false);
                freezeEventsCB.setEnabled(true);
                
            }catch(JposException e){
                JOptionPane.showMessageDialog(null, "Failed to release \""+logicalName+"\"\nException: "+ e.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
                System.err.println("Jpos exception " + e);
            }
        } else if(ae.getActionCommand().equals("close")){
            try{
                smartCardRW.close();
                deviceEnabledCB.setEnabled(false);
                freezeEventsCB.setEnabled(false);
                
            }catch(JposException e){
                JOptionPane.showMessageDialog(null, "Failed to close \""+logicalName+"\"\nException: "+ e.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
                System.err.println("Jpos exception " + e);
            }
        } else if(ae.getActionCommand().equals("info")){
            try{
                String ver = new Integer(smartCardRW.getDeviceServiceVersion()).toString();
                String msg = "Service Description: " + smartCardRW.getDeviceServiceDescription();
                msg = msg + "\nService Version: v"+ new Integer(ver.substring(0,1)) + "." + new Integer(ver.substring(1,4)) + "." + new Integer(ver.substring(4,7));
                ver = new Integer(smartCardRW.getDeviceControlVersion()).toString();
                msg += "\n\nControl Description: " + smartCardRW.getDeviceControlDescription();
                msg += "\nControl Version: v"+ new Integer(ver.substring(0,1)) + "." + new Integer(ver.substring(1,4)) + "." + new Integer(ver.substring(4,7));
                msg += "\n\nPhysical Device Name: " + smartCardRW.getPhysicalDeviceName();
                msg += "\nPhysical Device Description: " + smartCardRW.getPhysicalDeviceDescription();
                
                msg += "\n\nProperties:\n------------------------";
                
                msg += "\nCapPowerReporting: " + (smartCardRW.getCapPowerReporting() == JposConst.JPOS_PR_ADVANCED ? "Advanced" : (smartCardRW.getCapPowerReporting() == JposConst.JPOS_PR_STANDARD ? "Standard" : "None"));
                if(ver_18_complient)
                {
                	msg += "\nCapStatisticsReporting: " + smartCardRW.getCapStatisticsReporting();                    
                	msg += "\nCapUpdateStatistics: " + smartCardRW.getCapUpdateStatistics();
                }
                else
                {
                	msg += "\nCapStatisticsReporting: Service Object is not 1.8 complient";
                	msg += "\nCapUpdateStatistics: Service Object is not 1.8 complient";
                }
                
                if(ver_19_complient)
                {
                	msg += "\nCapCompareFirmwareVersion: " + smartCardRW.getCapCompareFirmwareVersion();                    
                	msg += "\nCapUpdateFirmware: " + smartCardRW.getCapUpdateFirmware();
                }
                else
                {
                	msg += "\nCapCompareFirmwareVersion: Service Object is not 1.9 complient";                    
                	msg += "\nCapUpdateFirmware: Service Object is not 1.9 complient";
                }
                
                msg += "\nCapCardErrorDetection: " + smartCardRW.getCapCardErrorDetection();
                msg += "\nCapInterfaceMode: " + smartCardRW.getCapInterfaceMode();
                msg += "\nCapIsoEmvMode: " + smartCardRW.getCapIsoEmvMode();
                msg += "\nCapSCPresentSensor: " + smartCardRW.getCapSCPresentSensor();
                msg += "\nCapSCSlots: " + smartCardRW.getCapSCSlots();
                msg += "\nCapTransmissionProtocol: " + smartCardRW.getCapTransmissionProtocol();
                
                JOptionPane.showMessageDialog(null, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
                
            }catch(JposException e){
                JOptionPane.showMessageDialog(null, "Exception in Info\nException: "+ e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
                System.err.println("Jpos exception " + e);
            }
        }else if(ae.getActionCommand().equals("stats")) {
            try{
                StatisticsDialog dlg = new StatisticsDialog(smartCardRW);
                dlg.setVisible(true);
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Exception: "+ e.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
            }
            
        }else if(ae.getActionCommand().equals("firmware")) {
            try{
                FirmwareUpdateDlg dlg = new FirmwareUpdateDlg(smartCardRW);
                dlg.setVisible(true);
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Exception: "+ e.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
        try {
            deviceEnabledCB.setSelected(smartCardRW.getDeviceEnabled());
            freezeEventsCB.setSelected(smartCardRW.getFreezeEvents());
        } catch(JposException je) {
            System.err.println("SmartCardRWPanel: MethodListener: JposException");
        }
    }
    
    
    class CheckBoxListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            Object source = e.getItemSelectable();
            try {
                if (source == deviceEnabledCB){
                    if (e.getStateChange() == ItemEvent.DESELECTED){
                        smartCardRW.setDeviceEnabled(false);
                    }else{
                        smartCardRW.setDeviceEnabled(true);
                    }
                }else if (source == freezeEventsCB){
                    if (e.getStateChange() == ItemEvent.DESELECTED){
                        smartCardRW.setFreezeEvents(false);
                    }else{
                        smartCardRW.setFreezeEvents(true);
                    }
                }
            } catch(JposException je) {
                System.err.println("SmartCardRWPanel: CheckBoxListener: Jpos Exception" + e);
            }
        }
    }
    private class StatusTimerUpdateTask extends java.util.TimerTask{
        public void run(){
            if(smartCardRW != null){
                mainButtonPanel.currentStatus.setText(MainButtonPanel.getStatusString(smartCardRW.getState()));
            }
        }
    }
}
