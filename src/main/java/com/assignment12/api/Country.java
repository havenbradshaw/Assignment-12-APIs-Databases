package com.assignment12.api;

public class Country {
    private final String name;
    private final long population;
    private final String region;

    public Country(String name, long population, String region) {
        this.name = name;
        this.population = population;
        this.region = region == null ? "" : region;
    }

    public String getName() { return name; }
    public long getPopulation() { return population; }
    public String getRegion() { return region; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %,d", name, region, population);
    }
}
