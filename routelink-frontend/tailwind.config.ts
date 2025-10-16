import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50:"#fff7e6",100:"#feefcc",200:"#fde09c",300:"#fcd26d",400:"#fac440",
          500:"#f9b61a",600:"#fbbf24",700:"#e0a013",800:"#b97f0f",900:"#8b5b0a",
        },
      },
    },
  },
  plugins: [],
} satisfies Config;
