import type { PropsWithChildren } from "react";

export default function Layout({ children }: PropsWithChildren) {
  return (
    <div className="page">
      <main className="container-narrow">
        {/* translucent content panel */}
        <div className="glass p-6 md:p-8">{children}</div>
      </main>
    </div>
  );
}
