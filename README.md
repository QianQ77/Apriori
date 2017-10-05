# Apriori
An implement of Apriori Algorithm using BitSet.

I. Directly run:

run command as below:
java -jar AprioriImplement.jar {min_sup} {k} transactionDB.txt output.txt

{min_sup}: the number of minimal support count, which should be an integer;
{k}: it should be an integer;
“transactionDB.txt” is the path of input Data file name.
“output.txt” is the path of output file name. The file contains all the results.


II. Build Jar from source code and run:

1. How to build Jar:

(1) on Unix-like platforms such as Linux and Mac OS X

	run command as below:
	./gradlew
	Then run this command:
	./gradlew fatJar

(2) on Windows

	run command as below:
	gradlew
	Then run this command:
	gradlew fatJar

In this way, the AprioriImplement.jar will be in the folder: build/libs


2. How to run the code:

run command as below:
java -jar build/libs/AprioriImplement.jar {min_sup} {k} transactionDB.txt output.txt

{min_sup}: the number of minimal support count, which should be an integer;
{k}: it should be an integer;
“transactionDB.txt” is the path of input Data file name.
“output.txt” is the path of output file name. The file contains all the results.


