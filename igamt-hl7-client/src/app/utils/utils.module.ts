/**
 * Created by hnt5 on 10/30/17.
 */
import {NgModule}     from '@angular/core';

import {FormsModule} from '@angular/forms';
import {DisplayBadgeComponent} from "../common/badge/display-badge.component";
import {EditSimpleConstraintComponent} from "../common/constraint/edit-simpleconstraint.component";
import {EditSimplePropositionComponent} from "../common/constraint/edit-simpleproposition.component";
import {EditSimplePropositionConstraintComponent}  from "../common/constraint/edit-simplepropositionconstraint.component";
import {EditComplexConstraintComponent} from "../common/constraint/edit-complexconstraint.component";

import {DtFlavorPipe} from "../igdocuments/igdocument-edit/segment-edit/coconstraint-table/datatype-name.pipe";
import {CommonModule} from "@angular/common";
import {EntityHeaderComponent} from "../common/entity-header/entity-header.component";
import {DisplayLabelComponent} from "../common/label/display-label.component";
import {Routes, RouterModule, ActivatedRouteSnapshot} from "@angular/router";

import {ButtonModule, ProgressSpinnerModule, OrganizationChartModule, DragDropModule} from 'primeng/primeng';
import {DndListModule} from 'ngx-drag-and-drop-lists';
import {DisplayRefComponent} from "../common/tree-table-label/display-ref.component";
import {DisplaySingleCodeComponent} from "../common/tree-table-label/display-singlecode.component";
import {DisplayConstantValueComponent} from "../common/tree-table-label/display-constantvalue.component";
import {DisplayCommentComponent} from "../common/tree-table-label/display-comment.component";
import {DisplayPathComponent} from "../common/constraint/display-path.component";

import {DropdownModule} from 'primeng/dropdown';
import {CheckboxModule} from 'primeng/checkbox';
import {TreeModule} from 'primeng/tree';
import {DialogModule} from 'primeng/dialog';
import {PanelModule} from 'primeng/panel';
import {FieldsetModule} from 'primeng/fieldset';
import {CrossReferenceComponent} from "../common/cross-reference/cross-reference.component";
import {DatatypeListManagerComponent} from "../common/datatype-list-manager/datatype-list-manager.component";
import {TableModule} from "primeng/components/table/table";
import {KeyFilterModule} from 'primeng/keyfilter';
import {MessagesModule} from 'primeng/messages';
import {MessageModule} from 'primeng/message';
import {DisplayMenuComponent} from "../common/display-menu/display-menu.component";
import {RadioButtonModule} from "primeng/components/radiobutton/radiobutton";
import {TreeTableModule} from "primeng/components/treetable/treetable";

import {NameColComponent} from "../common/tree-table/name/name-col.component";
import {UsageColComponent} from "../common/tree-table/usage/usage-col.component";
import {UsageReadonlyColComponent} from "../common/tree-table/usage/usage-readonly-col.component";
import {CardinalityColComponent} from "../common/tree-table/cardinality/cardinality-col.component";
import {CardinalityReadonlyColComponent} from "../common/tree-table/cardinality/cardinality-readonly-col.component";
import {LengthColComponent} from "../common/tree-table/length/length-col.component";
import {LengthReadonlyColComponent} from "../common/tree-table/length/length-readonly-col.component";
import {ConfLengthColComponent} from "../common/tree-table/conflength/conflength-col.component";
import {ConfLengthReadonlyColComponent} from "../common/tree-table/conflength/conflength-readonly-col.component";
import {DatatypeColComponent} from "../common/tree-table/datatype/datatype-col.component";
import {DatatypeReadonlyColComponent} from "../common/tree-table/datatype/datatype-readonly-col.component";
import {ValuesetColComponent} from "../common/tree-table/valueset/valueset-col.component";
import {ValuesetReadonlyColComponent} from "../common/tree-table/valueset/valueset-readonly-col.component";
import {SingleCodeColComponent} from "../common/tree-table/singlecode/singlecode-col.component";
import {SingleCodeReadonlyColComponent} from "../common/tree-table/singlecode/singlecode-readonly-col.component";
import {ConstantValueColComponent} from "../common/tree-table/constantvalue/constantvalue-col.component";
import {ConstantValueReadonlyColComponent} from "../common/tree-table/constantvalue/constantvalue-readonly-col.component";
import {TextdefColComponent} from "../common/tree-table/textdef/textdef-col.component";
import {TextdefReadonlyColComponent} from "../common/tree-table/textdef/textdef-readonly-col.component";
import {CommentColComponent} from "../common/tree-table/comment/comment-col.component";
import {CommentReadonlyColComponent} from "../common/tree-table/comment/comment-readonly-col.component";

import {CsSegmentTreeComponent} from '../common/cs-segment-tree/cs-segment-tree.component';
import {PatternEditorDemoComponent} from '../common/pattern-editor-demo/pattern-editor-demo.component';
import {PatternDialogComponent} from '../common/pattern-dialog/pattern-dialog.component';
@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    ButtonModule,
    FormsModule,
    DropdownModule,
    CheckboxModule,
    TreeModule,
    DialogModule,
    PanelModule,
    FieldsetModule,
    RadioButtonModule,
    TableModule,
    ProgressSpinnerModule,
    DragDropModule,
    OrganizationChartModule,
    TreeTableModule,
    KeyFilterModule,MessageModule,MessagesModule,
    DndListModule

  ],
  declarations: [
    DisplayBadgeComponent,
    EntityHeaderComponent,
    DisplayMenuComponent,
    DtFlavorPipe,
    DisplayLabelComponent,
    DisplayRefComponent,
    DisplaySingleCodeComponent,
    DisplayConstantValueComponent,
    DisplayCommentComponent,
    EditSimpleConstraintComponent,
    EditSimplePropositionComponent,
    EditSimplePropositionConstraintComponent,
    EditComplexConstraintComponent,
    DisplayPathComponent,
    CrossReferenceComponent,
    DatatypeListManagerComponent,

    NameColComponent,
    UsageColComponent,
    UsageReadonlyColComponent,
    CardinalityColComponent,
    CardinalityReadonlyColComponent,
    LengthColComponent,
    LengthReadonlyColComponent,
    ConfLengthColComponent,
    ConfLengthReadonlyColComponent,
    DatatypeColComponent,
    DatatypeReadonlyColComponent,
    ValuesetColComponent,
    ValuesetReadonlyColComponent,
    SingleCodeColComponent,
    SingleCodeReadonlyColComponent,
    ConstantValueColComponent,
    ConstantValueReadonlyColComponent,
    TextdefColComponent,
    TextdefReadonlyColComponent,
    CommentColComponent,
    CommentReadonlyColComponent,

    CsSegmentTreeComponent,
    PatternEditorDemoComponent,
    PatternDialogComponent
  ],

  exports: [
    DisplayBadgeComponent,
    EntityHeaderComponent,
    DtFlavorPipe,
    DisplayLabelComponent,
    DisplayRefComponent,
    DisplaySingleCodeComponent,
    DisplayConstantValueComponent,
    DisplayCommentComponent,
    EditSimpleConstraintComponent,
    EditSimplePropositionComponent,
    EditSimplePropositionConstraintComponent,
    EditComplexConstraintComponent,
    DisplayPathComponent,
    CrossReferenceComponent,DatatypeListManagerComponent,
    DisplayMenuComponent,

    NameColComponent,
    UsageColComponent,
    UsageReadonlyColComponent,
    CardinalityColComponent,
    CardinalityReadonlyColComponent,
    LengthColComponent,
    LengthReadonlyColComponent,
    ConfLengthColComponent,
    ConfLengthReadonlyColComponent,
    DatatypeColComponent,
    DatatypeReadonlyColComponent,
    ValuesetColComponent,
    ValuesetReadonlyColComponent,
    SingleCodeColComponent,
    SingleCodeReadonlyColComponent,
    ConstantValueColComponent,
    ConstantValueReadonlyColComponent,
    TextdefColComponent,
    TextdefReadonlyColComponent,
    CommentColComponent,
    CommentReadonlyColComponent,

    CsSegmentTreeComponent,
    PatternEditorDemoComponent,
    PatternDialogComponent
  ]
})
export class UtilsModule {}
