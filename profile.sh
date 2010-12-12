#!/bin/sh
java -cp bin -Xms512M -Xmx1024M -Xprof org.boblycat.abbots.Solver $*
