// src/lib/bookings.ts
import { api } from "./api";
import type { BookingDto } from "../types";

// Rider: list my bookings
export async function myBookings(): Promise<BookingDto[]> {
  return await api.get<BookingDto[]>("/api/bookings/me");
}

// Rider: request seats on a trip
export async function requestBooking(tripId: number, seats: number) {
  return await api.post<BookingDto>("/api/bookings/request", { tripId, seats });
}

// Rider: cancel my booking
export async function cancelBooking(id: number) {
  return await api.post<BookingDto>(`/api/bookings/${id}/cancel`);
}

// Driver: list bookings for a trip I own
export async function bookingsForTrip(tripId: number) {
  return await api.get<BookingDto[]>(`/api/bookings/trip/${tripId}`);
}

// Driver: confirm a booking
export async function confirmBooking(id: number) {
  return await api.post<BookingDto>(`/api/bookings/${id}/confirm`);
}

// Driver: decline a booking
export async function declineBooking(id: number) {
  return await api.post<BookingDto>(`/api/bookings/${id}/decline`);
}
