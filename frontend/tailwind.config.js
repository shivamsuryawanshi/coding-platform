/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Custom dark theme colors
        dark: {
          900: '#0a0a0f',
          800: '#0f1117',
          700: '#151821',
          600: '#1c1f2e',
          500: '#252837',
        },
        accent: {
          primary: '#6366f1',    // Indigo
          secondary: '#8b5cf6',  // Violet
          success: '#10b981',    // Emerald
          warning: '#f59e0b',    // Amber
          error: '#ef4444',      // Red
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      }
    },
  },
  plugins: [],
}

