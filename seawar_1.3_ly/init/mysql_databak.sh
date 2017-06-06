#######################################################################################################
#!/bin/sh
DUMP=/usr/bin/mysqldump
OUT_DIR=/mnt/
LINUX_USER=root 
DB_NAME=seawar
DB_USER=root 
DB_PASS=123123 
DAYS=14
cd $OUT_DIR
DATE=`date +%Y_%m_%d`
OUT_SQL="$DATE.sql"
TAR_SQL="mysqldata_bak_$DATE.tar.gz"
$DUMP -u$DB_USER -p$DB_PASS $DB_NAME --default-character-set=utf8 --hex-blob --opt -Q -R --skip-lock-tables> $OUT_SQL 
tar -czf $TAR_SQL ./$OUT_SQL 
rm $OUT_SQL 
chown $LINUX_USER:$LINUX_USER $OUT_DIR/$TAR_SQL 
find $OUT_DIR -name "mysqldata_bak*" -type f -mtime +$DAYS -exec rm {} \; 
