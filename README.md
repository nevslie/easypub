# easypub

Eine Android-App, die EPUBs nicht nur anzeigt, sondern als Bücherei verwaltet.
Bücher liegen in normalen Ordnern auf dem Gerät, die in der App als **Libraries** registriert werden, ähnlich wie Vaults in Obsidian.

Plan und Entscheidungen: [docs/PLAN.md](docs/PLAN.md)

## Projekt öffnen

1. Den Ordner in Android Studio öffnen (*File → Open*).
2. Android Studio lädt Gradle, das Android SDK (API 36) und alle Abhängigkeiten.
3. Die Run-Konfiguration `app` auf einem Emulator oder Gerät starten.

Unit-Tests ausführen:

```bash
./gradlew test
```

## Aufbau

```
app/src/main/java/io/github/nevslie/easypub/
├── AppContainer.kt          manuelle Dependency Injection
├── data/
│   ├── db/                  Room: Libraries, Bücher, Lesefortschritt
│   ├── library/             Libraries verwalten, Ordner scannen
│   ├── readium/             EPUBs öffnen (Readium)
│   └── settings/            DataStore: App-Theme, Reader-Einstellungen
├── domain/                  reine Logik (Suche, Fortschritt)
└── ui/
    ├── libraries/           Startseite mit Library-Kacheln
    ├── library/             Bücher einer Library mit Suche
    ├── reader/              Reader (Readium-Navigator + Leiste)
    ├── settings/            Einstellungen
    └── theme/               Pastell-Farben, Typografie
```

## Technik

Kotlin, Jetpack Compose (Material 3), Room, DataStore, Coil und das [Readium Kotlin Toolkit](https://github.com/readium/kotlin-toolkit). Mindestversion ist Android 8 (API 26).
