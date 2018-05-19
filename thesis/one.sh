#! /bin/sh
java -Xmx2048M -cp target:lib/ECLA.jar:lib/DTNConsoleConnection.jar:lib/jxl.jar core.DTNSim $*
