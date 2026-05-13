/* global React, ReactDOM, AppScreen */
// Splitnorder studio root — login sonrası ana ekran.
//
// NOT: app.jsx zaten `const { useState, useEffect, useRef } = React;` ile
// React hook'larını global scope'a destructure ediyor. Babel-standalone tüm
// `<script type="text/babel" src="...">` etiketlerini AYNI scope'a inject
// ettiği için burada aynı destructuring'i TEKRAR yaparsak
// "Identifier 'useState' has already been declared" hatası alırız.
// O yüzden bu dosyada React.* prefix'i ile çağırıyoruz.

function useAppScale(active) {
  const ref = React.useRef(null);
  React.useLayoutEffect(() => {
    if (!active) return;
    const apply = () => {
      const el = ref.current;
      if (!el) return;
      const w = el.clientWidth || window.innerWidth;
      const h = el.clientHeight || window.innerHeight;
      const s = Math.min(w / 1440, h / 900);
      el.style.setProperty("--app-scale", s.toFixed(4));
    };
    apply();
    requestAnimationFrame(apply);
    window.addEventListener("resize", apply);
    return () => window.removeEventListener("resize", apply);
  }, [active]);
  return ref;
}

const TWEAK_DEFAULTS = {
  trackName: "Midnight Bloom",
  artist: "Otherwave"
};

function StudioRoot() {
  const fitRef = useAppScale(true);
  return (
    <div className="app-fit" ref={fitRef}>
      <div className="app-stage">
        <AppScreen tweaks={TWEAK_DEFAULTS} />
      </div>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("splitnorder-studio-root")).render(<StudioRoot />);
