# Assignment 12 — APIs & Databases

This project fetches country data from the REST Countries API, parses the JSON with Gson, stores the relevant fields (name, population, region) into a SQLite database using JDBC, and provides a desktop GUI to fetch, search, refresh, and export results to CSV.

Features
- Fetch all countries from the REST Countries API and populate them into a local SQLite database (upsert semantics).
- Browse stored countries in a JTable.
- Search by name (case-insensitive, partial match) against the DB.
- Refresh view from DB.
- Export currently shown results to CSV.
- Basic error handling around API failures and DB errors.

Schema
- Table `countries` with columns:
	- `id` INTEGER PRIMARY KEY AUTOINCREMENT
	- `name` TEXT NOT NULL UNIQUE
	- `population` INTEGER NOT NULL CHECK(population >= 0)
	- `region` TEXT NOT NULL

Setup (Maven)
1. Ensure Java 11+ and Maven are installed (this project was built against Java 11+, newer JDKs are fine).
2. Build the project:

```powershell
mvn clean package
```

3. Run the JavaFX application (recommended during development):

```powershell
mvn javafx:run
```

4. Alternatively run the packaged jar (may require extra JVM flags for native access on some JDKs):

```powershell
# run directly
java -jar .\target\Assignment-12-APIs-Databases-1.0-SNAPSHOT.jar

# if you see native-access or module warnings, run with native access enabled:
java --enable-native-access=ALL-UNNAMED -jar .\target\Assignment-12-APIs-Databases-1.0-SNAPSHOT.jar
```

The program stores the SQLite file `countries.db` in the working directory.

Notes about dependencies
- Gson (`com.google.code.gson:gson`)
- SQLite JDBC (`org.xerial:sqlite-jdbc`)
- JavaFX (OpenJFX) — `org.openjfx:javafx-controls` and `org.openjfx:javafx-fxml` (Windows classifier) added to `pom.xml`.
 - JavaFX (OpenJFX) — `org.openjfx:javafx-controls` and `org.openjfx:javafx-fxml` (Windows classifier) added to `pom.xml`.

## AI Reflection

This project was assisted and refactored using an AI coding assistant to port the GUI from Swing to JavaFX and update build tooling.

- Automated tasks performed: added OpenJFX dependencies and the `javafx-maven-plugin` to `pom.xml`; created a new `MainFX` JavaFX `Application`; updated `APIProgram` to launch JavaFX and provide a shared `DBManager` instance.
- Design rationale: preserve the existing data layer (`DBManager`, `Country`, `CSVExporter`) and use background tasks for IO so the UI remains responsive. The port preserves original behavior (fetch → upsert → list/search → export) while switching UI toolkit.
- Limitations and risks: the AI made a best-effort port, but manual review is recommended for UI polish, accessibility, input validation, and concurrency edge cases. Packaging JavaFX into a single shaded jar can produce warnings about modular/native resources; prefer `mvn javafx:run` for development or produce a platform-specific runtime image for distribution.
- Suggested next steps (manual): review `MainFX` for styling and UX improvements, add unit tests for `DBManager` and `APIClient`, and consider `jpackage` or `jlink` to create a distributable native image that includes JavaFX native libraries.

