/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FolderWatcher;

import Controller.NSyncClient;
import Controller.SendObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 *
 * @author Ali
 */
public class FolderWatcher {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final Path dir = NSyncClient.dir;
    //private static BlockingQueue<SendObject> toSendQ = NSyncClient.toSendQ;
    private static BlockingQueue<SendObject> toSendQ = new LinkedBlockingQueue<SendObject>();

    public FolderWatcher() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();

        this.registerAllFolders(dir);
        this.eventHandler();

    }

    private void register(Path directory) throws IOException {
        WatchKey key = directory.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, directory);
    }

    private void registerAllFolders(Path directory) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void eventHandler() throws IOException {
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                return;
            }

            Path directory = keys.get(key);
            System.out.println("path:" + directory.toString());
            if (directory == null) {
                System.err.println("WatchKey not recognized! Event will be ignored.");
                continue;
            }

            int i = 0;
            List<WatchEvent<?>> oneElementsEvents = key.pollEvents();

            //checking if the event is a rename
            if (isRename(oneElementsEvents)) {
                WatchEvent<?> event0 = oneElementsEvents.get(0);
                WatchEvent<Path> ev0 = (WatchEvent<Path>) event0;
                Path fileNamePath0 = ev0.context();
                Path dirRelativePath0 = null;
                if (directory.isAbsolute()) {
                    dirRelativePath0 = dir.relativize(directory);
                } else {
                    dirRelativePath0 = directory;
                }
                
                //the new file name
                WatchEvent<?> event1 = oneElementsEvents.get(1);
                WatchEvent<Path> ev1 = (WatchEvent<Path>) event1;
                Path fileNamePath1 = ev1.context();
                
                
                SendObject sendObject = new SendObject(fileNamePath0.getFileName().toString(), dirRelativePath0.toString(),
                        SendObject.EventType.Rename, fileLastModified(directory.toString() + "\\" + fileNamePath1.getFileName()),
                        isADirectory(directory.toString() + "\\" + fileNamePath1.getFileName()), 
                        fileNamePath1.getFileName().toString());

                toSendQ.add(sendObject);
                System.out.println("\033[34mSENDOBJECT(Rename) CREATED AND ADDED TO THE QUEUE\n");
            } else {

                for (WatchEvent<?> event : oneElementsEvents) {
                    //getting the event kind
                    WatchEvent.Kind<?> kind = event.kind();

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileNamePath = ev.context();
                    Path dirRelativePath = null;
                    if (directory.isAbsolute()) {
                        dirRelativePath = dir.relativize(directory);
                    } else {
                        dirRelativePath = directory;
                    }

                    if (kind == ENTRY_CREATE) {
                        if (Files.isDirectory(directory, NOFOLLOW_LINKS)) {
                            this.registerAllFolders(directory);
                        }
                    }

                    //getting the current timestamp
                    java.util.Date date = new java.util.Date();
                    java.sql.Timestamp currTimeStamp = new java.sql.Timestamp(date.getTime());
                    System.out.println(currTimeStamp);

                    String relativePathWithFileName = dirRelativePath + "\\" + fileNamePath.getFileName();
                    System.out.println(i);
                    System.out.println("event type: " + kind.name());
                    System.out.println("relative path: " + relativePathWithFileName);
                    System.out.println("element name: " + fileNamePath.getFileName() + "\n");
                    
                    /*This if statement gets rid of all the useless Modify events. Modify is useful just if it comes
                    as the first event of the series, and if it is on a file and not a folder.
                    */
                    
                    if((kind == ENTRY_MODIFY) && ((i > 0) || isADirectory(directory.toString() + "\\" + fileNamePath.getFileName()))) {
                        break;
                    }
                    SendObject.EventType eventType;

                    //creating the SendObject
                    switch (kind.name().toString()) {
                        case "ENTRY_CREATE":
                            eventType = SendObject.EventType.Create;
                            break;
                        case "ENTRY_DELETE":
                            eventType = SendObject.EventType.Delete;
                            break;
                        default:
                            eventType = SendObject.EventType.Modify;
                    }

                    SendObject sendObject = new SendObject(fileNamePath.getFileName().toString(), dirRelativePath.toString(),
                            eventType, fileLastModified(directory.toString() + "\\" + fileNamePath.getFileName()),
                            isADirectory(directory.toString() + "\\" + fileNamePath.getFileName()), null);

                    toSendQ.add(sendObject);
                    System.out.println("\033[34mSENDOBJECT(" + kind.name() + ") CREATED AND ADDED TO THE QUEUE\n");
                    //increasing i
                    i++;
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }

        }
    }

    /*checks to see if the size of the events on one elements is equal or more than two
     and also if the first event is a delete and second one is a create, in which case the whole set
     represents rename of a file/folder.
     */
    private boolean isRename(List<WatchEvent<?>> oneElementsEvents) {
        if (oneElementsEvents.size() >= 2) {
            return (oneElementsEvents.get(0).kind() == ENTRY_DELETE) && (oneElementsEvents.get(1).kind() == ENTRY_CREATE);
        }
        return false;
    }

    private Date fileLastModified(String absolutePath) {
        File file = new File(absolutePath);
        return new Date(file.lastModified());
    }

    private boolean isADirectory(String elementPath) {
        Path elementPathInFormat = Paths.get(elementPath);
        return Files.isDirectory(elementPathInFormat, NOFOLLOW_LINKS);

    }

    public static void main(String[] args) {
        try {
            new FolderWatcher();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
