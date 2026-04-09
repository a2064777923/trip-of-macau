import { create } from 'zustand';
import type { AdminCurrentUser } from '../types/admin';

interface AuthStore {
  user: AdminCurrentUser | null;
  setUser: (user: AdminCurrentUser | null) => void;
}

export const useAuthStore = create<AuthStore>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
}));
