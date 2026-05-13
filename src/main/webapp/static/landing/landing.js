gsap.registerPlugin(ScrollTrigger);

// HER PARÇANIN PNG İÇİNDEKİ KONUMU (PNG center'a göre normalize edilmiş offset, -0.5..0.5)
// Yusuf'un logosundan hesaplandı
const PARTS = {
  vokal: { offX: 0.0166, offY: -0.2349, corner: { sx: -1, sy: -1 }, rot: -6 },  // sol üst
  davul: { offX: -0.0276, offY: -0.0476, corner: { sx:  1, sy: -1 }, rot:  5 }, // sağ üst
  bas:   { offX: -0.0002, offY:  0.0769, corner: { sx: -1, sy:  1 }, rot:  7 }, // sol alt
  diger: { offX: 0.0200,  offY:  0.2251, corner: { sx:  1, sy:  1 }, rot: -6 }, // sağ alt
};

// köşelerin viewport içindeki konumu (ekran merkezinden vw/vh)
const CORNER_OFFSET_VW = 34;  // x ekseninde merkezden % uzaklık
const CORNER_OFFSET_VH = 28;  // y ekseninde merkezden % uzaklık
const TARGET_SCALE = 0.40;

// her parça için target x,y hesabını dinamik yap (resize'da yeniden hesaplanır)
// scale parametresi sayesinde farklı scale'lerde de parça doğru konumda kalır
function getTargetX(part, scale) {
  const p = PARTS[part];
  const stackSize = document.querySelector('.logo-stack').offsetWidth;
  const vw = window.innerWidth / 100;
  const s = (scale !== undefined) ? scale : TARGET_SCALE;
  return p.corner.sx * CORNER_OFFSET_VW * vw - p.offX * stackSize * s;
}
function getTargetY(part, scale) {
  const p = PARTS[part];
  const stackSize = document.querySelector('.logo-stack').offsetHeight;
  const vh = window.innerHeight / 100;
  const s = (scale !== undefined) ? scale : TARGET_SCALE;
  return p.corner.sy * CORNER_OFFSET_VH * vh - p.offY * stackSize * s;
}

// MASTER TIMELINE
const masterTl = gsap.timeline({
  scrollTrigger: {
    trigger: 'body',
    start: 'top top',
    end: 'bottom bottom',
    scrub: 1,
    invalidateOnRefresh: true,  // resize'da yeniden hesapla
    onUpdate: (self) => {
      document.getElementById('progress').style.setProperty('--p', (self.progress * 100) + '%');
    }
  }
});

// ============ FAZ 0: INTRO HOLD + LOGO GROW (0 - 0.10) ============
gsap.set('.logo-stack', { scale: 0.92 });
gsap.set('#heroStage', { y: 30 });

masterTl.to('.logo-stack', {
  scale: 1.05,
  duration: 0.10,
  ease: 'power1.out'
}, 0);

// ============ FAZ 1: INTRO OUT — Split/Order uçuyor (0.10 - 0.20) ============
masterTl.to('.intro-split', {
  x: () => -window.innerWidth * 0.7,
  opacity: 0,
  duration: 0.10,
  ease: 'power2.in'
}, 0.10);
masterTl.to('.intro-order', {
  x: () => window.innerWidth * 0.7,
  opacity: 0,
  duration: 0.10,
  ease: 'power2.in'
}, 0.10);

// ============ FAZ 2: HERO IN — Yazı belirir hızlıca (0.20 - 0.28) ============
masterTl.to('.logo-stack', {
  scale: 1.0,
  duration: 0.08,
  ease: 'power2.inOut'
}, 0.20);
masterTl.to('.orbit', {
  opacity: 0.22,
  duration: 0.08
}, 0.20);
masterTl.to('#heroStage', {
  opacity: 1,
  y: 0,
  duration: 0.08,
  ease: 'power2.out'
}, 0.22);

// ============ FAZ 3: HERO SCROLL — Yazı yavaşça yukarı kayar ve kaybolur (0.28 - 0.52) ============
// Kullanıcı scroll yaptıkça yazı doğal bir şekilde yukarı doğru süzülür.
// Sabit hold yok — sürekli hafif hareket var, sıkıcı değil.
masterTl.to('#heroStage', {
  y: -180,                       // yukarı doğru kayma mesafesi
  opacity: 0,                    // yavaşça şeffaflaşıyor
  duration: 0.24,
  ease: 'none'                   // doğrusal — scroll ile birebir
}, 0.28);

// ============ FAZ 4: NEFES + LOGO PARLA (0.50 - 0.55) ============
// Hero text neredeyse gitmişken parçalar nefes almaya başlar
masterTl.to('.orbit', {
  opacity: 0.85,
  duration: 0.05,
  ease: 'power1.out'
}, 0.50);

const breath = {
  vokal: { x: -3, y: -2, rot: -1 },
  davul: { x:  3, y: -2, rot:  1 },
  bas:   { x: -3, y:  2, rot:  1 },
  diger: { x:  3, y:  2, rot: -1 },
};
Object.entries(breath).forEach(([key, b]) => {
  masterTl.to(`.layer.${key}`, {
    x: () => b.x * window.innerWidth/100,
    y: () => b.y * window.innerHeight/100,
    rotation: b.rot,
    duration: 0.05,
    ease: 'power1.in'
  }, 0.50);
});

// ============ FAZ 5: KÖŞELERE AÇILMA — HIZLI (0.55 - 0.68) ============
Object.keys(PARTS).forEach((key) => {
  masterTl.to(`.layer.${key}`, {
    x: () => getTargetX(key),
    y: () => getTargetY(key),
    rotation: PARTS[key].rot,
    scale: TARGET_SCALE,
    duration: 0.13,
    ease: 'power2.out'      // daha keskin, çabuk yerleşir
  }, 0.55);
});

// ============ FAZ 6: ETİKETLER (0.62 - 0.72) ============
['l-vocal','l-drum','l-bass','l-other'].forEach((cls, i) => {
  masterTl.to(`.${cls}`, { opacity: 1, duration: 0.06 }, 0.62 + i * 0.022);
});

// ============ FAZ 6.5: MANİFESTO YAZISI BELİRİR (0.74) ============
// Yazı default CSS'te opacity 0; sadece logo parçalandıktan + etiketler
// belirdikten sonra fade-in olur. Bundan önce kullanıcı yazıyı görmez.
masterTl.to('.manifesto-content', { opacity: 1, duration: 0.04, ease: 'power2.out' }, 0.74);

// ============ FAZ 6.6: LOGO PARÇALARI GİZLENİR (0.76) ============
// Manifesto pin başlamadan ÖNCE layer'ları master timeline kontrolünde
// gizle. Böylece manifesto pin sırasında layer state'ine başka timeline
// dokunmaz → reverse scroll'da takılı kalma sorunu olmaz.
masterTl.to('.layer', { opacity: 0, duration: 0.04, ease: 'power2.out' }, 0.76);

// ============ FAZ 7: MANİFESTO SENKRON MORF — AYRI PIN TIMELINE ============
// Master timeline'a değil, manifesto section'ına bağlı ayrı bir ScrollTrigger.
// pin: .manifesto-content viewport'a yapışır; end: '+=300%' = section
// kendi başına 3 viewport boyu scroll'la dolaşır → 4 renk + closing için
// fazlasıyla yer var, otomatik eşit dağılım.
const morphParts = [
  { word: '.manifesto-content .accent',    part: 'vokal', color: '#e2494a' },
  { word: '.manifesto-content .accent-2',  part: 'davul', color: '#efa060' },
  { word: '.manifesto-content .accent-3',  part: 'bas',   color: '#8b6cb8' },
  { word: '.manifesto-content .accent-4',  part: 'diger', color: '#36b9ad' },
];

const manifestoTl = gsap.timeline({
  scrollTrigger: {
    trigger: '.sec.manifesto',
    start: 'top top',
    end: '+=300%',           // 3 ekran boyu pin scroll'u — renklere bol nefes
    pin: '.manifesto-content',
    pinSpacing: true,
    scrub: 1,
    invalidateOnRefresh: true,
  }
});

// manifestoTl artık .layer'a hiç dokunmaz — layer kontrolü tamamen
// masterTl'da (FAZ 6.6). İki timeline aynı target'a yazınca scrub
// gecikmesi nedeniyle reverse scroll'da state takılı kalıyordu.
morphParts.forEach(({ word, color }, i) => {
  manifestoTl.to(word, { color: color, ease: 'power2.out' }, i);
});

// Son cümle son rengin hemen ardından beyazlaşır
manifestoTl.to('.manifesto-content .closing', {
  color: '#f5f2ea',
  ease: 'power2.out'
}, morphParts.length);

// Pin release'den hemen önce yazı yumuşak fade-out + hafifçe yukarı kayar.
// Pin bittiğinde kullanıcı görünmez yazıyla doğal akışa geçer; "bir anda
// kopma" hissi kaybolur, geçiş cinsi olur.
manifestoTl.to('.manifesto-content', {
  opacity: 0,
  y: -60,
  duration: 1.2,
  ease: 'power2.in'
}, morphParts.length + 0.6);

// MOUSE PARALLAX
const layers = {
  vokal: document.querySelector('.layer.vokal'),
  davul: document.querySelector('.layer.davul'),
  bas:   document.querySelector('.layer.bas'),
  diger: document.querySelector('.layer.diger'),
};
const depth = { vokal: 24, davul: 18, bas: 14, diger: 16 };
let mx = 0, my = 0;
window.addEventListener('mousemove', (e) => {
  mx = (e.clientX / window.innerWidth - 0.5) * 2;
  my = (e.clientY / window.innerHeight - 0.5) * 2;
});
gsap.ticker.add(() => {
  Object.entries(layers).forEach(([k, el]) => {
    const d = depth[k];
    gsap.set(el, { '--px': `${mx * d}px`, '--py': `${my * d}px` });
  });
});
const s = document.createElement('style');
s.textContent = `.layer { translate: var(--px,0) var(--py,0); transition: translate 0.5s cubic-bezier(.2,.7,.2,1); }`;
document.head.appendChild(s);

// AURORA DRIFT
gsap.to('.aurora', { yPercent: -15, scrollTrigger: { trigger: 'body', start: 'top top', end: 'bottom bottom', scrub: 2 } });
gsap.to('.blob-3', { xPercent: 30, yPercent: -20, scrollTrigger: { trigger: 'body', start: 'top top', end: 'bottom bottom', scrub: 3 } });
gsap.to('.blob-4', { xPercent: -25, yPercent: 15, scrollTrigger: { trigger: 'body', start: 'top top', end: 'bottom bottom', scrub: 3 } });
