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
 * 
 */
package gov.nist.hit.hl7.igamt.shared.domain;

import java.io.Serializable;

import javax.persistence.GeneratedValue;

/**
 * @author ena3
 *
 */
public class CompositeKey implements Serializable {

 /**
   * 
   */
  private static final long serialVersionUID = -5077046386179385282L;
  @GeneratedValue
  private String id; 
  private int version;
  private static final int firstVersion = 1;
 
 
public String getId() {
  return id;
}
public CompositeKey() {
  super();
  this.version = firstVersion;
}
public CompositeKey(String id) {
  this(id,firstVersion);
}
public CompositeKey(String id, int version) {
  super();
  this.id = id;
  this.version = version;
}
public void setId(String id) {
  this.id = id;
}
public int getVersion() {
  return version;
}
public void setVersion(int version) {
  this.version = version;
}
  

}
