

CWD=`pwd`
echo $CWD
cd .. 
export PDD_HOME=`pwd`
cd $CWD

export DEPLOY_DIR=${PDD_HOME}
export DATA_DIR=${PDD_HOME}/data

export CAS_FILEMGR_SERVER_PORT=9000
export CAS_FILEMGR_HOME=${DEPLOY_DIR}

export CAS_FILEMGR_ARCHIVE=${DATA_DIR}/archive
export CAS_FILEMGR_LUCENE=${DATA_DIR}/lucene

mkdir -p $DATA_DIR
mkdir -p $CAS_FILEMGR_ARCHIVE
