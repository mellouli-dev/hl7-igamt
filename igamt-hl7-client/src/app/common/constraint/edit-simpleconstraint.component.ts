/**
 * Created by Jungyub on 10/26/17.
 */
import {Component, Input} from "@angular/core";
import { ControlContainer, NgForm } from '@angular/forms';
import {GeneralConfigurationService} from "../../service/general-configuration/general-configuration.service";


@Component({
  selector : 'edit-simple-constraint',
  templateUrl : './edit-simpleconstraint.component.html',
  styleUrls : ['./edit-simpleconstraint.component.css'],
  viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ]
})
export class EditSimpleConstraintComponent {
  @Input() constraint : any;
  @Input() assertion : any;
  @Input() structure : any;
  @Input() groupName: string;
  @Input() level: string;

  needContext:boolean = false;

  needTargetOccurence:boolean = false;
  targetOccurenceIdPath:string;
  targetOccurenceLocationStr:string;
  targetOccurenceValue:string;
  targetOccurenceType:string;

  needCompareOccurence:boolean = false;
  compareOccurenceIdPath:string;
  compareOccurenceLocationStr:string;
  compareOccurenceValue:string;
  compareOccurenceType:string;

  declarativeType:string;

  needComparison:boolean = false;

  verbs: any[];
  occurenceTypes:any[];
  declarativeTypes:any[];
  declarativeCTypes:any[];

  constructor(private configService : GeneralConfigurationService){}

  ngOnInit(){
    if(!this.assertion) this.assertion = {};
    if(!this.assertion.complement) this.assertion.complement = {};
    if(!this.assertion.subject) this.assertion.subject = {};
    this.verbs = this.configService._simpleConstraintVerbs;
    this.occurenceTypes = this.configService._occurenceTypes;
    this.declarativeTypes = this.configService._declarativeTypes;
    this.declarativeCTypes = this.configService._declarativeCTypes;

    if(this.level === 'CONFORMANCEPROFILE'){
      this.needContext = true;
    }
  }

  selectTargetElementLocation(location){
    this.needTargetOccurence = false;
    this.targetOccurenceIdPath = null;
    this.targetOccurenceLocationStr = null;
    this.targetOccurenceValue = null;
    this.targetOccurenceType = null;
    this.assertion.subject = {path:location};
  }

  selectComparisonElementLocation(location){
    this.needCompareOccurence = false;
    this.compareOccurenceIdPath = null;
    this.compareOccurenceLocationStr = null;
    this.compareOccurenceValue = null;
    this.compareOccurenceType = null;
    this.assertion.complement.path = location;
  }

  getLocationLabel(location, type){
    if(location.path){
      let result:string = this.structure.name;
      result = this.getChildLocation(location.path.child, this.structure.structure, result, null, type);
      return result;
    }
    return null;
  }

  getChildLocation(path, list, result, elementName, type){
    if(path && list){
      for(let item of list){
        if(item.data.id === path.elementId) {
          if(item.data.type === 'FIELD'){
            result = result + '-' + item.data.position;
          }else if(item.data.type === 'COMPONENT' || item.data.type === 'SUBCOMPONENT'){
            result = result + '.' + item.data.position;
          }else {
            result = result + '.' + item.data.name;
          }
          elementName = item.data.name;

          if(item.data.max && item.data.max !== '0' && item.data.max !== '1'){
            if(type === 'TARGET'){
              if(!this.needTargetOccurence){
                this.needTargetOccurence = true;
                this.targetOccurenceIdPath = item.data.idPath;
                this.targetOccurenceLocationStr = result + "(" + elementName + ")";
              }
            }else{
              if(!this.needCompareOccurence){
                this.needCompareOccurence = true;
                this.compareOccurenceIdPath = item.data.idPath;
                this.compareOccurenceLocationStr = result + "(" + elementName + ")";
              }
            }
          }
          return this.getChildLocation(path.child,item.children, result, elementName, type);
        }
      }
    }
    return result + "(" + elementName + ")";
  }

  addListValue(list){
    if(!list) list = [];
    list.push('');
  }

  changeDeclarativeType(){
    if(this.assertion.complement.complementKey === 'containListValues' || this.assertion.complement.complementKey === 'containListCodes')
      this.assertion.complement.values = [];
  }
}
