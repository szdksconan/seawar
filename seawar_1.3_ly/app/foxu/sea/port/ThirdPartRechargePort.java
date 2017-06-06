package foxu.sea.port;

import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.Resources;
import foxu.sea.User;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.gems.GemsTrack;

/**
 * ��������ֵ�ӿ�
 * 
 * @author wangbing
 */
public class ThirdPartRechargePort implements HttpHandlerInterface
{
    private static Logger log = LogFactory.getLogger(ThirdPartRechargePort.class);

    public static final Base64 BASE64 = new Base64();

    // ��ȡ�����û�
    private static final String GET_USER = "get_character";

    // ��ֵ
    private static final String RECHARGE = "recharge";

    // ð��
    private static final String DATA = "data";

    // ���ַ���
    private static final String EMPTYSTRING = "";

    // ��ֵ�ɹ�
    private static final String SUCCESS = "ok";

    // ��ֵʧ��
    private static final String FAIL = "fail";
    
    // ���ݿ⻺��
    private CreatObjectFactory objectFactory;

    public String excuteString(HttpRequestMessage request, String ip)
    {
        try
        {
            String dataStr = request.getParameter(DATA);
            ByteBuffer data = load(dataStr);

            // ִ������
            String action = data.readUTF();

            // ����ָ���˻���ȡ�û�
            if (GET_USER.equals(action))
            {
                String account = data.readUTF();
                ByteBuffer result = new ByteBuffer();   
                // ��ѯ�û�
                String sql="select * from users where user_account='"
                    +account+"'";
                User[] users = objectFactory.getUserDBAccess().loadBySql(sql);
                //�������������user�����������
                if (users != null && users.length == 1)
                {
                    // ��ѯ����
                    sql="select * from players where user_id='"
                        +users[0].getId()+"'";
                    Player[] players = (Player[])objectFactory.getPlayerCache().getDbaccess().loadBySql(sql);
                    if (players != null)
                    {
                        result.writeInt(players.length);
                        for (Player player : players)
                        {
                            // ����д��id��name
                            result.writeInt(player.getId());
                            result.writeUTF(player.getName());
                        }
                    }
                    else
                    {
                    	log.error("sql="+sql);
                        result.writeInt(0);
                    }
                }
                else
                {
                	log.error("sql="+sql);
                    result.writeInt(0);
                }
                
                // ����󷵻�
                return createBase64(result);
            }
            else if (RECHARGE.equals(action))
            {
            	boolean isName=data.readBoolean();
            	if(isName)
            		return createBase64(rechargeByName(data));
            	else
            		return createBase64(recharge(data));
            }
            else
            {
                return EMPTYSTRING;
            }
        }
        catch (Exception e)
        {
            log.error(e);
            return EMPTYSTRING;
        }
    }

    private ByteBuffer recharge(ByteBuffer data)
    {
        String playerId = data.readUTF();
        String transId = data.readUTF();
        data.readInt();
        int gems = data.readInt();
        
        ByteBuffer result = new ByteBuffer();
        
        Player player = objectFactory.getPlayerById(toInt(playerId));
        if (player == null)
        {
            result.writeUTF(FAIL);
            return result;
        }
        // �������
        objectFactory.getPlayerByName(player.getName(),true);

        // ��ӱ�ʯ
        Resources.addGems(gems, player.getResources(), player);
        player.flushVIPlevel();
        // ��ʯ��־��¼
		objectFactory.createGemTrack(GemsTrack.THIRD_GEMS_PAY,
			player.getId(),gems,toInt(transId),
			Resources.getGems(player.getResources()));

		// �ɾ����ݲɼ�
		AchieveCollect.gemsStock(gems,player);
		data.clear();
		data.writeUTF(SUCCESS);
        return data;
    }
    
    private ByteBuffer rechargeByName(ByteBuffer data)
    {
        String playerName = data.readUTF();
        String transId = data.readUTF();
        data.readInt();
        int gems = data.readInt();
        
        ByteBuffer result = new ByteBuffer();
        
        // ��������Ҫ���һ�ݶ���
        Player player = objectFactory.getPlayerByName(playerName,true);
        if (player == null)
        {
            log.error("player is not exist.");
            result.writeUTF(FAIL);
            return result;
        }

        // ��ӱ�ʯ
        Resources.addGems(gems, player.getResources(), player);
        player.flushVIPlevel();
        // ��ʯ��־��¼
		objectFactory.createGemTrack(GemsTrack.THIRD_GEMS_PAY,
			player.getId(),gems,toInt(transId),
			Resources.getGems(player.getResources()));

		// �ɾ����ݲɼ�
		AchieveCollect.gemsStock(gems,player);
		data.writeUTF(SUCCESS);
        return data;
    }

    private Integer toInt(String transId)
    {
        int result = 0;
        try
        {
            result = Integer.valueOf(transId);
        }
        catch (NumberFormatException e)
        {
            result = transId.hashCode();
        }

        return result;
    }

    /**
     * ����key�������� ��һkey
     * 
     * @param key id
     * @return ���ص�ByteBuffer
     */
    public static ByteBuffer load(String data)
    {
        if (data == null || data.equals("null"))
        {
            return null;
        }

        ByteBuffer bb = new ByteBuffer();
        bb.clear();
        BASE64.decode(data, 0, data.length(), bb);
        return bb;
    }

    /**
     * Base64������㷨 ����������ת��Ϊ�ַ���
     */
    public static String createBase64(ByteBuffer data)
    {
        byte[] array = data.toArray();
        data.clear();
        BASE64.encode(array, 0, array.length, data);
        return new String(data.getArray(), 0, data.top());
    }

    @Override
    public byte[] excute(HttpRequestMessage request, String ip)
    {
        return excuteString(request,ip).getBytes();
    }

    /**
     * setter
     */
    public void setObjectFactory(CreatObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
