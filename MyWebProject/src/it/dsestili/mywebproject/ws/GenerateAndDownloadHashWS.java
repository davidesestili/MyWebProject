package it.dsestili.mywebproject.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import it.dsestili.jhashcode.core.DirectoryScanner;
import it.dsestili.jhashcode.core.DirectoryScannerNotRecursive;
import it.dsestili.jhashcode.core.DirectoryScannerRecursive;
import it.dsestili.jhashcode.core.Utils;
import it.dsestili.jhashcode.gui.MainWindow;
import it.dsestili.mywebproject.GenerateAndDownloadHash;

public class GenerateAndDownloadHashWS extends GenerateAndDownloadHash {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GenerateAndDownloadHashWS.class);

	private Result r = null;
	
	@Override
	protected void downloadFile(HttpServletResponse response, DirectoryScanner scanner) throws Throwable {
		File[] files = getFiles(scanner);
		File temp = generateTempFile(files);
		
		FileInputStream fis = new FileInputStream(temp);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader reader = new BufferedReader(isr);

		List<FileInfo> infos = new ArrayList<FileInfo>();
		
		String lineOfText;
		while((lineOfText = reader.readLine()) != null)
		{
			StringBuilder hashStringBuilder = new StringBuilder();
			StringBuilder fileNameStringBuilder = new StringBuilder();
			
			int i;
			for(i = 0; i < lineOfText.length(); i++)
			{
				char c = lineOfText.charAt(i);
				
				if(c == ' ')
				{
					i += 2;
					break;
				}
				
				hashStringBuilder.append(c);
			}

			String hash = hashStringBuilder.toString();
			
			while(i < lineOfText.length())
			{
				char c = lineOfText.charAt(i);
				fileNameStringBuilder.append(c);
				i++;
			}

			String fileName = fileNameStringBuilder.toString();
			
			FileInfo info = new FileInfo();
			info.setHash(hash);
			info.setFileName(fileName);
			
			infos.add(info);
		}
		
		reader.close();
		
		r = new Result();
		r.setResult(infos.toArray(new FileInfo[0]));
	}
	
	@Override
	protected File getTempFile() throws IOException {
		File temp = File.createTempFile("tempfile", ".tmp");
		return temp;
	}
	
	public Result generateAndDownloadHash(String folder, String algorithm, String modeParam)
	{
		long start = System.currentTimeMillis();
		
		MainWindow.setItalianLocale();
	
		this.algorithm = algorithm;
		
		logger.debug("Folder: " + folder);
		File directory = new File(folder);
		if(!directory.exists())
		{
			logger.debug("Directory inesistente");
			return r;
		}

		logger.debug("Algorithm: " + algorithm);
		try 
		{
			MessageDigest.getInstance(algorithm);
		} 
		catch(NoSuchAlgorithmException e) 
		{
			logger.debug("Algoritmo inesistente");
			return r;
		}

		DirectoryScanner scanner = null;

		if(modeParam != null && modeParam.trim().equals("not-recursive"))
		{
			try
			{
				recursive = true;
				scanner = new DirectoryScannerNotRecursive(directory, recursive);
				downloadFile(null, scanner);
			} 
			catch(Throwable e) 
			{
				logger.debug("Si è verificato un errore", e);
			}
		}
		else if(modeParam != null && modeParam.trim().equals("recursive"))
		{
			try 
			{
				recursive = true;
				scanner = new DirectoryScannerRecursive(directory, recursive);
				downloadFile(null, scanner);
			} 
			catch(Throwable e) 
			{
				logger.debug("Si è verificato un errore", e);
			}
		}
		else if(modeParam != null && modeParam.trim().equals("no-subfolders"))
		{
			try 
			{
				recursive = false;
				scanner = new DirectoryScannerNotRecursive(directory, recursive);
				downloadFile(null, scanner);
			} 
			catch(Throwable e) 
			{
				logger.debug("Si è verificato un errore", e);
			}
		}
		else
		{
			logger.debug("Mode error");
			return r;
		}
		
		long elapsed = System.currentTimeMillis() - start;
		logger.debug("Elapsed time: " + Utils.getElapsedTime(elapsed, true));

		return r;
	}
}