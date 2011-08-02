#!/bin/bash
#
# init script for XmlRpcFileManager
#
# chkconfig: 345 88 22
# description: CAS File Manager
#
# Copyright (c) 2005 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
# $Id: filemgr.sh 1979 2011-07-23 06:20:30Z jtran $

source config.sh

if [ -z $JAVA_HOME ] ; then
    JAVA_HOME=/usr/local/java
    export JAVA_HOME
fi

if [ -z $RUN_HOME ] ; then
    RUN_HOME=${CAS_FILEMGR_HOME}/run
    export RUN_HOME
fi

PATH=${JAVA_HOME}/bin:${CAS_FILEMGR_HOME}/bin:/usr/bin:/bin:/usr/sbin:/sbin
export PATH

## make sure that cas file manager has a run directory
## just to be on the safe side
mkdir -p ${RUN_HOME}
mkdir -p ${CAS_FILEMGR_HOME}/logs

# See how we were called.
case "$1" in
  start)
        echo -n "Starting cas file manager: "
        $JAVA_HOME/bin/java \
       	    -Djava.ext.dirs=${CAS_FILEMGR_HOME}/lib \
       	    -Djava.util.logging.config.file=${CAS_FILEMGR_HOME}/etc/logging.properties \
    	    -Dorg.apache.oodt.cas.filemgr.properties=${CAS_FILEMGR_HOME}/etc/filemgr.properties \
        	org.apache.oodt.cas.filemgr.system.XmlRpcFileManager \
        	--portNum ${CAS_FILEMGR_SERVER_PORT} &       
        echo $! > ${RUN_HOME}/cas.filemgr.pid 
        echo "OK"
        sleep 5
        ;;
  stop)
        echo -n "Shutting down cas file manager: "
        kill `cat ${RUN_HOME}/cas.filemgr.pid`
        rm -f ${RUN_HOME}/cas.filemgr.pid
        echo "OK"
        ;;
  restart)
        $0 stop
        $0 start
        ;;
  status)
        if [ -e ${RUN_HOME}/cas.filemgr.pid ] ; then
           pid=`cat ${RUN_HOME}/cas.filemgr.pid`
           echo "cas filemgr is running with pid: $pid" 
        else
           echo "cas filemgr is not running"
        fi
        ;;
  *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
esac

exit 0
