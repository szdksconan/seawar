package foxu.sea.head;

import mustang.io.ByteBuffer;

public class HeadData {
	private int sid;
	private boolean isEnabled;
	private boolean isUse;

	public HeadData(int sid, boolean isEnabled, boolean isUse) {
		this.sid = sid;
		this.isEnabled = isEnabled;
		this.isUse = isUse;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean isUse() {
		return isUse;
	}

	public void setUse(boolean isUse) {
		this.isUse = isUse;
	}

	public void writeTo(ByteBuffer buffer) {
		buffer.writeShort(sid);
		buffer.writeBoolean(isEnabled);
		buffer.writeBoolean(isUse);
	}

	@Override
	public String toString() {
		return sid + "," + isEnabled + "," + isUse + ";";
	}
}
