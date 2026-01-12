import { User } from './user.model';

export interface Comment {
  id: string;
  postId: string;
  userId: string;
  username: string;
  userAvatar?: string;
  text: string;
  createdAt: string;
  user?: User;
}

export interface CreateCommentRequest {
  text: string;
}
