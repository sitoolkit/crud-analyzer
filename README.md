
## How to run

1. Create list file of your project dependency
1. Create proprety file of this tool
1. Build and run this tool


### Create list file of  your project dependency


```
cd \path\to\your\project
mvn dependency:build-classpath -Dmdep.outputFile=jar-list.txt
```


### Create proprety file of this tool

file name : "crud-analyzer.properties"

```
srcDir=\\path\\to\\your\\project\\src\\main\\java
resDir=\\path\\to\\your\\project\\src\\main\\resources
repositoryPathPattern=.*Repository.xml
```


### Build and run this tool

```
cd \path\to\your\workspace
git clone https://github.com/sitoolkit/crud-analyzer.git
    or download zip and extract
cd crud-analyzer
mvnw clean package -Dmaven.test.skip=true
copy \path\to\your\project\jar-list.txt .
copy \path\to\your\crud-analyzer.properties .
java -Xms2g -Xmx2g -jar target\crud-analyzer-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

