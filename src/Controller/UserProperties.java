package Controller;

public class UserProperties {
    public static String userID;
    private static String username;   
    private static String directory = System.getProperty("user.home") + "\\NSync\\";
    private static String queueName;
    private static String password;

    public static String getDirectory() {
        return directory;
    }
    
    public static String getUsername() {
        return username;
    }

    public static void setUsername(String usernamet) {
        username = usernamet;
    }

    public static String getUserId() {
        return userID;
    }

    public static void setUserId(String userIdt) {
        userID = userIdt;
    }

	/**
	 * @return the queueName
	 */
	public static String getQueueName() {
		return queueName;
	}

	/**
	 * @param queueName the queueName to set
	 */
	public static void setQueueName(String queueName) {
		UserProperties.queueName = queueName;
	}

	/**
	 * @return the password
	 */
	public static String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public static void setPassword(String password) {
		UserProperties.password = password;
	}
    
    
}
