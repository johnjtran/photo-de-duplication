

package com.pcwerk.dedup;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

public class ImageExtractor extends AbstractFilemgrMetExtractor implements
        CoreMetKeys {
	private Logger LOG = Logger.getLogger(ImageProductVersioner.class.getName());
	
    public ImageExtractor() {
    }

    public Metadata doExtract(Product product, Metadata met)
            throws MetExtractionException {
        Metadata extractMet = new Metadata();
        merge(met, extractMet);

        File prodFile = getProductFile(product);
        
        String captureDate = ImageInfo.extract(prodFile);
        String filename = prodFile.getAbsolutePath();
        String checkSum = Tool.computeSHA(filename); 
        	
        LOG.log(Level.INFO, "[i] Filename: " + filename);
        LOG.log(Level.INFO, "[i] CaptureDate: " + captureDate);
        LOG.log(Level.INFO, "[i] CheckSum: " + checkSum);
        
        extractMet.addMetadata("CaptureDate", captureDate);
        extractMet.addMetadata("CheckSum", checkSum);
        
        return extractMet;
    }

    public void doConfigure() {
        if (this.configuration != null) {
        	// manage configuration
        }
    }

}
