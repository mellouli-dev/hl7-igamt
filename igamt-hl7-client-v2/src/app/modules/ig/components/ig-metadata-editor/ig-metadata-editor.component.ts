import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Actions } from '@ngrx/effects';
import { Action, Store } from '@ngrx/store';
import { BehaviorSubject, combineLatest, Observable, of, throwError } from 'rxjs';
import { catchError, flatMap, map, pluck, take, tap } from 'rxjs/operators';
import * as fromDam from 'src/app/modules/dam-framework/store/index';
import { Type } from 'src/app/modules/shared/constants/type.enum';
import { EditorID } from 'src/app/modules/shared/models/editor.enum';
import { IgEditResolverLoad } from '../../../../root-store/ig/ig-edit/ig-edit.actions';
import { selectIgVersions } from '../../../../root-store/ig/ig-edit/ig-edit.selectors';
import { AbstractEditorComponent } from '../../../core/components/abstract-editor-component/abstract-editor-component.component';
import { MessageService } from '../../../dam-framework/services/message.service';
import { FieldType, IMetadataFormInput } from '../../../shared/components/metadata-form/metadata-form.component';
import { Status } from '../../../shared/models/abstract-domain.interface';
import { IDisplayElement } from '../../../shared/models/display-element.interface';
import { FroalaService } from '../../../shared/services/froala.service';
import { IgDocument } from '../../models/ig/ig-document.class';
import { IgService } from '../../services/ig.service';

export interface IIgEditMetadata {
  coverPicture: string;
  title: string;
  subTitle: string;
  version: string;
  organization: string;
  authors: string[];
  status: Status;
  implementationNotes?: any;
}

@Component({
  selector: 'app-ig-metadata-editor',
  templateUrl: './ig-metadata-editor.component.html',
  styleUrls: ['./ig-metadata-editor.component.scss'],
})
export class IgMetadataEditorComponent extends AbstractEditorComponent implements OnInit {

  coverPictureFile$: BehaviorSubject<File>;
  metadataFormInput: IMetadataFormInput<IIgEditMetadata>;
  froalaConfig$: Observable<any>;

  constructor(
    store: Store<any>,
    actions$: Actions,
    private igService: IgService,
    private messageService: MessageService, private froalaService: FroalaService) {
    super(
      {
        id: EditorID.IG_METADATA,
        resourceType: Type.IGDOCUMENT,
        title: 'Metadata',
      },
      actions$,
      store,
    );

    this.coverPictureFile$ = new BehaviorSubject<File>(null);
    this.metadataFormInput = {
      viewOnly: this.viewOnly$,
      data: this.currentSynchronized$.pipe(
        flatMap((metadata) => {
          return this.store.select(selectIgVersions).pipe(
            take(1),
            map((versions) => {
              return {
                ...metadata,
                pictureFile: {
                  url: metadata.coverPicture,
                },
                hl7Versions: versions,
              };
            }),
          );
        }),
      ),
      model: {
        pictureFile: {
          label: 'Cover Picture',
          placeholder: '',
          validators: [],
          type: FieldType.FILE,
          id: 'picture',
          name: 'picture',
        },
        title: {
          label: 'Title',
          placeholder: 'Title',
          validators: [],
          type: FieldType.TEXT,
          id: 'title',
          name: 'Title',
        },
        subTitle: {
          label: 'Subtitle',
          placeholder: 'Subtitle',
          validators: [],
          type: FieldType.TEXT,
          id: 'subtitle',
          name: 'Subtitle',
        },
        version: {
          label: 'Version',
          placeholder: 'Version',
          validators: [],
          type: FieldType.TEXT,
          id: 'version',
          name: 'Version',
        },
        organization: {
          label: 'Organization',
          placeholder: 'Organization',
          validators: [],
          enum: [],
          type: FieldType.TEXT,
          id: 'Organization',
          name: 'Organization',
        },
        authors: {
          label: 'Authors',
          placeholder: 'Type name and press Enter',
          validators: [],
          enum: [],
          type: FieldType.STRING_LIST,
          id: 'Authors',
          name: 'Authors',
        },
        hl7Versions: {
          label: 'HL7 Versions',
          placeholder: 'HL7 Versions',
          validators: [],
          disabled: true,
          enum: [],
          type: FieldType.STRING_LIST,
          id: 'HL7Versions',
          name: 'HL7Versions',
        },
        authorNotes: {
          label: 'Author Notes',
          placeholder: 'Author Notes',
          validators: [],
          enum: [],
          type: FieldType.RICH,
          id: 'authorNotes',
          name: 'authorNotes',
        },
      },
    };
  }

  dataChange(form: FormGroup) {
    this.current$.pipe(
      take(1),
      tap((current) => {
        this.coverPictureFile$.next(form.getRawValue().pictureFile);
        this.editorChange(Object.assign(current.data, form.getRawValue()), form.valid);
      }),
    ).subscribe();
  }

  onEditorSave(action: fromDam.EditorSave): Observable<Action> {
    return combineLatest(this.elementId$, this.current$, this.coverPictureFile$.pipe(
      map((elm) => elm instanceof File ? elm : null),
    )).pipe(
      take(1),
      flatMap(([id, current, coverPicture]) => {
        const coverPictureName$: Observable<string> = !coverPicture ? of(null) :
          this.igService.uploadCoverImage(coverPicture).pipe(
            pluck('link'),
          );

        return coverPictureName$.pipe(
          flatMap((pictureName) => {
            return this.igService.saveMetadata(id, {
              ...current.data,
              coverPicture: pictureName ? pictureName : current.data.coverPicture,
            }).pipe(
              flatMap((response) => {
                return [
                  new IgEditResolverLoad(id),
                  new fromDam.EditorSaveSuccess({
                    ...current.data,
                    coverPicture: pictureName ? pictureName : current.data.coverPicture,
                  }),
                  this.messageService.messageToAction(response),
                ];
              }),
              catchError((error) => {
                return throwError(this.messageService.actionFromError(error));
              }),
            );
          }),
        );
      }),
    );
  }

  editorDisplayNode(): Observable<IDisplayElement> {
    return this.document$.pipe(
      map((document) => {
        return this.igService.igToIDisplayElement(document as IgDocument);
      }),
    );
  }

  ngOnInit() {
    this.froalaConfig$ = this.froalaService.getConfig();
  }

  onDeactivate() {

  }

}
