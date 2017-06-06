set app=../app/
java -server -Xms1024m -Xmx1024m  -Xmn600m -Xss256k -XX:+PrintGCDetails -Xloggc:gc.log -Dfile.encoding=UTF-8 -cp %app%log4j-1.2.16.jar;%app%bcprov-jdk16-145-1.jar;%app%javapns-jdk16-163.jar;%app%commons-io-2.0.1.jar;%app%commons-lang-2.5.jar;%app%bsh-2.0b4.jar;%app%js.jar;%app%mina-core-2.0.4.jar;%app%slf4j-api-1.6.4.jar;%app%mysql-5.1.6.jar;%app%jedis-2.1.0.jar;%app%slf4j-log4j12-1.6.4.jar;%app%mustang.zip;%app%shelby.zip;%app%. mustang.xlib.Start server.start.cfg

pause