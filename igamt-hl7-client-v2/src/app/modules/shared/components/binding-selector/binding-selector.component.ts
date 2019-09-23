import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';
import {mergeMap, take} from 'rxjs/operators';
import {selectIgId} from '../../../../root-store/ig/ig-edit/ig-edit.selectors';
import {TurnOnLoader} from '../../../../root-store/loader/loader.actions';
import {ValueSetService} from '../../../value-set/service/value-set.service';
import {IBindingType, IValuesetStrength} from '../../models/binding.interface';
import {IDisplayElement} from '../../models/display-element.interface';
import {ICodes, IValueSet} from '../../models/value-set.interface';

@Component({
  selector: 'app-binding-selector',
  templateUrl: './binding-selector.component.html',
  styleUrls: ['./binding-selector.component.css'],
})
export class BindingSelectorComponent<T> implements OnInit {
  selectedBindingType: IBindingType = IBindingType.VALUESET;
  selectedValueSet: IDisplayElement;
  currentValueSet: IValueSet;
  edit = {};
  editableBinding: IValueSetBindingDisplay;
  temp: IDisplayElement = null;
  selectedSingleCode: ISingleCodeDisplay;
  bindingStrengthOptions = [
    {label: 'Required', value: 'R'}, {label: 'Suggested', value: 'S'}, {label: 'Unspecified', value: 'U'},
  ];
  private selectedValueSets: IValueSetBindingDisplay[] = [];

  constructor(public dialogRef: MatDialogRef<BindingSelectorComponent<T>>,
              @Inject(MAT_DIALOG_DATA) public data: IBindingSelectorData, private valueSetService: ValueSetService,
              private store: Store<any>) {
    this.selectedSingleCode = this.data.selectedSingleCode;
    this.selectedValueSets = this.data.selectedValueSetBinding;
  }

  submit() {

    let result: IBindingDataResult = {selectedBindingType: this.selectedBindingType};
    switch (this.selectedBindingType) {
      case IBindingType.SINGLECODE:
        result = {...result, selectedSingleCode: this.selectedSingleCode};
        break;
      case IBindingType.VALUESET:
        result = {...result, selectedValueSets: this.selectedValueSets};
        break;
    }
    this.dialogRef.close(result);
  }

  cancel() {
    this.dialogRef.close();
  }

  // selectValueSet(elem: IDisplayElement) {
  //   const newBinding: IValueSetBindingDisplay = {display: elem, bindingStrength: IValuesetStrength.R};
  //   if (this.data.locationInfo.allowedBindingLocations && this.data.locationInfo.allowedBindingLocations.length === 1) {
  //     newBinding.bindingLocation = this.data.locationInfo.allowedBindingLocations[0].value;
  //   }
  //   if (!this.selectedValueSets) {
  //     this.selectedValueSets = [];
  //   }
  //   this.selectedValueSets.push(newBinding);
  // }
  addBinding() {
    if (!this.selectedValueSets ) {
      this.selectedValueSets = [];
    }
    this.editableBinding = {valueSets : [], bindingStrength: IValuesetStrength.R, bindingLocation: []};
    this.selectedValueSets.push(this.editableBinding);
  }
  submitValueSet(binding: IValueSetBindingDisplay) {
    binding.valueSets.push(this.temp);
    this.temp = null;
    this.edit = {};
  }
  addValueSet(binding: IValueSetBindingDisplay, index) {
    this.edit[index] = true;
    this.temp = null;
  }
  resetBinding(binding: IValueSetBindingDisplay, index) {
    this.edit[index] = true;
    this.temp = null;
  }
  submitBinding() {
  }
  removeValueSet(binding: IValueSetBindingDisplay, vs: IDisplayElement) {
  }
  getDefaultBindinglcation() {
    if (this.data.locationInfo.allowedBindingLocations && this.data.locationInfo.allowedBindingLocations.length === 1 ) {
      return this.data.locationInfo.allowedBindingLocations[1];
    } else {
      return [1];
    }
  }

  ngOnInit() {
  }

  loadCodes($event) {
    this.store.dispatch(new TurnOnLoader({blockUI: true}));
    this.getById($event.id).subscribe(
      (x) => {
        this.currentValueSet = x;
      },
    );
  }

  getById(id: string): Observable<IValueSet> {
    return this.store.select(selectIgId).pipe(
      take(1),
      mergeMap((x) => {
        return this.valueSetService.getById(x, id);
      }),
    );
  }

  selectCode(code: ICodes) {
    this.selectedSingleCode = {valueSet: this.selectedValueSet, code: code.value, codeSystem: code.codeSystem};
  }

  clearCode() {
    this.selectedSingleCode = null;
  }

  remove(rowData: IValueSetBindingDisplay) {
    //this.selectedValueSets = this.selectedValueSets.filter((x) => (x.display.id !== rowData.display.id) || (x.bindingLocation !== rowData.bindingLocation) || (x.bindingStrength !== rowData.bindingStrength));
  }
}

export interface IBindingLocationItem {
  label: string;
  value: number[];
}

export interface IBindingLocationInfo {
  allowedBindingLocations: IBindingLocationItem[];
  singleCodeAllowed: boolean;
  multiple: boolean;
  coded: boolean;
  allowSingleCode: boolean;
  allowValueSets: boolean;
}

export class IValueSetBindingDisplay {
  valueSets: IDisplayElement[];
  bindingStrength: IValuesetStrength;
  bindingLocation?: number[];
}

export class ISingleCodeDisplay {
  valueSet: IDisplayElement;
  code: string;
  codeSystem: string;
}

export interface IBindingDataResult {
  selectedBindingType?: IBindingType;
  selectedSingleCode?: ISingleCodeDisplay;
  selectedValueSets?: IValueSetBindingDisplay[];
}

export interface IBindingSelectorData {
  resources: IDisplayElement[];
  locationInfo: IBindingLocationInfo;
  path: string;
  existingBindingType: IBindingType;
  selectedValueSetBinding: IValueSetBindingDisplay[];
  selectedSingleCode: ISingleCodeDisplay;
}
