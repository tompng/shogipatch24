#! /bin/sh
mkdir -p classes
cd src
javac -cp ../dojo*.jar -d ../classes *.java */*/*.java */*/*/*/*.java
cd ../classes
jar cf ../res/dojo.jar *.class */*/*.class */*/*/*/*.class
dojos="OsakaDojo TokyoDojo"
for dojo in $dojos; do
  echo "Manifest-Version: 1.0\nMain-Class: $dojo" > MANIFEST
  jar cvfm ../$dojo.jar MANIFEST Dojo.class $dojo.class
done
