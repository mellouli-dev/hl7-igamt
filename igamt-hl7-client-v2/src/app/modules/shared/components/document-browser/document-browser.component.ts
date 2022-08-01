import { selectIgDocument } from './../../../../root-store/ig/ig-edit/ig-edit.selectors';
import { IWorkspaceListItem } from './../../models/workspace-list-item.interface';
import { WorkspaceLoadType } from './../../../../root-store/workspace/workspace-list/workspace-list.actions';
import { IgListItem, DiscoverableListItem } from './../../../document/models/document/ig-list-item.class';
import { HttpClient } from '@angular/common/http';
import { IgListLoad } from './../../../../root-store/ig/ig-list/ig-list.actions';
import { Observable } from 'rxjs';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { Component, OnInit, ChangeDetectionStrategy, Inject, ViewChild, TemplateRef } from '@angular/core';
import { Type } from '../../constants/type.enum';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-document-browser',
  templateUrl: './document-browser.component.html',
  styleUrls: ['./document-browser.component.scss'],
})
export class DocumentBrowserComponent implements OnInit {

loading : boolean = false;
currentList: DiscoverableListItem[] =[];
items: MenuItem[] = [];


@ViewChild('folder',null) folder:TemplateRef<HTMLElement>;
@ViewChild('ig',null) ig:TemplateRef<HTMLElement>;
@ViewChild('workspace',null) workspace:TemplateRef<HTMLElement>;
@ViewChild('default',null) default:TemplateRef<HTMLElement>;
  constructor(public dialogRef: MatDialogRef<DocumentBrowserComponent>,
    @Inject(MAT_DIALOG_DATA) public data: IDocumentFileBrowserData, private http: HttpClient) {
console.log(data);

}

ngOnInit() {
}

load(accessType: string, targetType: string ){
  this.loading = true;
  this.items = [];
  if(targetType.toLowerCase()=== 'workspace'){
    this.items.push({label: "Workspaces"})
    this.fetchWorkspaceList(accessType).subscribe((x) => {
      this.items.push({label: accessType})

      console.log(x);
      this.currentList = x;
      this.loading = false;
    });

  } else if(targetType.toLowerCase()=== 'igdocument'){
    this.items.push({label: "IG Document List"})

    this.fetchIgList(accessType).subscribe((x) => {
      this.items.push({label: accessType})

      console.log(x);
      this.currentList = x;
      this.loading = false;
    });
  }
}

fetchIgList(type: string): Observable<IgListItem[]> {
  return this.http.get<IgListItem[]>('api/igdocuments', {
    params: {
      type: type,
    },
  });


}


getTemplateByListItem(elm: DiscoverableListItem){
  if(elm.resourceType === Type.WORKSPACE){
    return this.workspace;

  }else if(elm.resourceType === Type.FOLDER){
    return this.folder;
  }else if(elm.resourceType === Type.IGDOCUMENT) {
    return this.ig;
  }else return this.default;
}

selectIgDocument(item: DiscoverableListItem){
  this.dialogRef.close({id: item.id });
}
fetchWorkspaceList(type: string): Observable<DiscoverableListItem[]> {
  return this.http.get<DiscoverableListItem[]>('api/workspaces', {
    params: {
      type:type,
    },
  });
}


done() {
}
}
export interface IDocumentFileBrowserData {
  root: IFolderTreeNode[],

}
export interface IDocumentFileBrowserReturn {
   newTitle?: string;
   id: string;
   type: Type;
   path: string; //workspaceId.folderId
}



export enum ISaveOption {
  COPY = 'COPY',
  MOVE = 'MOVE'
}

export interface IFolderTreeNode {
  title: string,
  children: IFolderTreeNode[],
  type: Type,
  id: string,
  leaf: boolean,
  data: any,
  loadChildren: (type, id) => Observable<IFolderTreeNode>,

}
