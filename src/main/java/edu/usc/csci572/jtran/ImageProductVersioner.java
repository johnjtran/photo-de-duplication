// $Id: ImageProductVersioner.java 1985 2011-07-25 22:55:41Z jtran $

package edu.usc.csci572.jtran;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.versioning.Versioner;
import org.apache.oodt.cas.metadata.Metadata;

public class ImageProductVersioner implements Versioner {

	private Logger LOG = Logger
			.getLogger(ImageProductVersioner.class.getName());

	public ImageProductVersioner() {
		super();
	}

	public void createDataStoreReferences(Product product, Metadata metadata)
			throws VersioningException {

		String captureDate = metadata.getMetadata("CaptureDate").replaceAll(
				"\\s+", "/");
		String productName = product.getProductName();
		String productRepoPath = product.getProductType()
				.getProductRepositoryPath();

		LOG.log(Level.INFO, "  [captureDate=" + captureDate + "] ");
		LOG.log(Level.INFO, "  [productName=" + productName + "] ");
		LOG.log(Level.INFO, "  [productRepoPath=" + productRepoPath + "] ");

		createBasicDataStoreRefsFlat(productName, productRepoPath, captureDate,
				product.getProductReferences());
	}

	@SuppressWarnings("deprecation")
	private void createBasicDataStoreRefsFlat(String productName,
			String productRepoPath, String captureDate,
			List<Reference> references) {
		for (Iterator<Reference> i = references.iterator(); i.hasNext();) {
			Reference r = i.next();

			String dataStoreRef = null;
			String productRepoPathRef = null;

			try {
				productRepoPathRef = new File(new URI(productRepoPath)).toURL()
						.toExternalForm();

				if (!productRepoPathRef.endsWith("/")) {
					productRepoPathRef += "/";
				}

				dataStoreRef = productRepoPathRef + captureDate + "/"
						+ new File(new URI(r.getOrigReference())).getName().replaceAll("\\s", "%20");
				
			} catch (IOException e) {
				LOG.log(Level.WARNING,
						"VersioningUtils: Error generating dataStoreRef for "
								+ r.getOrigReference() + ": Message: "
								+ e.getMessage());
			} catch (URISyntaxException e) {
				LOG.log(Level.WARNING,
						"VersioningUtils: Error generating dataStoreRef for "
								+ r.getOrigReference() + ": Message: "
								+ e.getMessage());
			}

			LOG.log(Level.FINE, "VersioningUtils: Generated data store ref: "
					+ dataStoreRef + " from origRef: " + r.getOrigReference());

			r.setDataStoreReference(dataStoreRef);
		}

	}

}
