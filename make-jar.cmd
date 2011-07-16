@set JDK_BIN=C:\Java\jdk1.6.0_24\bin
@set path=%path%;%JDK_BIN%

javac *.java -d . && jar cvfm gogolib.jar manifest.txt ReadMe.txt kr
pause
