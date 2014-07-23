/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controller;

import FolderWatcher.FolderWatcher;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Ali
 */
public class NSyncClient {
    public static BlockingQueue<SendObject> toSendQ;
    public static BlockingQueue<SendObject> sentQ;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // creating the queues:
        toSendQ = new LinkedBlockingQueue<SendObject>();
        sentQ = new LinkedBlockingQueue<SendObject>();
        //creating new folderWatcher, DBManager, and Communication classes
        try {
            new FolderWatcher();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
}
