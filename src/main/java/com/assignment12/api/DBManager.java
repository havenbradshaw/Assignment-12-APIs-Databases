package com.assignment12.api;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager implements AutoCloseable {
    private final Connection conn;

    public DBManager(String dbPath) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        this.conn = DriverManager.getConnection(url);
        initDatabase();
    }

    private void initDatabase() throws SQLException {
        String create = "CREATE TABLE IF NOT EXISTS countries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "population INTEGER NOT NULL CHECK(population >= 0), " +
                "region TEXT NOT NULL" +
                ");";
        try (Statement st = conn.createStatement()) {
            st.execute(create);
        }
    }

    public void saveCountries(List<Country> countries) throws SQLException {
        String sql = "INSERT INTO countries(name,population,region) VALUES(?,?,?) " +
                "ON CONFLICT(name) DO UPDATE SET population=excluded.population, region=excluded.region;";
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Country c : countries) {
                ps.setString(1, c.getName());
                ps.setLong(2, c.getPopulation());
                ps.setString(3, c.getRegion() == null ? "" : c.getRegion());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Country> listAll() throws SQLException {
        List<Country> out = new ArrayList<>();
        String q = "SELECT name,population,region FROM countries ORDER BY name COLLATE NOCASE";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                out.add(new Country(rs.getString("name"), rs.getLong("population"), rs.getString("region")));
            }
        }
        return out;
    }

    public List<Country> searchByName(String pattern) throws SQLException {
        List<Country> out = new ArrayList<>();
        String q = "SELECT name,population,region FROM countries WHERE name LIKE ? ORDER BY name COLLATE NOCASE";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, "%" + pattern + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Country(rs.getString("name"), rs.getLong("population"), rs.getString("region")));
                }
            }
        }
        return out;
    }

    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) conn.close();
    }
}
