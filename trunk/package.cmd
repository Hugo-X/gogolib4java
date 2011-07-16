@set ZIP_BIN=D:\Program Files\7-Zip
@set path=%path%;%ZIP_BIN%

7z a gogolib-all-files.zip ReadMe.txt gogolib.jar docs "GoGo Server Command Table.pdf"
pause