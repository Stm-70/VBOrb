/*
   Copyright (c) 1999 Martin.Both
*/

package mboth.util;

import java.util.*;
import java.io.*;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

/**
 * @author  Martin Both
 */

public class UserPrefs
{
	/**
	 */
	static public UserPrefs byIniFile(File iniDir, String iniFile)
	{
		UserPrefs uPrefs= new UserPrefs(iniDir, iniFile);
		return uPrefs;
	}
	
	/**
	 */
	private File fIniFile;
	private HashMap hmPrefs;
	
	/**
	 */
	private UserPrefs(File iniDir, String iniFile)
	{
		fIniFile= new File(iniDir, iniFile);
		hmPrefs= new HashMap();
	}
	
	/**
	 */
	public void readPrefs() throws TxtReadException
	{
		if(!fIniFile.exists())
			return;
		
		TxtTokenReader tr= new TxtPreProcessor(fIniFile.getPath());
		TxtToken tt= tr.readToken();
		ArrayList alValues= new ArrayList();
		while(!(tt instanceof TxtTokEndOfFile))
		{	//System.out.print("cmd= " + tt.toString());
			if(!(tt instanceof TxtTokWord))
			{	throw new TxtReadException(tt.getFilePos(),
					"Section name expected: " + tt.toString());
			}
			String secName= ((TxtTokWord)tt).getWord();
			alValues.clear();
			for(; ; )
			{	tt= tr.readToken();
				if(tt.isAfterNewLine())
					break;
				if(tt instanceof TxtTokString)
				{	alValues.add(((TxtTokString)tt).getOriginalStr());
				}else if(tt instanceof TxtTokDigits)
				{	alValues.add(new Integer(((TxtTokDigits)tt).getInt()));
				}else if(tt instanceof TxtTokWord)
				{	String wordValue= ((TxtTokWord)tt).getWord();
					alValues.add(Boolean.valueOf(wordValue));
				}else if(tt instanceof TxtTokSepChar)
				{	char ch= ((TxtTokSepChar)tt).getChar();
					if(ch == '-')
					{	tt= tr.readToken();
						if(tt.isAfterNewLine() ||
							!(tt instanceof TxtTokDigits))
						{	throw new TxtReadException(tt.getFilePos(),
								"Missing digits after '-' sign: " +
								tt.toString());
						}
						alValues.add(new Integer(-((TxtTokDigits)tt).getInt()));
					}else
					{	throw new TxtReadException(tt.getFilePos(),
							"Unknown character token: " + tt.toString());
					}
				}else
				{	throw new TxtReadException(tt.getFilePos(),
						"Unknown token: " + tt.toString());
				}
			}
			Object valObj;
			if(alValues.size() > 1)
				valObj= alValues.toArray();
			else
				valObj= alValues.get(0);
			//System.out.println("key= " + secName + ", val= " + valObj);
			hmPrefs.put(secName, valObj);
		}
	}

	/**
	 */
	public void clear()
	{	hmPrefs.clear();
	}
	
	/**
	 */
	public void writePrefs() throws Exception
	{
		FileWriter fwr= new FileWriter(fIniFile);
		try
		{	Iterator hmIt= hmPrefs.keySet().iterator();
			while(hmIt.hasNext())
			{	String key= (String)hmIt.next();
				fwr.write(key); fwr.write(' ');
				Object obj= hmPrefs.get(key);
				if(obj instanceof String)
				{	fwr.write('"'); fwr.write((String)obj); fwr.write('"');
				}else if(obj instanceof Boolean)
				{	fwr.write(((Boolean)obj).toString());
				}else if(obj instanceof Integer)
				{	fwr.write(((Integer)obj).toString());
				}else
				{	throw new Exception("Unknown kind of value: " +
						obj.toString());
				}
				fwr.write("\r\n");
			}
		}finally
		{	fwr.close();
		}
	}
	
	/**
	 */
	public String getStr(String key, String def)
	{	Object obj= hmPrefs.get(key);
		if(obj instanceof String)
		{	return (String)obj;
		}
		return def;
	}

	/**
	 */
	public void putStr(String key, String value)
	{	hmPrefs.put(key, value);
	}

	/**
	 */
	public String[] getStrArr(String prefix, String[] def)
	{
		String[] value= new String[def.length];
		for(int si= 0; si < value.length; si++)
		{	value[si]= getStr(prefix + Integer.toString(si), def[si]);
		}
		return value;
	}

	/**
	 */
	public void putStrArr(String prefix, String[] value)
	{
		for(int si= 0; si < value.length; si++)
		{	if(value[si] == null)
				continue;
			putStr(prefix + Integer.toString(si), value[si]);
		}
	}

	/**
	 */
	public boolean getBool(String key, boolean def)
	{
		Object obj= hmPrefs.get(key);
		if(obj instanceof Boolean)
		{	return ((Boolean)obj).booleanValue();
		}
		return def;
	}
	
	/**
	 */
	public void putBool(String key, boolean value)
	{	hmPrefs.put(key, new Boolean(value));
	}

	/**
	 */
	public int getInt(String key, int def)
	{
		Object obj= hmPrefs.get(key);
		if(obj instanceof Integer)
		{	return ((Integer)obj).intValue();
		}
		return def;
	}

	/**
	 */
	public void putInt(String key, int value)
	{	hmPrefs.put(key, new Integer(value));
	}

	/**
	 */
	public void getTblCols(String prefix, JTable table)
	{
		TableColumnModel tcm= table.getColumnModel();
		
		for(int mi= 0; mi < table.getColumnCount(); mi++)
		{	int ci= table.convertColumnIndexToView(mi);
			TableColumn col= tcm.getColumn(ci);
			int iWidth= getInt(prefix + "c" + mi + "width",
				col.getPreferredWidth());
			col.setPreferredWidth(iWidth);
			int newCi= getInt(prefix + "c" + mi + "pos", mi);
			tcm.moveColumn(ci, newCi);
		}
	}

	/**
	 */
	public void putTblCols(String prefix, JTable table)
	{
		TableColumnModel tcm= table.getColumnModel();
		
		for(int ci= 0; ci < tcm.getColumnCount(); ci++)
		{	TableColumn col= tcm.getColumn(ci);
			int iModIdx= col.getModelIndex();
			putInt(prefix + "c" + iModIdx + "width", col.getWidth());
			putInt(prefix + "c" + iModIdx + "pos", ci);
		}
	}
}
