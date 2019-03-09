decompile:
	curl https://lambci.s3.amazonaws.com/fs/java8.tgz | tar -zx
	java -cp /Applications/IntelliJ\ IDEA.app/Contents/plugins/java-decompiler/lib/java-decompiler.jar org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler -hdc=0 -dgs=1 -rsy=1 -lit=1 var/runtime/lib/LambdaSandboxJava-1.0.jar .
	mkdir -p src/main/java/
	unzip LambdaSandboxJava-1.0.jar -d src/main/java/
