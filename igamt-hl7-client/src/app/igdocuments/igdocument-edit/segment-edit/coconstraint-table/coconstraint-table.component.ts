/**
 * Created by hnt5 on 10/3/17.
 */

import {Component, Input, ViewChild, TemplateRef, OnInit} from '@angular/core';
import {
  CoConstraintTable, CCSelectorType, CCHeader, CellTemplate, VSCell, CCRow, CCCell,
  CCContent, DataCell, CodeCell, CCHeaders, CCGroup
} from './coconstraint.domain';
import {CCHeaderDialogDmComponent} from './header-dialog/header-dialog-dm.component';
import {CoConstraintTableService} from './coconstraint-table.service';
import {CCHeaderDialogUserComponent} from './header-dialog/header-dialog-user.component';
import {ValidatorFn, AbstractControl, NgForm, Validators} from '@angular/forms';
import {ValueSetBindingPickerComponent} from '../../../../common/valueset-binding-picker/valueset-binding-picker.component';
import * as _ from 'lodash';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
    selector: 'app-coconstraint-table',
    templateUrl: 'coconstraint-table.template.html',
    styleUrls: ['coconstraint-table.component.css']
})

export class CoConstraintTableComponent implements OnInit {



    @ViewChild(ValueSetBindingPickerComponent) vsPicker: ValueSetBindingPickerComponent;
    @ViewChild(CCHeaderDialogDmComponent) headerDialogDm: CCHeaderDialogDmComponent;
    @ViewChild(CCHeaderDialogUserComponent) headerDialogUser: CCHeaderDialogUserComponent;

    @ViewChild('empty')
    empty: TemplateRef<any>;
    @ViewChild('value')
    value: TemplateRef<any>;
    @ViewChild('valueset')
    valueset: TemplateRef<any>;
    @ViewChild('datatype')
    datatype: TemplateRef<any>;
    @ViewChild('flavor')
    flavor: TemplateRef<any>;
    @ViewChild('textArea')
    textArea: TemplateRef<any>;
    @ViewChild('code')
    code: TemplateRef<any>;
    @ViewChild('varies')
    varies: TemplateRef<any>;
    @ViewChild('ccForm')
    ccFormVar;

    dndGroups: boolean;
    table: CoConstraintTable;
    tableId: any;
    config: any;
    _segment: any;
    activeType: string;

    constructor(private ccTableService: CoConstraintTableService, private route: ActivatedRoute, private  router: Router) {
        this.activeType = 'data';
        this.table = {
          id: '',
      supportGroups: true,
      segment: 'OBX',
      headers: {
        selectors: [],
      data: [],
      user: []
    },
      content: {
        free: [],
        groups: []
      },
    };

    }

    @Input() set segment(value: any) {
        this._segment = value;
        this.table = this.ccTableService.getCCTableForSegment(this._segment);
        const ctrl = this;
        if (this.table.segment === 'OBX') {
            this.ccTableService.get_bound_codes(this._segment).then(function (response) {
                ctrl.config.dynCodes = [];
                for (const code of response.json().codes) {
                    ctrl.config.dynCodes.push({label: code.value, value: code.value});
                }
            });
        }
    }

    // ------ DND -----
    removeItem(item: any, list: any[]): void {
        list.splice(list.indexOf(item), 1);
    }

    headerDrop($event) {
       $event.event.preventDefault();
    }

    // ------ VALIDATOR -------
    uniq(form: NgForm, field: string, key: string, exclude: string): ValidatorFn {
        return (control: AbstractControl): {[key: string]: any} => {
            if (control.value && control.value !== '') {
                return this.found(form, control.value, field, key, exclude) ? { unique : 'This field must be unique' } : null;
            } else {
                return null;
            }
        };
    }

    required() {
        return Validators.required;
    }

    found(form: NgForm, value: string, field: string, key: string, exclude: string) {
        for (const control in form.controls) {
            if (control.includes(key) && control.includes(field) && form.controls[control].value === value && control !== exclude) {
                return true;
            }
        }
        return false;
    }

    // ------ TEMPLATE -----

    printForm(event) {
        console.log(this.ccFormVar);
    }

    print() {
        console.log(this.table);
    }

    headWidth(empty: boolean, x: number) {
        if (!empty) {
          return 'initial';
        } else {
            return (x + 1) * 200 + 'px';
        }
    }

    groupHeaderSize(table) {
        const data = table.headers.data.length === 0 ? 1 : table.headers.data.length;
        const selector = table.headers.selectors.length === 0 ? 1 : table.headers.selectors.length;
        const user = table.headers.user.length === 0 ? 1 : table.headers.user.length;
        return data + selector + user;
    }

    cTemplate(node: CCHeader) {
        if (node) {
            switch (node.template) {
                case CellTemplate.DATATYPE :
                    return this.datatype;
                case CellTemplate.FLAVOR :
                    return this.flavor;
                case CellTemplate.TEXTAREA :
                    return this.textArea;
                case CellTemplate.VARIES :
                    return this.varies;
                default :
                    switch (node.content.type) {
                        case CCSelectorType.VALUE :
                            return this.value;
                        case CCSelectorType.VALUESET :
                            return this.valueset;
                        case CCSelectorType.CODE :
                            return this.code;
                        default :
                            return this.empty;
                    }
            }
        }
    }

    isIgnore(node) {
        return node.type === CCSelectorType.IGNORE;
    }

    setVariesNodeValue(node, value) {
        switch (value) {
            case 'value' :
                node.type = CCSelectorType.VALUE;
                break;
            case 'vs' :
                node.type = CCSelectorType.VALUESET;
                break;
            case 'code' :
                node.type = CCSelectorType.CODE;
                break;
        }
    }

    clearVariesNodeValue(node) {
        node.type = CCSelectorType.IGNORE;
    }

    // ------ DIALOGS ------

    openVSDialog(obj: any, key: string) {
        console.log(obj);
        this.vsPicker.open({
            libraryId: this.tableId,
            selectedTables: (<VSCell> obj[key]).vs
        }).subscribe(
            result => {
                (<VSCell> obj[key]).vs = result;
            }
        );
    }

    openHeaderDialog (h: string) {
        const resolve = {
            header: h,
            selectedPaths: [],
            type: null,
            fixed: false
        };

        if (h === 'selectors') {
            resolve.type = CCSelectorType.VALUE;
            resolve.header = 'IF';
            resolve.fixed = true;
        } else if (h === 'data') {
            resolve.header = 'THEN';
        }

        this.headerDialogDm.open(resolve).subscribe(
            result => {
                this.table.headers[h].push(result);
                this.initColumn(result);
            }
        );
    }

    openUserHeaderDialog() {
        this.headerDialogUser.open({ header : 'USER'}).subscribe(
            result => {
                this.table.headers.user.push(result);
                this.initColumn(result);
            }
        );
    }

    // ------- TABLE CONTROLS -------

    delCol(list: any[], i: number, column: string) {
        this.reqDel(this.table, column);
        list.splice(i, 1);
    }

    reqDel(obj, key) {
        if (obj.content.free) {
            for (const cc of obj.content.free) {
                delete cc[key];
            }
        }
        if (obj.content.groups) {
            for (const gr of obj.content.groups) {
                this.reqDel(gr, key);
            }
        }
    }

    delRow(list: any[], i: number) {
        this.removeControlFromForm(list[i]);
        list.splice(i, 1);
    }

    removeControlFromForm(row: CCRow) {
        for (const x in this.ccFormVar.controls) {
            if ('-' + x.includes(row.id) + '-') {
                delete this.ccFormVar.controls[x];
            }
        }
    }

    addCc(list: any[]) {
        list.push(this.ccTableService.new_line(this.table.headers.selectors, this.table.headers.data, this.table.headers.user));
    }

    initColumn(obj) {

        const ctrl = this;
        const initReq = function (table) {
            if (table.content.free) {
                for (const cc of table.content.free) {
                    ctrl.ccTableService.init_cell(cc, obj);
                }
            }
            if (table.content.groups) {
                for (const grp of table.content.groups) {
                    initReq(grp);
                }
            }
        };
        initReq(this.table);
    }

    addCcGroup() {
        this.table.content.groups.push({
            data: {
                name: '',
                requirements: {
                    usage: 'R',
                    cardinality: {
                        min: 1,
                        max: '1'
                    }
                }
            },
            content: {
                free: []
            }
        });
    }

    delGroup(list: any[], i: number) {
        list.splice(i, 1);
    }

    dtChange(node) {
        node.value = '';
    }

    main() {
        console.log(this.table);
    }


    ngOnInit() {
        // console.log("[CONF]");
        // console.log(this.conf.codedElements);
        // console.log(this.conf.usages);
        // console.log(this.conf.getBindingLocationByVersion("2.5"));
        const ctrl = this;
        const segmentId = this.route.snapshot.params['segmentId'];
        this.route.data.map(data => data.segment).subscribe(x => {
          ctrl.segment = x;
        });

        this.config = {
            usages: [
                {
                    label: 'R',
                    value: 'R'
                },
                {
                    label: 'O',
                    value: 'O'
                }
            ],
            dynCodes: [],
            datatypes: []
        };
        this.dndGroups = false;
        // this.tableId = ig.profile.tableLibrary.id;
        // this.segment = this._ws.getCurrent(Entity.SEGMENT);
        // for (let dt of ig.profile.datatypeLibrary.children) {
        //     this.config.datatypes.push({label: dt.label, value: dt});
        // }
    }
}


