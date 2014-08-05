package Communication;

import Communication.CommunicationManager;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.windowsazure.services.blob.models.AccessCondition;

import Controller.FileFunctions;
import Controller.UserProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlobManager {
	// add the username to this string instead of default

	private static String containerName = "yanki";// UserProperties.getUsername().trim();
	private static String url = CommunicationManager.getURL() + containerName
			+ "/";

	// remember to set container name back to user if you have to change it for
	// any reason
	public static void setContainerName(String newContainerName) {
		containerName = newContainerName;
	}

	public synchronized static void createContainter(String containerName) {
		containerName = containerName.toLowerCase();
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			container.createIfNotExists();
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public static void uploadFileAsBlob(String fullPath) {

		FileInputStream fis = null;
		CloudBlockBlob blob = null;
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			blob = container.getBlockBlobReference(FileFunctions
					.getRelativePath(fullPath));
			File source = new File(fullPath);
			if (source.exists()) {
				fis = new FileInputStream(source);
				HashMap<String, String> meta = new HashMap<String, String>();
				meta.put("dateModified", FileFunctions
						.convertTimeToUTC(FileFunctions
								.convertTimestampToDate(source.lastModified())));
				blob.setMetadata(meta);
				if (!source.isHidden()) {
					System.out.println(source.getName()
							+ " is not up to date so it is uploaded");
					blob.upload(fis, source.length());
				}
				fis.close();
				blob.breakLease(0);
			}
		} catch (URISyntaxException | InvalidKeyException | StorageException
				| IOException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
			if (fis != null) {
				try {
					fis.close();
					blob.breakLease(0);
				} catch (IOException | StorageException e) {
					e.printStackTrace();
				}
			}
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static ArrayList<String> getBlobsList(String startsWith) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			EnumSet<BlobListingDetails> details = EnumSet
					.of(BlobListingDetails.METADATA);
			for (ListBlobItem blobItem : container.listBlobs(startsWith, true,
					details, null, null)) {
				// System.out.println(blobItem.getUri().toString()
				// .substring(url.length() - 1));
				// CloudBlob b = (CloudBlob) blobItem;
				// b.acquireLease(60, "dddddddddddddddddddddddddddddddd");
				list.add(blobItem.getUri().toString().substring(url.length()));
			}
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return list;
	}

	public synchronized static void downloadAllBlobs() {
		// CommunicationManager.watchFolder = false;
		String filePath = UserProperties.getDirectory();
		System.out.println("The conn string is  " + filePath);
		FileOutputStream fos = null;
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			EnumSet<BlobListingDetails> details = EnumSet
					.of(BlobListingDetails.METADATA);
			for (ListBlobItem blobItem : container.listBlobs("", true, details,
					null, null)) {
				if (blobItem.getUri().toString().length() > 0) {
					CloudBlob blob = (CloudBlob) blobItem;
					blob.downloadAttributes();
					File yourFile = new File(filePath + blob.getName());
					if (!yourFile.exists()) {
						yourFile.getParentFile().mkdirs();
					}
					if (yourFile.exists()) {
						System.out.println(yourFile.getName() + " does exist");
						fos = new FileOutputStream(filePath + blob.getName());
						blob.download(fos);
						fos.close();
					} else {
						System.out
								.println(yourFile.getName()
										+ "File does not exist. Downloading from server");
						fos = new FileOutputStream(filePath + blob.getName());
						blob.download(fos);
						fos.close();
					}
				}
			}
		} catch (URISyntaxException | InvalidKeyException | StorageException
				| IOException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// CommunicationManager.watchFolder = true;
	}

	public static void downloadBlob(String fullPath) {
		String filePath = UserProperties.getDirectory();
		String blobUri = fullPath.substring(filePath.length());
		System.out.println("The blob uri is " + blobUri);
		CloudBlob blob = null;
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			blob = container.getBlockBlobReference(blobUri);
			blob.downloadAttributes();
			File yourFile = new File(filePath + blob.getName());
			if (!yourFile.exists()) {
				yourFile.getParentFile().mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(filePath
					+ blob.getName());
			blob.download(fos);
			blob.breakLease(0);
			fos.close();
		} catch (URISyntaxException | InvalidKeyException | StorageException
				| IOException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
			try {
				blob.breakLease(0);
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized static void deleteBlob(String fullPath) {
		String blobName = FileFunctions.getRelativePath(fullPath);
		if (blobName.contains("\\")) {
			blobName = blobName.replace("\\", "/");
		}
		System.out.println("Blob is " + blobName);
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			EnumSet<BlobListingDetails> details = EnumSet
					.of(BlobListingDetails.METADATA);
			System.out.println("Blob name is " + blobName);
			for (ListBlobItem blobItem : container.listBlobs(blobName, true,
					details, null, null)) {

				CloudBlob blob = (CloudBlob) blobItem;
				System.out.println("Blob name found is " + blob.getName());
				blob.delete();
			}
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public synchronized static void deleteBlobContainer() {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager.getStorageConnectionString());

			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			container.delete();
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public synchronized static void renameBlob(String newName, String oldName) {
		File file = new File(oldName);
		if (file.isDirectory()) {
			System.out.println("Blob is a directory");
			renameBlobDir(oldName, newName);
		}

		if (file.isFile()) {
			System.out.println("Blob is a file");
			renameSingleBlob(oldName, newName);
		}

	}

	private static void renameSingleBlob(String oldName, String newName) {
		oldName = FileFunctions.getRelativePath(oldName);
		System.out.println("The oldname is " + oldName);
		newName = FileFunctions.getRelativePath(newName);
		System.out.println("The new name is " + newName);
		CloudBlob oldBlob = null;
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			System.out.println("The old path is " + url + oldName
					+ " and the new path is " + url + newName);
			oldBlob = container.getBlockBlobReference(oldName);
			CloudBlob newBlob = container.getBlockBlobReference(newName);
			/*
			 * File f = null; if (!newBlob.exists()) { String path =
			 * System.getProperty("user.home") + "\\Desktop" + "\\p.txt";
			 * System.out.println("The path is " + path); f = new File(path); if
			 * (!f.exists()) { f.createNewFile(); }
			 * newBlob.uploadFromFile(path); }
			 */
			// newBlob.startCopyFromBlob(oldBlob);
			// oldBlob.delete();
			// f.delete();

			String path = System.getProperty("user.dir").replaceAll("\\", "/")
					+ "/" + oldBlob.getName();
			File f = new File(path);
			if (!f.exists()) {
				f.createNewFile();
			}
			oldBlob.downloadToFile(path);
			newBlob.uploadFromFile(path);// .startCopyFromBlob(oldBlob);
			oldBlob.breakLease(0);
			oldBlob.delete();
			f.delete();

		} catch (URISyntaxException | InvalidKeyException | StorageException
				| IOException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
			try {
				oldBlob.breakLease(0);
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void renameBlobDir(String oldName, String newName) {
		oldName = FileFunctions.getRelativePath(oldName);
		System.out.println("The oldname is " + oldName);
		newName = FileFunctions.getRelativePath(newName);
		System.out.println("The new name is " + newName);
		CloudBlob oldBlob = null;
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			EnumSet<BlobListingDetails> details = EnumSet
					.of(BlobListingDetails.METADATA);
			for (ListBlobItem blobItem : container.listBlobs(oldName, true,
					details, null, null)) {
				CloudBlob blob = (CloudBlob) blobItem;
				String oName = blob.getName();
				String nName = newName + oName.substring(oldName.length());
				System.out.println("New name is " + nName);
				CloudBlob newBlob = container.getBlockBlobReference(nName);
				oldBlob = container.getBlockBlobReference(oName);
				System.out.println("The blob names are " + blob.getName());
				// System.out.println("The copy status is " +
				// blob.getCopyState());
				// newBlob.startCopyFromBlob(oldBlob);
				// oldBlob.delete();
				String path = System.getProperty("user.dir").replaceAll("\\",
						"/")
						+ "/" + oldBlob.getName();
				File f = new File(path);
				if (!f.exists()) {
					f.createNewFile();
				}
				oldBlob.downloadToFile(path);
				newBlob.uploadFromFile(path);// .startCopyFromBlob(oldBlob);
				oldBlob.breakLease(0);
				oldBlob.delete();
				f.delete();

			}
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
			System.out.println("The message of the exception is "
					+ ex.getMessage());
			try {
				oldBlob.breakLease(0);
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String acquireLease(String blobName, String containerName,
			int serverId) {
		CloudBlob b = null;
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(CommunicationManager
							.getStorageConnectionString(serverId));

			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			b = container.getBlockBlobReference(blobName);

			if (b.exists()) {
				String leaseID = b.acquireLease(null, generateLeaseId());
				// b.breakLease(0);
				return leaseID;
			} else {
				return "BlobDoesNotExist";
			}
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			ex.printStackTrace();
			try {
				b.breakLease(0);
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	private static String generateLeaseId() {
		String uuid = UUID.randomUUID().toString();
		// System.out.println("uuid = " + uuid);
		return uuid;
	}
	public static void main(String[] args) {
		String l = acquireLease("ali.JPG", "yanki", 2);
		System.out.println("Lease is " + l);

	} 
}
