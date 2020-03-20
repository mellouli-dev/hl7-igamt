import {Scope} from '../../../shared/constants/scope.enum';
import {Type} from '../../../shared/constants/type.enum';
import {IAddingInfo} from '../../../shared/models/adding-info';
import {IDocumentMetaData} from './document-metadata.interface';

export interface IDocumentCreationWrapper {
  metadata: IDocumentMetaData;
  scope: Scope;
  added: IAddingInfo[];
  type: Type;
}
