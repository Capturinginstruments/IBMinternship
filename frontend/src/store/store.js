import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,

      setAuth: (user, accessToken, refreshToken) => {
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', refreshToken)
        set({ user, accessToken, refreshToken, isAuthenticated: true })
      },

      updateUser: (user) => set({ user }),

      logout: () => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        set({ user: null, accessToken: null, refreshToken: null, isAuthenticated: false })
      },

      isAdmin: () => get().user?.role === 'ADMIN',
      isOfficer: () => get().user?.role === 'OFFICER',
      isFarmer: () => get().user?.role === 'FARMER',
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated })
    }
  )
)

export const useThemeStore = create(
  persist(
    (set, get) => ({
      isDark: window.matchMedia('(prefers-color-scheme: dark)').matches,
      toggle: () => {
        const isDark = !get().isDark
        document.documentElement.classList.toggle('dark', isDark)
        set({ isDark })
      },
      init: () => {
        const isDark = get().isDark
        document.documentElement.classList.toggle('dark', isDark)
      }
    }),
    { name: 'theme-storage' }
  )
)

export const useNotificationStore = create((set) => ({
  unreadCount: 0,
  setUnreadCount: (count) => set({ unreadCount: count }),
  decrement: () => set((s) => ({ unreadCount: Math.max(0, s.unreadCount - 1) })),
  reset: () => set({ unreadCount: 0 }),
}))
