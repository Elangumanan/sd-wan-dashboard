package com.example.sdwan.dto;

import java.util.Objects;

public final class OrganizationSummaryDto {

    private final String id;
    private final String name;
    private final String description;
    private final String region;
    private final int    totalSites;
    private final int    healthySites;
    private final int    degradedSites;
    private final int    downSites;

    public OrganizationSummaryDto(String id, String name, String description, String region,
                                   int totalSites, int healthySites, int degradedSites, int downSites) {
        this.id            = Objects.requireNonNull(id,          "id");
        this.name          = Objects.requireNonNull(name,        "name");
        this.description   = Objects.requireNonNull(description, "description");
        this.region        = Objects.requireNonNull(region,      "region");
        this.totalSites    = totalSites;
        this.healthySites  = healthySites;
        this.degradedSites = degradedSites;
        this.downSites     = downSites;
    }

    public String id()            { return id; }
    public String name()          { return name; }
    public String description()   { return description; }
    public String region()        { return region; }
    public int    totalSites()    { return totalSites; }
    public int    healthySites()  { return healthySites; }
    public int    degradedSites() { return degradedSites; }
    public int    downSites()     { return downSites; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizationSummaryDto other)) return false;
        return totalSites == other.totalSites
            && healthySites == other.healthySites
            && degradedSites == other.degradedSites
            && downSites == other.downSites
            && Objects.equals(id, other.id)
            && Objects.equals(name, other.name)
            && Objects.equals(description, other.description)
            && Objects.equals(region, other.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, region, totalSites, healthySites, degradedSites, downSites);
    }

    @Override
    public String toString() {
        return "OrganizationSummaryDto[id=" + id + ", name=" + name
            + ", totalSites=" + totalSites + ", healthySites=" + healthySites
            + ", degradedSites=" + degradedSites + ", downSites=" + downSites + "]";
    }
}
