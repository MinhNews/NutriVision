// ============================================================
//  99_main.js – lắp ráp toàn bộ file Figma
// ============================================================

const SCREEN_BUILDERS = [
  s01_onboarding, s02_login, s03_register, s04_profile,
  s05_dashboard, s06_camera, s07_result, s08_portion,
  s09_diary, s10_search, s11_item, s12_stats,
  s13_settings, s14_states
];

const GRID = { x: 70, y: 130, gx: 70, gy: 110, cols: 4 };

function pageHeader(page, title, sub, accent) {
  const t = txt(title, { size: 30, weight: 'bold', color: PAL.text });
  page.appendChild(t);
  t.x = GRID.x; t.y = 44;

  const s = txt(sub, { size: 13, color: PAL.text2, w: 1100 });
  page.appendChild(s);
  s.x = GRID.x; s.y = 82;

  const line = frame('accent', { w: 46, h: 4, radius: 2, bg: accent || PAL.primary });
  page.appendChild(line);
  line.x = GRID.x; line.y = 30;
}

function buildScreenSet(page, prefix, label) {
  page.backgrounds = [{ type: 'SOLID', color: hx('#F1F5F9') }];
  pageHeader(page, label, prefix === 'W'
    ? 'Bản phác thảo khung xương (Low-Fidelity Wireframe): Tập trung cấu trúc, phân cấp thông tin và luồng thao tác. Sử dụng hộp placeholder ảnh gạch chéo (✕), nút viền phác thảo, không đổ bóng.'
    : 'Bản màu (High-Fidelity Mockup): Dùng đúng token ở trang Design System. Thể hiện giao diện hoàn thiện thực tế khi app chạy.');

  SCREEN_BUILDERS.forEach(function (fn, i) {
    const c = i % GRID.cols;
    const r = Math.floor(i / GRID.cols);
    const f = fn();
    f.name = prefix + f.name;
    page.appendChild(f);
    f.x = GRID.x + c * (IW + GRID.gx);
    f.y = GRID.y + r * (IH + GRID.gy);

    const lb = txt(f.name.replace(/^[WM]/, ''), { size: 12, weight: 'semibold', color: PAL.text2 });
    page.appendChild(lb);
    lb.x = f.x;
    lb.y = f.y - 26;
  });
}

async function buildAll() {
  // ---- font
  await figma.loadFontAsync(FONT.regular);
  await figma.loadFontAsync(FONT.medium);
  await figma.loadFontAsync(FONT.semibold);
  await figma.loadFontAsync(FONT.bold);

  if (figma.loadAllPagesAsync) { await figma.loadAllPagesAsync(); }

  // ---- style
  try { createTextStyles(); } catch (e) { console.warn('text styles', e); }
  try { createColorStyles(); } catch (e) { console.warn('color styles', e); }

  // Helper lấy trang hiện có hoặc tạo trang mới (Tối đa 3 trang để tương thích hoàn hảo gói Figma Starter miễn phí)
  function getOrCreatePage(index, name) {
    var p;
    if (figma.root.children.length > index) {
      p = figma.root.children[index];
    } else {
      try {
        p = figma.createPage();
      } catch (e) {
        p = figma.root.children[figma.root.children.length - 1];
      }
    }
    p.name = name;
    p.children.slice().forEach(function (c) { try { c.remove(); } catch (err) {} });
    return p;
  }

  // ---- 1. MOCKUP (14 màn hình màu - quan trọng nhất đưa lên đầu)
  WIRE = false;
  const mk = getOrCreatePage(0, '01 · Mockup');
  mk.backgrounds = [{ type: 'SOLID', color: hx('#F1F5F9') }];
  buildScreenSet(mk, 'M', 'Mockup · 14 màn hình');

  // ---- 2. WIREFRAME (14 màn hình phác thảo xám)
  WIRE = true;
  const wf = getOrCreatePage(1, '02 · Wireframe');
  wf.backgrounds = [{ type: 'SOLID', color: hx('#F1F5F9') }];
  buildScreenSet(wf, 'W', 'Wireframe · 14 màn hình');

  // ---- 3. DESIGN SYSTEM & SCREEN FLOW (Gộp chung vào trang 3 để không vượt quá 3 trang miễn phí)
  WIRE = false;
  const ds = getOrCreatePage(2, '03 · Design System & Flow');
  ds.backgrounds = [{ type: 'SOLID', color: hx('#FFFFFF') }];
  buildDesignSystem(ds);

  const flRoot = buildFlow(ds);
  if (flRoot) {
    flRoot.x = 1380;
    flRoot.y = 0;
  }

  figma.currentPage = mk;
  try {
    figma.viewport.scrollAndZoomIntoView(mk.children.filter(function (n) { return n.type === 'FRAME'; }).slice(0, 4));
  } catch (e) { /* bỏ qua */ }

  return { pages: figma.root.children.length, screens: SCREEN_BUILDERS.length };
}

buildAll()
  .then(function (r) {
    figma.notify('NutriVision: đã tạo ' + (r.screens * 2) + ' frame trên ' + r.pages + ' trang.', { timeout: 6000 });
    figma.closePlugin();
  })
  .catch(function (e) {
    console.error(e);
    figma.notify('Lỗi: ' + e.message, { error: true, timeout: 12000 });
  });
