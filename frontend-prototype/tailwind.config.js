/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        canvas: {
          DEFAULT: "#F4F1EC",
          soft: "#FAF7F2",
          line: "#E6E0D6",
        },
        ink: {
          DEFAULT: "#1F2230",
          soft: "#4A4F62",
          mute: "#8A8FA3",
        },
        stem: {
          vocals: "#E8554E",
          "vocals-soft": "#FDECEA",
          drums: "#F2A35E",
          "drums-soft": "#FEF3E6",
          bass: "#6B5B95",
          "bass-soft": "#EDE9F5",
          other: "#3FB8AF",
          "other-soft": "#E4F5F3",
        },
      },
      fontFamily: {
        sans: ['"Inter"', "system-ui", "sans-serif"],
        display: ['"Fraunces"', "serif"],
        mono: ['"JetBrains Mono"', "monospace"],
      },
      boxShadow: {
        card: "0 1px 2px rgba(31,34,48,0.04), 0 8px 24px rgba(31,34,48,0.06)",
        "card-lg": "0 4px 8px rgba(31,34,48,0.06), 0 24px 48px rgba(31,34,48,0.08)",
      },
      borderRadius: {
        xl2: "20px",
      },
    },
  },
  plugins: [],
};
