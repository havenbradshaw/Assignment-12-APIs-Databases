# Assignment 12 — APIs & Databases

This project fetches country data from the REST Countries API, parses the JSON with Gson, stores the relevant fields (name, population, region) into a SQLite database using JDBC, and provides a desktop GUI to fetch, search, refresh, and export results to CSV.

Note: The UI was ported from Swing to JavaFX. The Swing sources remain in the repository (`MainGUI`) but the application now launches the JavaFX UI by default.

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

Design notes
- The JavaFX UI mirrors the original Swing behavior: Fetch from API (background task), upsert to SQLite (transactional batch), list, search (LIKE %q%), and export CSV.
- Long-running work runs on background `Task`s and updates the UI on the FX Application Thread.
- The DB schema and `DBManager` behavior (transactions, upsert by name) are unchanged.

Further improvements
- Better progress reporting and cancellation support.
- Paging or lazy-loading for very large datasets.
- Add unit tests for `DBManager` and `APIClient`.
- Produce native runtime images via `jlink`/`jpackage` for distribution.

Troubleshooting
- If you see warnings about `Restricted methods` or `native access`, the app is still functional; to silence them add JVM flags like `--enable-native-access=ALL-UNNAMED` or `--enable-native-access=javafx.graphics` when running a packaged jar.
- If you see shading/module warnings during `mvn package` (from the shade plugin), prefer `mvn javafx:run` for development to avoid bundling modular JavaFX jars into an uber-jar.
- If controls look small on high-DPI displays, try setting JavaFX system properties for DPI scaling in your JVM args.

Files changed / added
- `pom.xml` — updated: added JavaFX dependencies and `javafx-maven-plugin` (see property `javafx.version`).
- `src/main/java/com/assignment12/api/APIProgram.java` — now launches JavaFX and exposes a `getDbManager()` accessor used by the FX app.
- `src/main/java/com/assignment12/api/MainFX.java` — new JavaFX `Application` that replaces the Swing `MainGUI` at runtime.
- `src/main/java/com/assignment12/api/MainGUI.java` — original Swing GUI (kept for reference).