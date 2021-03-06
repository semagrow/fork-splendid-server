# !/bin/sh

mainclass=de.uni_koblenz.west.splendid.statistics.util.StructurednessAnalyzer

# set classpath
classpath=../bin

# include all jar files in classpath
for jar in ../lib/*.jar; do classpath=$classpath:$jar; done

# run main class with classpath setting
java -cp $classpath -Xmx2g $mainclass $*

