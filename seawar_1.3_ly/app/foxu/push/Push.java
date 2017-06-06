package foxu.push;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;


public class Push
{
	String pname;
	String content;
	int createAt;
	
	public Push(String content,String pname)
	{
		this.pname=pname;
		this.content=content;
		this.createAt=TimeKit.getSecondTime();
	}
	public void BytesWrite(ByteBuffer data)
	{
		data.writeUTF(content);
	}
	
	public String getPname()
	{
		return pname;
	}
	
	public String getContent()
	{
		return content;
	}
	
}
