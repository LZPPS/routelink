// src/lib/trips.ts
import { api } from "./api";
import type { TripDto } from "@/types";

// --- create trip ---
export type CreateTripReq = {
  startPlace: string; startLat: number; startLng: number;
  endPlace: string;   endLat: number;   endLng: number;
  polyline?: string | null;
  rideAt: string;                 // ISO OffsetDateTime
  pricePerSeat: number;
  seatsTotal: number;
};

export async function createTrip(req: CreateTripReq) {
  const data = await api.post<TripDto>("/api/trips", req);
  return data;
}

// --- list (all trips - admin/debug) ---
export async function listTrips() {
  const data = await api.get<TripDto[]>("/api/trips");
  return data;
}

/** Driver’s own trips (Dashboard) */
export async function listMyTrips(): Promise<TripDto[]> {
  try {
    const data = await api.get<TripDto[]>("/api/trips/mine");
    return data;
  } catch {
    const data = await api.get<TripDto[]>("/api/trips", { params: { mine: true } });
    return data;
  }
}

export async function closeTrip(id: number) {
  const data = await api.post<TripDto>(`/api/trips/${id}/close`);
  return data;
}

export async function reopenTrip(id: number) {
  const data = await api.post<TripDto>(`/api/trips/${id}/reopen`);
  return data;
}

// --- search by date (DB paged) ---
export type SearchByDateReq = {
  date: string; at?: string; windowMin?: number;
  sort?: "rideAt" | "pricePerSeat" | "time" | "price";
  order?: "asc" | "desc";
  page?: number; size?: number;
  minSeats?: number; minPrice?: number; maxPrice?: number;
};
export async function searchByDate(q: SearchByDateReq) {
  const data = await api.get<TripDto[]>("/api/trips/search", { params: q });
  return data;
}

// --- search near ---
export type SearchNearReq = {
  startLat: number; startLng: number;
  endLat: number;   endLng: number;
  date: string;
  radiusKm?: number; at?: string; windowMin?: number;
  sort?: "time" | "price"; order?: "asc" | "desc";
  page?: number; size?: number; minSeats?: number;
  minPrice?: number; maxPrice?: number;
};
export async function searchNear(q: SearchNearReq) {
  const data = await api.get<TripDto[]>("/api/trips/search/near", { params: q });
  return data;
}

// --- search along route ---
export type SearchRouteReq = {
  pickupLat: number; pickupLng: number;
  dropLat: number;   dropLng: number;
  date: string;
  radiusKm?: number; at?: string; windowMin?: number;
  sort?: "time" | "price"; order?: "asc" | "desc";
  page?: number; size?: number; minSeats?: number;
  minPrice?: number; maxPrice?: number;
};
export async function searchRoute(q: SearchRouteReq) {
  const data = await api.get<TripDto[]>("/api/trips/search/route", { params: q });
  return data;
}

// --- get one trip ---
export async function getTrip(id: number) {
  const data = await api.get<TripDto>(`/api/trips/${id}`);
  return data;
}

// --- set/replace polyline ---
export type PolylinePoint = { lat: number; lng: number };
export async function setPolyline(id: number, points: PolylinePoint[]) {
  const data = await api.put<TripDto>(`/api/trips/${id}/polyline`, { points });
  return data;
}
