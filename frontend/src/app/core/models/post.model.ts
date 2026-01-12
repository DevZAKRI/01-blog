import { User } from './user.model';

export interface Post {
  id: string;
  authorId: string;
  authorUsername: string;
  authorAvatar?: string;
  title: string;
  description: string;
  mediaUrls?: string[];
  likesCount: number;
  commentsCount: number;
  isLiked: boolean;
  hidden?: boolean;
  createdAt: string;
  updatedAt: string;
  author?: User;
}

export interface CreatePostRequest {
  title: string;
  description: string;
  mediaUrls?: string[];
}

export interface UpdatePostRequest {
  title?: string;
  description?: string;
  mediaUrls?: string[];
}
