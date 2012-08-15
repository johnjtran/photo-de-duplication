package edu.usc.csci572.jtran;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ImageInfo {

	public Metadata parse(File f) throws IOException, SAXException,
			TikaException {
		FileInputStream is = new FileInputStream(f);

		ContentHandler contenthandler = new BodyContentHandler();
		ParseContext context = new ParseContext();
		Metadata metadata = new Metadata();

		metadata.set(Metadata.RESOURCE_NAME_KEY, f.getName());

		Parser parser = new AutoDetectParser();
		parser.parse(is, contenthandler, metadata, context);

		return metadata;
	}

	protected void listAvailableMetaDataFields(final Metadata metadata) {
		for (int i = 0; i < metadata.names().length; i++) {
			String name = metadata.names()[i];
			System.out.println(name + " : " + metadata.get(name));
		}
	}

	private String normalizeDate(String date) {
		return date.replaceAll("[:\\-]", " ").replaceAll("[tT]", " ");
	}

	private String estimateDate(final Metadata metadata) {
		String date = null;
		
		Pattern p1 = Pattern.compile("[Oo]riginal");
		Pattern p2 = Pattern.compile("[dD]ate");
		for (int i = 0; i < metadata.names().length; i++) {
			String name = metadata.names()[i];
			Matcher m1 = p1.matcher(name);
			Matcher m2 = p2.matcher(name);
			if (m1.find()) {
				return normalizeDate(metadata.get(name));
			} else if (m2.find()) {
				return normalizeDate(metadata.get(name));
			}
		}
		
		return date;
	}

	public static String extract(String filename) {
		return extract(new File(filename));	
	}
	
	public static String extract(File file) {
		ImageInfo info = new ImageInfo();
		String date = null;

		try {
			Metadata metadata = info.parse(file);
			date = info.estimateDate(metadata);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (date == null) {
			Long lastModified = file.lastModified();
	        Date lastModifiedDate = new Date(lastModified);
	        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");  
	        date = df.format(lastModifiedDate);
		}
		
		return info.normalizeDate(date);
	}

	public static void main(String args[]) {
		if (args.length >= 1) {
			String filename = args[0];
			String date = extract(filename);
			System.out.println(filename + " : " + date);
		}
	}
}
