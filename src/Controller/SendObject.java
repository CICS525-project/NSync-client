/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controller;

import java.util.Date;

/**
 *
 * @author Ali
 */
public class SendObject {
    private String fileName;
    private String newFileName;  //used just in case of a Rename event
    private String filePath;    //relative filePath
    private EventType event;
    private boolean enteredIntoDB;
    private Date timeStamp;
    private boolean isAFolder;

    
     public enum EventType {
        Create, Delete, Rename, Modify
    }

    
    public SendObject() {
        enteredIntoDB = false;
    }
    
    public SendObject(String fileName,String filePath,EventType event, Date timeStamp, 
            boolean isAFolder, String newFileName) {
        this.fileName = fileName;
        this.newFileName = newFileName;
        this.filePath = filePath;
        this.event = event;
        this.timeStamp = timeStamp;
        this.isAFolder = isAFolder;
        this.enteredIntoDB = false;
        
    }
    
    /**
     * @return the newFileName
     */
    public String getNewFileName() {
        return newFileName;
    }

    /**
     * @param newFileName the newFileName to set
     */
    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    /**
     * @return the isAFolder
     */
    public boolean isIsAFolder() {
        return isAFolder;
    }

    /**
     * @param isAFolder the isAFolder to set
     */
    public void setIsAFolder(boolean isAFolder) {
        this.isAFolder = isAFolder;
    }
    
    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the event
     */
    public EventType getEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(EventType event) {
        this.event = event;
    }

    /**
     * @return the enteredIntoDB
     */
    public boolean isEnteredIntoDB() {
        return enteredIntoDB;
    }

    /**
     * @param enteredIntoDB the enteredIntoDB to set
     */
    public void setEnteredIntoDB(boolean enteredIntoDB) {
        this.enteredIntoDB = enteredIntoDB;
    }

    /**
     * @return the timeStamp
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
    
}
