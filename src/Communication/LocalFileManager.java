package Communication;

import java.io.File;

import Controller.SendObject;
import Controller.UserProperties;

public class LocalFileManager {

	public static void download(SendObject s) {
		String filePath = UserProperties.getDirectory()
				+ pathParser(s.getFilePath()) + s.getFileName();
		BlobManager.downloadBlob(filePath);
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

		String oldName = UserProperties.getDirectory()
				+ pathParser(s.getFilePath()) + s.getFileName();
		System.out.println("Old name is " + oldName);
		String newName = UserProperties.getDirectory()
				+ pathParser(s.getFilePath()) + s.getNewFileName();
		System.out.println("Old name is " + newName);
		File oldname = new File(oldName);
		File newname = new File(newName);
		oldname.renameTo(newname);
	}

	private static String pathParser(String s) {
		if (s == null) {
			return "";
		} else {
			return s;
		}
	}

	/*
	 * public static void main(String[] args) { SendObject s = new SendObject();
	 * s.setFileName("poos.mp3"); //s.setNewFileName("poos.mp3"); delete(s);
	 * //rename(s); }
	 */
}
