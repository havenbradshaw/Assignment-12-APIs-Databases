# Assignment 12 — APIs & Databases

This project fetches country data from the REST Countries API, parses the JSON with Gson, stores the relevant fields (name, population, region) into a SQLite database using JDBC, and exposes a small Swing GUI to fetch, search, refresh, and export results to CSV.

Features
- Fetch all countries from the REST Countries API and persist them into a local SQLite database (upsert semantics).
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
1. Ensure Java 11+ and Maven are installed.
2. From the project root run:

```powershell
mvn compile
mvn exec:java -Dexec.mainClass="com.assignment12.api.APIProgram"
```

This will open the Swing GUI. The program stores the SQLite file `countries.db` in the working directory.

Notes about dependencies
- Gson (com.google.code.gson:gson)
- SQLite JDBC (org.xerial:sqlite-jdbc)

Design notes and reflection
- Contract: API fetch returns a list of Country objects (name, population, region). DBManager offers save/list/search operations. GUI displays lists and keeps operations off the EDT via SwingWorker.
- Edge cases considered: missing name/population/region in JSON; HTTP errors; DB transaction rollback on batch failure; CSV escaping of commas and quotes.
- Further improvements: better progress reporting, paging for very large datasets, nicer UI, unit tests for DB and API client, and packaging into a runnable jar.

Files added
- `pom.xml` — Maven project file and dependencies
- `src/main/java/com/assignment12/api/*` — Java sources (client, DB manager, GUI, helpers)

If you'd like, I can run a quick compile and attempt to launch the GUI here and report any build errors.