package com.example.sdwan.domain;

import java.util.Objects;

public final class Organization {

    private final String id;
    private final String name;
    private final String description;
    private final String region;

    public Organization(String id, String name, String description, String region) {
        this.id          = Objects.requireNonNull(id,          "id");
        this.name        = Objects.requireNonNull(name,        "name");
        this.description = Objects.requireNonNull(description, "description");
        this.region      = Objects.requireNonNull(region,      "region");
    }

    public String id()          { return id; }
    public String name()        { return name; }
    public String description() { return description; }
    public String region()      { return region; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Organization other)) return false;
        return Objects.equals(id, other.id)
            && Objects.equals(name, other.name)
            && Objects.equals(description, other.description)
            && Objects.equals(region, other.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, region);
    }

    @Override
    public String toString() {
        return "Organization[id=" + id + ", name=" + name + ", region=" + region + "]";
    }
}
