/**
 * This software was developed at the National Institute of Standards and Technology by employees of
 * the Federal Government in the course of their official duties. Pursuant to title 17 Section 105
 * of the United States Code this software is not subject to copyright protection and is in the
 * public domain. This is an experimental system. NIST assumes no responsibility whatsoever for its
 * use by other parties, and makes no guarantees, expressed or implied, about its quality,
 * reliability, or any other characteristic. We would appreciate acknowledgement if the software is
 * used. This software can be redistributed and/or modified freely provided that any derivative
 * works bear some notice that they are derived from it, and any modified versions bear some notice
 * that they have been modified.
 */
package gov.nist.hit.hl7.igamt.profilecomponent.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nist.hit.hl7.igamt.common.base.domain.display.DisplayElement;
import gov.nist.hit.hl7.igamt.profilecomponent.domain.ProfileComponent;
import gov.nist.hit.hl7.igamt.profilecomponent.domain.ProfileComponentContext;
import gov.nist.hit.hl7.igamt.profilecomponent.domain.ProfileComponentItem;
import gov.nist.hit.hl7.igamt.profilecomponent.repository.ProfileComponentRepository;
import gov.nist.hit.hl7.igamt.profilecomponent.service.ProfileComponentService;


/**
 * 
 * Created by Maxence Lefort on Feb 20, 2018.
 */
@Service("profileComponentService")
public class ProfileComponentServiceImpl implements ProfileComponentService {

  @Autowired
  private ProfileComponentRepository profileComponentRepository;

  @Override
  public ProfileComponent findById(String id) {
    return profileComponentRepository.findById(id).orElse(null);
  }

  @Override
  public ProfileComponent create(ProfileComponent profileComponent) {
    profileComponent.setId(new String());
    profileComponent = profileComponentRepository.save(profileComponent);
    return profileComponent;
  }

  @Override
  public List<ProfileComponent> findAll() {
    return profileComponentRepository.findAll();
  }

  @Override
  public ProfileComponent save(ProfileComponent profileComponent) {
    // profileComponent.setId(StringUtil.updateVersion(profileComponent.getId()));
    profileComponent = profileComponentRepository.save(profileComponent);
    return profileComponent;
  }

  @Override
  public List<ProfileComponent> saveAll(List<ProfileComponent> profileComponents) {
    ArrayList<ProfileComponent> savedProfileComponents = new ArrayList<>();
    for (ProfileComponent profileComponent : profileComponents) {
      savedProfileComponents.add(this.save(profileComponent));
    }
    return savedProfileComponents;
  }

  @Override
  public void delete(String id) {
    profileComponentRepository.deleteById(id);
  }

  @Override
  public void removeCollection() {
    profileComponentRepository.deleteAll();
  }

  /* (non-Javadoc)
   * @see gov.nist.hit.hl7.igamt.profilecomponent.service.ProfileComponentService#findByIdIn(java.util.Set)
   */
  @Override
  public List<ProfileComponent> findByIdIn(Set<String> ids) {
    // TODO Auto-generated method stub
    return profileComponentRepository.findByIdIn(ids);
  }
  
  @Override 
  public ProfileComponent addChildrenFromDisplayElement(String id, List<DisplayElement> children) {
    
    ProfileComponent pc = this.findById(id);
    if(pc.getChildren() == null) {
      pc.setChildren(new HashSet<ProfileComponentContext>());
    }

    for(int i=0; i < children.size(); i++) {
      DisplayElement elm = children.get(i);
      // TODO set struct ID 
      ProfileComponentContext ctx = new ProfileComponentContext(elm.getId(), elm.getType(), elm.getId(), elm.getFixedName(), i+ pc.getChildren().size()+1, new HashSet<ProfileComponentItem>());
      pc.getChildren().add(ctx);
    }
    this.save(pc);
    return pc;
  }
  

}
