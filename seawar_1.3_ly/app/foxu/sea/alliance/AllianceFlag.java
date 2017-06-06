package foxu.sea.alliance;

import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.util.Sample;
import foxu.sea.PublicConst;

/***
 * ��������
 * 
 * @author lihon
 *
 */
public class AllianceFlag
{

	public static int FLAG_COST_GEMS=1000;
	/** ���� 0 ͼ�� 1 ��ɫ 2 ���� */
	int[] allianceFlag=new int[Flag.ALLIANCEFLAG];
	/** ��Ѵ��� **/
	int freeTime=1;

	/** ��������������� **/
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

	/**���л���ǰ̨**/
	public void showBytesWriteFlagsInfo(ByteBuffer data)
	{
		data.writeByte(freeTime);	
		showBytesWriteAllianceFlag(data);
	}
	
	/** ���л���ǰ̨ **/
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

	/** �����Ѵ��� **/
	public boolean checkFreeTimes(int count)
	{
		if(count<=0) return false;
		return freeTime>=count;
	}

	/** ���Ӵ��� **/
	public void addFreeTime(int count)
	{
		if(count<=0) return;
		freeTime=count+freeTime;
		if(freeTime>Integer.MAX_VALUE)
		{
			freeTime=Integer.MAX_VALUE;
		}
	}

	/** ���ٴ��� **/
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
	/** д **/
	public void bytesWrite(ByteBuffer data)
	{
		data.writeByte(freeTime);
		data.writeShort(allianceFlag[0]);
		data.writeShort(allianceFlag[1]);
		data.writeShort(allianceFlag[2]);
	}
	/** �� **/
	public void bytesRead(ByteBuffer data)
	{
		freeTime=data.readUnsignedByte();
		allianceFlag[0]=data.readUnsignedShort();
		allianceFlag[1]=data.readUnsignedShort();
		allianceFlag[2]=data.readUnsignedShort();
	}

	/**��֤������������ԭ�����õ�������ͬ**/
	public String checkAllianceFlag(int image_sid,int clour_sid,int model_sid)
	{
		if(image_sid==allianceFlag[0]&&clour_sid==allianceFlag[1]
			&&model_sid==allianceFlag[2]) return "flag same";
		return null;
	}
	

	/**�����õ�ʱ�򱣴浱ǰ����������**/
	public String getAllianceFlagToString()
	{
		return allianceFlag[0]+"-"+allianceFlag[1]+"-"+allianceFlag[2]+freeTime;
	}
	
	/** �������� **/
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
