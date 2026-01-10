import { User } from './user.model';

export interface Post {
  id: string;
  userId: string;
  content: string;
  mediaUrl?: string;
  mediaType?: 'image' | 'video';
  likesCount: number;
  commentsCount: number;
  isLiked: boolean;
  hidden?: boolean;
  createdAt: string;
  updatedAt: string;
  user?: User;
}

export interface CreatePostRequest {
  content: string;
  mediaUrl?: string;
  mediaType?: 'image' | 'video';
}

export interface UpdatePostRequest {
  content?: string;
  mediaUrl?: string;
  mediaType?: 'image' | 'video';
}
