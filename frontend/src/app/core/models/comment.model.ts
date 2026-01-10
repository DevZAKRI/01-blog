import { User } from './user.model';

export interface Comment {
  id: string;
  postId: string;
  userId: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  user?: User;
}

export interface CreateCommentRequest {
  content: string;
}
