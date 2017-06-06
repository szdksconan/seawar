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
 * 第三方充值接口
 * 
 * @author wangbing
 */
public class ThirdPartRechargePort implements HttpHandlerInterface
{
    private static Logger log = LogFactory.getLogger(ThirdPartRechargePort.class);

    public static final Base64 BASE64 = new Base64();

    // 获取所有用户
    private static final String GET_USER = "get_character";

    // 充值
    private static final String RECHARGE = "recharge";

    // 冒号
    private static final String DATA = "data";

    // 空字符串
    private static final String EMPTYSTRING = "";

    // 充值成功
    private static final String SUCCESS = "ok";

    // 充值失败
    private static final String FAIL = "fail";
    
    // 数据库缓存
    private CreatObjectFactory objectFactory;

    public String excuteString(HttpRequestMessage request, String ip)
    {
        try
        {
            String dataStr = request.getParameter(DATA);
            ByteBuffer data = load(dataStr);

            // 执行命令
            String action = data.readUTF();

            // 根据指定账户获取用户
            if (GET_USER.equals(action))
            {
                String account = data.readUTF();
                ByteBuffer result = new ByteBuffer();   
                // 查询用户
                String sql="select * from users where user_account='"
                    +account+"'";
                User[] users = objectFactory.getUserDBAccess().loadBySql(sql);
                //如果出现了两个user，是有问题的
                if (users != null && users.length == 1)
                {
                    // 查询条数
                    sql="select * from players where user_id='"
                        +users[0].getId()+"'";
                    Player[] players = (Player[])objectFactory.getPlayerCache().getDbaccess().loadBySql(sql);
                    if (players != null)
                    {
                        result.writeInt(players.length);
                        for (Player player : players)
                        {
                            // 依次写入id和name
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
                
                // 编码后返回
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
        // 保存玩家
        objectFactory.getPlayerByName(player.getName(),true);

        // 添加宝石
        Resources.addGems(gems, player.getResources(), player);
        player.flushVIPlevel();
        // 宝石日志记录
		objectFactory.createGemTrack(GemsTrack.THIRD_GEMS_PAY,
			player.getId(),gems,toInt(transId),
			Resources.getGems(player.getResources()));

		// 成就数据采集
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
        
        // 服务器需要另存一份订单
        Player player = objectFactory.getPlayerByName(playerName,true);
        if (player == null)
        {
            log.error("player is not exist.");
            result.writeUTF(FAIL);
            return result;
        }

        // 添加宝石
        Resources.addGems(gems, player.getResources(), player);
        player.flushVIPlevel();
        // 宝石日志记录
		objectFactory.createGemTrack(GemsTrack.THIRD_GEMS_PAY,
			player.getId(),gems,toInt(transId),
			Resources.getGems(player.getResources()));

		// 成就数据采集
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
     * 根据key加载数据 单一key
     * 
     * @param key id
     * @return 返回的ByteBuffer
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
     * Base64编解码算法 二进制数据转化为字符串
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
