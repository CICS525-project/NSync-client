/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
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
    private String hash;
    private String ID;
  
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
        this.setHash();
        
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
    
    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(String ID) {
        this.ID = ID;
    }
    
     /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash() {
        try {
            if(event == EventType.Rename){
                this.hash = getChecksum(NSyncClient.dir.toString() + "\\" + this.filePath + "\\" + this.newFileName, "MD5");
            } else {
                this.hash = getChecksum(NSyncClient.dir.toString() + "\\" + this.filePath + "\\" + this.fileName, "MD5");
            }
        } catch (Exception e){
            System.out.println("Exception during creating hash of the file (" + this.fileName + ")" + e.getMessage());
        }
    }
    
    /*
    Creating the hash for the file
    */
    public static String getChecksum(String filename, String algo) throws Exception {  
      byte[] b = createChecksum(filename, algo);  
      String result = "";  
      for (int i=0; i < b.length; i++) {  
           result +=  
                     Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );  
      }  
      return result;  
    }  
    
    public static byte[] createChecksum(String filename, String algo) throws Exception{  
      InputStream fis = new FileInputStream(filename);  
      byte[] buffer = new byte[1024];  
      MessageDigest complete = MessageDigest.getInstance(algo); //One of the following "SHA-1", "SHA-256", "SHA-384", and "SHA-512"  
      int numRead;  
      do {  
           numRead = fis.read(buffer);  
           if (numRead > 0) {  
                complete.update(buffer, 0, numRead);  
           }  
      } while (numRead != -1);  
      fis.close();  
      return complete.digest();  
 }  
    
}
