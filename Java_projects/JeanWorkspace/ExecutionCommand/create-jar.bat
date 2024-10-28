del .\Library.jar

call .\compile.bat

cd .\bin 

jar cmf ..\manifest.mf ..\Library.jar . 
cd ..

java -jar .\Library.jar