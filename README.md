# elwis-anki-parser
ELWIS SBF Parser for (semi-)automatically creating ANKI Decks

# ANKI Flashcard (Karteikarten) Deck für den SBF Binnen (Motor+Segel)

kompiliert direkt von elwis.de (Stand: 1. Juni 2017)

Download [letzte Version](https://github.com/kralo/elwis-anki-parser/releases/latest)

# ANKI Deck für den SBF See (ohne Navigationsaufgaben)

kompiliert direkt von elwis.de (Stand: 1. Juni 2017)

Download [letzte Version](https://github.com/kralo/elwis-anki-parser/releases/latest)


##Vorgehen:
1. dieses Repository clonen
2. einen Ordner `img` im gleichen Verzeichnis wie das Skript anlegen
3. Alle Bilder von der zugehörigen ELWIS-Seite (z.B. SBF-Binnen) nach `img` herunterladen. (z.B. Firefox-Seiteninformationen - Media - Grafiken speichern)
4. launch.sh editieren, auf richtigen Scala-Pfad achten, dann ausführen. Es sollte eine fragen.csv erstellt werden und die Bilderdateien werden auch umbenannt
5. Anki Sammlung anlegen, neuer Kartentyp mit den Feldern (QuestionNr, Question, Answer, Distractor1, Distractor2, Distractor3)
6. Siehe Anki-Template für css, front & back
7. In Anki Datei-Importieren, die csv in den Kartenstapel importieren
8. Das Anki Deck exportieren. Die Datei als (zip)Archiv öffnen. Alle Dateien, die nur eine Zahl als Dateinamen aus "img" haben hinzufügen. Datei media hinzufügen.
9. Das Deck in Anki reimportieren, jetzt sollten alle Medien vorhanden sein.

## Known Limitations
The shuffling of answers will be redone when you want to see the answer. There is no known solution to this right now, because the html page is rerendered every time.

