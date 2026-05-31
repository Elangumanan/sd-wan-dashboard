package com.example.sdwan.domain;

import java.util.Objects;

public final class Site {

    private final String id;
    private final String orgId;
    private final String name;
    private final String location;

    public Site(String id, String orgId, String name, String location) {
        this.id       = Objects.requireNonNull(id,       "id");
        this.orgId    = Objects.requireNonNull(orgId,    "orgId");
        this.name     = Objects.requireNonNull(name,     "name");
        this.location = Objects.requireNonNull(location, "location");
    }

    public String id()       { return id; }
    public String orgId()    { return orgId; }
    public String name()     { return name; }
    public String location() { return location; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Site other)) return false;
        return Objects.equals(id, other.id)
            && Objects.equals(orgId, other.orgId)
            && Objects.equals(name, other.name)
            && Objects.equals(location, other.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orgId, name, location);
    }

    @Override
    public String toString() {
        return "Site[id=" + id + ", orgId=" + orgId + ", name=" + name + ", location=" + location + "]";
    }
}
