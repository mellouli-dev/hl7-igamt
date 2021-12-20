package gov.nist.hit.hl7.igamt.coconstraints.serialization;

import gov.nist.hit.hl7.igamt.coconstraints.model.CoConstraintGrouper;
import gov.nist.hit.hl7.igamt.common.base.domain.Type;

public class SerializableGrouper extends CoConstraintGrouper  {
    private String name;
    private String description;
    private String version;
    private String datatype;
    private Type type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
