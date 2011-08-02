#!/bin/sh

./crawler_launcher \
	-cid StdProductCrawler \
	-fm http://localhost:9000 \
	-ct org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory \
	-mfx met \
	-pp $* 
