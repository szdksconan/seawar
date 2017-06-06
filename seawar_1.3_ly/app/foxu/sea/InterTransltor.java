package foxu.sea;

import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.IntKeyHashMap;
import mustang.text.BaseTranslator;
import mustang.text.Translator;
import mustang.util.TranslatorInit;

/**
 * 多语言翻译机.根据客户端的设置,翻译为对应的语言
 * </p>
 * 必须配置默认翻译机
 * 
 * @author rockzyt
 */
public class InterTransltor
{

	/* static fields */
	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(InterTransltor.class);
	private static InterTransltor translator=new InterTransltor();

	/* static methods */
	/** 获得实例 */
	public static InterTransltor getInstance()
	{
		return translator;
	}
	
	/** 将自身配置为当前的文字转换器 */
	public static void configure()
	{
		translator=new InterTransltor();
	}

	/* fields */
	/** 默认翻译器标示 */
	int defLanguage=PublicConst.kLanguageEnglish;
	/** 默认翻译器 */
	Translator defTranslator;
	/** 翻译机容器 */
	IntKeyHashMap translators=new IntKeyHashMap();

	/* properties */
	/** 设置默认语言环境 */
	public void setDefLanguage(int def)
	{
		defLanguage=def;
	}

	/** 初始化翻译器 */
	public void init()
	{
		try
		{
			// 中文 1
			String str=System.getProperty("translator.file.cn");
			BaseTranslator t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageChinese,t);
			// 英文 2
			str=System.getProperty("translator.file.en");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageEnglish,t);
			defTranslator=t;
			// 繁体中文 3
			str=System.getProperty("translator.file.fan_cn");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageChineseHant,t);
			// 德文 4
			str=System.getProperty("translator.file.de");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageGerman,t);
			// 日文　5
			str=System.getProperty("translator.file.jp");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageJapness,t);
			// 法文　6
			str=System.getProperty("translator.file.fr");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageFrench,t);
			// 俄语　7
			str=System.getProperty("translator.file.ru");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageRussian,t);
			// 意大利　8
			str=System.getProperty("translator.file.it");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageItalian,t);
			// 韩文　9
			str=System.getProperty("translator.file.kr");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageKorean,t);
			// 泰国　10
			str=System.getProperty("translator.file.th");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageThailand,t);
			// 越南　11
			str=System.getProperty("translator.file.vn");
			t=new BaseTranslator(null);
			if(str!=null) (new TranslatorInit()).init(str,t);
			t.configure();
			translators.put(PublicConst.kLanguageVietnam,t);
			// 阿拉伯　12
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
	 * 初始化方法
	 * </p>
	 * 两个参数的长度必须一样
	 * 
	 * @param flag 语言环境标志数组
	 * @param ts 翻译器
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
	 * 获得和客户端语言匹配的语言环境
	 * 
	 * @param key 语言标示
	 * @return 返回对应的翻译机.如果没有则返回默认翻译机
	 */
	public Translator getTranslator(int key)
	{
		Translator t=(Translator)translators.get(key);
		if(t==null) return defTranslator;
		return t;
	}

	/** 翻译文字 */
	public String getTransByKey(int key,String str)
	{
		return getTranslator(key).translate(str);
	}
}