#!/bin/bash

COUNT=$1
PRODUCTNAME=$2

if [ -z $PRODUCTNAME ] ; then
  echo "usage: $0 <count> <producttype>"
  exit
fi


for (( c=1 ; c<=$COUNT; c++))
do 
   echo $PRODUCTNAME $c
   ./filemgr-client --url http://localhost:9000 --operation --getFirstPage --productTypeName $PRODUCTNAME | awk -F, '{ print $1 }' | awk -F= '{ print $2 }' > /tmp/productListing

   for f in `cat /tmp/productListing`; do java -Djava.ext.dirs=../lib org.apache.oodt.cas.filemgr.tools.DeleteProduct --fileManagerUrl http://localhost:9000 --productID $f ; done
 
done

