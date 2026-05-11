import { createContext, useContext, useState, useEffect } from 'react';
import translations from '../i18n/translations';

const I18nContext = createContext();

export function I18nProvider({ children }) {
  const [lang, setLang] = useState(() => {
    return localStorage.getItem('splitnorder_lang') || 'tr';
  });

  useEffect(() => {
    localStorage.setItem('splitnorder_lang', lang);
    document.documentElement.lang = lang;
  }, [lang]);

  const t = (key) => {
    return translations[lang]?.[key] || translations['en']?.[key] || key;
  };

  const toggleLang = () => {
    setLang((prev) => (prev === 'tr' ? 'en' : 'tr'));
  };

  return (
    <I18nContext.Provider value={{ lang, setLang, toggleLang, t }}>
      {children}
    </I18nContext.Provider>
  );
}

export function useI18n() {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error('useI18n must be used within an I18nProvider');
  }
  return context;
}
