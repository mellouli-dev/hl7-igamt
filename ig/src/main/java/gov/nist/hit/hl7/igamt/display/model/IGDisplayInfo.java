package gov.nist.hit.hl7.igamt.display.model;

import java.util.HashSet;
import java.util.Set;

import gov.nist.hit.hl7.igamt.common.base.domain.display.DisplayElement;
import gov.nist.hit.hl7.igamt.ig.domain.Ig;

public class IGDisplayInfo {

	private Ig ig;
	private Set<DisplayElement> messages = new HashSet<DisplayElement>();
	private Set<DisplayElement> segments = new HashSet<DisplayElement>();
	private Set<DisplayElement> datatypes = new HashSet<DisplayElement>();
	private Set<DisplayElement> valueSets = new HashSet<DisplayElement>();
	private Set<DisplayElement> coConstraintGroups = new HashSet<DisplayElement>();
	private Set<DisplayElement> profileComponents = new HashSet<DisplayElement>();
	private Set<DisplayElement> compositePofiles = new HashSet<DisplayElement>();

	public Ig getIg() {
		return ig;
	}
	public void setIg(Ig ig) {
		this.ig = ig;
	}
	public Set<DisplayElement> getMessages() {
		return messages;
	}
	public void setMessages(Set<DisplayElement> messages) {
		this.messages = messages;
	}
	public Set<DisplayElement> getSegments() {
		return segments;
	}
	public void setSegments(Set<DisplayElement> segments) {
		this.segments = segments;
	}
	public Set<DisplayElement> getDatatypes() {
		return datatypes;
	}
	public void setDatatypes(Set<DisplayElement> datatypes) {
		this.datatypes = datatypes;
	}
	public Set<DisplayElement> getValueSets() {
		return valueSets;
	}
	public void setValueSets(Set<DisplayElement> valueSets) {
		this.valueSets = valueSets;
	}
	public Set<DisplayElement> getCoConstraintGroups() {
		return coConstraintGroups;
	}
	public void setCoConstraintGroups(Set<DisplayElement> coConstraintGroups) {
		this.coConstraintGroups = coConstraintGroups;
	}
  public Set<DisplayElement> getProfileComponents() {
    return profileComponents;
  }
  public void setProfileComponents(Set<DisplayElement> profileComponents) {
    this.profileComponents = profileComponents;
  }
  public Set<DisplayElement> getCompositePofiles() {
    return compositePofiles;
  }
  public void setCompositePofiles(Set<DisplayElement> compositePofiles) {
    this.compositePofiles = compositePofiles;
  }
}
