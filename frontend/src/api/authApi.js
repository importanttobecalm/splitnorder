const API_BASE_URL = 'http://localhost:8090/stemsep/api/auth';

const authApi = {
  /**
   * Normal (e-posta/şifre) kayıt
   */
  register: async (username, email, password, lang = 'tr') => {
    const res = await fetch(`${API_BASE_URL}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password, lang }),
    });
    return res.json();
  },

  /**
   * Normal (e-posta/şifre) giriş
   */
  login: async (email, password) => {
    const res = await fetch(`${API_BASE_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
    return res.json();
  },



  /**
   * E-posta doğrulama
   */
  verifyEmail: async (token) => {
    const res = await fetch(`${API_BASE_URL}/verify-email?token=${encodeURIComponent(token)}`);
    return res.json();
  },

  /**
   * Doğrulama maili tekrar gönder
   */
  resendVerification: async (email, lang = 'tr') => {
    const res = await fetch(`${API_BASE_URL}/resend-verification`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, lang }),
    });
    return res.json();
  },
};

export default authApi;
