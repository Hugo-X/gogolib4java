@ECHO off
ECHO If you don't Launch4j, download it from http://launch4j.sourceforge.net
SET lanuch4j_home="C:\Program Files\Launch4j"
SET PATH=%lanuch4j_home%;%PATH%
launch4jc gogomonitor.xml
pause
