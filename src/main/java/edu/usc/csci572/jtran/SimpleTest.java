package edu.usc.csci572.jtran;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;

public class SimpleTest {
	private static String fmUrl = "http://localhost:9000";
	
	public static void main(String[] args) {		
		QueryTool query;
		try {
			query = new QueryTool(new URL(fmUrl));
			
			List products = query.
			searchForProducts(fmUrl, "CheckSum", 
			"b0d8de46e12fe18d6c166148a682490660279f8ce391d0526cb4ef304bfe35fd");
			
			if (products != null && products.size() > 0) {
				for (Iterator i = products.iterator(); i.hasNext();) {
					Product product = (Product) i.next();
					Metadata metadata = query.getMetadata(product);
					
					for (String key: metadata.getAllKeys()) {
						System.out.println(key + "=" + metadata.getMetadata(key));
					}
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
