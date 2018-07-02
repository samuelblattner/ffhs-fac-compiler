# Emotica
Semesterprojekt für FAC.BSc INF 2014.ZH1.FS18

6/2018 Samuel Blattner

## Inhalt
Emotica ist eine einfache DuckTyping-Skript-Sprache, deren Befehle über Emoticons des Unicode-Zeichensatzes geschrieben werden.
Das Projekt umfasst einen Scanner (jFlex), Parser (CUP) und einen Java-Validator, Interpreter sowie -Compiler.
Im aktuellen Zustand sind ein paar wenige Funktionen der Sprachdefinition umgesetzt. Dies umfasst: Zuweisen von Variablen, 
definieren und Aufrufen von Funktionen, Ausführen von Schlaufen.

## Installation
Das Projekt wurde mit Gradle eingerichtet und sollte in die meisten IDEs (z.B. IntelliJ) als solches importiert werden
können. Sämtliche Bibliotheken werden damit automatisch hinzugefügt und die Projekteinstellungen automatisch eingerichtet. 

## Aufbau
Das Projekt ist in mehrere Module aufgeteilt:

### Sprache
Das Verzeichnis ``language`` enthält die Symbol-Definitionen (``emotica.flex``), die Sprach-Definition (``emotica.cup``) für 
den Parser sowie eine allgemeine Beschreibung der Sprache (``fac_sprachentwurf_samuelblattner.pdf``).

### Hauptprojekt
Das Verzeichnis ``src/main/java`` enthält sämtlichen Code, der zum Scannen, Parsen, Validieren sowie Interpretieren
der Sprache notwendig ist. Im Folgenden werden die einzelnen Bestandteile kurz erläutert:

#### Interface *ifInstructionVisitor*
Jedem Validieren oder Interpretieren eines Skripts geht das Erstellen eines Parse-Baumes voraus. Der Baum besteht aus
einer hierarchischen Gliederung der einzelnen Instruktionen (``*Instruction``-Klassen), die im Skript enthalten sind, beginnend dem Skript als
Ganzes (Klasse ``ScriptInstruction``). Klassen, die diesen Baum ablaufen können, werden in diesem Projekt "Visitors" genannt
und implementieren das ``ifInstructionVisitor``-Interface. Damit ist jeder Visitor fähig, spezifisch auf die einzelnen
Instruktionen der Sprache zu reagieren.

#### Abstrakte Klasse *AbstractScopedVisitor*
Ein Scoped Visitor verwendet das Wertobjekt ``Scope``, sowie einige Hilfsmethoden, um den Zustand der Umgebung zu 
speichern. Bei einem Funktionsaufruf wird ein neues, "inneres" Scope erzeugt, das lokale Variablen aufnehmen kann.
Nach dem Funktionsaufruf wird dieses Scope wieder gelöscht. Variablen aus äusseren Scopes sind aus inneren Scopes
sichtbar, nicht aber umgekehrt. Das Wertobjekt speichert ausserdem die Reihenfolge, in der die Variablen
definiert wurden. Dies ermöglicht später bei der Kompilierung eine einfache Referenzierung von lokalen Variablen.   

#### Klasse *EmoticaValidator*
Der Validator läuft den Parse-Baum ab und überprüft die Integrität eines Skripts. In der aktuellen Umsetzung überprüft der
Validator ausschliesslich, ob sämtlichen verwendeten Variablen ein Wert zugewiesen wurde und warnt, falls Variablen
zwar initialisiert aber nirgends verwendet wurden. 

#### Klasse *EmoticaInterpreter*
Wie der Validator läuft auch der Interpreter den Parse-Baum ab. Im Gegensatz zum Validator führt aber der Interpreter
die einzelnen Instruktionen innerhalb der JVM direkt aus. 

### Shell
Das Verzeichnis ``shell`` enthält Code für eine einfache Emotica-Shell, in der Befehle eingegeben und ausgeführt werden
können. Die Shell verwendet den ``EmoticaValidator``, um die Eingabe zu validieren sowie den ``EmoticaInterpreter``, um 
den eingegebenen Code auszuführen. Die Shell kann mit der bereits vorhandenen Gradle-Run-Konfiguration ``emoticas:shell[run]``
ausgeführt werden.


### Compiler
Das Verzeichnis ``compiler`` enthält den Code für den Emotica-Compiler. Ähnlich wieder Interpreter läuft der Compiler den
Parse-Baum ab, führt die Instruktionen jedoch nicht aus, sondern übersetzt sie mit der ``asm``-Bibliothek in
ausführbaren Java-Byte-Code. Der Compiler nimmt über die Kommandozeile den Pfad zur Skript-Datei entgegen. In der bereits
vorhandenen Grade-Konfiguration ``emotica:compiler [run]`` wird standardmässig das Programm ``helloworld.emo`` im
Verzeichnis ``programs`` übergeben.

Der Compiler erzeugt eine Klasse ``EmoticaClass`` und fügt dieser eine main-Methode hinzu, sodass der Byte-Code direkt 
mit dem Befehl ``java -cp <classpath> EmoticaClass`` ausgeführt werden kann. Globale Variablen werden als statische
Felder gespeichert, lokale Variablen entsprechend im Frame der jeweils aufgerufenen Methode.

Die genaue Funktionsweise ist im Code beschrieben. 