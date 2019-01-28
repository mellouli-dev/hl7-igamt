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
package gov.nist.hit.hl7.igamt.common.base.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import gov.nist.hit.hl7.igamt.common.base.domain.AbstractDomain;
import gov.nist.hit.hl7.igamt.common.base.domain.Scope;
import gov.nist.hit.hl7.igamt.common.base.exception.ForbiddenOperationException;
import gov.nist.hit.hl7.igamt.common.base.service.CommonService;

/**
 * @author ena3
 *
 */
@Service
public class CommonServiceImpl implements CommonService {



@Override
public void checkAuthority(Authentication auth, String role)  throws ForbiddenOperationException {
	// TODO Auto-generated method stub
	if(auth.getAuthorities().contains(new SimpleGrantedAuthority(role))) {
		
	throw  new ForbiddenOperationException("The User must have the" +role+ "authority"+"to perform this operation");
	}
}

@Override
public void checkOwnerShip(Authentication auth, AbstractDomain obj) throws ForbiddenOperationException {
	// TODO Auto-generated method stub
	if(obj.getUsername()==null&&!auth.getName().equals(obj.getUsername())) {
		throw  new ForbiddenOperationException("The User must be the owner of this resource to perform this operation");
	}
}

  
  
  
  
  
  
  

}
