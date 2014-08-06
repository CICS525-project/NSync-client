package GUI;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.TimeZone;

import Communication.CommunicationManager;
import Controller.FileFunctions;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

public class ShareFiles {

	public static String shareFiles(String containerName, String filename)
			throws InvalidKeyException, URISyntaxException, StorageException {
		final String storageConnectionString = CommunicationManager
				.getStorageConnectionString();
		CloudStorageAccount storageAccount = CloudStorageAccount
				.parse(storageConnectionString);
		CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
		CloudBlobContainer container = blobClient
				.getContainerReference(containerName);
		// Generate shared access signature on blob
		// Define the start and end time to granting permissions.
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		// cal.setTimeZone(TimeZone.getTimeZone("UTC"));

		// cal.setTimeZone(TimeZone.setDefault("UTC");
		cal.add(Calendar.HOUR, 0);
		Date sharedAccessStartTime = new Date(
				FileFunctions.convertTimeToUTC(cal.getTime()));

		// cal.add(Calendar.MINUTE, 0);

		cal.add(Calendar.HOUR, 1);
		Date sharedAccessExpiryTime = new Date(
				FileFunctions.convertTimeToUTC(cal.getTime()));

		System.out.println(sharedAccessStartTime
				+ sharedAccessExpiryTime.toString());

		// Define shared access policy
		SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
		EnumSet<SharedAccessBlobPermissions> perEnumSet = EnumSet.of(
				SharedAccessBlobPermissions.WRITE,
				SharedAccessBlobPermissions.READ);
		policy.setPermissions(perEnumSet);
		policy.setSharedAccessExpiryTime(sharedAccessExpiryTime);
		// policy.setSharedAccessStartTime(sharedAccessStartTime);
		CloudBlob blob = container.getBlockBlobReference(filename);
		// System.out.println(blob.generateSharedAccessSignature(policy,
		// "myPolicy"));
		// Generating Shared Access Signature
		if (blob.exists()) {
			String sharedUri = blob.generateSharedAccessSignature(policy, "");
			blob.uploadProperties();
			// container.uploadPermissions(permissions);
			System.out.println(blob.getUri() + "?" + sharedUri);
			return blob.getUri() + "?" + sharedUri;
		} else {
			return "Bad link";
		}
	}
}
