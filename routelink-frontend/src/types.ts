// --- Roles/Auth ---
export type Role = "RIDER" | "DRIVER";

export interface AuthUser {
  id: number;
  email: string;
  name: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  name: string;
  role: Role;
}

export type User = AuthUser;

export type AuthState = {
  user: User | null;
  token: string; // keep as string; use "" when logged out if you prefer
};

// --- Trips / Bookings ---
export type TripStatus = "OPEN" | "FULL" | "CLOSED";

export interface TripDto {
  id: number;
  driverId: number | null;                 // backend can return null
  startPlace: string;
  startLat: number;
  startLng: number;
  endPlace: string;
  endLat: number;
  endLng: number;
  rideAt: string;                          // ISO
  pricePerSeat: number | string;           // BigDecimal may arrive as string
  seatsLeft: number;
  seatsTotal: number;
  status: TripStatus;
  active: boolean;
  polyline?: string | null;
}

export type BookingStatus =
  | "REQUESTED"
  | "CONFIRMED"
  | "DECLINED"
  | "CANCELLED";

export interface BookingDto {
  id: number;
  seats: number;
  status: BookingStatus;
  createdAt?: string;
  tripId: number;
  rider?: { id: number; name: string; email: string } | null; // driver dashboard view
}

// --- Unified Search (NEW) ---
export type UnifiedSearchReq = {
  startText?: string;
  endText?: string;
  startLat: number;
  startLng: number;
  endLat: number;
  endLng: number;
  seats: number;
  date: string; // "YYYY-MM-DD"
};

export type TripSearchDto = {
  trip: TripDto;
  score: number;
  matchedBy: "NEAR" | "ALONG" | "BOTH";
};

// Helpers
export const asNumber = (v: number | string) => (typeof v === "string" ? parseFloat(v) : v);
export const isTripSearchDto = (r: unknown): r is TripSearchDto =>
  !!r && typeof r === "object" && "trip" in (r as any);
