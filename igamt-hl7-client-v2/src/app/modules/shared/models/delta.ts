import { TreeNode } from 'primeng/api';
import { Type } from '../constants/type.enum';
import { ICoConstraintBindingContext } from './co-constraint.interface';
import { IDomainInfo } from './domain-info.interface';

export interface IDelta<T> {
  type: Type;
  source: IDeltaInfo;
  target: IDeltaInfo;
  delta: T;
  conformanceStatements?: Array<IDeltaNode<any>>;
  coConstraintBindings?: ICoConstraintBindingContext[];
  dynamicMapping?: IDynamicMappingItemDelta[];
}

export interface IDeltaTreeNode extends TreeNode {
  data: {
    action?: any;
    position?: any;
    type: string;
    usage: IDeltaNode<string>;
    constantValue: IDeltaNode<string>;
    minLength: IDeltaNode<string>;
    maxLength: IDeltaNode<string>;
    minCardinality?: IDeltaNode<number>;
    maxCardinality?: IDeltaNode<string>;
    confLength: IDeltaNode<string>;
    definition: IDeltaNode<string>;
    reference: IDeltaReference;
    name: IDeltaNode<string>;
  };
  children?: IDeltaTreeNode[];
}

export interface IDeltaReference {
  type: Type;
  id: IDeltaNode<string>;
  domainInfo: IDeltaNode<IDomainInfo>;
  label: IDeltaNode<string>;
}

export interface IDeltaNode<T> {
  previous: T;
  current: T;
  action?: DeltaAction;
}

export interface IDeltaInfo {
  document: ISourceDocument;
  domainInfo: IDomainInfo;
  name: string;
  ext: string;
  description: string;
  id: string;
}

export interface ISourceDocument {
  id: string;
  name?: any;
  scope: string;
}

export interface  IDynamicMappingItemDelta {
  action: DeltaAction;
  datatypeName: string;
  flavorId: IDeltaNode<string>;
}

export enum DeltaAction {
  UNCHANGED = 'UNCHANGED',
  ADDED = 'ADDED',
  DELETED = 'DELETED',
  UPDATED = 'UPDATED',
}
