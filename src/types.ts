export type ApplicationStatus = 'Pending' | 'Accepted' | 'Rejected';

export interface UserProfile {
  uid: string;
  fullName: string;
  email: string;
  createdAt: string;
}

export interface Application {
  id: string;
  userId: string;
  title: string;
  organization: string;
  status: ApplicationStatus;
  deadline: string;
  createdAt: string;
}

export enum OperationType {
  CREATE = 'create',
  UPDATE = 'update',
  DELETE = 'delete',
  LIST = 'list',
  GET = 'get',
  WRITE = 'write',
}

export interface FirestoreErrorInfo {
  error: string;
  operationType: OperationType;
  path: string | null;
  authInfo: {
    userId?: string;
    email?: string | null;
    emailVerified?: boolean;
    isAnonymous?: boolean;
    tenantId?: string | null;
    providerInfo: {
      providerId: string;
      displayName: string | null;
      email: string | null;
      photoUrl: string | null;
    }[];
  }
}
