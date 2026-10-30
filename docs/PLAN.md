# easypub – Plan für Version 1

Ziel von V1: EPUBs aus selbst festgelegten Ordnern (Libraries) verwalten und lesen.
easypub ist dabei nicht nur ein Reader, sondern eine Bücherei.

## Grundkonzept: Libraries

Angelehnt an die Vaults in Obsidian.

- Eine **Library** ist ein normaler Ordner auf dem Gerät, den man in der App als EPUB-Quelle registriert.
- Die Auswahl läuft über den Ordner-Explorer von Android (Storage Access Framework, `ACTION_OPEN_DOCUMENT_TREE`). Die App erhält dauerhaften Zugriff nur auf diesen Ordner (`takePersistableUriPermission`).
- Im selben Dialog kann man auch einen neuen Ordner anlegen. Damit sind „Ordner als Lib bestimmen“ und „Lib anlegen“ beide abgedeckt.
- Unterordner werden **rekursiv** mit durchsucht.
- Die Ordner sind unabhängig von der App. In V1 gibt es keine eigene Struktur im Ordner (kein `.easypub`).
- Wird eine Library in der App entfernt, verschwindet nur die Registrierung. Die Dateien bleiben unangetastet.
- Hinweis: Ab Android 11 lässt sich der Download-Ordner selbst nicht auswählen, wohl aber Unterordner darin (z. B. `Download/Bücher`).

### Berechtigungen

Die App soll **berechtigungsarm** bleiben.

- Kein gerätweiter Scan nach EPUBs und keine Berechtigung „Zugriff auf alle Dateien“
- Zugriff ausschließlich auf die registrierten Library-Ordner

## Screens

1. **Libraries (Startseite):** ein Grid mit Kacheln aller registrierten Libraries und ein „+“ zum Hinzufügen
2. **Library-Ansicht:** ein Grid der Bücher mit Cover, darunter Titel und Autor, plus Fortschrittsanzeige. Oben eine Suchleiste, die nach Titel und Autor sucht.
3. **Reader:** zeigt den Buchinhalt an
4. **Einstellungen:** App-Theme (System / Hell / Dunkel)

## Reader

- **Durchgehendes vertikales Scrollen**, kein seitenweises Blättern
- Ein Tipp auf den Inhalt blendet oben eine **Leiste** ein mit:
  - einem Umschalter **Hell/Dunkel nur für den Buchinhalt**, unabhängig vom App-Theme (z. B. dunkle App und helles Buch)
  - der **Schriftgröße**
- Die Reader-Einstellungen gelten global für alle Bücher und bleiben gespeichert.
- Die Farben des Buchinhalts nutzen dieselbe Pastell-Palette wie die App.

## Lesefortschritt

- Gespeichert wird die **aktuelle Stelle** als Readium-`Locator`, also Kapitel, Position im Kapitel und ein kurzer Textausschnitt. Der Wert hängt am Text und bleibt deshalb auch bei geänderter Schriftgröße korrekt.
- Das Buch öffnet sich wieder an der aktuellen Stelle.
- Das Cover in der Library zeigt den Fortschritt in Prozent, ebenfalls basierend auf der aktuellen Stelle.
- Der Fortschritt wird in der App-Datenbank gespeichert.
- Später: die am weitesten gelesene Stelle, siehe [#1](https://github.com/nevslie/easypub/issues/1).

## Design

- Modern, leichtgewichtig und minimalistisch, angelehnt an iOS und Obsidian
- Material 3 als Basis, aber reduziert: viel Weißraum, abgerundete Ecken, dezente Schatten, ruhige Typografie
- Eigene **Pastell-Palette** für **Light und Dark Mode**

## Technik

| Bereich | Wahl |
|---|---|
| Sprache | Kotlin |
| UI | Jetpack Compose (Material 3), Navigation Compose |
| EPUB | Readium Kotlin Toolkit (Streamer für Metadaten und Cover, Navigator für die Anzeige) |
| Datenbank | Room (Index aus Libraries, Büchern und Fortschritt) |
| Einstellungen | DataStore (App-Theme, Reader-Theme, Schriftgröße) |
| Bilder | Coil (Cover) |
| Architektur | MVVM (ViewModel + Repository), Dependency Injection vorerst manuell |
| Min. Android | API 26 (Android 8) |

Die **Dateien sind die Quelle der Wahrheit**. Die Datenbank ist nur ein Index bzw. Cache und kann jederzeit durch einen neuen Scan aufgebaut werden. Cover werden beim Scan extrahiert und im internen Speicher der App abgelegt (`files/covers`). Der Cache-Ordner wäre ungeeignet, weil Android ihn jederzeit leeren darf und unveränderte Bücher beim Scan nicht neu eingelesen werden.

### Datenmodell

- `Library`: id, Name, Ordner-URI, hinzugefügt am
- `Book`: id, libraryId, Datei-URI, relativer Pfad, Titel, Autor, Cover-Pfad, EPUB-Identifier, Dateigröße, Änderungsdatum (zum Erkennen geänderter Dateien), aktueller Locator, Fortschritt (0–1), zuletzt geöffnet

So ist es für spätere Erweiterungen gedacht: Tags und Kategorien kommen als eigene Tabellen mit n:m-Verknüpfung zu `Book` dazu. Am bestehenden Modell muss dafür nichts umgebaut werden.

## Verteilung & Testen

- Kein Play Store. Die APK wird über **GitHub Actions** gebaut und als **GitHub Release** veröffentlicht.
- Die GitHub Actions richtet nevslie **selbst als Übung** ein (bei Bedarf mit Unterstützung). Sie sind nicht Teil der V1-Meilensteine und kommen später.
- Für signierte Releases muss ein Keystore als GitHub-Secret hinterlegt werden.
- Getestet wird in Android Studio, im Emulator oder auf dem eigenen Gerät.

## Meilensteine V1

1. **Projekt-Setup:** Gradle, Compose, Theme (Pastell, Light/Dark), Navigation
2. **Libraries:** hinzufügen (Ordnerauswahl), entfernen, Grid anzeigen, Berechtigungen dauerhaft halten
3. **Scan & Index:** EPUBs rekursiv finden, Metadaten und Cover über Readium auslesen, in Room speichern, Änderungen erkennen
4. **Library-Ansicht:** Bücher-Grid mit Cover, Titel, Autor und Fortschritt, Suche
5. **Reader:** Readium Navigator im Scroll-Modus, Leiste mit Hell/Dunkel-Umschalter und Schriftgröße, Lesefortschritt speichern und wiederherstellen
6. **Feinschliff:** leere Zustände, Fehlerfälle (z. B. Ordner gelöscht oder Zugriff entzogen), App-Icon

## Später (nicht V1)

- Am weitesten gelesene Stelle ([#1](https://github.com/nevslie/easypub/issues/1))
- Tags und Kategorien, um Libraries in Abteilungen zu unterteilen
- Auswahl der Schriftart im Reader
- Ansicht „Alle Bücher“ über alle Libraries hinweg
- Fortschritt als Datei in der Library speichern, damit er eine Neuinstallation überlebt
