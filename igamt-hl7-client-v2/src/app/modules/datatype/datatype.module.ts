import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { DatatypeEditEffects } from '../../root-store/datatype-edit/datatype-edit.effects';
import { SharedModule } from '../shared/shared.module';
import { DatatypeConformanceStatementEditorComponent } from './components/conformance-statement-editor/datatype-conformance-statement-editor.component';
import { DatatypeBindingsEditorComponent } from './components/datatype-bindings-editor/datatype-bindings-editor.component';
import { DatatypeCrossRefsComponent } from './components/datatype-cross-refs/datatype-cross-refs.component';
import { DatatypeStructureEditorComponent } from './components/datatype-structure-editor/datatype-structure-editor.component';
import { DeltaEditorComponent } from './components/delta-editor/delta-editor.component';
import { DtmDeltaEditorComponent } from './components/dtm-delta-editor/dtm-delta-editor.component';
import { MetadataEditComponent } from './components/metadata-edit/metadata-edit.component';
import { PostdefEditorComponent } from './components/postdef-editor/postdef-editor.component';
import { PredefEditorComponent } from './components/predef-editor/predef-editor.component';
import { DatatypeRoutingModule } from './datatype-routing.module';
import { DatatypeService } from './services/datatype.service';

@NgModule({
  declarations: [PredefEditorComponent, PostdefEditorComponent, MetadataEditComponent, DatatypeStructureEditorComponent, DatatypeCrossRefsComponent, DatatypeConformanceStatementEditorComponent, DeltaEditorComponent, DtmDeltaEditorComponent, DatatypeBindingsEditorComponent],
  imports: [
    CommonModule,
    DatatypeRoutingModule,
    SharedModule,
    EffectsModule.forFeature([DatatypeEditEffects]),
    StoreModule,
  ],
  providers: [
    DatatypeService,
    DatatypeEditEffects,
  ],
  exports: [PredefEditorComponent, PostdefEditorComponent],
})
export class DatatypeModule { }
