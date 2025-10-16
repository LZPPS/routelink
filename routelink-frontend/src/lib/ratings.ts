// src/lib/ratings.ts
import { api } from "./api";

export type CreateRatingReq = {
  bookingId: number;
  stars: number;          // 1..5
  comment?: string;
};

// POST /api/ratings
export async function rateBooking(req: CreateRatingReq) {
  const  data  = await api.post("/api/ratings", req);
  return data;
}
