package com.pcwerk.dedup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

public class Tool {

	public static String extractName(String filename) {
		String[] block = filename.split("/");
		if (block.length > 0) {
			return block[block.length - 1];
		}
		return null;
	}

	public static String extractDescription(String filename) {
		return filename.replaceAll("[/\\.]+", " ").replaceFirst("^[ ]+", "");
	}

	public static String computeSize(String filename) {
		File file = new File(filename);
		return new String("" + file.length());
	}

	public static String getModifiedDate(String filename) {
		File file = new File(filename);
		Long lastmodified = file.lastModified();
		Date date = new Date(lastmodified);

		return date.toString();
	}

	public static String computeSHA(String filename) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		byte[] dataBytes = new byte[1024];

		int nread = 0;
		try {
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		;
		byte[] mdbytes = md.digest();

		// convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}

		return sb.toString();
	}

	public static boolean isType(String filename, String searchKeys) {
		boolean status = false;
		String[] array = searchKeys.split("\\|");

		for (String needle : array) {
			if (filename.endsWith(needle))
				return true;
		}

		return status;
	}

	public static String guessRun(String filename) {
		String run = "Unknown";
		String regex = "([rR])(un)*([0-9])+";
		Matcher m = Pattern.compile(regex).matcher(filename);

		m.find();

		try {
			String myMatch = filename.substring(m.start(), m.end());
			run = myMatch.replaceAll("([rR])(un)*(0)+", "R");
		} catch (Exception e) {
			// nothing found
		}

		return run;
	}

	public static String getClassName(@SuppressWarnings("rawtypes") Class c) {
		String className = c.getName();
		int firstChar;
		firstChar = className.lastIndexOf('.') + 1;
		if (firstChar > 0) {
			className = className.substring(firstChar);
		}
		return className;
	}

	public static String detectFileType(String filename) throws IOException {
		FileInputStream stream = null;

		File file = new File(filename);
		stream = new FileInputStream(file);

		Metadata metadata = new Metadata();
		metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());

		Tika tika = new Tika();
		String type = tika.detect(stream);

		if (stream != null)
			stream.close();

		return type;
	}

	public static String extractFileLocation(String filename) {
		File file = new File(filename);
		return file.getParent();
	}

}
