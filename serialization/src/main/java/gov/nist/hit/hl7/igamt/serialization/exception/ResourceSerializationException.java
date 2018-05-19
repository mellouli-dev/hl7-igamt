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
package gov.nist.hit.hl7.igamt.serialization.exception;

import gov.nist.hit.hl7.igamt.shared.domain.Resource;
import gov.nist.hit.hl7.igamt.shared.domain.Type;

/**
 *
 * @author Maxence Lefort on Mar 22, 2018.
 */
public class ResourceSerializationException extends SerializationException {

  /**
   * 
   */
  private static final long serialVersionUID = -3476863921150657947L;
  
  public ResourceSerializationException(Exception originException, Type type, Resource resource, String message) {
    super(originException,type,"id="+resource.getId().getId()+",version="+resource.getId().getVersion()+",name="+resource.getName());
    this.message = message;
  }
  
  public ResourceSerializationException(Exception originException, Type type, Resource resource) {
    this(originException,type,resource,null);
  }
  
}
