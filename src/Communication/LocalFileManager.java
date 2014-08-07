package Communication;

import Controller.SendObject;
import Controller.UserProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalFileManager {

	public static void download(SendObject s) {
		String filePath = UserProperties.getDirectory()
				+ pathParser(s.getFilePath()) + s.getFileName();
		System.out.println("File to be downloaded is "
				+ pathParser(s.getFilePath()) + s.getFileName());
		BlobManager.downloadBlob(filePath);
		File df = new File(filePath);		
		df.setLastModified(s.getTimeStamp().getTime());		
	}

	public static void delete(SendObject s) {
		String filePath = UserProperties.getDirectory()
				+ pathParser(s.getFilePath()) + s.getFileName();
		File f = new File(filePath);
		if (f.exists()) {
			f.delete();
		}
	}

	public static void rename(SendObject s) {
		
		File f = new File(UserProperties.getDirectory()
				+ pathParser(s.getFilePath()) + s.getFileName());
		if(!f.exists()) {
			BlobManager.downloadBlob(UserProperties.getDirectory()
					+ pathParser(s.getFilePath()) + s.getNewFileName());
			return;
		}

		String oldName = UserProperties.getDirectory()
				+ pathParser(s.getFilePath()) + s.getFileName();
		System.out.println("Old name is " + oldName);
		String newName = UserProperties.getDirectory()
				+ pathParser(s.getFilePath()) + s.getNewFileName();
		System.out.println("Old name is " + newName);
		File oldname = new File(oldName);
		File newname = new File(newName);
		oldname.renameTo(newname);

		File df = new File(newName);
		df.setLastModified(s.getTimeStamp().getTime());
	}

	/*
	 * receives a SendObject associated with a file in conflict and copies it
	 * with the new name given to it as a string in second parameter.
	 */

	public static void copyConflictedFile(SendObject sendObject, String newName)
			throws IOException {
		String filePath = UserProperties.getDirectory()
				+ pathParser(sendObject.getFilePath())
				+ sendObject.getFileName();
		String newFilePath = UserProperties.getDirectory()
				+ pathParser(sendObject.getFilePath()) + newName;
		Files.copy(Paths.get(filePath), Paths.get(newFilePath), COPY_ATTRIBUTES);

	}

	private static String pathParser(String s) {
		if (s == null) {
			return "";
		} else {
			return s;
		}
	}

	/*
	 * public static void main(String[] args){ SendObject sendObject = new
	 * SendObject("aaa.txt","",null, null, false, null); try {
	 * copyConflictedFile(sendObject, "bbb.txt"); } catch (IOException ex) {
	 * Logger.getLogger(LocalFileManager.class.getName()).log(Level.SEVERE,
	 * null, ex); } }
	 */
	/*
	 * public static void main(String[] args) { SendObject s = new SendObject();
	 * s.setFileName("poos.mp3"); //s.setNewFileName("poos.mp3"); delete(s);
	 * //rename(s); }
	 */
}
