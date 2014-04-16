#!/usr/bin/env sh
java -cp .:./lib/scala-library.jar:./lib/jna.jar:./lib/jfxrt.jar:./lib/purejavacomm-0.0.17.jar:muterm2.jar muterm.MuTerm
#java -server -Xms128m -Xmx512m -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC -XX:SoftRefLRUPolicyMSPerMB=50 -Dsun.java2d.opengl=true -cp .:./lib/scala-library.jar:./lib/jna.jar:./lib/jfxrt.jar:./lib/purejavacomm-0.0.17.jar:muterm2.jar muterm.MuTerm

