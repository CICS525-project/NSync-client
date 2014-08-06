package Controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendObject implements Serializable {

    private String fileName;
    private String newFileName;  //used just in case of a Rename event
    private String filePath;    //relative filePath
    private EventType event;
    private boolean enteredIntoDB;
    private Date timeStamp;
    private boolean isAFolder;
    private String hash;
    private String ID;      //this is the file ID
    private String userID;
    private String sharedWith;

    private static final long serialVersionUID = 1L;

    public enum EventType {

        Create, Delete, Rename, Modify, Share
    }

    public SendObject() {
        enteredIntoDB = false;
        sharedWith = null;
        this.setUserID(UserProperties.getUsername());
    }

    public SendObject(String fileName, String filePath, EventType event, Date timeStamp,
            boolean isAFolder, String newFileName) {
        this.fileName = fileName;
        this.newFileName = newFileName;
        this.filePath = filePath;
        this.event = event;
        this.timeStamp = timeStamp;
        this.isAFolder = isAFolder;
        this.enteredIntoDB = false;
        this.setUserID(UserProperties.userID);
        
        sharedWith = null;
        
        if ((event != EventType.Delete) && !isAFolder) {
            this.setHash();
        } else {
            this.hash = null;
        }
    }

    public SendObject(String fileID, String fileName, String filePath, EventType event, Date timeStamp,
            boolean isAFolder, String newFileName) {
        this.ID = fileID;
        this.fileName = fileName;
        this.newFileName = newFileName;
        this.filePath = filePath;
        this.event = event;
        this.timeStamp = timeStamp;
        this.isAFolder = isAFolder;
        this.enteredIntoDB = false;
        
        this.setUserID(UserProperties.userID);
        
        if ((event != EventType.Delete) && !isAFolder) {
            this.setHash();
        } else {
            this.hash = null;
        }
    }
    
    public SendObject(String fileID, String fileName, String filePath, EventType event, Date timeStamp,
            boolean isAFolder, String newFileName, String hash, String sharedWith) {
        this.ID = fileID;
        this.fileName = fileName;
        this.newFileName = newFileName;
        this.filePath = filePath;
        this.event = event;
        this.timeStamp = timeStamp;
        this.isAFolder = isAFolder;
        this.enteredIntoDB = false;
        this.hash = hash;
        this.sharedWith = sharedWith;
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
        	
            if (event == EventType.Rename) {
            	FileInputStream toBeHashed = new FileInputStream(NSyncClient.dir.toString() + "\\" + this.filePath + "\\" + this.newFileName);
                this.hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(toBeHashed); //getChecksum(NSyncClient.dir.toString() + "\\" + this.filePath + "\\" + this.newFileName, "MD5");
                toBeHashed.close();
            } else {
            	FileInputStream toBeHashed = new FileInputStream(NSyncClient.dir.toString() + "\\" + this.filePath + "\\" + this.fileName);
                this.hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(toBeHashed); //getChecksum(NSyncClient.dir.toString() + "\\" + this.filePath + "\\" + this.fileName, "MD5");
                toBeHashed.close();
            }
        } 
        catch(FileNotFoundException e){
            System.out.println("Exception during creating hash of the file (" + this.fileName + ")" + e.getMessage());
            
            //This part is for the large files which take sometime to copy. 
            //wait for 4 seconds
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SendObject.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (this.event != EventType.Delete)
                this.setHash();
        }
        catch (Exception e) {
            System.out.println("Exception during creating hash of the file (" + this.fileName + ")" + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    /*
     Creating the hash for the file
     */
    public static String getChecksum(String filename, String algo) throws Exception {
        byte[] b = createChecksum(filename, algo);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result
                    += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static byte[] createChecksum(String filename, String algo) throws Exception {
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

    /**
     * @return the userID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * @return the sharedWith
     */
    public String getSharedWith() {
        return sharedWith;
    }

    /**
     * @param sharedWith the sharedWith to set
     */
    public void setSharedWith(String sharedWith) {
        this.sharedWith = sharedWith;
    }
}
