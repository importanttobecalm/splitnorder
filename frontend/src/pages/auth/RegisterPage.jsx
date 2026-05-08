import { useState, useMemo, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useI18n } from '../../hooks/useI18n';
import authApi from '../../api/authApi';
import AuthLayout from './AuthLayout';
import '../../styles/auth.css';

export default function RegisterPage() {
  const { t, lang } = useI18n();

  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setError('');
  };

  // Şifre gücü kontrolü
  const passwordRules = useMemo(() => {
    const p = form.password;
    return {
      length: p.length >= 8,
      upper: /[A-Z]/.test(p),
      lower: /[a-z]/.test(p),
      digit: /[0-9]/.test(p),
    };
  }, [form.password]);

  const isPasswordValid = passwordRules.length && passwordRules.upper && passwordRules.lower && passwordRules.digit;

  // Email validasyonu
  const isValidEmail = (email) => {
    const emailRegex = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email) && email.length <= 254;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Client-side validasyon
    if (!isValidEmail(form.email)) {
      setError(t('error.INVALID_EMAIL'));
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError(t('error.PASSWORDS_NOT_MATCH'));
      return;
    }

    if (!isPasswordValid) {
      setError(t('error.PASSWORD_WEAK'));
      return;
    }

    setLoading(true);

    try {
      const data = await authApi.register(form.username, form.email, form.password, lang);

      if (data.success) {
        setSuccess(true);
      } else {
        setError(t(`error.${data.error}`) || t('error.INTERNAL_ERROR'));
      }
    } catch (err) {
      setError(t('error.INTERNAL_ERROR'));
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleRegister = () => {
    window.location.href = 'http://localhost:8090/stemsep/api/auth/google/login';
  };

  // Başarılı kayıt sonrası
  if (success) {
    return (
      <AuthLayout>
        <div className="auth-header">
          <div className="auth-logo">
            <span className="auth-logo-icon">🎵</span>
            <span className="auth-logo-text">{t('common.appName')}</span>
          </div>
        </div>
        <div className="auth-success">
          <span className="auth-message-icon">✅</span>
          <div>
            <strong>{t('register.success')}</strong>
            <br />
            {t('register.checkEmail')}
          </div>
        </div>
        <div className="auth-footer" style={{ marginTop: '20px' }}>
          <Link to="/login" className="auth-footer-link">
            {t('register.login')}
          </Link>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <div className="auth-header">
        <div className="auth-logo">
          <span className="auth-logo-icon">🎵</span>
          <span className="auth-logo-text">{t('common.appName')}</span>
        </div>
        <h1 className="auth-title">{t('register.title')}</h1>
        <p className="auth-subtitle">{t('register.subtitle')}</p>
      </div>

      {error && (
        <div className="auth-error">
          <span className="auth-message-icon">⚠️</span>
          <span>{error}</span>
        </div>
      )}

      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="auth-field">
          <label className="auth-label" htmlFor="register-username">
            {t('register.username')}
          </label>
          <div className="auth-input-wrapper">
            <input
              id="register-username"
              className="auth-input"
              type="text"
              name="username"
              value={form.username}
              onChange={handleChange}
              placeholder={t('register.username.placeholder')}
              required
              minLength={3}
              autoComplete="username"
            />
            <span className="auth-input-icon">👤</span>
          </div>
        </div>

        <div className="auth-field">
          <label className="auth-label" htmlFor="register-email">
            {t('register.email')}
          </label>
          <div className="auth-input-wrapper">
            <input
              id="register-email"
              className="auth-input"
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder={t('register.email.placeholder')}
              required
              autoComplete="email"
            />
            <span className="auth-input-icon">✉</span>
          </div>
        </div>

        <div className="auth-field">
          <label className="auth-label" htmlFor="register-password">
            {t('register.password')}
          </label>
          <div className="auth-input-wrapper">
            <input
              id="register-password"
              className="auth-input"
              type={showPassword ? 'text' : 'password'}
              name="password"
              value={form.password}
              onChange={handleChange}
              placeholder={t('register.password.placeholder')}
              required
              minLength={8}
              autoComplete="new-password"
            />
            <span className="auth-input-icon">🔒</span>
            <button
              type="button"
              className="auth-password-toggle"
              onClick={() => setShowPassword(!showPassword)}
              tabIndex={-1}
            >
              {showPassword ? '🙈' : '👁'}
            </button>
          </div>
          {form.password.length > 0 && (
            <div className="password-rules">
              <span className={`password-rule ${passwordRules.length ? 'valid' : ''}`}>
                <span className="password-rule-icon">{passwordRules.length ? '✓' : '○'}</span>
                {t('password.rule.length')}
              </span>
              <span className={`password-rule ${passwordRules.upper ? 'valid' : ''}`}>
                <span className="password-rule-icon">{passwordRules.upper ? '✓' : '○'}</span>
                {t('password.rule.upper')}
              </span>
              <span className={`password-rule ${passwordRules.lower ? 'valid' : ''}`}>
                <span className="password-rule-icon">{passwordRules.lower ? '✓' : '○'}</span>
                {t('password.rule.lower')}
              </span>
              <span className={`password-rule ${passwordRules.digit ? 'valid' : ''}`}>
                <span className="password-rule-icon">{passwordRules.digit ? '✓' : '○'}</span>
                {t('password.rule.digit')}
              </span>
            </div>
          )}
        </div>

        <div className="auth-field">
          <label className="auth-label" htmlFor="register-confirm">
            {t('register.confirmPassword')}
          </label>
          <div className="auth-input-wrapper">
            <input
              id="register-confirm"
              className="auth-input"
              type={showConfirm ? 'text' : 'password'}
              name="confirmPassword"
              value={form.confirmPassword}
              onChange={handleChange}
              placeholder={t('register.confirmPassword.placeholder')}
              required
              autoComplete="new-password"
            />
            <span className="auth-input-icon">🔒</span>
            <button
              type="button"
              className="auth-password-toggle"
              onClick={() => setShowConfirm(!showConfirm)}
              tabIndex={-1}
            >
              {showConfirm ? '🙈' : '👁'}
            </button>
          </div>
        </div>

        <button
          type="submit"
          className="auth-submit-btn"
          disabled={loading}
        >
          {loading && <span className="auth-spinner" />}
          {loading ? t('register.loading') : t('register.button')}
        </button>
      </form>

      <div className="auth-divider">
        <div className="auth-divider-line" />
        <span className="auth-divider-text">{t('common.or')}</span>
        <div className="auth-divider-line" />
      </div>

      <button
        type="button"
        className="auth-google-btn"
        onClick={handleGoogleRegister}
      >
        <svg className="auth-google-icon" viewBox="0 0 24 24">
          <path
            d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"
            fill="#4285F4"
          />
          <path
            d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            fill="#34A853"
          />
          <path
            d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
            fill="#FBBC05"
          />
          <path
            d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
            fill="#EA4335"
          />
        </svg>
        {t('register.google')}
      </button>

      <div className="auth-footer">
        {t('register.hasAccount')}
        <Link to="/login" className="auth-footer-link">
          {t('register.login')}
        </Link>
      </div>
    </AuthLayout>
  );
}
