const translations = {
  tr: {
    // Login
    'login.title': 'Giriş Yap',
    'login.subtitle': 'Hesabınıza giriş yapın',
    'login.email': 'E-posta',
    'login.email.placeholder': 'ornek@mail.com',
    'login.password': 'Şifre',
    'login.password.placeholder': 'Şifrenizi girin',
    'login.button': 'Giriş Yap',
    'login.google': 'Google ile Giriş',
    'login.noAccount': 'Hesabınız yok mu?',
    'login.register': 'Kayıt Ol',
    'login.loading': 'Giriş yapılıyor...',

    // Register
    'register.title': 'Kayıt Ol',
    'register.subtitle': 'Yeni hesap oluşturun',
    'register.username': 'Kullanıcı Adı',
    'register.username.placeholder': 'Kullanıcı adınız',
    'register.email': 'E-posta',
    'register.email.placeholder': 'ornek@mail.com',
    'register.password': 'Şifre',
    'register.password.placeholder': 'En az 8 karakter',
    'register.confirmPassword': 'Şifre Tekrar',
    'register.confirmPassword.placeholder': 'Şifrenizi tekrar girin',
    'register.button': 'Kayıt Ol',
    'register.google': 'Google ile Kayıt',
    'register.hasAccount': 'Zaten hesabınız var mı?',
    'register.login': 'Giriş Yap',
    'register.loading': 'Kayıt yapılıyor...',
    'register.success': 'Kayıt başarılı! E-posta adresinize bir doğrulama maili gönderdik.',
    'register.checkEmail': 'Lütfen e-postanızı kontrol edin.',

    // Password rules
    'password.rule.length': 'En az 8 karakter',
    'password.rule.upper': 'En az 1 büyük harf',
    'password.rule.lower': 'En az 1 küçük harf',
    'password.rule.digit': 'En az 1 rakam',

    // Verify
    'verify.title': 'E-posta Doğrulama',
    'verify.success': 'E-postanız başarıyla doğrulandı!',
    'verify.fail': 'Doğrulama bağlantısı geçersiz veya süresi dolmuş.',
    'verify.goLogin': 'Giriş Sayfasına Git',
    'verify.loading': 'Doğrulanıyor...',
    'verify.resend': 'Doğrulama Maili Tekrar Gönder',
    'verify.resendSuccess': 'Doğrulama maili tekrar gönderildi!',

    // Errors
    'error.USERNAME_EXISTS': 'Bu kullanıcı adı zaten kullanılıyor',
    'error.EMAIL_EXISTS': 'Bu e-posta adresi zaten kayıtlı',
    'error.USER_NOT_FOUND': 'Bu e-posta adresiyle kayıtlı kullanıcı bulunamadı',
    'error.INVALID_PASSWORD': 'Şifre hatalı',
    'error.EMAIL_NOT_VERIFIED': 'E-posta adresiniz henüz doğrulanmamış',
    'error.USE_GOOGLE_LOGIN': 'Bu hesap Google ile oluşturulmuş. Lütfen Google ile giriş yapın.',
    'error.PASSWORDS_NOT_MATCH': 'Şifreler eşleşmiyor',
    'error.GOOGLE_AUTH_FAILED': 'Google ile giriş başarısız',
    'error.INTERNAL_ERROR': 'Bir hata oluştu. Lütfen tekrar deneyin.',
    'error.PASSWORD_WEAK': 'Şifre en az 1 büyük harf, 1 küçük harf ve 1 rakam içermelidir',
    'error.PASSWORD_TOO_SHORT': 'Şifre en az 8 karakter olmalıdır',
    'error.INVALID_EMAIL': 'Geçerli bir e-posta adresi girin',
    'error.USERNAME_REQUIRED': 'Kullanıcı adı gereklidir',
    'error.ALREADY_VERIFIED': 'E-posta zaten doğrulanmış',
    'error.INVALID_OR_EXPIRED_TOKEN': 'Doğrulama bağlantısı geçersiz veya süresi dolmuş',

    // Theme
    'theme.light': 'Açık Tema',
    'theme.dark': 'Koyu Tema',

    // Common
    'common.or': 'veya',
    'common.appName': 'Splitnorder',
    'common.appTagline': 'AI ile Müzik Stem Ayırma',
  },

  en: {
    // Login
    'login.title': 'Sign In',
    'login.subtitle': 'Sign in to your account',
    'login.email': 'Email',
    'login.email.placeholder': 'example@mail.com',
    'login.password': 'Password',
    'login.password.placeholder': 'Enter your password',
    'login.button': 'Sign In',
    'login.google': 'Sign in with Google',
    'login.noAccount': "Don't have an account?",
    'login.register': 'Sign Up',
    'login.loading': 'Signing in...',

    // Register
    'register.title': 'Sign Up',
    'register.subtitle': 'Create a new account',
    'register.username': 'Username',
    'register.username.placeholder': 'Your username',
    'register.email': 'Email',
    'register.email.placeholder': 'example@mail.com',
    'register.password': 'Password',
    'register.password.placeholder': 'At least 8 characters',
    'register.confirmPassword': 'Confirm Password',
    'register.confirmPassword.placeholder': 'Re-enter your password',
    'register.button': 'Sign Up',
    'register.google': 'Sign up with Google',
    'register.hasAccount': 'Already have an account?',
    'register.login': 'Sign In',
    'register.loading': 'Signing up...',
    'register.success': 'Registration successful! We sent a verification email to your address.',
    'register.checkEmail': 'Please check your email.',

    // Password rules
    'password.rule.length': 'At least 8 characters',
    'password.rule.upper': 'At least 1 uppercase letter',
    'password.rule.lower': 'At least 1 lowercase letter',
    'password.rule.digit': 'At least 1 digit',

    // Verify
    'verify.title': 'Email Verification',
    'verify.success': 'Your email has been verified successfully!',
    'verify.fail': 'Verification link is invalid or has expired.',
    'verify.goLogin': 'Go to Sign In',
    'verify.loading': 'Verifying...',
    'verify.resend': 'Resend Verification Email',
    'verify.resendSuccess': 'Verification email has been resent!',

    // Errors
    'error.USERNAME_EXISTS': 'This username is already taken',
    'error.EMAIL_EXISTS': 'This email is already registered',
    'error.USER_NOT_FOUND': 'No user found with this email address',
    'error.INVALID_PASSWORD': 'Incorrect password',
    'error.EMAIL_NOT_VERIFIED': 'Your email has not been verified yet',
    'error.USE_GOOGLE_LOGIN': 'This account was created with Google. Please sign in with Google.',
    'error.PASSWORDS_NOT_MATCH': 'Passwords do not match',
    'error.GOOGLE_AUTH_FAILED': 'Google sign in failed',
    'error.INTERNAL_ERROR': 'An error occurred. Please try again.',
    'error.PASSWORD_WEAK': 'Password must contain at least 1 uppercase, 1 lowercase, and 1 digit',
    'error.PASSWORD_TOO_SHORT': 'Password must be at least 8 characters',
    'error.INVALID_EMAIL': 'Enter a valid email address',
    'error.USERNAME_REQUIRED': 'Username is required',
    'error.ALREADY_VERIFIED': 'Email is already verified',
    'error.INVALID_OR_EXPIRED_TOKEN': 'Verification link is invalid or has expired',

    // Theme
    'theme.light': 'Light Mode',
    'theme.dark': 'Dark Mode',

    // Common
    'common.or': 'or',
    'common.appName': 'Splitnorder',
    'common.appTagline': 'AI Music Stem Separation',
  },
};

export default translations;
