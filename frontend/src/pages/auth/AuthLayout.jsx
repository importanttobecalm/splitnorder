import { useI18n } from '../../hooks/useI18n';
import { useTheme } from '../../hooks/useTheme';
import '../../styles/auth.css';

/**
 * Auth sayfaları için ortak layout wrapper.
 * Tema ve dil değiştirme butonlarını içerir.
 */
export default function AuthLayout({ children }) {
  const { lang, toggleLang } = useI18n();
  const { theme, toggleTheme } = useTheme();

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-card">
          {/* Üst kontrol butonları: tema + dil */}
          <div className="auth-top-controls">
            <button
              type="button"
              className="auth-toggle-btn"
              onClick={toggleTheme}
              title={theme === 'dark' ? 'Light Mode' : 'Dark Mode'}
            >
              <span className="toggle-icon">
                {theme === 'dark' ? '☀️' : '🌙'}
              </span>
              {theme === 'dark' ? '☀️' : '🌙'}
            </button>

            <button
              type="button"
              className="auth-toggle-btn"
              onClick={toggleLang}
              title={lang === 'tr' ? 'English' : 'Türkçe'}
            >
              <span className="toggle-icon">🌐</span>
              {lang === 'tr' ? 'EN' : 'TR'}
            </button>
          </div>

          {children}
        </div>
      </div>
    </div>
  );
}
