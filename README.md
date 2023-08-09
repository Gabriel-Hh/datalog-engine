# Welcome to our datalog engine

Please look at the **SETUP.md** file setup, maven, and github instructions.

## Developing
Once you have a local branch with maven setup, you compile and run with:
- `mvn compile` to compile
- `mvn exec:java` to run with the default test file (test.dl)
- `mvn exec:java "-Dexec.args=other_filename"` to run a different datalog file from the **input** folder.
**NEW**
- `mvn exec:java "-Dexec.args=-verbose"` for complete console output.
- `mvn exec:java "-Dexec.args=other_filename -verbose"` to run a different datalog file from the **input** folder AND get complete console output.
- `mvn exec:java "-Dexec.mainClass=c6591.AnotherClass"` to run a different classes main method.

Java files go here:
- `src/main/java/c6591`
