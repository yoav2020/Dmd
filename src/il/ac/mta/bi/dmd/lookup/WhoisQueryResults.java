package il.ac.mta.bi.dmd.lookup;


public class WhoisQueryResults {
	private String asNum;
	private String ipAddr;
	private String bgpPrefix;
	private String cc;
	private String asOnwer;
	
	public String getAsNum() {
		return asNum;
	}
	public String getAsOnwer() {
		return asOnwer;
	}
	public void setAsNum(String asNum) {
		this.asNum = asNum;
	}
	public void setAsOnwer(String asOnwer) {
		this.asOnwer = asOnwer;
	}
	public String getBgpPrefix() {
		return bgpPrefix;
	}
	public String getCc() {
		return cc;
	}
	public String getIpAddr() {
		return ipAddr;
	}
	public void setBgpPrefix(String bgpPrefix) {
		this.bgpPrefix = bgpPrefix;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	
}
