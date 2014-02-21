#Classpath Searcher (2002)
This is a copy of an app I wrote in 2002 to search for classes
within jar files on your classpath. It will search the jars on the classpath
for the app itself, and from the menu in the app you can add other jars from other locations to be
searched.

##Source Code Archaeology
This is obviously some old code. It's 2014 now, so I wrote this 12 years ago. I'm not intending on
updating or maintaining this code in it's current form, but I thought it would be useful to keep a copy
here in it's original state, as there's some parts of this app that would be useful
to convert to using Java 8 Lambdas, so I'm using this as a starting point.

The code here is in it's original format from 2002, other than adding a Maven pom.xml to build and
and package it as a jar, and I removed the dependencies on Log4J and Commons-Logging for
now, just to make it easier to build and run as an executable jar.

##To build and run
Build with 'mvn package', and run from the jar with java -jar [jarname]'.