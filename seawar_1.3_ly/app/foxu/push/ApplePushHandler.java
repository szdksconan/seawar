package foxu.push;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import mustang.log.LogFactory;
import mustang.log.Logger;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.Devices;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.json.JSONException;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;

public class ApplePushHandler implements PushHandler
{

	/* log */
	private static final Logger log=LogFactory
		.getLogger(ApplePushHandler.class);

	public static final String[] NULL_ARRAY={};

	/** ��Ϣ������󳤶� */
	public static final int MAX_MESSAGE_LEN=250;

	/** device token ���� */
	public static final int DEVICE_TOKEN_LEN=64;

	/** �Ƿ����������� */
	private boolean production=true;

	/** �������͹����� */
	private PushNotificationManager developManager;

	/** �������ͷ�������Ϣ */
	private AppleNotificationServer developServer;

	/** ��ʽ��Ʒ���͹����� */
	private PushNotificationManager productionManager;

	/** ��ʽ���ͷ�������Ϣ */
	private AppleNotificationServer productionServer;

	/**
	 * ��ʼ��������ֻ��ʼ����Ʒ����
	 * 
	 * @param productionKeystore
	 * @param productionPassword
	 */
	public void init(String productionKeystore,String productionPassword)
	{
		init(null,null,productionKeystore,productionPassword);
	}

	/**
	 * ��ʼ��������ʹ����ͬ�����ʼ����Ʒ�Ͳ�������
	 * 
	 * @param developKeystore
	 * @param productionKeystore
	 * @param password
	 */
	public void init(String developKeystore,String productionKeystore,
		String password)
	{
		init(developKeystore,password,productionKeystore,password);
	}

	/**
	 * ��ʼ��������ʹ�ò�ͬ�����ʼ����Ʒ�Ͳ�������
	 * 
	 * @param developKeystore
	 * @param developPassword
	 * @param productionKeystore
	 * @param productionPassword
	 */
	public void init(String developKeystore,String developPassword,
		String productionKeystore,String productionPassword)
	{
		try
		{
			// ��ʼ���������ͻ���
			if(developKeystore!=null&&developPassword!=null)
			{
				PushNotificationManager manager=new PushNotificationManager();
				InputStream keystore=new BufferedInputStream(
					new FileInputStream(developKeystore));
				AppleNotificationServer server=new AppleNotificationServerBasicImpl(
					keystore,developPassword,false);

				manager.initializeConnection(server);

				developManager=manager;
				developServer=server;

			}
			// ��ʼ����ʽ��Ʒ���ͻ���
			if(productionKeystore!=null&&productionPassword!=null)
			{
				PushNotificationManager manager=new PushNotificationManager();
				InputStream keystore=new BufferedInputStream(
					new FileInputStream(productionKeystore));
				AppleNotificationServer server=new AppleNotificationServerBasicImpl(
					keystore,productionPassword,true);

				manager.initializeConnection(server);

				productionManager=manager;
				productionServer=server;
			}
		}
		catch(KeystoreException e)
		{
			if(log.isErrorEnabled()) log.error("keystore error.",e);
		}
		catch(CommunicationException e)
		{
			if(log.isErrorEnabled()) log.error("communication faild.",e);
		}
		catch(FileNotFoundException e)
		{
			if(log.isErrorEnabled())
				log.error("keystore file not found.",e);
		}
	}

	/**
	 * �����Ƿ�����������
	 * 
	 * @param b
	 */
	public void setProduction(boolean b)
	{
		this.production=b;
	}

	@Override
	public boolean checkMessageLength(String message)
	{
		return (message!=null&&message.getBytes().length<=MAX_MESSAGE_LEN);
	}

	/**
	 * @param msg ��������
	 * @param params ���ӵ���Ϣ����ʽΪ{sound}
	 */
	@Override
	public void push(String message,String deviceToken,String[] params)
	{
		try
		{
			if(deviceToken==null||deviceToken.length()!=DEVICE_TOKEN_LEN)
			{
				if(log.isErrorEnabled())
					log.error("device token error.token="+deviceToken);
				return;
			}
			PushNotificationManager pushNotificationManager=this.production
				?productionManager:developManager;
			AppleNotificationServer server=this.production?productionServer:developServer;
			if(pushNotificationManager==null)
			{
				if(log.isErrorEnabled())
					log.error("push manager is null.production="+this.production);
				return;
			}
			if(params==null) params=NULL_ARRAY;
			String sound=params.length>1?params[0]:"default".intern();
			PushNotificationPayload payload=new PushNotificationPayload();
			payload.addAlert(message);
			payload.addSound(sound);
			payload.addBadge(1);

			Device device=new BasicDevice();
			device.setToken(deviceToken);
			pushNotificationManager.restartConnection(server);
			pushNotificationManager.sendNotification(device,payload);
		}
		catch(JSONException e)
		{
			if(log.isErrorEnabled())
				log.error("push message error.content="+message,e);
		}
		catch(CommunicationException e)
		{
			if(log.isErrorEnabled()) log.error("communication failed.",e);
		}
		catch(KeystoreException e)
		{
			e.printStackTrace();
		}

	}
	@Override
	public void push(String message,String[] deviceTokens,String[] params)
	{
		try
		{
			if(deviceTokens==null||deviceTokens.length==0)
			{
				return;
			}
			PushNotificationManager pushNotificationManager=this.production
							?productionManager:developManager;
			AppleNotificationServer server=this.production?productionServer:developServer;
			if(pushNotificationManager==null)
			{
				if(log.isErrorEnabled())
					log.error("push manager is null.production="+this.production);
				return;
			}
			if(params==null) params=NULL_ARRAY;
			String sound=params.length>1?params[0]:"default".intern();
			PushNotificationPayload payload=new PushNotificationPayload();
			payload.addAlert(message);
			payload.addSound(sound);
			payload.addBadge(1);

			List<Device> devices=Devices.asDevices(deviceTokens);
			pushNotificationManager.restartConnection(server);
			pushNotificationManager.sendNotifications(payload,devices);
		}
		catch(JSONException e)
		{
			if(log.isErrorEnabled())
				log.error("push message error.content="+message,e);
		}
		catch(CommunicationException e)
		{
			if(log.isErrorEnabled()) log.error("communication failed.",e);
		}
		catch(KeystoreException e)
		{
			if(log.isErrorEnabled()) log.error("keystore error.",e);
		}
	}

}
