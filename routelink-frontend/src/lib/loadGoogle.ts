// src/lib/loadGoogle.ts
let loading: Promise<void> | null = null;

/**
 * Loads Google Maps JS with the Places library exactly once.
 * Usage:
 *   const key = import.meta.env.VITE_GOOGLE_MAPS_KEY as string;
 *   await loadGooglePlaces(key);
 *   // window.google.maps.places is now available
 */
export function loadGooglePlaces(apiKey: string): Promise<void> {
  // already loaded?
  if ((window as any).google?.maps?.places) return Promise.resolve();
  // currently loading?
  if (loading) return loading;

  loading = new Promise<void>((resolve, reject) => {
    const script = document.createElement("script");
    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places`;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("Failed to load Google Maps JS/Places"));
    document.head.appendChild(script);
  });

  return loading;
}
