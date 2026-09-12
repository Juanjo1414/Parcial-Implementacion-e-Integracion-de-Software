// Estos tipos reflejan uno a uno los DTOs expuestos por la API
// (com.eia.camelracing.*.dto). Mantenerlos alineados con el backend es lo
// que le da a esta capa su valor: si el contrato cambia, TypeScript avisa
// en tiempo de compilación en vez de fallar en producción.

export type Role = "ADMIN" | "ORGANIZER" | "VIEWER";

export type CompetitorType = "DWARF" | "CAMEL" | "MEDIUM" | "OTHER";
export type CompetitorStatus = "ACTIVE" | "INJURED" | "SUSPENDED" | "RETIRED";
export type TeamStatus = "ACTIVE" | "SUSPENDED" | "INACTIVE";
export type RaceType = "INDIVIDUAL" | "TEAM" | "MIXED";
export type RaceStatus =
  | "DRAFT"
  | "OPEN_FOR_REGISTRATION"
  | "CLOSED_FOR_REGISTRATION"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CANCELLED";
export type RegistrationStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";
export type ResultStatus = "FINISHED" | "DISQUALIFIED" | "DID_NOT_FINISH" | "DID_NOT_START";

export interface AuthResponse {
  token: string;
  username: string;
  role: Role;
}

export interface CompetitorRequest {
  name: string;
  nickname: string;
  type: CompetitorType;
  birthDate: string | null;
  weight: number;
  height: number;
  originCountry: string | null;
}

export interface CompetitorResponse {
  id: string;
  name: string;
  nickname: string;
  type: CompetitorType;
  birthDate: string | null;
  weight: number;
  height: number;
  originCountry: string | null;
  status: CompetitorStatus;
  registeredAt: string;
  wins: number;
  losses: number;
  racesCompleted: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface TeamMemberSummary {
  id: string;
  name: string;
  nickname: string;
}

export interface TeamRequest {
  name: string;
  description: string | null;
  coachName: string | null;
}

export interface TeamResponse {
  id: string;
  name: string;
  description: string | null;
  coachName: string | null;
  status: TeamStatus;
  createdAt: string;
  wins: number;
  losses: number;
  members: TeamMemberSummary[];
}

export interface RaceRequest {
  name: string;
  description: string | null;
  scheduledAt: string;
  startLocation: string | null;
  finishLocation: string | null;
  distanceMeters: number;
  maxParticipants: number;
  type: RaceType;
  organizerName: string | null;
  registrationDeadline: string;
}

export interface RaceResponse {
  id: string;
  name: string;
  description: string | null;
  scheduledAt: string;
  startLocation: string | null;
  finishLocation: string | null;
  distanceMeters: number;
  maxParticipants: number;
  type: RaceType;
  status: RaceStatus;
  organizerName: string | null;
  registrationDeadline: string;
  createdAt: string;
  updatedAt: string;
}

export interface RegistrationRequest {
  competitorId: string | null;
  teamId: string | null;
  startingPosition: number | null;
}

export interface RegistrationResponse {
  id: string;
  raceId: string;
  competitorId: string | null;
  competitorNickname: string | null;
  teamId: string | null;
  teamName: string | null;
  registrationDate: string;
  status: RegistrationStatus;
  startingPosition: number | null;
  validationNotes: string | null;
  performedBy: string | null;
}

export interface ResultRequest {
  registrationId: string;
  finalPosition: number | null;
  completionTimeSeconds: number | null;
  penaltyTimeSeconds: number | null;
  status: ResultStatus;
  notes: string | null;
}

export interface ResultResponse {
  id: string;
  registrationId: string;
  raceId: string;
  participantLabel: string;
  finalPosition: number | null;
  completionTimeSeconds: number | null;
  penaltyTimeSeconds: number | null;
  status: ResultStatus;
  points: number;
  notes: string | null;
  recordedBy: string | null;
  recordedAt: string;
}

export interface CompetitorStandingResponse {
  competitorId: string;
  nickname: string;
  totalPoints: number;
  wins: number;
  racesCompleted: number;
}

export interface TeamStandingResponse {
  teamId: string;
  teamName: string;
  totalPoints: number;
  wins: number;
  racesCompleted: number;
}

export interface AuditLogResponse {
  id: string;
  username: string;
  action: string;
  entityType: string;
  entityId: string | null;
  timestamp: string;
  description: string | null;
  previousValue: string | null;
  newValue: string | null;
}

export interface ApiErrorBody {
  message: string;
  status: number;
  timestamp: string;
  validationErrors?: Record<string, string>;
}
