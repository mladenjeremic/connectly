export type Role = "USER" | "ADMIN";
export type AccountStatus = "ACTIVE" | "SUSPENDED" | "BANNED" | "DEACTIVATED";
export type ReportStatus = "PENDING" | "REVIEWED" | "DISMISSED";
export type ReportTargetType = "POST" | "COMMENT";
export type NotificationType =
  | "LIKE" | "COMMENT" | "FOLLOW" | "ACCOUNT_STATUS" | "COMMENT_LIKE" | "COMMENT_REPLY" | "STORY_REPLY";
export type ReportDecision = "DISMISS" | "REVIEW" | "REMOVE_CONTENT";
export type AdminActionType =
  | "BAN" | "UNBAN" | "PERMANENT_BAN" | "DEACTIVATE" | "REACTIVATE"
  | "REMOVE_POST" | "REMOVE_COMMENT" | "REPORT_REVIEWED" | "REPORT_DISMISSED";

export interface AuthResult {
  token: string;
  userId: number;
  username: string;
  role: Role;
}

export interface Me {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  bio: string;
  profileImageUrl: string | null;
  role: Role;
  createdAt: string;
}

export interface UserSummary {
  id: number;
  username: string;
  fullName: string;
  profileImageUrl: string | null;
  followedByMe: boolean;
  self: boolean;
}

export interface Profile {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  bio: string;
  profileImageUrl: string | null;
  createdAt: string;
  followerCount: number;
  followingCount: number;
  postCount: number;
  followedByMe: boolean;
  ownProfile: boolean;
  blockedByMe: boolean;
}

export interface PostImage {
  id: number;
  url: string;
}

export interface Post {
  id: number;
  authorId: number;
  authorUsername: string;
  authorFullName: string;
  authorProfileImageUrl: string | null;
  content: string;
  images: PostImage[];
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  commentCount: number;
  likedByMe: boolean;
  ownPost: boolean;
  savedByMe: boolean;
}

export interface Comment {
  id: number;
  postId: number;
  /** null = komentar prvog nivoa, inače id komentara-korena niti */
  parentId: number | null;
  authorId: number;
  authorUsername: string;
  authorProfileImageUrl: string | null;
  content: string;
  createdAt: string;
  canDelete: boolean;
  likeCount: number;
  likedByMe: boolean;
  replyCount: number;
}

export interface SaveResult {
  saved: boolean;
}

export interface FollowResult {
  following: boolean;
  followerCount: number;
}

export interface LikeResult {
  liked: boolean;
  likeCount: number;
}

export interface Story {
  id: number;
  imageUrl: string;
  createdAt: string;
  expiresAt: string;
  ownStory: boolean;
}

export interface StoryGroup {
  userId: number;
  username: string;
  profileImageUrl: string | null;
  ownGroup: boolean;
  stories: Story[];
}

export interface ChatMessage {
  id: number;
  senderId: number;
  content: string;
  mine: boolean;
  /** poruka je odgovor na priču */
  storyReply: boolean;
  createdAt: string;
  read: boolean;
}

export interface Conversation {
  id: number;
  user: UserSummary;
  lastMessage: string;
  lastMessageMine: boolean;
  lastMessageAt: string;
  unreadCount: number;
}

export interface AppNotification {
  id: number;
  type: NotificationType;
  actorId: number | null;
  actorUsername: string | null;
  actorProfileImageUrl: string | null;
  postId: number | null;
  message: string | null;
  read: boolean;
  createdAt: string;
}

// ---- admin ----
export interface Dashboard {
  totalUsers: number;
  activeUsers: number;
  suspendedUsers: number;
  bannedUsers: number;
  totalPosts: number;
  pendingReports: number;
}

export interface BanInfo {
  id: number;
  reason: string;
  startsAt: string;
  endsAt: string | null;
  active: boolean;
  permanent: boolean;
  adminUsername: string | null;
}

export interface AdminUser {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  profileImageUrl: string | null;
  role: Role;
  status: AccountStatus;
  createdAt: string;
  postCount: number;
  activeBan: BanInfo | null;
}

export interface AdminAction {
  id: number;
  adminId: number;
  adminUsername: string | null;
  targetUserId: number | null;
  targetUsername: string | null;
  actionType: AdminActionType;
  reason: string | null;
  metadata: string | null;
  createdAt: string;
}

export interface AdminUserDetail {
  user: AdminUser;
  bans: BanInfo[];
  actions: AdminAction[];
}

export interface Report {
  id: number;
  reporterId: number;
  reporterUsername: string | null;
  targetType: ReportTargetType;
  targetId: number;
  postId: number | null;
  targetAuthorId: number | null;
  targetAuthorUsername: string | null;
  preview: string | null;
  imageUrl: string | null;
  targetAvailable: boolean;
  category: string;
  description: string;
  status: ReportStatus;
  reviewedByUsername: string | null;
  reviewedAt: string | null;
  createdAt: string;
}
