# Author Blake Preston
# Version 2020-11-17

# The following make commands are supported
# make		-- makes Epidemic.class
# make test	-- runs a demo of the simulator
# make clean	-- deletes all file created above
# make html	-- makes website of internal documentation

# compiles all of the needed files
make:
	javac *.java
	
# runs a test on the given iput file
test:
	java Epidemic testepi
	
# builds the given javadoc files
html:
	javadoc *.java

# cleans all of the generated files
clean:
	rm -f *.class *.html package-list *.js stylesheet.css *.zip
	
##########
# primary make target

UtilityClasses = Error.class MyScanner.class Simulator.class
Epidemic.class: Epidemic.java $(UtilityClasses)
	javac RoadNetwork.java

##########
# secondary make target -- simulation model

Employee.class: Employee.java Person.class WorkPlace.class $(UtilityClasses)
	javac Employee.java

HomePlace.class: HomePlace.java Place.class Person.class $(UtilityClasses)

MyRandom.class: MyRandom.java
	javac MyRandom.java

Person.class: Person.java Place.class Employee.class $(UtilityClasses)
	javac Person.java

Place.class: Place.java WorkPlace.class HomePlace.class $(UtilityClasses)
	javac Place.java
        
WorkPlace.class: WorkPlace.java Employee.class Place.class $(UtilityClasses)
	javac WorkPlace.java

# secondary make target -- utility classes

MyScanner.class: MyScanner.java Error.class
	javac MyScanner.java

Error.class: Error.java
	javac Error.java

Random.class: Random.java
	javac Random.java

Simulator.class: Simulator.java
	javac Simulator.java
