package foxu.sea.alliance;

import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.util.Sample;
import foxu.sea.PublicConst;

/***
 * 联盟旗帜
 * 
 * @author lihon
 *
 */
public class AllianceFlag
{

	public static int FLAG_COST_GEMS=1000;
	/** 旗帜 0 图案 1 颜色 2 造型 */
	int[] allianceFlag=new int[Flag.ALLIANCEFLAG];
	/** 免费次数 **/
	int freeTime=1;

	/** 随机设置联盟旗帜 **/
	public void randomAllianceFlag()
	{
		if(allianceFlag[2]==0||allianceFlag[1]==0||allianceFlag[0]==0)
		{
			int random1=MathKit.randomValue(0,PublicConst.IMAGE.length);
			int random2=MathKit.randomValue(0,PublicConst.COLOUR.length);
			int random3=MathKit.randomValue(0,PublicConst.MODEL.length);
			allianceFlag[0]=PublicConst.IMAGE[random1];
			allianceFlag[1]=PublicConst.COLOUR[random2];
			allianceFlag[2]=PublicConst.MODEL[random3];
		}
	}

	public void setAllianceFlag(int image_sid,int clour_sid,int model_sid)
	{
		allianceFlag[0]=image_sid;
		allianceFlag[1]=clour_sid;
		allianceFlag[2]=model_sid;
	}

	/**序列化给前台**/
	public void showBytesWriteFlagsInfo(ByteBuffer data)
	{
		data.writeByte(freeTime);	
		showBytesWriteAllianceFlag(data);
	}
	
	/** 序列化给前台 **/
	public void showBytesWriteAllianceFlag(ByteBuffer data)
	{
		randomAllianceFlag();
		data.writeShort(allianceFlag[0]);
		data.writeShort(allianceFlag[1]);
		data.writeShort(allianceFlag[2]);
	}

	public int[] getAllianceFlag()
	{
		return allianceFlag;
	}

	public void setAllianceFlag(int[] allianceFlag)
	{
		this.allianceFlag=allianceFlag;
	}

	/** 检测免费次数 **/
	public boolean checkFreeTimes(int count)
	{
		if(count<=0) return false;
		return freeTime>=count;
	}

	/** 增加次数 **/
	public void addFreeTime(int count)
	{
		if(count<=0) return;
		freeTime=count+freeTime;
		if(freeTime>Integer.MAX_VALUE)
		{
			freeTime=Integer.MAX_VALUE;
		}
	}

	/** 减少次数 **/
	public void reduceFreeTime(int count)
	{
		if(count<=0) return;
		freeTime=freeTime-count;
		if(freeTime<0) freeTime=0;
	}

	public int getFreeTime()
	{
		return freeTime;
	}

	public void setFreeTime(int freeTime)
	{
		this.freeTime=freeTime;
	}
	/** 写 **/
	public void bytesWrite(ByteBuffer data)
	{
		data.writeByte(freeTime);
		data.writeShort(allianceFlag[0]);
		data.writeShort(allianceFlag[1]);
		data.writeShort(allianceFlag[2]);
	}
	/** 读 **/
	public void bytesRead(ByteBuffer data)
	{
		freeTime=data.readUnsignedByte();
		allianceFlag[0]=data.readUnsignedShort();
		allianceFlag[1]=data.readUnsignedShort();
		allianceFlag[2]=data.readUnsignedShort();
	}

	/**验证联盟旗帜是与原来设置的旗帜相同**/
	public String checkAllianceFlag(int image_sid,int clour_sid,int model_sid)
	{
		if(image_sid==allianceFlag[0]&&clour_sid==allianceFlag[1]
			&&model_sid==allianceFlag[2]) return "flag same";
		return null;
	}
	

	/**在设置的时候保存当前的旗帜数据**/
	public String getAllianceFlagToString()
	{
		return allianceFlag[0]+"-"+allianceFlag[1]+"-"+allianceFlag[2]+freeTime;
	}
	
	/** 加载旗帜 **/
	public static void setGiveTheValue()
	{
		Sample[] samples=Flag.factory.getSamples();
		for(int i=0;i<samples.length;i++)
		{
			if(samples[i]==null) continue;
			Flag flag=(Flag)samples[i];
			if(flag==null) continue;
			if(flag.getType()==Flag.FLAGI_MAGE)
			{
				if(PublicConst.IMAGE==null)
				{
					PublicConst.IMAGE=new int[1];
					PublicConst.IMAGE[0]=flag.getSid();
				}
				else
				{
					int[] temp=new int[PublicConst.IMAGE.length+1];
					System.arraycopy(PublicConst.IMAGE,0,temp,0,
						PublicConst.IMAGE.length);
					PublicConst.IMAGE=temp;
					PublicConst.IMAGE[PublicConst.IMAGE.length-1]=flag
						.getSid();
				}
			}
			else if(flag.getType()==Flag.FLAG_COLOUR)
			{
				if(PublicConst.COLOUR==null)
				{
					PublicConst.COLOUR=new int[1];
					PublicConst.COLOUR[0]=flag.getSid();
				}
				else
				{
					int[] temp=new int[PublicConst.COLOUR.length+1];
					System.arraycopy(PublicConst.COLOUR,0,temp,0,
						PublicConst.COLOUR.length);
					PublicConst.COLOUR=temp;
					PublicConst.COLOUR[PublicConst.COLOUR.length-1]=flag
						.getSid();
				}
			}
			else if(flag.getType()==Flag.FLAG_MODEL)
			{
				if(PublicConst.MODEL==null)
				{
					PublicConst.MODEL=new int[1];
					PublicConst.MODEL[0]=flag.getSid();
				}
				else
				{
					int[] temp=new int[PublicConst.MODEL.length+1];
					System.arraycopy(PublicConst.MODEL,0,temp,0,
						PublicConst.MODEL.length);
					PublicConst.MODEL=temp;
					PublicConst.MODEL[PublicConst.MODEL.length-1]=flag
						.getSid();
				}
			}
		}
	}
}
