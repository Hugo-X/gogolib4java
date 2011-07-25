@set JDK_BIN=C:\Java\jdk1.6.0_24\bin
@set path=%path%;%JDK_BIN%

javadoc -author -version -d docs -package -windowtitle "gogolib API Specification"  GoGoClient.java GoGoSessionManager.java package-info.java
pause
