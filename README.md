# Welcome to our datalog engine

Please look at the **SETUP.md** file setup, maven, and github instructions.

## Developing
Once you have a local branch with maven setup, you compile and run with:
- `mvn compile` to compile
- `mvn exec:java` to run with the default test file (test.dl)
- `mvn exec:java "-Dexec.args=other_filename"` to run a different datalog file from the **input** folder.
- `mvn exec:java "-Dexec.mainClass=c6591.AnotherClass"` to run a different classes main method.

