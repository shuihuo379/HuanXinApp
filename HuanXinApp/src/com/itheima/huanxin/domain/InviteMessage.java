package com.itheima.huanxin.domain;

public class InviteMessage {
	private String from;
	//时间
	private long time;
	//添加理由
	private String reason;
	
	//未验证，已同意等状态
	private InviteMessageStatus status;
	//群id
	private String groupId;
	//群名称
	private String groupName;
	

	private int id;
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}


	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public InviteMessageStatus getStatus() {
		return status;
	}

	public void setStatus(InviteMessageStatus status) {
		this.status = status;
	}

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public enum InviteMessageStatus{
		/**被邀请*/
		BEINVITEED,
		/**被拒绝*/
		BEREFUSED,
		/**对方同意*/
		BEAGREED,
		/**对方申请*/
		BEAPPLYED,
		/**我同意了对方的请求*/
		AGREED,
		/**我拒绝了对方的请求*/
		REFUSED
		
	}
}
