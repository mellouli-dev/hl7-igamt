package gov.nist.hit.hl7.igamt.segment.domain;

import java.util.Set;

import gov.nist.diff.annotation.DeltaField;
import gov.nist.hit.hl7.igamt.common.base.domain.Comment;
import gov.nist.hit.hl7.igamt.common.base.domain.Ref;
import gov.nist.hit.hl7.igamt.common.base.domain.SubStructElement;
import gov.nist.hit.hl7.igamt.common.base.domain.Type;
import gov.nist.hit.hl7.igamt.common.base.domain.Usage;
import gov.nist.hit.hl7.igamt.common.slicing.domain.Slicing;

public class Field extends SubStructElement {

  @DeltaField
  private int min;
  @DeltaField
  private String max;
  
  private Slicing slicing;

  public Field() {
    super();
    this.setType(Type.FIELD);
  }

  public Field(String id, String name, int position, Usage usage, String text, boolean custom, String maxLength, String minLength, String confLength, Ref ref, int min, String max, String constantValue, Set<Comment> comments) {
    super(id, name, position, usage, Type.FIELD, text, custom, maxLength, minLength, confLength,
        ref, comments);
    this.min = min;
    this.max = max;
    this.setConstantValue(constantValue);
    this.setType(Type.FIELD);
  }

  public int getMin() {
    return min;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public String getMax() {
    return max;
  }

  public void setMax(String max) {
    this.max = max;
  }

  public Slicing getSlicing() {
    return slicing;
  }

  public void setSlicing(Slicing slicing) {
    this.slicing = slicing;
  }



  
  
}
