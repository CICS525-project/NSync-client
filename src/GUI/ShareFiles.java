package GUI;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import Controller.FileFunctions;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.SharedAccessPolicy;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

public class ShareFiles {	 

	public static void main(String[] args) throws InvalidKeyException, 
	     URISyntaxException, StorageException 
	  {
		  final String storageConnectionString = "DefaultEndpointsProtocol=http;"
					+ "AccountName=portalvhdsh8ghz0s9b7mx9;"
					+ "AccountKey=ThVIUXcwpsYqcx08mtIhRf6+XxvEuimK35/M65X+XlkdVCQNl4ViUiB/+tz/nq+eeZAEZCNrmFVQwwN3QiykvA==";
	//  final String storageConnectionString = creds.getstorageconnectionstring();
	  CloudStorageAccount storageAccount = 
	     CloudStorageAccount.parse(storageConnectionString);
	  CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
	  CloudBlobContainer container = blobClient.getContainerReference("yanki");
	// Generate shared access signature on blob
	// Define the start and end time to granting permissions.
	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	//cal.setTimeZone(TimeZone.getTimeZone("UTC"));
	 
	
	
	//cal.setTimeZone(TimeZone.setDefault("UTC");
	cal.add(Calendar.HOUR, 0);
	Date sharedAccessStartTime = new Date(FileFunctions.convertTimeToUTC(cal.getTime()));
	
	//cal.add(Calendar.MINUTE, 0);
	
	cal.add(Calendar.HOUR, 1);
	Date sharedAccessExpiryTime = new Date(FileFunctions.convertTimeToUTC(cal.getTime()));
	
	System.out.println(sharedAccessStartTime +  sharedAccessExpiryTime.toString());
	                                               
	// Define shared access policy
	SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
	EnumSet<SharedAccessBlobPermissions> perEnumSet = EnumSet.of(SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.READ);
	policy.setPermissions(perEnumSet);
	policy.setSharedAccessExpiryTime(sharedAccessExpiryTime);
	//policy.setSharedAccessStartTime(sharedAccessStartTime);
	CloudBlob blob = container.getBlockBlobReference("New Microsoft Excel Worksheet.xlsx");       
	//System.out.println(blob.generateSharedAccessSignature(policy, "myPolicy"));
	//Generating Shared Access Signature
        
	String sharedUri = blob.generateSharedAccessSignature(policy, "");
	//blob.
	blob.uploadProperties();
	
	//container.uploadPermissions(permissions);
	System.out.println(blob.getUri() + "?" + sharedUri);
	
	  }
}
