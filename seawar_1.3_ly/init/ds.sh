#!/bin/sh

#/sbin/fuser -k -u -n file mina.lo
kill -9 $(netstat -tlnp|grep 7610|awk '{print $7}'|awk -F '/' '{print $1}')

sleep 2

#>../log/ds.log


java -server -Xms2560m -Xmx2560m -Xmn2048M -XX:SurvivorRatio=4 -XX:PermSize=64m -XX:MaxPermSize=64m -Dfile.encoding=UTF-8 -cp ../app/log4j-1.2.16.jar:../app/bsh-2.0b4.jar:../app/js.jar:../app/mina-core-2.0.4.jar:../app/slf4j-api-1.6.4.jar:../app/slf4j-log4j12-1.6.4.jar:../app/mysql-5.1.6.jar:../app/jedis-2.1.0.jar:../app/bcprov-jdk16-145-1.jar:../app/javapns-jdk16-163.jar:../app/commons-io-2.0.1.jar:../app/commons-lang-2.5.jar:../app/. mustang.xlib.Start server.start.cfg

#tail ../init/log/mina.log -f
