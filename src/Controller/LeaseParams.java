
package Controller;

import java.io.Serializable;

public class LeaseParams implements Serializable{
    private String server1Lease;
    private String server2Lease;
    private String server3Lease;
    private boolean leaseGranted;
    public static final int serverId = 0;

    private static final long serialVersionUID = 1L;
    
    /**
     * @return the server1Lease
     */
    public LeaseParams() {
        
    }
    
    public LeaseParams(String s, String s2, String s3, boolean b) {
        server1Lease = s;
        server2Lease = s2;
        server2Lease = s3;
        leaseGranted = b;
    }
    
    public String getServer1Lease() {
        return server1Lease;
    }

    /**
     * @param server1Lease the server1Lease to set
     */
    public void setServer1Lease(String server1Lease) {
        this.server1Lease = server1Lease;
    }

    /**
     * @return the server2Lease
     */
    public String getServer2Lease() {
        return server2Lease;
    }

    /**
     * @param server2Lease the server2Lease to set
     */
    public void setServer2Lease(String server2Lease) {
        this.server2Lease = server2Lease;
    }

    /**
     * @return the server3Lease
     */
    public String getServer3Lease() {
        return server3Lease;
    }

    /**
     * @param server3Lease the server3Lease to set
     */
    public void setServer3Lease(String server3Lease) {
        this.server3Lease = server3Lease;
    }

    /**
     * @return the leaseGranted
     */
    public boolean isLeaseGranted() {
        return leaseGranted;
    }

    /**
     * @param leaseGranted the leaseGranted to set
     */
    public void setLeaseGranted(boolean leaseGranted) {
        this.leaseGranted = leaseGranted;
    }
    
    
}
