import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {
  CompositeProfileActionTypes,
  LoadCompositeProfile, OpenCompositeProfileDeltaEditor,
  OpenCompositeProfileMetadataEditor, OpenCompositeProfilePostDefEditor,
  OpenCompositeProfilePreDefEditor,
  OpenCompositeProfileStructureEditor,
  OpenCompositionEditor,
} from '../../root-store/composite-profile/composite-profile.actions';
import { DataLoaderGuard } from '../dam-framework/guards/data-loader.guard';
import { EditorActivateGuard } from '../dam-framework/guards/editor-activate.guard';
import { EditorDeactivateGuard } from '../dam-framework/guards/editor-deactivate.guard';
import { Type } from '../shared/constants/type.enum';
import { EditorID } from '../shared/models/editor.enum';
import {CompositeDeltaEditorComponent} from './components/composite-delta-editor/composite-delta-editor.component';
import {CompositeProfileMetadataEditorComponent} from './components/composite-profile-metadata-editor/composite-profile-metadata-editor.component';
import {CompositeProfilePostDefComponent} from './components/composite-profile-post-def/composite-profile-post-def.component';
import {CompositeProfilePreDefComponent} from './components/composite-profile-pre-def/composite-profile-pre-def.component';
import { CompositionEditorComponent } from './components/composition-editor/composition-editor.component';
import { StructureEditorComponent } from './components/structure-editor/structure-editor.component';

const routes: Routes = [
  {
    path: ':compositeProfileId',
    data: {
      routeParam: 'compositeProfileId',
      loadAction: LoadCompositeProfile,
      successAction: CompositeProfileActionTypes.LoadCompositeProfileSuccess,
      failureAction: CompositeProfileActionTypes.LoadCompositeProfileFailure,
      redirectTo: ['ig', 'error'],
    },
    canActivate: [DataLoaderGuard],
    children: [
      {
        path: '',
        redirectTo: 'composition',
        pathMatch: 'full',
      },
      {
        path: 'composition',
        component: CompositionEditorComponent,
        canActivate: [EditorActivateGuard],
        canDeactivate: [EditorDeactivateGuard],
        data: {
          editorMetadata: {
            id: EditorID.COMPOSITE_PROFILE_COMPOSITION,
            title: 'Composition',
            resourceType: Type.COMPOSITEPROFILE,
          },
          onLeave: {
            saveEditor: true,
            saveTableOfContent: false,
          },
          action: OpenCompositionEditor,
          idKey: 'compositeProfileId',
        },
      },
      {
        path: 'structure',
        component: StructureEditorComponent,
        canActivate: [EditorActivateGuard],
        canDeactivate: [EditorDeactivateGuard],
        data: {
          editorMetadata: {
            id: EditorID.COMPOSITE_PROFILE_STRUCTURE,
            title: 'Structure',
            resourceType: Type.COMPOSITEPROFILE,
          },
          onLeave: {
            saveEditor: true,
            saveTableOfContent: false,
          },
          action: OpenCompositeProfileStructureEditor,
          idKey: 'compositeProfileId',
        },
      },
      {
        path: 'metadata',
        component: CompositeProfileMetadataEditorComponent,
        canActivate: [EditorActivateGuard],
        canDeactivate: [EditorDeactivateGuard],
        data: {
          editorMetadata: {
            id: EditorID.COMPOSITE_PROFILE_METADATA,
            title: 'Metadata',
            resourceType: Type.COMPOSITEPROFILE,
          },
          onLeave: {
            saveEditor: true,
            saveTableOfContent: true,
          },
          action: OpenCompositeProfileMetadataEditor,
          idKey: 'compositeProfileId',
        },
      },
      {
        path: 'pre-def',
        component: CompositeProfilePreDefComponent,
        canActivate: [EditorActivateGuard],
        canDeactivate: [EditorDeactivateGuard],
        data: {
          editorMetadata: {
            id: EditorID.PREDEF,
            title: 'Pre-definition',
            resourceType: Type.COMPOSITEPROFILE,
          },
          onLeave: {
            saveEditor: true,
            saveTableOfContent: true,
          },
          action: OpenCompositeProfilePreDefEditor,
          idKey: 'compositeProfileId',
        },
      },
      {
        path: 'post-def',
        component: CompositeProfilePostDefComponent,
        canActivate: [EditorActivateGuard],
        canDeactivate: [EditorDeactivateGuard],
        data: {
          editorMetadata: {
            id: EditorID.POSTDEF,
            title: 'Post-definition',
            resourceType: Type.COMPOSITEPROFILE,
          },
          onLeave: {
            saveEditor: true,
            saveTableOfContent: true,
          },
          action: OpenCompositeProfilePostDefEditor,
          idKey: 'compositeProfileId',
        },
      },
      {
        path: 'delta',
        component: CompositeDeltaEditorComponent,
        canActivate: [EditorActivateGuard],
        canDeactivate: [EditorDeactivateGuard],
        data: {
          editorMetadata: {
            id: EditorID.COMPOSITE_PROFILE_DELTA,
            title: 'Delta',
            resourceType: Type.COMPOSITEPROFILE,
          },
          onLeave: {
            saveEditor: true,
            saveTableOfContent: true,
          },
          action: OpenCompositeProfileDeltaEditor,
          idKey: 'compositeProfileId',
        },
      },

    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CompositeProfileRoutingModule { }
