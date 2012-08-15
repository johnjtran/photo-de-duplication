package edu.usc.csci572.jtran;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.IngestException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * Hello world!
 * 
 */
public class PhotoCrawler {
	private static final String version = "0.0.1";
	private String sourceFolder = "";
	private String fmUrl = "http://localhost:9000";
	private static final Logger LOG = Logger.getLogger(QueryTool.class
			.getName());
	private RecursiveFile srcFiles = null;

	public PhotoCrawler(String args[]) {
		if (args.length < 1) {
			System.out
					.println("usage: java PhotoCrawler <fmUrl> <sourceDirectory>");
			System.exit(0);
		}
		ArrayList<String> watchPatterns = new ArrayList<String>();

		String patterns[] = { "jpg", "JPG", "jpeg", "gif", "GIF", "png", "PNG",
				"tif", "tiff", "TIF" };

		for (String f : patterns)
			watchPatterns.add(f);

		fmUrl = args[0];
		sourceFolder = args[1];

		srcFiles = new RecursiveFile(sourceFolder, watchPatterns);
	}

	public void run() {
		System.out.println("Photo Deduplication version: " + version);
		System.out.println("fm url: " + fmUrl);
		System.out.println("source folder: " + sourceFolder);

		for (String filename : srcFiles.getFiles()) {
			addToFM(filename, ImageInfo.extract(filename));
		}

		System.out.println("*** ingestion complete ****");
	}

	@SuppressWarnings("static-access")
	public void addToFM(String fileName, String captureDate) {
		String checkSum = Tool.computeSHA(fileName);

		if (QueryTool.exist(fmUrl, "CheckSum", checkSum)) {
			LOG.log(Level.INFO, fileName + " already exist: [" + checkSum + "]");
			return;
		}

		LOG.log(Level.INFO, "Ingesting: " + " [filename=" + fileName + "]"
				+ " [captureDate=" + captureDate + "]" + " [checkSum="
				+ checkSum + "]");

		String transferService = "org.apache.oodt.cas.filemgr.datatransfer.RemoteDataTransferFactory";
		StdIngester ingester = new StdIngester(transferService);
		Metadata transferMet = new Metadata();

		String fullPath = fileName;
		String filename = Tool.extractName(fullPath);
		String location = Tool.extractFileLocation(fullPath);
		String productName = filename;

		transferMet.addMetadata(ingester.PRODUCT_TYPE, "ImageFile");
		transferMet.addMetadata(ingester.FILE_LOCATION, location);
		transferMet.addMetadata(ingester.FILENAME, filename);
		transferMet.addMetadata(ingester.PRODUCT_NAME, productName);
		transferMet.addMetadata("CaptureDate", captureDate);

		File file = new File(fullPath);


		try {
			URL url = new URL(fmUrl);
			String id = ingester.ingest(url, file, transferMet);
			System.out.println("[i] ingested: " + id);

			List products = QueryTool.searchForProducts(fmUrl, "ProductId", id);
			if (products != null && products.size() > 0) {
				for (Iterator i = products.iterator(); i.hasNext();) {
					Product product = (Product) i.next();
				}
			}
		} catch (IngestException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		PhotoCrawler app = new PhotoCrawler(args);
		app.run();
	}
}
