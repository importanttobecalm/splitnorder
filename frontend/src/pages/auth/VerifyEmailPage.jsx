import { useEffect, useState, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useI18n } from '../../hooks/useI18n';
import authApi from '../../api/authApi';
import AuthLayout from './AuthLayout';
import '../../styles/auth.css';

export default function VerifyEmailPage() {
  const { t } = useI18n();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [status, setStatus] = useState('loading'); // loading | success | error
  const hasAttempted = useRef(false);

  useEffect(() => {
    if (!token) {
      setStatus('error');
      return;
    }

    if (hasAttempted.current) return;
    hasAttempted.current = true;

    const verify = async () => {
      try {
        const data = await authApi.verifyEmail(token);
        setStatus(data.success ? 'success' : 'error');
      } catch (err) {
        setStatus('error');
      }
    };

    verify();
  }, [token]);

  return (
    <AuthLayout>
      <div className="auth-header">
        <div className="auth-logo">
          <span className="auth-logo-icon">🎵</span>
          <span className="auth-logo-text">{t('common.appName')}</span>
        </div>
        <h1 className="auth-title">{t('verify.title')}</h1>
      </div>

      <div className="auth-verify-content">
        {status === 'loading' && (
          <>
            <span className="auth-verify-icon">⏳</span>
            <p className="auth-verify-text">{t('verify.loading')}</p>
            <div className="auth-spinner" style={{ margin: '0 auto' }} />
          </>
        )}

        {status === 'success' && (
          <>
            <span className="auth-verify-icon">✅</span>
            <p className="auth-verify-text">{t('verify.success')}</p>
            <Link to="/login" className="auth-verify-link">
              {t('verify.goLogin')}
            </Link>
          </>
        )}

        {status === 'error' && (
          <>
            <span className="auth-verify-icon">❌</span>
            <p className="auth-verify-text">{t('verify.fail')}</p>
            <Link to="/login" className="auth-verify-link">
              {t('verify.goLogin')}
            </Link>
          </>
        )}
      </div>
    </AuthLayout>
  );
}
