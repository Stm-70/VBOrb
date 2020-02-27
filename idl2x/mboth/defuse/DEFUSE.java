/*
   Copyright (c) 1999 Martin.Both
*/

package mboth.defuse;

import java.util.Hashtable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import mboth.util.*;

/**
 * @author  Martin Both
 */
public class DEFUSE
{
	/**
	 */
	public static final String version= "50";

	/**
	 */
	private static Hashtable codeWords;
	private static String lineSeparator;

	/** 
	 */
	static public void main(String args[])
	{
		if(args.length < 2)
		{	printVersion();
			printUsage();
			return;
		}
		codeWords= new Hashtable();
		lineSeparator= System.getProperty("line.separator");
		
		File fOutPath= new File(args[args.length - 1]);
		if(!fOutPath.isDirectory())
		{	System.out.println("E: " + args[args.length - 1]
				+ " is not a directory");
			printUsage();
			return;
		}
		String sOutPath= fOutPath.getPath();
		
		File fInPaths[]= new File[args.length - 1];
		for(int i= 0; i < args.length - 1; i++)
		{	fInPaths[i]= new File(args[i]);
			if(!fInPaths[i].canRead())
			{	System.out.println("E: Cannot read " + args[i]);
				printUsage();
				return;
			}
		}
		
		try
		{
			/* Add codeword files
			*/
			for(int i= 0; i < fInPaths.length; i++)
			{	addCodeWordFiles(fInPaths[i]);
			}
			/* Convert files or directories
			*/
			for(int i= 0; i < fInPaths.length; i++)
			{	if(fInPaths[i].isDirectory())
				{	String path= fInPaths[i].getPath();
					String inList[]= fInPaths[i].list();
					for(int j= 0; j < inList.length; j++)
						convertJavaFile(new File(path, inList[j]), sOutPath);
				}else
				{	convertJavaFile(fInPaths[i], sOutPath);
				}
			}
		}catch(ChainException ex)
		{	do
			{	System.out.println("E: " + ex.toString());
				ex= ex.getNextException();
			}while(ex != null);
		}
	}

	/** 
	 */
	static public void printVersion()
	{	System.out.println("DEFUSE " + version);
	}
	
	/** 
	 */
	static public void printUsage()
	{	System.out.println("usage: java mboth.defuse.DEFUSE inpath(s) outpath");
	}

	
	/*
	 */
	static public void addCodeWordFiles(File file) throws ChainException
	{
		if(file.isDirectory())
		{	String path= file.getPath();
			String inList[]= file.list();
			for(int i= 0; i < inList.length; i++)
				addCodeWordFiles(new File(path, inList[i]));
			return;
		}
		if(!file.getName().equals("codewords.txt"))
			return;
		System.out.println("I: Add CodeFile " + file.getPath());
		TxtTokenReader tr= new TxtReader(file.getPath());
		TxtToken tt= tr.readToken();
		while(!(tt instanceof TxtTokEndOfFile))
		{
			if(tt instanceof TxtTokWord)
			{	String codeWord= ((TxtTokWord)tt).getWord();
				codeWords.put(codeWord, codeWord);
			}else
			{	throw new TxtReadException(tt.getFilePos(),
					"CodeWord expected");
			}
			tt= tr.readToken();
		}		
	}
	
	/*
	*/
	static public void convertJavaFile(File fOldFile, String sOutPath)
		throws ChainException
	{
		String sOldFile= fOldFile.getPath();
		if(fOldFile.isDirectory())
		{
			if(fOldFile.getName().equals("CVS"))
				return;
			String sNewDir= sOutPath + File.separator + fOldFile.getName();
			File fNewDir= new File(sNewDir);
			if(fNewDir.exists())
			{	throw new ChainException("Directory " + sNewDir + " exists");
			}
			if(!fNewDir.mkdir())
			{	throw new ChainException("Cannot mkdir " + sNewDir);
			}
			String inList[]= fOldFile.list();
			for(int i= 0; i < inList.length; i++)
				convertJavaFile(new File(sOldFile, inList[i]), sNewDir);
			return;
		}
		if(!sOldFile.endsWith(".java"))
			return;
		String sNewFile= fOldFile.getName();
		sNewFile= sOutPath + File.separator
			+ convertWord(sNewFile.substring(0, sNewFile.length() - 5))
			+ ".java";
		File fNewFile= new File(sNewFile);
		if(fNewFile.exists())
		{	throw new ChainException("File " + sNewFile + " exists");
		}

		/* Convert File
		*/
		System.out.println("I: Convert file " + sOldFile + " to "
			+ fNewFile.getName());
		TxtTokenReader tr= new TxtReader(sOldFile);
		try
		{	FileWriter fw= new FileWriter(sNewFile);
			TxtToken tt= tr.readToken();
			while(!(tt instanceof TxtTokEndOfFile))
			{
				if(tt instanceof TxtTokWord)
				{	TxtTokWord oldWord= (TxtTokWord)tt;
					if(oldWord.isAfterNewLine())
						fw.write(lineSeparator);
					if(oldWord.isAfterBlanks())
						fw.write(" ");
					fw.write(convertWord(oldWord.getWord()));
				}else
				{	fw.write(tt.toString());
				}
				tt= tr.readToken();
			}
			if(tt.isAfterNewLine())
				fw.write(lineSeparator);
			fw.close();
		}catch(IOException ex)
		{	throw new TxtReadException(sNewFile, ex.getMessage());
		}
	}

	/**
	 */
	private static String startWord;
	private static int nextCode;

	/*
	 */
	static public String convertWord(String oldWord)
	{
		String newWord= (String)codeWords.get(oldWord);
		if(newWord == null)
		{	int endCode= nextCode % 8;
			if(endCode == 0)
			{	startWord= "";
				int code= nextCode / 8;
				while(code > 0)
				{	startWord= startWord + (char)((int)'a' + code % 8);
					code= code / 8;
				}
			}
			newWord= startWord + (char)((int)'a' + endCode);
			codeWords.put(oldWord, newWord);
			nextCode++;
		}
		return newWord;
	}
}
