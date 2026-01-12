export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN'
}

export interface User {
  id: string;
  email: string;
  username: string;
  fullName?: string;
  bio?: string;
  avatar?: string;
  role: UserRole;
  banned?: boolean;
  createdAt: string;
  updatedAt: string;
  // from backend UserDto
  subscriberIds?: number[];
  subscriptionIds?: number[];
  subscribed?: boolean;
  subscribersCount?: number;
  subscriptionsCount?: number;
}

export interface AuthResponse {
  user: User;
  accessToken: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  fullName?: string;
}
