package foxu.cross.war;

import mustang.io.ByteBuffer;


/**
 * һ�֣����ظ�һ�Σ�
 * @author yw
 *
 */
public class CrossWarRoundSave
{
	/* static fields */
	/** ս�����ͣ�  PREԤ��  64����  32���� 16���� */
	public static int PRE=0,FIN64=1,FIN32=2,FIN16=3,FINAL=4;
	/** Ψһ ID */
	int id;
	/** ���սid */
	int warid;
	/** ս������*/
	int type;
	/** ����ʱ�� */
	int createtime;
	//��Ϊ���Ψһid
	/** ����id */
	int attackid;
	/** ����id */
	int defenceid;
	/** ʤ��id */
	int winid;
	//ԭ������Ψһid
	/** ����id */
	int attackpid;
	/** ���������� */
	String attackname;
	/** ���������������� */
	String aservername;
	/** �������� */
	String anational;
	/** ����ip */
	String attackip;
	/** �����ȼ�  */
	int attacklv;
	/** ����id */
	int defencepid;
	/** ���ط����� */
	String defencename;
	/** ���ط����������� */
	String dservername;
	/** �������� */
	String dnational;
	/** ����ip*/
	String defenceip;
	/** �����ȼ� */
	int defencelv;
	/** ս��1 */
	CrossWarFightSave s1;
	/** ս��2 */
	CrossWarFightSave s2;
	
	public ByteBuffer showBytesWrite(ByteBuffer data)
	{
		//todo
		data.writeInt(id);
//		//System.out.println("------id-------:"+id);
		data.writeInt(warid);
		data.writeByte(type);
		data.writeInt(createtime);
		data.writeInt(attackid);
		data.writeInt(defenceid);
		data.writeInt(winid);
		data.writeInt(attackpid);
		data.writeUTF(attackname);
		data.writeUTF(aservername);
		data.writeUTF(attackip);
		data.writeUTF(anational);
		
		data.writeInt(defencepid);
		data.writeUTF(defencename);
		data.writeUTF(dservername);
		data.writeUTF(defenceip);
		data.writeUTF(dnational);
		
		data.writeShort(attacklv);
		data.writeShort(defencelv);
		s1.showBytesWrite(data);
		s2.showBytesWrite(data);
		return data;
	}
	
	public ByteBuffer showBytesRead(ByteBuffer data)
	{
		// todo
		id=data.readInt();
		warid=data.readInt();
		type=data.readUnsignedByte();
//		//System.out.println(id+":---id-showBytesRead------type---:"+type);
		createtime=data.readInt();
		attackid=data.readInt();
		defenceid=data.readInt();
		
		winid=data.readInt();
		
		attackpid=data.readInt();
		attackname=data.readUTF();
		aservername=data.readUTF();
		attackip=data.readUTF();
		anational=data.readUTF();
		
		defencepid=data.readInt();
		defencename=data.readUTF();
		dservername=data.readUTF();
		defenceip=data.readUTF();
		dnational=data.readUTF();
		
		attacklv=data.readUnsignedShort();
		defencelv=data.readUnsignedShort();
//		//System.out.println("------showBytesReadWrite---1--------");
		s1=new CrossWarFightSave();
		s1.showBytesRead(data);
		s2=new CrossWarFightSave();
		s2.showBytesRead(data);
//		//System.out.println("------showBytesReadWrite---2--------");
//		if(attackpid==167819162||defencepid==167819162)
//		{
//			//System.out.println("-----showBytesRead------167819162---------");
//		}
		return data;
	}
	
	public void clientBytesWrite(int pid,ByteBuffer data,boolean isfinal)
	{
		data.writeInt(this.id);
		data.writeInt(createtime);
//		//System.out.println(attackname+":------attackname-------:"+defencename);
		if(pid!=defencepid||isfinal)
		{
//			//System.out.println("---------clientBytesWrite---0-------");
			data.writeByte(winid==attackid?1:2);
			data.writeBoolean(s1.isAttackWin());
			data.writeBoolean(!s2.isAttackWin());
			data.writeUTF(attackname);
			data.writeUTF(aservername);
			data.writeUTF(anational);//����
			data.writeUTF(defencename);
			data.writeUTF(dservername);
			data.writeUTF(dnational);
			data.writeShort(s1.getAttackLose()+s2.getDefenceLose());
			data.writeShort(s1.getDefenceLose()+s2.getAttackLose());
			//System.out.println("-----getLosePercent(true)---------:"+getLosePercent(true));
			data.writeFloat(getLosePercent(true));
			//System.out.println("-----getLosePercent(false)---------:"+getLosePercent(false));
			data.writeFloat(getLosePercent(false));
		}
		else
		{
//			//System.out.println("---------clientBytesWrite---1-------");
			data.writeByte(winid==defenceid?1:2);
			data.writeBoolean(s2.isAttackWin());
			data.writeBoolean(!s1.isAttackWin());
			data.writeUTF(defencename);
			data.writeUTF(dservername);
			data.writeUTF(dnational);//����
			data.writeUTF(attackname);
			data.writeUTF(aservername);
			data.writeUTF(anational);//����
			data.writeShort(s1.getDefenceLose()+s2.getAttackLose());
			data.writeShort(s1.getAttackLose()+s2.getDefenceLose());
			//System.out.println("-----getLosePercent(false)---------:"+getLosePercent(false));
			data.writeFloat(getLosePercent(false));
			//System.out.println("-----getLosePercent(true)---------:"+getLosePercent(true));
			data.writeFloat(getLosePercent(true));
		}
	}
	/** Ĭ��ս�� */ //��Ҫ��������ʵ���л���ʽһ��
	public static void defClientRepWrite(ByteBuffer data)
	{
		data.writeInt(0);
		data.writeInt(0);
		data.writeByte(0);
		data.writeBoolean(true);
		data.writeBoolean(true);
		data.writeUTF(null);
		data.writeUTF(null);
		data.writeUTF(null);
		data.writeUTF(null);
		data.writeUTF(null);
		data.writeUTF(null);
		data.writeShort(0);
		data.writeShort(0);
		data.writeFloat(0f);
		data.writeFloat(0f);
	}
	
	/**
	 * ����ս���
	 * @param isattack �Ƿ��ǹ�����
	 */
	public float getLosePercent(boolean isattack)
	{
		float lp=0f;
		if(isattack)
		{
			lp+=s1.getLosePercent(true);
			lp+=s2.getLosePercent(false);
		}
		else
		{
			lp+=s1.getLosePercent(false);
			lp+=s2.getLosePercent(true);
		}
//		//System.out.println("-----lp-----:"+lp);
		lp=lp/2;
//		//System.out.println("-----lp/2-----:"+lp);
		return (Math.round(lp*10000)/10000f);
	}
	
	/** �жϸ�ս���Ƿ�����ĳ��� */
	public boolean belong(int id)
	{
		return id==attackpid||id==defencepid;
	}
	
	/** дս��1 */
	public void bytesWriteS1(ByteBuffer data)
	{
		s1.showBytesWrite(data);
	}
	/** ��ս��1 */
	public void bytesReadS1(ByteBuffer data)
	{
		s1=new CrossWarFightSave();
		s1.showBytesRead(data);
	}
	/** дս��2 */
	public void bytesWriteS2(ByteBuffer data)
	{
		s2.showBytesWrite(data);
	}
	/** ��ս��2 */
	public void bytesReadS2(ByteBuffer data)
	{
		s2=new CrossWarFightSave();
		s2.showBytesRead(data);
	}
	
	public int getAttacklv()
	{
		return attacklv;
	}

	
	public void setAttacklv(int attacklv)
	{
		this.attacklv=attacklv;
	}

	
	public int getDefencelv()
	{
		return defencelv;
	}

	
	public void setDefencelv(int defencelv)
	{
		this.defencelv=defencelv;
	}

	public CrossWarFightSave getS1()
	{
		return s1;
	}
	
	public void setS1(CrossWarFightSave s1)
	{
		this.s1=s1;
	}
	
	public CrossWarFightSave getS2()
	{
		return s2;
	}
	
	public void setS2(CrossWarFightSave s2)
	{
		this.s2=s2;
	}

	
	public int getAttackid()
	{
		return attackid;
	}

	
	public void setAttackid(int attackid)
	{
		this.attackid=attackid;
	}

	
	public int getDefenceid()
	{
		return defenceid;
	}

	
	public void setDefenceid(int defenceid)
	{
		this.defenceid=defenceid;
	}

	
	public int getWinid()
	{
		return winid;
	}

	
	public void setWinid(int winid)
	{
		this.winid=winid;
	}

	
//	public boolean isSend()
//	{
//		return send;
//	}
//
//	
//	public void setSend(boolean send)
//	{
//		this.send=send;
//	}
	
	
	public int getId()
	{
		return id;
	}

	
	public void setId(int id)
	{
		this.id=id;
	}

	
	public int getType()
	{
		return type;
	}

	
	public void setType(int type)
	{
		this.type=type;
	}

	
//	public boolean isIsattack()
//	{
//		return isattack;
//	}
//
//	
//	public void setIsattack(boolean isattack)
//	{
//		this.isattack=isattack;
//	}

	
	public int getCreatetime()
	{
		return createtime;
	}

	
	public void setCreatetime(int createtime)
	{
		this.createtime=createtime;
	}

	
	public int getAttackpid()
	{
		return attackpid;
	}

	
	public void setAttackpid(int attackpid)
	{
		this.attackpid=attackpid;
	}

	
	public int getDefencepid()
	{
		return defencepid;
	}

	
	public void setDefencepid(int defencepid)
	{
		this.defencepid=defencepid;
	}

	
	public String getAttackname()
	{
		return attackname;
	}

	
	public void setAttackname(String attackname)
	{
		this.attackname=attackname;
	}

	
	public String getAservername()
	{
		return aservername;
	}

	
	public void setAservername(String aservername)
	{
		this.aservername=aservername;
	}

	
	public String getDefencename()
	{
		return defencename;
	}

	
	public void setDefencename(String defencename)
	{
		this.defencename=defencename;
	}

	
	public String getDservername()
	{
		return dservername;
	}

	
	public void setDservername(String dservername)
	{
		this.dservername=dservername;
	}

	
	public String getAttackip()
	{
		return attackip;
	}

	
	public void setAttackip(String attackip)
	{
		this.attackip=attackip;
	}

	
	public String getDefenceip()
	{
		return defenceip;
	}

	
	public void setDefenceip(String defenceip)
	{
		this.defenceip=defenceip;
	}

	
	public int getWarid()
	{
		return warid;
	}

	
	public void setWarid(int warid)
	{
		this.warid=warid;
	}

	
	public String getAnational()
	{
		return anational;
	}

	
	public void setAnational(String anational)
	{
		this.anational=anational;
	}

	
	public String getDnational()
	{
		return dnational;
	}

	
	public void setDnational(String dnational)
	{
		this.dnational=dnational;
	}
	
	
}
