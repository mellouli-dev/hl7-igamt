import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Actions } from '@ngrx/effects';
import { MemoizedSelectorWithProps, Store } from '@ngrx/store';
import { SelectItem } from 'primeng/api';
import { combineLatest, Observable, of } from 'rxjs';
import { map, mergeMap, take } from 'rxjs/operators';
import * as fromIgamtDisplaySelectors from 'src/app/root-store/dam-igamt/igamt.resource-display.selectors';
import * as fromIgamtSelectedSelectors from 'src/app/root-store/dam-igamt/igamt.selected-resource.selectors';
import * as fromIgamtSelectors from 'src/app/root-store/dam-igamt/igamt.selectors';
import { LoadValueSet } from 'src/app/root-store/value-set-edit/value-set-edit.actions';
import { StructureEditorComponent } from '../../../core/components/structure-editor/structure-editor.component';
import { Message } from '../../../dam-framework/models/messages/message.class';
import { MessageService } from '../../../dam-framework/services/message.service';
import { Scope } from '../../../shared/constants/scope.enum';
import { Type } from '../../../shared/constants/type.enum';
import { IDocumentRef } from '../../../shared/models/abstract-domain.interface';
import { SourceType } from '../../../shared/models/adding-info';
import { IDisplayElement } from '../../../shared/models/display-element.interface';
import { EditorID } from '../../../shared/models/editor.enum';
import { IChange } from '../../../shared/models/save-change';
import { IValueSet } from '../../../shared/models/value-set.interface';
import { StoreResourceRepositoryService } from '../../../shared/services/resource-repository.service';
import { ValueSetService } from '../../service/value-set.service';

@Component({
  selector: 'app-value-set-structure-editor',
  templateUrl: './value-set-structure-editor.component.html',
  styleUrls: ['./value-set-structure-editor.component.css'],
})
export class ValueSetStructureEditorComponent extends StructureEditorComponent<IValueSet> implements OnDestroy, OnInit {

  selectedColumns: any[] = [];
  cols: any[] = [];
  @Input()
  viewOnly: boolean;
  codeSystemOptions: any[];
  hasOrigin$: Observable<boolean>;
  get viewOnly$() {
    return this._viewOnly$;
  }
  constructor(
    readonly repository: StoreResourceRepositoryService,
    private valueSetService: ValueSetService,
    messageService: MessageService,
    actions$: Actions,
    store: Store<any>) {
    super(
      repository,
      messageService,
      actions$,
      store,
      {
        id: EditorID.VALUESET_STRUCTURE,
        title: 'Structure',
        resourceType: Type.VALUESET,
      },
      LoadValueSet,
      [],
      [],
    );
    this._viewOnly$ = combineLatest(
      this.store.select(fromIgamtSelectors.selectViewOnly),
      this.store.select(fromIgamtSelectors.selectDelta),
      this.store.select(fromIgamtSelectors.selectWorkspaceActive).pipe(
        map((active) => {
          return active.display.domainInfo && !(active.display.domainInfo.scope === Scope.USER || (active.display.domainInfo.scope === Scope.PHINVADS && active.display.flavor));
        }),
      ),
    ).pipe(
      map(([vOnly, delta, notUser]) => {
        return vOnly || notUser || delta;
      }),
    );

    this.hasOrigin$ = this.store.select(fromIgamtSelectedSelectors.selectedResourceHasOrigin);
    this.resource$.subscribe((resource: IValueSet) => {
      this.cols = [];
      this.cols.push({ field: 'value', header: 'Value' });
      this.cols.push({ field: 'pattern', header: 'Pattern' });
      this.cols.push({ field: 'description', header: 'Description' });
      this.cols.push({ field: 'codeSystem', header: 'Code System' });
      if (resource.sourceType !== SourceType.EXTERNAL) {
        this.cols.push({ field: 'usage', header: 'Usage' });
      }
      this.cols.push({ field: 'comments', header: 'Comments' });
      this.selectedColumns = this.cols;
      this.codeSystemOptions = this.getCodeSystemOptions(resource);
    });
  }

  getCodeSystemOptions(resource: IValueSet): SelectItem[] {
    if (resource.codeSystems && resource.codeSystems.length > 0) {
      return resource.codeSystems.map((codeSystem: string) => {
        return { label: codeSystem, value: codeSystem };
      });
    } else {
      return [];
    }
  }

  saveChanges(id: string, documentRef: IDocumentRef, changes: IChange[]): Observable<Message<any>> {
    return this.valueSetService.saveChanges(id, documentRef, changes);
  }

  getById(id: string): Observable<IValueSet> {
    return this.documentRef$.pipe(
      take(1),
      mergeMap((x) => {
        return this.valueSetService.getById(x, id);
      }),
    );
  }

  elementSelector(): MemoizedSelectorWithProps<object, { id: string; }, IDisplayElement> {
    return fromIgamtDisplaySelectors.selectValueSetById;
  }

  isDTM(): Observable<boolean> {
    return of(false);
  }

}
