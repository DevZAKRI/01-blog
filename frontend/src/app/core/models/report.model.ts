import { User } from './user.model';
import { Post } from './post.model';

export enum ReportStatus {
  PENDING = 'PENDING',
  REVIEWED = 'REVIEWED',
  RESOLVED = 'RESOLVED'
}

export interface Report {
  id: string;
  reporterId: string;
  reportedUserId?: string;
  targetPostId?: string;
  reason: string;
  status: ReportStatus;
  createdAt: string;
  reporter?: User;
  reportedUser?: User;
  reportedPost?: Post;
}

export interface CreateReportRequest {
  reportedUserId: string;
  reason: string;
}

export interface CreatePostReportRequest {
  postId: string;
  reason: string;
}
