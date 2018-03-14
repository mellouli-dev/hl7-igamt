/**
 * 
 * This software was developed at the National Institute of Standards and Technology by employees of
 * the Federal Government in the course of their official duties. Pursuant to title 17 Section 105
 * of the United States Code this software is not subject to copyright protection and is in the
 * public domain. This is an experimental system. NIST assumes no responsibility whatsoever for its
 * use by other parties, and makes no guarantees, expressed or implied, about its quality,
 * reliability, or any other characteristic. We would appreciate acknowledgement if the software is
 * used. This software can be redistributed and/or modified freely provided that any derivative
 * works bear some notice that they are derived from it, and any modified versions bear some notice
 * that they have been modified.
 * 
 */
package gov.nist.hit.hl7.igamt.conformanceprofile.domain;

import java.util.HashSet;
import java.util.Set;

import gov.nist.hit.hl7.igamt.shared.domain.MsgStructElement;
import gov.nist.hit.hl7.igamt.shared.domain.Resource;
import gov.nist.hit.hl7.igamt.shared.domain.binding.ResourceBinding;

/**
 *
 * @author Maxence Lefort on Mar 9, 2018.
 */
public class ConformanceProfile extends Resource {

  private String identifier;
  private Set<MsgStructElement> children = new HashSet<MsgStructElement>();
  private ResourceBinding binding;

  public ConformanceProfile() {
    super();
  }

  public Set<MsgStructElement> getChildren() {
    return children;
  }

  public void setChildren(Set<MsgStructElement> children) {
    this.children = children;
  }

  public ResourceBinding getBinding() {
    return binding;
  }

  public void setBinding(ResourceBinding binding) {
    this.binding = binding;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void addChild(MsgStructElement mse) {
    this.children.add(mse);
  }

}
