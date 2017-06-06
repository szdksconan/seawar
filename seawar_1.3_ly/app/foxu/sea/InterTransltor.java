package foxu.sea;

import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.IntKeyHashMap;
import mustang.text.BaseTranslator;
import mustang.text.Translator;
import mustang.util.TranslatorInit;

/**
 * �����Է����.���ݿͻ��˵�����,����Ϊ��Ӧ������
 * </p>
 * ��������Ĭ�Ϸ����
 * 
 * @author rockzyt
 */
public class InterTransltor
{

	/* static fields */
	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(InterTransltor.class);
	private static InterTransltor translator=new InterTransltor();

	/* static methods */
	/** ���ʵ�� */
	public static InterTransltor getInstance()
	{
		return translator;
	}
	
	/** ����������Ϊ��ǰ������ת���� */
	public static void configure()
	{
		translator=new InterTransltor();
	}

	/* fields */
	/** Ĭ�Ϸ�������ʾ */
	int defLanguage=PublicConst.kLanguageEnglish;
	/** Ĭ�Ϸ����� */
	Translator defTranslator;
	/** ��������� */
	IntKeyHashMap translators=new IntKeyHashMap();

	/* properties */
	/** ����Ĭ�����Ի��� */
	public void setDefLanguage(int def)
	{
		defLanguage=def;
	}

	/** ��ʼ�������� */
	public void init()
	{
		try
		{
			// ���� 1
			String str=System.getProperty("translator.file.cn");
			BaseTranslator t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageChinese,t);
			// Ӣ�� 2
			str=System.getProperty("translator.file.en");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageEnglish,t);
			defTranslator=t;
			// �������� 3
			str=System.getProperty("translator.file.fan_cn");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageChineseHant,t);
			// ���� 4
			str=System.getProperty("translator.file.de");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageGerman,t);
			// ���ġ�5
			str=System.getProperty("translator.file.jp");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageJapness,t);
			// ���ġ�6
			str=System.getProperty("translator.file.fr");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageFrench,t);
			// ���7
			str=System.getProperty("translator.file.ru");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageRussian,t);
			// �������8
			str=System.getProperty("translator.file.it");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageItalian,t);
			// ���ġ�9
			str=System.getProperty("translator.file.kr");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageKorean,t);
			// ̩����10
			str=System.getProperty("translator.file.th");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageThailand,t);
			// Խ�ϡ�11
			str=System.getProperty("translator.file.vn");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageVietnam,t);
			// ��������12
			str=System.getProperty("translator.file.ae");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageArab,t);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled())
				log.warn("start error, translator init fail",e);
		}
	}

	/* methods */
	/**
	 * ��ʼ������
	 * </p>
	 * ���������ĳ��ȱ���һ��
	 * 
	 * @param flag ���Ի�����־����
	 * @param ts ������
	 */
	public void init(int[] flag,Translator[] ts)
	{
		if(flag.length!=ts.length)
			throw new IllegalArgumentException(this+". flag.length="
				+flag.length+", Translator.length="+ts.length);
		for(int i=flag.length-1;i>=0;i--)
		{
			if(flag[i]==defLanguage)
			{
				defTranslator=ts[i];
				continue;
			}
			translators.put(flag[i],ts[i]);
		}
		if(defTranslator==null)
			throw new IllegalArgumentException("have no default language.");
	}
	
	/**
	 * ��úͿͻ�������ƥ������Ի���
	 * 
	 * @param key ���Ա�ʾ
	 * @return ���ض�Ӧ�ķ����.���û���򷵻�Ĭ�Ϸ����
	 */
	public Translator getTranslator(int key)
	{
		Translator t=(Translator)translators.get(key);
		if(t==null) return defTranslator;
		return t;
	}

	/** �������� */
	public String getTransByKey(int key,String str)
	{
		return getTranslator(key).translate(str);
	}
}