import { User } from './user.model';

export enum ReportStatus {
  PENDING = 'PENDING',
  REVIEWED = 'REVIEWED',
  RESOLVED = 'RESOLVED'
}

export interface Report {
  id: string;
  reporterId: string;
  reportedUserId: string;
  reason: string;
  status: ReportStatus;
  createdAt: string;
  reporter?: User;
  reportedUser?: User;
}

export interface CreateReportRequest {
  reportedUserId: string;
  reason: string;
}
