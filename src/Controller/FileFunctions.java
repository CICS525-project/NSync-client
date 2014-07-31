package Controller;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FileFunctions {

    public static boolean isDirectory(String fullPath) {
        File myFile = new File(fullPath);
        if (myFile.isDirectory()) {
            return true;
        }
        return false;
    }

    public static String getLastModifiedDate(String fullPath) {
        File myFile = new File(fullPath);
        Timestamp stamp = new Timestamp(myFile.lastModified());
        Date date = new Date(stamp.getTime());
        return date.toString();
    }

    public static String getRelativePath(String fullPath) {
        return fullPath.substring((System.getProperty("user.home") + "/NSync/").length());
    }

    public static String getExtension(String fullPath) {
        String extension = "";
        fullPath = getRelativePath(fullPath);
        int i = fullPath.indexOf('.');
        int p = Math.max(fullPath.lastIndexOf('/'), fullPath.lastIndexOf('\\'));

        if (i > p) {
            extension = fullPath.substring(i + 1);
        }
        return extension;
    }

    public static String convertTimeToUTC(Date localTime) {
        String format = "yyyy/MM/dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        // Convert Local Time to UTC (Works Fine)
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gmtTime = new Date(sdf.format(localTime));
        System.out.println("Local:" + localTime.toString() + "," + localTime.getTime() + " --> UTC time:"
                + gmtTime.toString() + "," + gmtTime.getTime());
        return gmtTime.toString();
    }
    
    public static Date convertUTCToLocal(String utcTime) {
        String format = "yyyy/MM/dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        // Convert Local Time to UTC (Works Fine)
        sdf.setTimeZone(TimeZone.getDefault());
        Date gmtTime = new Date(new Date(utcTime).getTime() + TimeZone.getDefault().getOffset(new Date().getTime()));
        //Date gmtTime = new Date(sdf.format(new Date(utcTime)));
        System.out.println("UTC:" + utcTime + " --> Local time:"
                + gmtTime.toString() + "," + gmtTime.getTime());
        System.out.println("The local time is " + gmtTime.toString());
        return gmtTime;
    }

    
    public static Date convertTimestampToDate(long timestamp) {
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(stamp.getTime());
        //System.out.println(date);
        return date;
    }

    public static boolean checkIfDownload(File localFile, Date serverFileDate) {       
        String utcDate = convertTimeToUTC(convertTimestampToDate(localFile.lastModified()));
        if (new Date(utcDate).before(serverFileDate)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static void main(String[] args) {
    	System.out.println(FileFunctions.convertUTCToLocal("Thu Jul 24 00:56:05 PDT 2014").toString());
    	System.out.println(TimeZone.getDefault());
    }
}
