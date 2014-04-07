@echo off

set JAVA_OPTS=-Djava.library.path=x64

set JAVA_CMD=java.exe
set CLASS_PATH=..\lib\*
set BOOT_CLASS=kr.ac.scnu.cn.gogolib.GoGoMonitor

%JAVA_CMD% -cp %CLASS_PATH% %JAVA_OPTS% %BOOT_CLASS%

