package gov.nist.hit.hl7.igamt.segment.domain;

import java.util.Date;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import gov.nist.hit.hl7.igamt.shared.domain.DomainInfo;
import gov.nist.hit.hl7.igamt.shared.domain.DynamicMappingInfo;
import gov.nist.hit.hl7.igamt.shared.domain.Field;
import gov.nist.hit.hl7.igamt.shared.domain.Resource;
import gov.nist.hit.hl7.igamt.shared.domain.Scope;
import gov.nist.hit.hl7.igamt.shared.domain.binding.ResourceBinding;

@Document(collection = "segment")

public class Segment extends Resource {
  private String ext;
  private DynamicMappingInfo dynamicMappingInfo;
  private ResourceBinding binding;

  private Set<Field> children;

  public Segment() {
    super();
  }

  public ResourceBinding getBinding() {
    return binding;
  }

  public void setBinding(ResourceBinding binding) {
    this.binding = binding;
  }

  public Set<Field> getChildren() {
    return children;
  }

  public void setChildren(Set<Field> children) {
    this.children = children;
  }

  public String getExt() {
    return ext;
  }

  public void setExt(String ext) {
    this.ext = ext;
  }

  public DynamicMappingInfo getDynamicMappingInfo() {
    return dynamicMappingInfo;
  }

  public void setDynamicMappingInfo(DynamicMappingInfo dynamicMappingInfo) {
    this.dynamicMappingInfo = dynamicMappingInfo;
  }



  @Override
  public Segment clone() {

    Segment clone = new Segment();
    clone.setBinding(this.binding);
    clone.setChildren(children);
    clone.setComment(this.getComment());
    clone.setCreatedFrom(this.getId().getId());
    clone.setDescription(this.getDescription());
    DomainInfo domainInfo = this.getDomainInfo();
    domainInfo.setScope(Scope.USER);
    clone.setDynamicMappingInfo(dynamicMappingInfo);
    clone.setId(null);
    clone.setPostDef(this.getPostDef());
    clone.setPreDef(this.getPreDef());
    clone.setName(this.getName());
    clone.setDomainInfo(domainInfo);
    clone.setCreationDate(new Date());
    clone.setUpdateDate(new Date());
    return clone;

  };


}
