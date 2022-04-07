echo=off


all: main

main:
	mvn 
	mvn exec:java -Dexec.mainClass="com.pdc.sol.App" -Dexec.cleanupDaemonThreads=false -Dexec.args=$@

