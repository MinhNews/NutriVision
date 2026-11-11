// AUTO-GENERATED from src/*.js - run build.ps1 to rebuild

// ===== 01_core.js =====
// ============================================================
//  NUTRIVISION – TUẦN 2 · FIGMA BUILDER
//  01_core.js – token, helper vẽ, icon, component nền
// ============================================================

const PAL = {
  primary:      '#10B981',
  primaryDeep:  '#059669',
  primarySoft:  '#D1FAE5',
  primaryMist:  '#ECFDF5',
  secondary:    '#F59E0B',
  secondarySoft:'#FEF3C7',
  accent:       '#6366F1',
  accentSoft:   '#E0E7FF',
  protein:      '#3B82F6',
  carbs:        '#F59E0B',
  fat:          '#EF4444',
  fiber:        '#10B981',
  sodium:       '#8B5CF6',
  bg:           '#F8FAFC',
  surface:      '#FFFFFF',
  border:       '#E2E8F0',
  borderSoft:   '#EEF2F7',
  text:         '#0F172A',
  text2:        '#64748B',
  text3:        '#94A3B8',
  danger:       '#EF4444',
  dangerSoft:   '#FEE2E2',
  ink:          '#0B1220'
};

const FONT = {
  regular:  { family: 'Inter', style: 'Regular' },
  medium:   { family: 'Inter', style: 'Medium' },
  semibold: { family: 'Inter', style: 'Semi Bold' },
  bold:     { family: 'Inter', style: 'Bold' }
};

const IW = 390;      // width màn hình
const IH = 844;      // height màn hình
const PAD = 16;

let WIRE = false;    // true = wireframe (xám)

// ---------------------------------------------------------------- màu
function hx(hex) {
  const s = hex.replace('#', '');
  return {
    r: parseInt(s.substr(0, 2), 16) / 255,
    g: parseInt(s.substr(2, 2), 16) / 255,
    b: parseInt(s.substr(4, 2), 16) / 255
  };
}

function toGrey(hex) {
  const c = hx(hex);
  const l = 0.299 * c.r + 0.587 * c.g + 0.114 * c.b;
  if (l > 0.94) return { r: 1, g: 1, b: 1 };
  if (l < 0.12) return { r: 0.24, g: 0.24, b: 0.24 };
  const v = 0.18 + l * 0.74;
  return { r: v, g: v, b: v };
}

// đổi màu hex sang xám khi vẽ wireframe (dùng cho SVG icon & vòng)
function wc(hex) {
  if (!WIRE) return hex;
  const c = toGrey(hex);
  const h = function (v) {
    const s = Math.round(v * 255).toString(16);
    return s.length < 2 ? '0' + s : s;
  };
  return '#' + h(c.r) + h(c.g) + h(c.b);
}

function solid(hex, opacity) {
  const c = WIRE ? toGrey(hex) : hx(hex);
  return { type: 'SOLID', color: { r: c.r, g: c.g, b: c.b }, opacity: opacity === undefined ? 1 : opacity };
}

// ---------------------------------------------------------------- chữ
function txt(str, o) {
  o = o || {};
  const t = figma.createText();
  t.fontName = FONT[o.weight || 'regular'];
  t.characters = str;
  t.fontSize = o.size || 14;
  t.lineHeight = { unit: 'PIXELS', value: o.lh || Math.round((o.size || 14) * 1.4) };
  t.fills = [solid(o.color || PAL.text, o.opacity)];
  if (o.spacing) t.letterSpacing = { unit: 'PIXELS', value: o.spacing };
  if (o.upper) t.textCase = 'UPPER';
  if (o.align) t.textAlignHorizontal = o.align;

  if (o.w) {
    t.textAutoResize = 'HEIGHT';
    t.resize(o.w, 8);
  } else {
    t.textAutoResize = 'WIDTH_AND_HEIGHT';
  }
  if (o.align) t.textAlignHorizontal = o.align;
  t.name = o.name || ('text/' + String(str).slice(0, 28));
  return t;
}

// ---------------------------------------------------------------- khung
function frame(name, o) {
  o = o || {};
  const f = figma.createFrame();
  f.name = name;
  f.fills = o.bg ? [solid(o.bg)] : [];
  f.clipsContent = o.clip !== false;
  if (o.radius) f.cornerRadius = o.radius;

  if (o.dir) {
    f.layoutMode = o.dir;
    f.itemSpacing = o.gap || 0;
    const px = o.px === undefined ? (o.pad || 0) : o.px;
    const py = o.py === undefined ? (o.pad || 0) : o.py;
    f.paddingLeft = px; f.paddingRight = px;
    f.paddingTop = py;  f.paddingBottom = py;
    f.primaryAxisAlignItems = o.justify || 'MIN';
    f.counterAxisAlignItems = o.cross || 'MIN';

    // thứ tự quan trọng: FIXED trước khi resize, AUTO sau
    const horiz = o.dir === 'HORIZONTAL';
    f.primaryAxisSizingMode = 'FIXED';
    f.counterAxisSizingMode = 'FIXED';
    f.resize(o.w || 120, o.h || 24);
    const primAuto = horiz ? !o.w : (o.hug || !o.h);
    const crossAuto = horiz ? !o.h : !o.w;
    f.primaryAxisSizingMode = primAuto ? 'AUTO' : 'FIXED';
    f.counterAxisSizingMode = crossAuto ? 'AUTO' : 'FIXED';
    if (o.h) f.resize(o.w || f.width, o.h);
  } else if (o.w) {
    f.resize(o.w, o.h || 10);
  }

  if (WIRE) {
    // WIREFRAME: Phẳng, không bóng đổ! Nếu có border hoặc là card thì vẽ nét phác thảo rõ ràng
    if (o.border) {
      f.strokes = [solid('#94A3B8')];
      f.strokeWeight = o.bw || 1.5;
      f.strokeAlign = 'INSIDE';
    } else if (o.shadow || (o.bg && (o.bg === PAL.surface || o.bg === '#FFFFFF'))) {
      f.strokes = [solid('#CBD5E1')];
      f.strokeWeight = 1.5;
      f.strokeAlign = 'INSIDE';
    }
  } else {
    if (o.border) {
      f.strokes = [solid(o.border)];
      f.strokeWeight = o.bw || 1;
      f.strokeAlign = 'INSIDE';
    }
    if (o.shadow) {
      f.effects = [{
        type: 'DROP_SHADOW',
        color: { r: 0.06, g: 0.09, b: 0.16, a: o.shadow === true ? 0.07 : o.shadow },
        offset: { x: 0, y: 6 }, radius: 16, spread: 0,
        visible: true, blendMode: 'NORMAL'
      }];
    }
  }
  return f;
}

function place(parent, node, x, y) {
  parent.appendChild(node);
  node.x = x; node.y = y;
  return node;
}

// con trỏ xếp dọc
function colOf(parent, x, y) {
  return {
    parent: parent, x: x, y: y,
    add: function (node, gap) {
      place(parent, node, x, this.y);
      this.y += node.height + (gap || 0);
      return node;
    },
    skip: function (h) { this.y += h; }
  };
}

// ---------------------------------------------------------------- icon (Lucide, nét 2px)
const ICONS = {
  home:      '<path d="M3 10.4 12 3l9 7.4"/><path d="M5.4 9.2V21h13.2V9.2"/>',
  calendar:  '<rect x="3" y="4.5" width="18" height="16.5" rx="2.5"/><path d="M8 2.5v4M16 2.5v4M3 9.5h18"/>',
  camera:    '<path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h3l1.8-3h6.4L17 7h3a2 2 0 0 1 2 2z"/><circle cx="12" cy="13.5" r="3.6"/>',
  chart:     '<path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/>',
  user:      '<circle cx="12" cy="8" r="4"/><path d="M4.5 21c0-3.8 3.4-6 7.5-6s7.5 2.2 7.5 6"/>',
  search:    '<circle cx="11" cy="11" r="7"/><path d="M20 20l-3.6-3.6"/>',
  back:      '<path d="M15 18l-6-6 6-6"/>',
  next:      '<path d="M9 6l6 6-6 6"/>',
  plus:      '<path d="M12 5v14M5 12h14"/>',
  minus:     '<path d="M5 12h14"/>',
  check:     '<path d="M20 6.5 9.2 17.3 4 12.1"/>',
  close:     '<path d="M18 6 6 18M6 6l12 12"/>',
  bell:      '<path d="M18 8.5a6 6 0 0 0-12 0c0 6.5-2.5 8.5-2.5 8.5h17S18 15 18 8.5"/><path d="M13.8 20.5a2.2 2.2 0 0 1-3.6 0"/>',
  settings:  '<circle cx="12" cy="12" r="3"/><path d="M12 2.5v3M12 18.5v3M21.5 12h-3M5.5 12h-3M18.7 5.3l-2.1 2.1M7.4 16.6l-2.1 2.1M18.7 18.7l-2.1-2.1M7.4 7.4 5.3 5.3"/>',
  clock:     '<circle cx="12" cy="12" r="9"/><path d="M12 7v5.4l3.4 2"/>',
  edit:      '<path d="M12 20h9"/><path d="M16.6 3.6a2.1 2.1 0 0 1 3 3L7.4 18.8 3 20l1.2-4.4z"/>',
  trash:     '<path d="M3.5 6.5h17M9 6.5V4h6v2.5M6 6.5 7 20h10l1-13.5"/>',
  image:     '<rect x="3" y="4" width="18" height="16" rx="2.5"/><circle cx="8.6" cy="9.6" r="1.6"/><path d="M21 16.5 15.5 11 6 20"/>',
  sun:       '<circle cx="12" cy="12" r="4"/><path d="M12 2.5v2M12 19.5v2M2.5 12h2M19.5 12h2M5.6 5.6l1.4 1.4M17 17l1.4 1.4M18.4 5.6 17 7M7 17l-1.4 1.4"/>',
  moon:      '<path d="M21 13.4A9 9 0 1 1 10.6 3a7 7 0 0 0 10.4 10.4z"/>',
  flame:     '<path d="M12 2.8s5 4.2 5 8.2a5 5 0 0 1-10 0c0-2 1-3.4 2-4.6"/>',
  bowl:      '<path d="M3 11h18a9 9 0 0 1-18 0z"/><path d="M8 3.2v3M12 2.4v3.8M16 3.2v3"/>',
  leaf:      '<path d="M20 4C11 4 5 8.6 5 14a6 6 0 0 0 6 6c5.4 0 9-6 9-16z"/><path d="M4 21 12 13"/>',
  target:    '<circle cx="12" cy="12" r="8.5"/><circle cx="12" cy="12" r="4.5"/><circle cx="12" cy="12" r="1"/>',
  lock:      '<rect x="4.5" y="10.5" width="15" height="10.5" rx="2.5"/><path d="M8 10.5V7a4 4 0 0 1 8 0v3.5"/>',
  mail:      '<rect x="2.8" y="4.8" width="18.4" height="14.4" rx="2.5"/><path d="M3.5 6.5 12 13l8.5-6.5"/>',
  chevDown:  '<path d="M6 9l6 6 6-6"/>',
  arrowRight:'<path d="M4 12h15M13.5 6.5 20 12l-6.5 5.5"/>',
  more:      '<circle cx="5" cy="12" r="1.4"/><circle cx="12" cy="12" r="1.4"/><circle cx="19" cy="12" r="1.4"/>',
  scale:     '<path d="M4 20V9M10 20V4M16 20v-8M22 20H2"/>',
  sparkle:   '<path d="M12 3l1.9 4.6L18.5 9.5 13.9 11.4 12 16l-1.9-4.6L5.5 9.5l4.6-1.9z"/>',
  water:     '<path d="M12 3s6 6.4 6 10.4a6 6 0 0 1-12 0C6 9.4 12 3 12 3z"/>'
};

function icon(name, size, color, weight) {
  const s = size || 24;
  const svg = '<svg xmlns="http://www.w3.org/2000/svg" width="' + s + '" height="' + s +
    '" viewBox="0 0 24 24" fill="none" stroke="' + wc(color || PAL.text) +
    '" stroke-width="' + (weight || 1.9) + '" stroke-linecap="round" stroke-linejoin="round">' +
    ICONS[name] + '</svg>';
  const n = figma.createNodeFromSvg(svg);
  n.name = 'icon/' + name;
  return n;
}

// icon nằm trong khung tròn màu
function iconCircle(name, size, bg, fg, iconSize) {
  if (WIRE) {
    const f = frame('icon-circle/' + name, { w: size, h: size, radius: size / 2, bg: '#FFFFFF', border: '#94A3B8', bw: 1.5 });
    f.clipsContent = true;
    f.layoutMode = 'HORIZONTAL';
    f.primaryAxisAlignItems = 'CENTER';
    f.counterAxisAlignItems = 'CENTER';
    const ic = icon(name, iconSize || Math.round(size * 0.5), '#475569');
    f.appendChild(ic);
    ic.x = (size - ic.width) / 2;
    ic.y = (size - ic.height) / 2;
    return f;
  }
  const f = frame('icon-circle/' + name, { w: size, h: size, radius: size / 2, bg: bg });
  f.clipsContent = true;
  f.layoutMode = 'HORIZONTAL';
  f.primaryAxisAlignItems = 'CENTER';
  f.counterAxisAlignItems = 'CENTER';
  const ic = icon(name, iconSize || Math.round(size * 0.5), fg);
  f.appendChild(ic);
  ic.x = (size - ic.width) / 2;
  ic.y = (size - ic.height) / 2;
  return f;
}

// ---------------------------------------------------------------- ảnh placeholder
function photo(w, h, o) {
  o = o || {};
  const f = frame(o.name || 'photo', { w: w, h: h, radius: o.radius === undefined ? 12 : (WIRE ? Math.min(o.radius, 8) : o.radius) });
  if (WIRE) {
    // WIREFRAME IMAGE PLACEHOLDER: Hộp có 2 đường gạch chéo X + Tag tên khung
    f.fills = [solid('#F8FAFC')];
    f.strokes = [solid('#94A3B8')];
    f.strokeWeight = 1.5;

    const svg = '<svg width="' + w + '" height="' + h + '" viewBox="0 0 ' + w + ' ' + h + '" fill="none" xmlns="http://www.w3.org/2000/svg">' +
      '<line x1="0" y1="0" x2="' + w + '" y2="' + h + '" stroke="#CBD5E1" stroke-width="1.5"/>' +
      '<line x1="' + w + '" y1="0" x2="0" y2="' + h + '" stroke="#CBD5E1" stroke-width="1.5"/>' +
      '</svg>';
    try {
      const cross = figma.createNodeFromSvg(svg);
      cross.name = 'wire/cross';
      f.appendChild(cross);
      cross.x = 0; cross.y = 0;
    } catch (e) {}

    const capText = o.caption ? ('[ ' + o.caption.toUpperCase() + ' ]') : '[ ẢNH MINH HỌA ]';
    const tag = frame('wire/tag', { dir: 'HORIZONTAL', hug: true, bg: '#FFFFFF', border: '#94A3B8', bw: 1, pad: 5, radius: 4 });
    tag.counterAxisAlignItems = 'CENTER';
    tag.itemSpacing = 5;
    tag.appendChild(icon(o.icon || 'bowl', 14, '#64748B', 1.8));
    tag.appendChild(txt(capText, { size: 10, weight: 'semibold', color: '#64748B' }));
    f.appendChild(tag);
    tag.x = Math.max(0, (w - tag.width) / 2);
    tag.y = Math.max(0, (h - tag.height) / 2);
    return f;
  }
  f.fills = [{
    type: 'GRADIENT_LINEAR',
    gradientTransform: [[1, 0, 0], [0, 1, 0]],
    gradientStops: [
      { position: 0, color: { r: 0.87, g: 0.98, b: 0.94, a: 1 } },
      { position: 0.55, color: { r: 0.72, g: 0.94, b: 0.85, a: 1 } },
      { position: 1, color: { r: 0.55, g: 0.87, b: 0.76, a: 1 } }
    ]
  }];
  const ic = icon(o.icon || 'bowl', Math.round(Math.min(w, h) * 0.34), '#0E9F6E');
  f.appendChild(ic);
  ic.x = (w - ic.width) / 2;
  ic.y = (h - ic.height) / 2 - (o.caption ? 8 : 0);

  if (o.caption) {
    const cap = txt(o.caption, { size: 10, color: '#0B7A57', weight: 'medium', align: 'CENTER', w: w });
    f.appendChild(cap);
    cap.x = 0; cap.y = (h / 2) + 22;
  }
  return f;
}

// ---------------------------------------------------------------- vòng calo
function ring(size, pct, trackColor, fillColor, strokeW) {
  if (WIRE) {
    const r = (size - 6) / 2;
    const cx = size / 2;
    const svg = '<svg xmlns="http://www.w3.org/2000/svg" width="' + size + '" height="' + size + '" viewBox="0 0 ' + size + ' ' + size + '">' +
      '<circle cx="' + cx + '" cy="' + cx + '" r="' + r + '" fill="none" stroke="#94A3B8" stroke-width="2" stroke-dasharray="6 4"/>' +
      '<circle cx="' + cx + '" cy="' + cx + '" r="' + (r - 12) + '" fill="none" stroke="#CBD5E1" stroke-width="1"/>' +
      '</svg>';
    const n = figma.createNodeFromSvg(svg);
    n.name = 'ring/wireframe';
    return n;
  }
  const r = (size - (strokeW || 14)) / 2;
  const c = 2 * Math.PI * r;
  const dash = Math.max(0.001, Math.min(1, pct)) * c;
  const cx = size / 2;
  const svg = '<svg xmlns="http://www.w3.org/2000/svg" width="' + size + '" height="' + size +
    '" viewBox="0 0 ' + size + ' ' + size + '">' +
    '<circle cx="' + cx + '" cy="' + cx + '" r="' + r + '" fill="none" stroke="' + trackColor +
    '" stroke-width="' + (strokeW || 14) + '"/>' +
    '<circle cx="' + cx + '" cy="' + cx + '" r="' + r + '" fill="none" stroke="' + fillColor +
    '" stroke-width="' + (strokeW || 14) + '" stroke-linecap="round" stroke-dasharray="' + dash + ' ' + c +
    '" transform="rotate(-90 ' + cx + ' ' + cx + ')"/>' +
    '</svg>';
  const n = figma.createNodeFromSvg(svg);
  n.name = 'ring/' + Math.round(pct * 100) + '%';
  return n;
}

// ---------------------------------------------------------------- thanh tiến độ
function bar(w, pct, color, o) {
  o = o || {};
  const h = o.h || 8;
  const t = frame('bar/track', { w: w, h: h, radius: h / 2, bg: o.track || PAL.borderSoft });
  const v = frame('bar/value', { w: Math.max(h, Math.round(w * Math.max(0.02, Math.min(1, pct)))), h: h, radius: h / 2, bg: color });
  place(t, v, 0, 0);
  t.clipsContent = true;
  return t;
}

// ---------------------------------------------------------------- bottom navigation
function bottomNav(active) {
  if (WIRE) {
    const nav = frame('bottom-nav', { w: IW, h: 84, bg: '#FFFFFF', border: '#94A3B8', bw: 1.5 });
    place(nav, frame('nav/divider', { w: IW, h: 1, bg: '#CBD5E1' }), 0, 0);

    const items = [
      { id: 'home', label: 'Trang chủ', x: 47 },
      { id: 'calendar', label: 'Nhật ký', x: 131 },
      { id: 'chart', label: 'Thống kê', x: 259 },
      { id: 'user', label: 'Cá nhân', x: 343 }
    ];
    items.forEach(function (it) {
      const on = it.id === active;
      const c = on ? '#0F172A' : '#94A3B8';
      const ic = icon(it.id, 24, c, on ? 2.4 : 1.8);
      place(nav, ic, it.x - 12, 18);
      const lb = txt(it.label, { size: 10, weight: on ? 'bold' : 'medium', color: c, align: 'CENTER', w: 64 });
      place(nav, lb, it.x - 32, 46);
      if (on) {
        const ind = frame('ind', { w: 20, h: 3, radius: 1.5, bg: '#0F172A' });
        place(nav, ind, it.x - 10, 64);
      }
    });

    // nút camera nổi trong wireframe: Khung tròn viền 2.5px
    const fab = frame('nav/camera', { w: 56, h: 56, radius: 28, bg: '#FFFFFF', border: '#0F172A', bw: 2.5 });
    place(nav, fab, IW / 2 - 28, 6);
    const cam = icon('camera', 26, '#0F172A', 2.2);
    place(fab, cam, 15, 15);

    place(nav, txt('[ Quét món ]', { size: 9, weight: 'bold', color: '#0F172A', align: 'CENTER', w: 70 }), IW / 2 - 35, 66);
    return nav;
  }
  const nav = frame('bottom-nav', { w: IW, h: 84, bg: PAL.surface });
  place(nav, frame('nav/divider', { w: IW, h: 1, bg: PAL.border }), 0, 0);
  nav.effects = [{
    type: 'DROP_SHADOW', color: { r: 0.06, g: 0.09, b: 0.16, a: 0.06 },
    offset: { x: 0, y: -4 }, radius: 14, spread: 0, visible: true, blendMode: 'NORMAL'
  }];

  const items = [
    { id: 'home', label: 'Trang chủ', x: 47 },
    { id: 'calendar', label: 'Nhật ký', x: 131 },
    { id: 'chart', label: 'Thống kê', x: 259 },
    { id: 'user', label: 'Cá nhân', x: 343 }
  ];
  items.forEach(function (it) {
    const on = it.id === active;
    const c = on ? PAL.primary : PAL.text3;
    const ic = icon(it.id, 24, c, on ? 2.2 : 1.8);
    place(nav, ic, it.x - 12, 20);
    const lb = txt(it.label, { size: 10, weight: on ? 'semibold' : 'medium', color: c, align: 'CENTER', w: 64 });
    place(nav, lb, it.x - 32, 48);
  });

  // nút camera nổi
  const fab = frame('nav/camera', { w: 56, h: 56, radius: 28, bg: PAL.primary, shadow: 0.18 });
  place(nav, fab, IW / 2 - 28, 6);
  const cam = icon('camera', 26, '#FFFFFF', 2);
  place(fab, cam, 15, 15);

  place(nav, txt('Chụp món', { size: 9, weight: 'semibold', color: PAL.text3, align: 'CENTER', w: 60 }), IW / 2 - 30, 66);
  return nav;
}

// ---------------------------------------------------------------- header màn hình
function appBar(title, o) {
  o = o || {};
  const h = 60;
  const b = frame('app-bar', { w: IW, h: h, bg: o.bg || PAL.surface });
  if (o.back) {
    const ic = icon('back', 24, o.icon || PAL.text, 2.1);
    place(b, ic, 16, 18);
  }
  place(b, txt(title, { size: 17, weight: 'semibold', color: o.color || PAL.text, w: 240, align: o.back ? 'CENTER' : 'LEFT' }), o.back ? 75 : 16, 19);
  if (o.rightIcon) {
    const r = icon(o.rightIcon, 24, PAL.text, 2);
    place(b, r, IW - 40, 18);
  }
  place(b, frame('app-bar/line', { w: IW, h: 1, bg: PAL.borderSoft }), 0, h - 1);
  return b;
}

// ---------------------------------------------------------------- nhãn nhỏ
function pill(label, o) {
  o = o || {};
  const p = frame('pill/' + label, { dir: 'HORIZONTAL', gap: 4, pad: 8, py: 4, radius: 99, bg: o.bg || PAL.primarySoft, hug: true });
  p.paddingLeft = 10; p.paddingRight = 10;
  const t = txt(label, { size: o.size || 11, weight: 'semibold', color: o.color || PAL.primaryDeep });
  p.appendChild(t);
  return p;
}

// ---------------------------------------------------------------- section title
function sectionTitle(str, o) {
  o = o || {};
  const f = frame('section/' + str, { dir: 'HORIZONTAL', w: o.w || 358, hug: true });
  f.counterAxisAlignItems = 'CENTER';
  const t = txt(str, { size: 15, weight: 'semibold', color: PAL.text });
  f.appendChild(t);
  if (o.link) {
    const l = txt(o.link, { size: 12, weight: 'medium', color: PAL.primary });
    f.appendChild(l);
    f.primaryAxisAlignItems = 'SPACE_BETWEEN';
    t.layoutGrow = 0;
    l.layoutAlign = 'INHERIT';
  }
  return f;
}

// ---------------------------------------------------------------- avatar
function avatar(letter, size, bg) {
  const a = frame('avatar', { w: size, h: size, radius: size / 2, bg: bg || PAL.primarySoft });
  const t = txt(letter, { size: Math.round(size * 0.4), weight: 'bold', color: PAL.primaryDeep, align: 'CENTER', w: size });
  a.appendChild(t);
  t.y = size / 2 - 10;
  return a;
}

// ---------------------------------------------------------------- snackbar
function snackbar(str, o) {
  o = o || {};
  const s = frame('snackbar', { w: 358, h: 48, radius: 12, bg: o.bg || PAL.ink });
  const ic = icon(o.icon || 'check', 18, o.iconColor || '#34D399', 2.2);
  place(s, ic, 16, 15);
  place(s, txt(str, { size: 12, weight: 'medium', color: '#FFFFFF', w: 250 }), 44, 15);
  place(s, txt(o.action || 'Hoàn tác', { size: 12, weight: 'semibold', color: o.actionColor || '#FBBF24' }), 286, 15);
  return s;
}


// ===== 02_atoms.js =====
// ============================================================
//  02_atoms.js – nút, ô nhập, chip, card, hàng dinh dưỡng
// ============================================================

function btn(label, o) {
  o = o || {};
  const w = o.w || 358;
  const h = o.h || 52;
  const kind = o.kind || 'primary';

  if (WIRE) {
    const isPrimary = kind === 'primary';
    const b = frame('btn/' + label, {
      dir: 'HORIZONTAL',
      w: w,
      h: h,
      radius: o.radius === undefined ? 8 : Math.min(o.radius, 10),
      bg: isPrimary ? '#F8FAFC' : '#FFFFFF',
      border: isPrimary ? '#0F172A' : '#94A3B8',
      bw: isPrimary ? 2 : 1.5
    });
    b.primaryAxisAlignItems = 'CENTER';
    b.counterAxisAlignItems = 'CENTER';
    b.itemSpacing = 8;
    if (o.icon) {
      b.appendChild(icon(o.icon, 18, isPrimary ? '#0F172A' : '#64748B', 2));
    }
    b.appendChild(txt(label, {
      size: o.size || 14,
      weight: 'bold',
      color: isPrimary ? '#0F172A' : '#334155'
    }));
    return b;
  }

  const map = {
    primary: { bg: PAL.primary, fg: '#FFFFFF', bd: null },
    soft:    { bg: PAL.primarySoft, fg: PAL.primaryDeep, bd: null },
    ghost:   { bg: PAL.surface, fg: PAL.text, bd: PAL.border },
    danger:  { bg: PAL.dangerSoft, fg: PAL.danger, bd: null },
    dark:    { bg: PAL.ink, fg: '#FFFFFF', bd: null }
  };
  const k = map[kind];

  const b = frame('btn/' + label, { dir: 'HORIZONTAL', w: w, h: h, radius: o.radius === undefined ? 14 : o.radius, bg: k.bg, border: k.bd });
  b.primaryAxisAlignItems = 'CENTER';
  b.counterAxisAlignItems = 'CENTER';
  b.itemSpacing = 8;
  if (kind === 'primary') b.effects = [{
    type: 'DROP_SHADOW', color: { r: 0.06, g: 0.71, b: 0.51, a: 0.25 },
    offset: { x: 0, y: 6 }, radius: 14, spread: 0, visible: true, blendMode: 'NORMAL'
  }];
  if (o.icon) {
    const ic = icon(o.icon, 18, k.fg, 2.1);
    b.appendChild(ic);
  }
  b.appendChild(txt(label, { size: o.size || 14, weight: 'semibold', color: k.fg }));
  return b;
}

function input(label, value, o) {
  o = o || {};
  const w = o.w || 358;
  const wrap = frame('input/' + label, { dir: 'VERTICAL', w: w, gap: 7, hug: true });
  if (label) wrap.appendChild(txt(label, { size: 12, weight: 'medium', color: WIRE ? '#334155' : PAL.text2 }));

  const f = frame('field', {
    dir: 'HORIZONTAL',
    w: w,
    h: 52,
    radius: WIRE ? 8 : 12,
    bg: '#FFFFFF',
    border: WIRE ? '#94A3B8' : (o.focus ? PAL.primary : PAL.border),
    bw: WIRE ? 1.5 : (o.focus ? 1.5 : 1)
  });
  f.paddingLeft = 14; f.paddingRight = 14;
  f.counterAxisAlignItems = 'CENTER';
  f.itemSpacing = 10;
  if (o.icon) f.appendChild(icon(o.icon, 18, WIRE ? '#64748B' : (o.focus ? PAL.primary : PAL.text3), 1.9));
  f.appendChild(txt(value || (WIRE ? ('[ Nhập ' + (label || 'thông tin') + ' ]') : ''), {
    size: 14,
    weight: value ? 'medium' : 'regular',
    color: value ? (WIRE ? '#0F172A' : PAL.text) : (WIRE ? '#94A3B8' : PAL.text3)
  }));
  if (o.trailing) f.appendChild(icon(o.trailing, 18, WIRE ? '#64748B' : PAL.text3, 1.9));
  if (o.error) {
    f.strokes = [solid(WIRE ? '#334155' : PAL.danger)];
    f.strokeWeight = 1.5;
    wrap.appendChild(txt(o.error, { size: 11, weight: 'medium', color: WIRE ? '#334155' : PAL.danger }));
  }
  wrap.appendChild(f);
  return wrap;
}

function chip(label, o) {
  o = o || {};
  const on = !!o.active;
  if (WIRE) {
    const c = frame('chip/' + label, {
      dir: 'HORIZONTAL', hug: true, radius: 8, gap: 6,
      bg: on ? '#F1F5F9' : '#FFFFFF',
      border: on ? '#0F172A' : '#CBD5E1',
      bw: on ? 1.5 : 1
    });
    c.paddingLeft = 12; c.paddingRight = 12;
    c.paddingTop = 6; c.paddingBottom = 6;
    c.counterAxisAlignItems = 'CENTER';
    if (o.icon) c.appendChild(icon(o.icon, 14, on ? '#0F172A' : '#64748B', 2));
    c.appendChild(txt(label, { size: 12, weight: on ? 'bold' : 'medium', color: on ? '#0F172A' : '#64748B' }));
    return c;
  }
  const c = frame('chip/' + label, {
    dir: 'HORIZONTAL', hug: true, radius: 99, gap: 6,
    bg: on ? PAL.primary : PAL.surface,
    border: on ? null : PAL.border
  });
  c.paddingLeft = 14; c.paddingRight = 14;
  c.paddingTop = 8; c.paddingBottom = 8;
  c.counterAxisAlignItems = 'CENTER';
  if (o.icon) c.appendChild(icon(o.icon, 14, on ? '#FFFFFF' : PAL.text2, 2));
  c.appendChild(txt(label, { size: 12, weight: on ? 'semibold' : 'medium', color: on ? '#FFFFFF' : PAL.text2 }));
  return c;
}

function segmented(items, activeIndex, o) {
  o = o || {};
  const w = o.w || 358;
  if (WIRE) {
    const s = frame('segmented', { dir: 'HORIZONTAL', w: w, h: 42, radius: 8, bg: '#FFFFFF', border: '#94A3B8', bw: 1.5, pad: 3, gap: 4 });
    const iw = Math.floor((w - 6 - (items.length - 1) * 4) / items.length);
    items.forEach(function (it, i) {
      const on = i === activeIndex;
      const seg = frame('seg/' + it, { dir: 'HORIZONTAL', w: iw, h: 34, radius: 6, bg: on ? '#F1F5F9' : '#FFFFFF', border: on ? '#0F172A' : null, bw: on ? 1.5 : 0 });
      seg.primaryAxisAlignItems = 'CENTER';
      seg.counterAxisAlignItems = 'CENTER';
      seg.appendChild(txt(it, { size: 12, weight: on ? 'bold' : 'medium', color: on ? '#0F172A' : '#64748B' }));
      s.appendChild(seg);
    });
    return s;
  }
  const s = frame('segmented', { dir: 'HORIZONTAL', w: w, h: 44, radius: 12, bg: o.bg || '#EDF2F7', pad: 4, gap: 4 });
  const iw = Math.floor((w - 8 - (items.length - 1) * 4) / items.length);
  items.forEach(function (it, i) {
    const on = i === activeIndex;
    const seg = frame('seg/' + it, { dir: 'HORIZONTAL', w: iw, h: 36, radius: 9, bg: on ? PAL.surface : null });
    seg.primaryAxisAlignItems = 'CENTER';
    seg.counterAxisAlignItems = 'CENTER';
    if (on) seg.effects = [{
      type: 'DROP_SHADOW', color: { r: 0.06, g: 0.09, b: 0.16, a: 0.10 },
      offset: { x: 0, y: 2 }, radius: 6, spread: 0, visible: true, blendMode: 'NORMAL'
    }];
    seg.appendChild(txt(it, { size: 12, weight: on ? 'semibold' : 'medium', color: on ? PAL.text : PAL.text2 }));
    s.appendChild(seg);
  });
  return s;
}

function divider(w) {
  return frame('divider', { w: w || 358, h: 1, bg: PAL.borderSoft });
}

// hàng chỉ số dinh dưỡng (nhãn · giá trị · thanh)
function macroRow(label, valueText, pct, color, o) {
  o = o || {};
  const w = o.w || 326;
  const r = frame('macro/' + label, { dir: 'VERTICAL', w: w, gap: 5, hug: true });
  const head = frame('head', { dir: 'HORIZONTAL', w: w, hug: true });
  head.primaryAxisAlignItems = 'SPACE_BETWEEN';
  head.counterAxisAlignItems = 'CENTER';
  const left = frame('l', { dir: 'HORIZONTAL', hug: true, gap: 6 });
  left.counterAxisAlignItems = 'CENTER';
  const dot = frame('dot', { w: 8, h: 8, radius: 4, bg: color });
  left.appendChild(dot);
  left.appendChild(txt(label, { size: 12, weight: 'medium', color: PAL.text2 }));
  head.appendChild(left);
  head.appendChild(txt(valueText, { size: 12, weight: 'semibold', color: PAL.text }));
  r.appendChild(head);
  r.appendChild(bar(w, pct, color, { h: 7 }));
  return r;
}

// hàng bữa ăn gọn (dashboard)
function mealRow(name, iconName, kcal, filled, tint, iconColor) {
  const r = frame('meal-row/' + name, { dir: 'HORIZONTAL', w: 358, h: 58, radius: 14, bg: PAL.surface, border: PAL.borderSoft, pad: 12 });
  r.counterAxisAlignItems = 'CENTER';
  r.itemSpacing = 10;
  r.appendChild(iconCircle(iconName, 32, tint || PAL.primaryMist, iconColor || PAL.primary, 17));
  const c = frame('t', { dir: 'VERTICAL', gap: 1, hug: true });
  c.appendChild(txt(name, { size: 13, weight: 'semibold', color: PAL.text }));
  c.appendChild(txt(filled ? kcal : 'Chưa ghi món nào', { size: 11, color: filled ? PAL.text2 : PAL.text3 }));
  r.appendChild(c);
  c.layoutGrow = 1;
  if (filled) {
    r.appendChild(txt(kcal, { size: 12, weight: 'semibold', color: PAL.text }));
  } else {
    r.appendChild(icon('plus', 18, PAL.primary, 2.4));
  }
  return r;
}

// thẻ bữa ăn
function mealCard(name, iconName, total, items, o) {
  o = o || {};
  const w = o.w || 358;
  const c = frame('meal/' + name, { dir: 'VERTICAL', w: w, gap: 12, pad: 14, radius: 16, bg: PAL.surface, border: PAL.borderSoft, hug: true });

  const head = frame('head', { dir: 'HORIZONTAL', w: w - 28, hug: true });
  head.counterAxisAlignItems = 'CENTER';
  head.itemSpacing = 10;
  head.appendChild(iconCircle(iconName, 34, o.tint || PAL.primaryMist, o.iconColor || PAL.primary, 18));
  const col = frame('titles', { dir: 'VERTICAL', gap: 2, hug: true });
  col.appendChild(txt(name, { size: 14, weight: 'semibold', color: PAL.text }));
  col.appendChild(txt(total || 'Chưa có món ăn', { size: 11.5, color: PAL.text2 }));
  head.appendChild(col);
  c.appendChild(head);

  if (items && items.length) {
    items.forEach(function (it) {
      const row = frame('item', { dir: 'HORIZONTAL', w: w - 28, h: 30, hug: false });
      row.counterAxisAlignItems = 'CENTER';
      row.primaryAxisAlignItems = 'SPACE_BETWEEN';
      row.appendChild(txt(it[0], { size: 12.5, color: PAL.text, w: 220 }));
      row.appendChild(txt(it[1], { size: 12.5, weight: 'semibold', color: PAL.text2 }));
      c.appendChild(row);
    });
  } else {
    const add = frame('add', { dir: 'HORIZONTAL', w: w - 28, h: 38, radius: 10, bg: PAL.primaryMist, border: PAL.primarySoft });
    add.primaryAxisAlignItems = 'CENTER';
    add.counterAxisAlignItems = 'CENTER';
    add.itemSpacing = 6;
    add.appendChild(icon('plus', 15, PAL.primaryDeep, 2.4));
    add.appendChild(txt('Thêm món', { size: 12, weight: 'semibold', color: PAL.primaryDeep }));
    c.appendChild(add);
  }
  return c;
}

// hàng kết quả tìm kiếm / món ăn
function dishRow(name, sub, kcal, o) {
  o = o || {};
  const w = 358;
  const r = frame('dish/' + name, { dir: 'HORIZONTAL', w: w, h: 62, gap: 12, hug: false });
  r.counterAxisAlignItems = 'CENTER';
  r.appendChild(photo(o.size || 46, o.size || 46, { radius: 12, icon: o.icon || 'bowl' }));
  const c = frame('t', { dir: 'VERTICAL', gap: 3, hug: true });
  c.appendChild(txt(name, { size: 13.5, weight: 'semibold', color: PAL.text }));
  c.appendChild(txt(sub, { size: 11.5, color: PAL.text2 }));
  r.appendChild(c);
  const right = frame('r', { dir: 'VERTICAL', w: 44, gap: 2, hug: true });
  right.appendChild(txt(kcal, { size: 13, weight: 'bold', color: PAL.primaryDeep, align: 'RIGHT', w: 44 }));
  right.appendChild(txt('kcal', { size: 9.5, color: PAL.text3, align: 'RIGHT', w: 44 }));
  r.appendChild(right);
  r.primaryAxisAlignItems = 'SPACE_BETWEEN';
  return r;
}

// dòng chỉ số trong bảng dinh dưỡng
function nutrientRow(label, value, color) {
  const r = frame('nutrient/' + label, { dir: 'HORIZONTAL', w: 326, h: 26, hug: false });
  r.counterAxisAlignItems = 'CENTER';
  r.primaryAxisAlignItems = 'SPACE_BETWEEN';
  const l = frame('l', { dir: 'HORIZONTAL', gap: 8, hug: true });
  l.counterAxisAlignItems = 'CENTER';
  l.appendChild(frame('dot', { w: 7, h: 7, radius: 4, bg: color }));
  l.appendChild(txt(label, { size: 12, color: PAL.text2 }));
  r.appendChild(l);
  r.appendChild(txt(value, { size: 12, weight: 'semibold', color: PAL.text }));
  return r;
}

// ô số liệu lớn (2x2)
function statTile(label, value, unit, color) {
  const w = 157;
  const t = frame('stat/' + label, { dir: 'VERTICAL', w: w, gap: 2, pad: 12, radius: 12, bg: PAL.bg, hug: true });
  const v = frame('v', { dir: 'HORIZONTAL', gap: 3, hug: true });
  v.counterAxisAlignItems = 'MAX';
  v.appendChild(txt(value, { size: 18, weight: 'bold', color: color || PAL.text }));
  v.appendChild(txt(unit, { size: 10, weight: 'medium', color: PAL.text3 }));
  t.appendChild(v);
  t.appendChild(txt(label, { size: 10.5, weight: 'medium', color: PAL.text2 }));
  return t;
}

// khối bọc tiêu đề + nội dung
function block(title, link) {
  const b = frame('block/' + title, { dir: 'VERTICAL', w: 358, gap: 10, hug: true });
  const h = frame('h', { dir: 'HORIZONTAL', w: 358, hug: true });
  h.counterAxisAlignItems = 'CENTER';
  h.appendChild(txt(title, { size: 14.5, weight: 'semibold', color: PAL.text }));
  if (link) {
    h.primaryAxisAlignItems = 'SPACE_BETWEEN';
    h.appendChild(txt(link, { size: 11.5, weight: 'medium', color: PAL.primary }));
  }
  b.appendChild(h);
  return b;
}


// ===== 10_screens_a.js =====
// ============================================================
//  10_screens_a.js – màn 01 → 07
// ============================================================

function mkScreen(name, bg) {
  if (WIRE) {
    const f = frame(name, { w: IW, h: IH, bg: '#FFFFFF' });
    f.name = name;
    f.strokes = [solid('#64748B')];
    f.strokeWeight = 2;
    f.cornerRadius = 24;
    return f;
  }
  const f = frame(name, { w: IW, h: IH, bg: bg || PAL.bg });
  f.name = name;
  return f;
}

// ---------------------------------------------------------------- 01 Onboarding
function s01_onboarding() {
  const s = mkScreen('01 · Onboarding', PAL.surface);

  const hero = photo(IW, 372, { radius: 0, icon: 'bowl' });
  place(s, hero, 0, 0);

  const dots = frame('dots', { dir: 'HORIZONTAL', gap: 6, hug: true });
  place(s, dots, IW / 2 - 26, 396);
  [1, 0, 0].forEach(function (on) {
    const d = frame('dot', { w: on ? 20 : 7, h: 7, radius: 4, bg: on ? PAL.primary : PAL.border });
    dots.appendChild(d);
  });

  place(s, txt('Ăn uống thông minh,\nngay từ bữa đầu tiên', {
    size: 28, weight: 'bold', color: PAL.text, lh: 36, w: 342
  }), 24, 424);

  place(s, txt('Chụp ảnh món ăn, NutriVision nhận diện ngay trên máy và cho bạn biết calo, đạm, carb, béo — không cần mạng.', {
    size: 14.5, color: PAL.text2, lh: 22, w: 330
  }), 24, 512);

  const feat = frame('features', { dir: 'VERTICAL', w: 342, gap: 10, hug: true });
  [
    ['camera', 'Nhận diện 15 món Việt bằng model trên thiết bị'],
    ['target', 'Khẩu phần theo tô, bát, cái — đúng cách ăn của người Việt'],
    ['lock', 'Dữ liệu nằm trên máy bạn, không gửi lên máy chủ']
  ].forEach(function (it) {
    const r = frame('f', { dir: 'HORIZONTAL', w: 342, gap: 10, hug: true });
    r.counterAxisAlignItems = 'CENTER';
    r.appendChild(iconCircle(it[0], 30, PAL.primaryMist, PAL.primaryDeep, 16));
    r.appendChild(txt(it[1], { size: 12.5, color: PAL.text2, lh: 18, w: 300 }));
    feat.appendChild(r);
  });
  place(s, feat, 24, 598);

  place(s, btn('Bắt đầu ngay', { w: 342 }), 24, 726);
  place(s, txt('Đã có tài khoản?  Đăng nhập', { size: 12.5, weight: 'medium', color: PAL.text2, align: 'CENTER', w: 342 }), 24, 792);
  return s;
}

// ---------------------------------------------------------------- 02 Login
function s02_login() {
  const s = mkScreen('02 · Đăng nhập', PAL.surface);
  place(s, icon('back', 24, PAL.text, 2.1), 16, 52);

  place(s, txt('Chào mừng trở lại', { size: 26, weight: 'bold', color: PAL.text, w: 342 }), 24, 110);
  place(s, txt('Đăng nhập để tiếp tục theo dõi bữa ăn của bạn.', { size: 13.5, color: PAL.text2, lh: 20, w: 320 }), 24, 148);

  place(s, input('Email', 'minh.thai@example.com', { icon: 'mail' }), 24, 208);
  place(s, input('Mật khẩu', '••••••••', { icon: 'lock', trailing: 'eye' }), 24, 300);
  place(s, txt('Quên mật khẩu?', { size: 12, weight: 'semibold', color: PAL.primary, align: 'RIGHT', w: 326 }), 24, 386);

  place(s, btn('Đăng nhập', { w: 342 }), 24, 424);

  const or = frame('or', { dir: 'HORIZONTAL', w: 342, gap: 12, hug: true });
  or.counterAxisAlignItems = 'CENTER';
  or.appendChild(divider(120));
  or.appendChild(txt('hoặc', { size: 11.5, color: PAL.text3 }));
  or.appendChild(divider(120));
  place(s, or, 24, 496);

  place(s, btn('Tạo tài khoản mới', { w: 342, kind: 'ghost' }), 24, 532);

  const note = frame('note', { dir: 'HORIZONTAL', w: 342, gap: 8, pad: 12, radius: 12, bg: PAL.primaryMist, hug: true });
  note.counterAxisAlignItems = 'CENTER';
  note.appendChild(icon('lock', 16, PAL.primaryDeep, 2));
  note.appendChild(txt('Tài khoản lưu cục bộ trên máy. Không cần internet.', { size: 11.5, color: PAL.primaryDeep, lh: 17, w: 292 }));
  place(s, note, 24, 650);
  return s;
}

// ---------------------------------------------------------------- 03 Register
function s03_register() {
  const s = mkScreen('03 · Đăng ký', PAL.surface);
  place(s, icon('back', 24, PAL.text, 2.1), 16, 52);
  place(s, txt('Tạo tài khoản', { size: 26, weight: 'bold', color: PAL.text, w: 342 }), 24, 110);
  place(s, txt('Mất 30 giây. Nhật ký bữa ăn của bạn được lưu riêng trên thiết bị.', { size: 13.5, color: PAL.text2, lh: 20, w: 322 }), 24, 148);

  place(s, input('Email', 'ban@example.com', { icon: 'mail' }), 24, 208);
  place(s, input('Mật khẩu', '••••••', { icon: 'lock', focus: true, trailing: 'eye' }), 24, 300);
  place(s, txt('Tối thiểu 6 ký tự.', { size: 11, color: PAL.text3 }), 24, 384);
  place(s, input('Nhập lại mật khẩu', '••••••', { icon: 'lock' }), 24, 410);

  const agree = frame('agree', { dir: 'HORIZONTAL', w: 342, gap: 10, hug: true });
  agree.counterAxisAlignItems = 'MIN';
  const box = frame('checkbox', { w: 18, h: 18, radius: 5, bg: PAL.primary });
  place(box, icon('check', 12, '#FFFFFF', 3), 3, 3);
  agree.appendChild(box);
  agree.appendChild(txt('Tôi đồng ý với điều khoản sử dụng và chính sách riêng tư.', { size: 11.5, color: PAL.text2, lh: 17, w: 310 }));
  place(s, agree, 24, 494);

  place(s, btn('Tạo tài khoản', { w: 342 }), 24, 560);
  place(s, txt('Đã có tài khoản?  Đăng nhập', { size: 12.5, weight: 'medium', color: PAL.primaryDeep, align: 'CENTER', w: 342 }), 24, 628);
  return s;
}

// ---------------------------------------------------------------- 04 Thiết lập hồ sơ
function s04_profile() {
  const s = mkScreen('04 · Thiết lập hồ sơ', PAL.bg);
  const top = frame('top', { dir: 'VERTICAL', w: IW, gap: 10, pad: 20, bg: PAL.surface, hug: true });
  const stepRow = frame('step', { dir: 'HORIZONTAL', w: 350, hug: true });
  stepRow.primaryAxisAlignItems = 'SPACE_BETWEEN';
  stepRow.counterAxisAlignItems = 'CENTER';
  stepRow.appendChild(txt('Bước 2 / 3', { size: 12, weight: 'semibold', color: PAL.primaryDeep }));
  stepRow.appendChild(txt('Chỉ số cơ thể', { size: 12, color: PAL.text2 }));
  top.appendChild(stepRow);
  top.appendChild(bar(350, 0.66, PAL.primary, { h: 6 }));
  place(s, top, 0, 0);

  place(s, txt('Cho chúng tôi biết về bạn', { size: 22, weight: 'bold', color: PAL.text, w: 342 }), 24, 118);
  place(s, txt('Dùng để tính TDEE và mục tiêu calo mỗi ngày.', { size: 13, color: PAL.text2, w: 330 }), 24, 152);

  const g = frame('gender', { dir: 'HORIZONTAL', w: 342, gap: 12, hug: true });
  [['Nam', true], ['Nữ', false]].forEach(function (it) {
    const c = frame('g/' + it[0], { dir: 'VERTICAL', w: 165, h: 82, radius: 14, gap: 4, bg: it[1] ? PAL.primaryMist : PAL.surface, border: it[1] ? PAL.primary : PAL.border, bw: it[1] ? 1.5 : 1 });
    c.primaryAxisAlignItems = 'CENTER';
    c.counterAxisAlignItems = 'CENTER';
    c.appendChild(icon('user', 22, it[1] ? PAL.primaryDeep : PAL.text3, 1.8));
    c.appendChild(txt(it[0], { size: 13, weight: it[1] ? 'semibold' : 'medium', color: it[1] ? PAL.primaryDeep : PAL.text2 }));
    g.appendChild(c);
  });
  place(s, g, 24, 190);

  place(s, input('Chiều cao (cm)', '170', { w: 165, icon: 'scale' }), 24, 292);
  place(s, input('Cân nặng (kg)', '65', { w: 165 }), 201, 292);

  place(s, txt('Mức vận động', { size: 12, weight: 'medium', color: PAL.text2 }), 24, 386);
  const chips = frame('chips', { dir: 'HORIZONTAL', w: 342, gap: 8, hug: true });
  chips.appendChild(chip('Ít vận động', {}));
  chips.appendChild(chip('Vừa phải', { active: true }));
  chips.appendChild(chip('Nhiều', {}));
  place(s, chips, 24, 408);

  const card = frame('tdee', { dir: 'VERTICAL', w: 342, gap: 10, pad: 16, radius: 16, bg: PAL.primaryMist, hug: true });
  card.appendChild(txt('Mục tiêu đề xuất', { size: 11.5, weight: 'semibold', color: PAL.primaryDeep, upper: true, spacing: 0.6 }));
  const big = frame('big', { dir: 'HORIZONTAL', gap: 6, hug: true });
  big.counterAxisAlignItems = 'MAX';
  big.appendChild(txt('1.940', { size: 30, weight: 'bold', color: PAL.text }));
  big.appendChild(txt('kcal / ngày', { size: 12, weight: 'medium', color: PAL.text2 }));
  card.appendChild(big);
  card.appendChild(txt('TDEE 2.430 kcal · giảm 20% để giảm cân an toàn (0,3–0,5 kg/tuần).', { size: 11.5, color: PAL.text2, lh: 17, w: 310 }));
  place(s, card, 24, 466);

  place(s, btn('Hoàn tất & vào trang chủ', { w: 342 }), 24, 690);
  place(s, txt('Có thể đổi lại bất cứ lúc nào trong Cài đặt', { size: 11, color: PAL.text3, align: 'CENTER', w: 342 }), 24, 756);
  return s;
}

// ---------------------------------------------------------------- 05 Dashboard
function s05_dashboard() {
  const s = mkScreen('05 · Dashboard', PAL.bg);

  const head = frame('header', { dir: 'HORIZONTAL', w: IW, h: 96, pad: 20 });
  head.paddingTop = 42;
  head.paddingBottom = 6;
  head.counterAxisAlignItems = 'MIN';
  const hc = frame('hc', { dir: 'VERTICAL', gap: 3, hug: true });
  hc.appendChild(txt('Chào buổi sáng, Minh 👋', { size: 12, weight: 'medium', color: PAL.text2 }));
  hc.appendChild(txt('Hôm nay, 05/11', { size: 19, weight: 'bold', color: PAL.text }));
  head.appendChild(hc);
  hc.layoutGrow = 1;
  head.appendChild(avatar('M', 38));
  place(s, head, 0, 0);

  const ringCard = frame('calo-card', { w: 358, h: 196, radius: 20, bg: PAL.surface, shadow: 0.08 });
  const r = ring(132, 0.69, PAL.borderSoft, PAL.primary, 14);
  place(ringCard, r, 113, 16);
  const center = frame('center', { dir: 'VERTICAL', w: 358, gap: 0 });
  center.primaryAxisAlignItems = 'CENTER';
  center.counterAxisAlignItems = 'CENTER';
  center.appendChild(txt('1.250', { size: 27, weight: 'bold', color: PAL.text, align: 'CENTER' }));
  center.appendChild(txt('kcal còn lại', { size: 10.5, weight: 'medium', color: PAL.text2, align: 'CENTER' }));
  place(ringCard, center, 0, 54);

  const statRow = frame('stat-row', { dir: 'HORIZONTAL', w: 330, hug: true });
  statRow.primaryAxisAlignItems = 'SPACE_BETWEEN';
  statRow.counterAxisAlignItems = 'CENTER';
  const leftS = frame('l', { dir: 'HORIZONTAL', gap: 5, hug: true });
  leftS.counterAxisAlignItems = 'CENTER';
  leftS.appendChild(frame('dot', { w: 7, h: 7, radius: 4, bg: PAL.primary }));
  leftS.appendChild(txt('Đã nạp 550 kcal', { size: 10.5, weight: 'semibold', color: PAL.primaryDeep }));
  statRow.appendChild(leftS);
  statRow.appendChild(txt('Mục tiêu 1.800 kcal', { size: 10.5, weight: 'medium', color: PAL.text2 }));
  place(ringCard, statRow, 14, 166);
  place(s, ringCard, 16, 100);

  const macro = frame('macro-card', { dir: 'VERTICAL', w: 358, gap: 6, pad: 12, radius: 16, bg: PAL.surface, hug: true });
  macro.appendChild(macroRow('Đạm', '42 / 110 g', 0.38, PAL.protein, { w: 334 }));
  macro.appendChild(macroRow('Carb', '98 / 220 g', 0.45, PAL.carbs, { w: 334 }));
  macro.appendChild(macroRow('Béo', '24 / 60 g', 0.40, PAL.fat, { w: 334 }));
  place(s, macro, 16, 306);

  const sec = frame('sec', { dir: 'HORIZONTAL', w: 358, hug: true });
  sec.primaryAxisAlignItems = 'SPACE_BETWEEN';
  sec.counterAxisAlignItems = 'CENTER';
  sec.appendChild(txt('Bữa ăn hôm nay', { size: 15, weight: 'semibold', color: PAL.text }));
  sec.appendChild(txt('Xem nhật ký', { size: 11.5, weight: 'medium', color: PAL.primary }));
  place(s, sec, 16, 442);

  const cards = frame('meals', { dir: 'VERTICAL', w: 358, gap: 8, hug: true });
  cards.appendChild(mealRow('Bữa sáng', 'sun', '340 kcal', true));
  cards.appendChild(mealRow('Bữa trưa', 'bowl', '620 kcal', true, '#FFF7E6', PAL.secondary));
  cards.appendChild(mealRow('Bữa tối', 'moon', '', false));
  cards.appendChild(mealRow('Bữa phụ', 'water', '', false));
  place(s, cards, 16, 466);

  place(s, bottomNav('home'), 0, 760);
  return s;
}

// ---------------------------------------------------------------- 06 Camera
function s06_camera() {
  if (WIRE) {
    const s = mkScreen('06 · Camera', '#FFFFFF');
    place(s, txt('[ CAMERA VIEWFINDER - MÀN HÌNH CHỤP ẢNH ]', { size: 12.5, weight: 'bold', color: '#0F172A', align: 'CENTER', w: IW }), 0, 75);

    // Khung ngắm chụp ảnh với nét phác thảo và dấu chữ thập
    const vf = frame('viewfinder', { w: 286, h: 286, radius: 16, bg: '#F8FAFC', border: '#475569', bw: 2 });
    const svgCross = '<svg width="286" height="286" viewBox="0 0 286 286" fill="none" xmlns="http://www.w3.org/2000/svg">' +
      '<line x1="0" y1="0" x2="286" y2="286" stroke="#E2E8F0" stroke-width="1.5"/>' +
      '<line x1="286" y1="0" x2="0" y2="286" stroke="#E2E8F0" stroke-width="1.5"/>' +
      '</svg>';
    try {
      const cross = figma.createNodeFromSvg(svgCross);
      vf.appendChild(cross);
    } catch (e) {}

    const vfc = frame('vfc', { dir: 'VERTICAL', hug: true, gap: 8 });
    vfc.counterAxisAlignItems = 'CENTER';
    vfc.appendChild(icon('camera', 36, '#475569', 2));
    vfc.appendChild(txt('[ KHUNG NGẮM CAMERA 286 x 286 ]', { size: 12, weight: 'bold', color: '#0F172A' }));
    vfc.appendChild(txt('Đặt đĩa thức ăn vào giữa khung hình', { size: 11, color: '#64748B' }));
    vf.appendChild(vfc);
    vfc.x = (286 - vfc.width) / 2;
    vfc.y = (286 - vfc.height) / 2;
    place(s, vf, 52, 175);

    place(s, icon('back', 24, '#0F172A', 2), 20, 48);
    place(s, btn('⚡ Flash', { w: 84, h: 36, kind: 'ghost' }), 286, 44);

    place(s, txt('AI Offline (TFLite) nhận diện trong ~100ms', { size: 11.5, weight: 'medium', color: '#64748B', align: 'CENTER', w: IW }), 0, 480);

    // Thanh điều khiển đáy
    const botBar = frame('camera-controls', { dir: 'HORIZONTAL', w: 358, h: 104, border: '#CBD5E1', bw: 1.5, radius: 16, bg: '#F8FAFC' });
    botBar.primaryAxisAlignItems = 'SPACE_AROUND';
    botBar.counterAxisAlignItems = 'CENTER';

    const libBtn = frame('lib', { dir: 'VERTICAL', gap: 4, hug: true });
    libBtn.counterAxisAlignItems = 'CENTER';
    libBtn.appendChild(iconCircle('image', 44, '#FFFFFF', '#0F172A', 20));
    libBtn.appendChild(txt('Thư viện', { size: 11, weight: 'medium', color: '#334155' }));
    botBar.appendChild(libBtn);

    const shutterBtn = frame('shutter', { w: 72, h: 72, radius: 36, bg: '#FFFFFF', border: '#0F172A', bw: 3 });
    const innerShut = frame('in', { w: 54, h: 54, radius: 27, bg: '#F1F5F9', border: '#0F172A', bw: 2 });
    place(shutterBtn, innerShut, 9, 9);
    botBar.appendChild(shutterBtn);

    const searchBtn = frame('search', { dir: 'VERTICAL', gap: 4, hug: true });
    searchBtn.counterAxisAlignItems = 'CENTER';
    searchBtn.appendChild(iconCircle('search', 44, '#FFFFFF', '#0F172A', 20));
    searchBtn.appendChild(txt('Tìm món', { size: 11, weight: 'medium', color: '#334155' }));
    botBar.appendChild(searchBtn);

    place(s, botBar, 16, 680);
    return s;
  }

  const s = mkScreen('06 · Camera', '#0B1220');
  const bg = photo(IW, IH, { radius: 0, icon: 'bowl' });
  bg.fills = [{
    type: 'GRADIENT_LINEAR',
    gradientTransform: [[0.6, 1, 0], [-0.4, 0.6, 0.2]],
    gradientStops: [
      { position: 0, color: { r: 0.10, g: 0.13, b: 0.18, a: 1 } },
      { position: 0.6, color: { r: 0.28, g: 0.33, b: 0.29, a: 1 } },
      { position: 1, color: { r: 0.12, g: 0.16, b: 0.15, a: 1 } }
    ]
  }];
  place(s, bg, 0, 0);
  bg.children.forEach(function (c) { c.visible = false; });

  const vf = frame('viewfinder', { w: 286, h: 286, radius: 24 });
  vf.strokes = [solid('#FFFFFF', 0.85)];
  vf.strokeWeight = 2;
  vf.strokeAlign = 'INSIDE';
  place(s, vf, 52, 268);

  place(s, txt('Đặt món ăn vào giữa khung hình', { size: 12.5, weight: 'medium', color: '#FFFFFF', align: 'CENTER', w: IW }), 0, 578);
  place(s, txt('Giữ máy chắc, đủ sáng để AI nhận diện chính xác', { size: 11, color: '#CBD5E1', align: 'CENTER', w: IW }), 0, 600);

  place(s, icon('back', 24, '#FFFFFF', 2.1), 20, 60);
  const fl = frame('flash', { w: 36, h: 36, radius: 18 });
  fl.fills = [solid('#FFFFFF', 0.14)];
  place(s, fl, 334, 54);
  place(s, icon('sun', 18, '#FFFFFF', 2), 343, 63);

  place(s, iconCircle('image', 48, '#FFFFFF', PAL.ink, 22), 40, 690);
  const shutter = frame('shutter', { w: 76, h: 76, radius: 38, bg: '#FFFFFF' });
  place(s, shutter, 157, 676);
  place(s, frame('shutter/inner', { w: 62, h: 62, radius: 31, bg: PAL.primary }), 164, 683);
  place(s, icon('search', 22, '#FFFFFF', 2.1), 322, 703);
  place(s, txt('Thư viện', { size: 9.5, color: '#E2E8F0', align: 'CENTER', w: 60 }), 34, 746);
  place(s, txt('Tìm món', { size: 9.5, color: '#E2E8F0', align: 'CENTER', w: 60 }), 313, 746);
  return s;
}

// ---------------------------------------------------------------- 07 Kết quả AI
function s07_result() {
  const s = mkScreen('07 · Kết quả nhận diện', PAL.bg);
  const hero = photo(IW, 320, { radius: 0, icon: 'bowl' });
  place(s, hero, 0, 0);
  place(s, iconCircle('back', 36, '#FFFFFF', PAL.ink, 20), 16, 52);
  place(s, pill('AI trên thiết bị · TFLite', { bg: PAL.accentSoft, color: '#4338CA' }), 16, 96);

  const sheet = frame('sheet', { dir: 'VERTICAL', w: IW, gap: 14, pad: 20, radius: 24, bg: PAL.bg, hug: true });
  sheet.paddingTop = 34;

  const headRow = frame('h', { dir: 'HORIZONTAL', w: 350, hug: true });
  headRow.primaryAxisAlignItems = 'SPACE_BETWEEN';
  headRow.counterAxisAlignItems = 'MIN';
  const hc = frame('hc', { dir: 'VERTICAL', gap: 3, hug: true });
  hc.appendChild(txt('Phở bò tái nạm', { size: 24, weight: 'bold', color: PAL.text }));
  hc.appendChild(txt('1 tô vừa · 500 g', { size: 12, color: PAL.text2 }));
  headRow.appendChild(hc);
  headRow.appendChild(pill('89%'));
  sheet.appendChild(headRow);

  sheet.appendChild(txt('Độ tin cậy của model', { size: 11, weight: 'medium', color: PAL.text2 }));
  sheet.appendChild(bar(350, 0.89, PAL.primary, { h: 8 }));

  sheet.appendChild(txt('Không đúng món?', { size: 12.5, weight: 'semibold', color: PAL.text }));

  const alts = frame('alts', { dir: 'VERTICAL', w: 350, gap: 8, hug: true });
  [['Phở gà', '7%'], ['Bún bò Huế', '3%']].forEach(function (a) {
    const row = frame('alt/' + a[0], { dir: 'HORIZONTAL', w: 350, h: 46, radius: 12, bg: PAL.surface, border: PAL.border });
    row.counterAxisAlignItems = 'CENTER';
    row.primaryAxisAlignItems = 'SPACE_BETWEEN';
    row.paddingLeft = 14; row.paddingRight = 14;
    row.appendChild(txt(a[0], { size: 13, weight: 'medium', color: PAL.text }));
    row.appendChild(txt(a[1], { size: 12, weight: 'semibold', color: PAL.text2 }));
    alts.appendChild(row);
  });
  sheet.appendChild(alts);

  const manual = frame('manual', { dir: 'HORIZONTAL', w: 350, h: 44, radius: 12, bg: PAL.surface, border: PAL.border });
  manual.counterAxisAlignItems = 'CENTER';
  manual.primaryAxisAlignItems = 'CENTER';
  manual.itemSpacing = 8;
  manual.appendChild(icon('search', 17, PAL.text2, 2));
  manual.appendChild(txt('Tìm món khác trong 100+ món Việt', { size: 12.5, weight: 'medium', color: PAL.text2 }));
  sheet.appendChild(manual);

  const note = frame('note', { dir: 'HORIZONTAL', w: 350, gap: 8, hug: true });
  note.counterAxisAlignItems = 'MIN';
  note.appendChild(icon('sparkle', 15, PAL.accent, 1.8));
  note.appendChild(txt('Model chỉ gợi ý tên món. Dinh dưỡng lấy từ cơ sở dữ liệu nội bộ, không lấy từ AI.', { size: 11, color: PAL.text2, lh: 16, w: 320 }));
  sheet.appendChild(note);

  place(s, sheet, 0, 296);
  place(s, btn('Tiếp tục chọn khẩu phần', { w: 342 }), 24, 730);
  return s;
}


// ===== 11_screens_b.js =====
// ============================================================
//  11_screens_b.js – màn 08 → 14
// ============================================================

function dashLine(w, color) {
  const svg = '<svg xmlns="http://www.w3.org/2000/svg" width="' + w + '" height="2" viewBox="0 0 ' + w + ' 2">' +
    '<path d="M0 1 H' + w + '" stroke="' + color + '" stroke-width="1.4" stroke-dasharray="5 4"/></svg>';
  return figma.createNodeFromSvg(svg);
}

// ---------------------------------------------------------------- 08 Chọn khẩu phần
function s08_portion() {
  const s = mkScreen('08 · Chọn khẩu phần', PAL.bg);
  place(s, appBar('Chọn khẩu phần', { back: true }), 0, 0);

  const dish = frame('dish', { dir: 'HORIZONTAL', w: 358, gap: 14, pad: 14, radius: 16, bg: PAL.surface, hug: true });
  dish.counterAxisAlignItems = 'CENTER';
  dish.appendChild(photo(64, 64, { radius: 12 }));
  const dc = frame('dc', { dir: 'VERTICAL', gap: 3, hug: true });
  dc.appendChild(txt('Phở bò tái nạm', { size: 16, weight: 'semibold', color: PAL.text }));
  dc.appendChild(txt('105 kcal / 100 g · nguồn: Viện Dinh dưỡng VN', { size: 10.5, color: PAL.text2, lh: 15, w: 240 }));
  dish.appendChild(dc);
  place(s, dish, 16, 76);

  place(s, txt('Khẩu phần thực tế', { size: 13, weight: 'semibold', color: PAL.text }), 16, 178);
  place(s, segmented(['Nửa tô', '1 tô', 'Tô lớn'], 1, { w: 358 }), 16, 202);

  const portionRow = frame('portion', { dir: 'HORIZONTAL', w: 358, gap: 10, hug: true });
  const g = frame('gram', { dir: 'HORIZONTAL', w: 174, h: 50, radius: 12, bg: PAL.surface, border: PAL.border, pad: 12 });
  g.counterAxisAlignItems = 'CENTER';
  g.primaryAxisAlignItems = 'SPACE_BETWEEN';
  g.appendChild(txt('500', { size: 16, weight: 'semibold', color: PAL.text }));
  g.appendChild(txt('gram', { size: 12, color: PAL.text2 }));
  portionRow.appendChild(g);
  const hint = frame('hint', { dir: 'HORIZONTAL', w: 174, h: 50, radius: 12, bg: PAL.primaryMist, pad: 12, gap: 8 });
  hint.counterAxisAlignItems = 'CENTER';
  hint.appendChild(icon('target', 16, PAL.primaryDeep, 2));
  hint.appendChild(txt('≈ 1 tô vừa', { size: 12, weight: 'medium', color: PAL.primaryDeep }));
  portionRow.appendChild(hint);
  place(s, portionRow, 16, 258);

  const nut = frame('nutrition', { dir: 'VERTICAL', w: 358, gap: 10, pad: 14, radius: 16, bg: PAL.surface, hug: true });
  const nh = frame('nh', { dir: 'HORIZONTAL', w: 330, hug: true });
  nh.primaryAxisAlignItems = 'SPACE_BETWEEN';
  nh.counterAxisAlignItems = 'CENTER';
  nh.appendChild(txt('Dinh dưỡng theo khẩu phần', { size: 12.5, weight: 'semibold', color: PAL.text }));
  nh.appendChild(pill('500 g'));
  nut.appendChild(nh);

  const grid = frame('grid', { dir: 'HORIZONTAL', w: 330, gap: 8, hug: true });
  grid.appendChild(statTile('Calo', '525', 'kcal', PAL.text));
  grid.appendChild(statTile('Đạm', '32', 'g', PAL.protein));
  nut.appendChild(grid);
  const grid2 = frame('grid2', { dir: 'HORIZONTAL', w: 330, gap: 8, hug: true });
  grid2.appendChild(statTile('Carb', '55', 'g', PAL.carbs));
  grid2.appendChild(statTile('Béo', '16', 'g', PAL.fat));
  nut.appendChild(grid2);

  nut.appendChild(divider(330));
  nut.appendChild(nutrientRow('Chất xơ', '3,4 g', PAL.fiber));
  nut.appendChild(nutrientRow('Natri', '1.980 mg', PAL.sodium));
  place(s, nut, 16, 324);

  const warn = frame('warn', { dir: 'HORIZONTAL', w: 358, gap: 8, pad: 12, radius: 12, bg: '#FEF2F2', border: '#FECACA', hug: true });
  warn.counterAxisAlignItems = 'CENTER';
  warn.appendChild(icon('flame', 16, PAL.danger, 2));
  warn.appendChild(txt('Natri gần chạm ngưỡng 2.000 mg/ngày của WHO.', { size: 11, color: '#B91C1C', lh: 16, w: 300 }));
  place(s, warn, 16, 636);

  place(s, btn('Lưu vào bữa trưa · 525 kcal', { w: 358 }), 16, 764);
  place(s, txt('Có thể sửa lại sau khi lưu', { size: 10.5, color: PAL.text3, align: 'CENTER', w: 358 }), 16, 822);
  return s;
}

// ---------------------------------------------------------------- 09 Nhật ký ngày
function s09_diary() {
  const s = mkScreen('09 · Nhật ký ngày', PAL.bg);
  place(s, appBar('Nhật ký', { rightIcon: 'calendar' }), 0, 0);

  const strip = frame('week', { dir: 'HORIZONTAL', w: 358, gap: 6, hug: true });
  ['T2 03', 'T3 04', 'T4 05', 'T5 06', 'T6 07', 'T7 08', 'CN 09'].forEach(function (d, i) {
    const on = i === 2;
    const c = frame('day/' + d, { dir: 'VERTICAL', w: 44, h: 58, radius: 12, bg: on ? PAL.primary : PAL.surface, border: on ? null : PAL.border });
    c.primaryAxisAlignItems = 'CENTER';
    c.counterAxisAlignItems = 'CENTER';
    c.appendChild(txt(d.split(' ')[0], { size: 10, weight: 'medium', color: on ? '#ECFDF5' : PAL.text3 }));
    c.appendChild(txt(d.split(' ')[1], { size: 13, weight: 'bold', color: on ? '#FFFFFF' : PAL.text }));
    strip.appendChild(c);
  });
  place(s, strip, 16, 76);

  const sum = frame('summary', { dir: 'VERTICAL', w: 358, gap: 8, pad: 14, radius: 16, bg: PAL.surface, hug: true });
  const sh = frame('sh', { dir: 'HORIZONTAL', w: 330, hug: true });
  sh.primaryAxisAlignItems = 'SPACE_BETWEEN';
  sh.counterAxisAlignItems = 'MIN';
  const sl = frame('sl', { dir: 'VERTICAL', gap: 2, hug: true });
  sl.appendChild(txt('1.550', { size: 22, weight: 'bold', color: PAL.text }));
  sl.appendChild(txt('trong 1.800 kcal mục tiêu', { size: 10.5, color: PAL.text2 }));
  sh.appendChild(sl);
  sh.appendChild(pill('còn 250 kcal'));
  sum.appendChild(sh);
  sum.appendChild(bar(330, 0.86, PAL.primary, { h: 8 }));
  place(s, sum, 16, 150);

  const list = frame('meals-list', { dir: 'VERTICAL', w: 358, gap: 10, hug: true });

  const m1 = frame('m1', { dir: 'VERTICAL', w: 358, gap: 8, pad: 12, radius: 14, bg: PAL.surface, hug: true });
  const m1h = frame('h', { dir: 'HORIZONTAL', w: 334, hug: true });
  m1h.counterAxisAlignItems = 'CENTER';
  m1h.itemSpacing = 8;
  m1h.appendChild(icon('sun', 16, PAL.secondary, 2));
  m1h.appendChild(txt('Bữa sáng', { size: 12.5, weight: 'semibold', color: PAL.text }));
  m1h.appendChild(txt('340 kcal', { size: 11.5, weight: 'semibold', color: PAL.text2 }));
  m1h.primaryAxisAlignItems = 'SPACE_BETWEEN';
  m1.appendChild(m1h);
  const it1 = frame('it', { dir: 'HORIZONTAL', w: 334, h: 24 });
  it1.primaryAxisAlignItems = 'SPACE_BETWEEN';
  it1.counterAxisAlignItems = 'CENTER';
  it1.appendChild(txt('Bánh mì thịt · 1 cái', { size: 12, color: PAL.text2 }));
  it1.appendChild(txt('340 kcal', { size: 12, weight: 'semibold', color: PAL.text }));
  m1.appendChild(it1);

  const m2 = frame('m2', { dir: 'VERTICAL', w: 358, gap: 8, pad: 12, radius: 14, bg: PAL.surface, hug: true });
  const m2h = frame('h', { dir: 'HORIZONTAL', w: 334, hug: true });
  m2h.counterAxisAlignItems = 'CENTER';
  m2h.itemSpacing = 8;
  m2h.appendChild(icon('bowl', 16, PAL.primary, 2));
  m2h.appendChild(txt('Bữa trưa', { size: 12.5, weight: 'semibold', color: PAL.text }));
  m2h.appendChild(txt('525 kcal', { size: 11.5, weight: 'semibold', color: PAL.text2 }));
  m2h.primaryAxisAlignItems = 'SPACE_BETWEEN';
  m2.appendChild(m2h);
  [['Phở bò tái · 1 tô vừa', '525 kcal'], ['Trà đá · 1 ly', '0 kcal']].forEach(function (x) {
    const r = frame('it', { dir: 'HORIZONTAL', w: 334, h: 24 });
    r.primaryAxisAlignItems = 'SPACE_BETWEEN';
    r.counterAxisAlignItems = 'CENTER';
    r.appendChild(txt(x[0], { size: 12, color: PAL.text2 }));
    r.appendChild(txt(x[1], { size: 12, weight: 'semibold', color: PAL.text }));
    m2.appendChild(r);
  });

  const m3 = frame('m3', { dir: 'HORIZONTAL', w: 358, h: 48, radius: 14, bg: PAL.surface, border: PAL.borderSoft, pad: 12 });
  m3.counterAxisAlignItems = 'CENTER';
  m3.itemSpacing = 8;
  m3.appendChild(icon('moon', 16, PAL.text2, 2));
  m3.appendChild(txt('Bữa tối', { size: 12.5, weight: 'semibold', color: PAL.text2 }));
  m3.appendChild(txt('+ Thêm món', { size: 12, weight: 'semibold', color: PAL.primary }));
  m3.primaryAxisAlignItems = 'SPACE_BETWEEN';

  const m4 = frame('m4', { dir: 'HORIZONTAL', w: 358, h: 48, radius: 14, bg: PAL.surface, border: PAL.borderSoft, pad: 12 });
  m4.counterAxisAlignItems = 'CENTER';
  m4.itemSpacing = 8;
  m4.appendChild(icon('water', 16, PAL.text2, 2));
  m4.appendChild(txt('Bữa phụ', { size: 12.5, weight: 'semibold', color: PAL.text2 }));
  m4.appendChild(txt('+ Thêm món', { size: 12, weight: 'semibold', color: PAL.primary }));
  m4.primaryAxisAlignItems = 'SPACE_BETWEEN';

  list.appendChild(m1);
  list.appendChild(m2);
  list.appendChild(m3);
  list.appendChild(m4);
  place(s, list, 16, 262);

  const fab = frame('fab', { w: 56, h: 56, radius: 28, bg: PAL.primary, shadow: 0.25 });
  place(s, fab, 318, 686);
  place(fab, icon('plus', 26, '#FFFFFF', 2.4), 15, 15);

  place(s, bottomNav('calendar'), 0, 760);
  return s;
}

// ---------------------------------------------------------------- 10 Tìm kiếm
function s10_search() {
  const s = mkScreen('10 · Tìm kiếm món', PAL.bg);
  place(s, appBar('Tìm món', { back: true, rightIcon: 'more' }), 0, 0);

  const sf = frame('search-field', { dir: 'HORIZONTAL', w: 358, h: 48, radius: 12, bg: PAL.surface, border: PAL.border, pad: 12 });
  sf.counterAxisAlignItems = 'CENTER';
  sf.itemSpacing = 8;
  sf.appendChild(icon('search', 18, PAL.text3, 2));
  sf.appendChild(txt('phở bò', { size: 14, weight: 'medium', color: PAL.text }));
  sf.appendChild(icon('close', 15, PAL.text3, 2));
  sf.primaryAxisAlignItems = 'SPACE_BETWEEN';
  place(s, sf, 16, 72);

  const chips = frame('filters', { dir: 'HORIZONTAL', w: 358, gap: 8, hug: true });
  ['Tất cả', 'Món nước', 'Cơm', 'Bún – phở'].forEach(function (c, i) {
    chips.appendChild(chip(c, { active: i === 3 }));
  });
  place(s, chips, 16, 136);

  place(s, txt('8 kết quả cho “phở bò”', { size: 11.5, color: PAL.text2 }), 16, 194);

  const rows = frame('results', { dir: 'VERTICAL', w: 358, gap: 4, hug: true });
  [
    ['Phở bò tái nạm', 'Món nước · 1 tô 500 g', '525'],
    ['Phở bò chín', 'Món nước · 1 tô 500 g', '480'],
    ['Phở bò gầu', 'Món nước · 1 tô 500 g', '560'],
    ['Phở bò viên', 'Món nước · 1 tô 500 g', '495'],
    ['Bún bò Huế', 'Món nước · 1 tô 500 g', '610'],
    ['Cơm tấm sườn', 'Cơm · 1 đĩa 400 g', '720']
  ].forEach(function (x) {
    rows.appendChild(dishRow(x[0], x[1], x[2], {}));
  });
  place(s, rows, 16, 216);
  return s;
}

// ---------------------------------------------------------------- 11 Chi tiết mục nhật ký
function s11_item() {
  const s = mkScreen('11 · Chi tiết mục nhật ký', PAL.bg);
  const hero = photo(IW, 220, { radius: 0 });
  place(s, hero, 0, 0);
  place(s, iconCircle('back', 36, '#FFFFFF', PAL.ink, 20), 16, 52);
  place(s, iconCircle('edit', 36, '#FFFFFF', PAL.ink, 18), 338, 52);

  const sheet = frame('sheet', { dir: 'VERTICAL', w: IW, gap: 14, pad: 20, radius: 24, bg: PAL.bg, hug: true });
  sheet.paddingTop = 34;
  const h = frame('h', { dir: 'HORIZONTAL', w: 350, hug: true });
  h.primaryAxisAlignItems = 'SPACE_BETWEEN';
  h.counterAxisAlignItems = 'MIN';
  const hc = frame('hc', { dir: 'VERTICAL', gap: 3, hug: true });
  hc.appendChild(txt('Phở bò tái nạm', { size: 22, weight: 'bold', color: PAL.text }));
  hc.appendChild(txt('Bữa trưa · 05/11 · 12:20', { size: 11.5, color: PAL.text2 }));
  h.appendChild(hc);
  h.appendChild(pill('1 tô vừa'));
  sheet.appendChild(h);

  const cal = frame('cal', { dir: 'HORIZONTAL', w: 350, h: 74, radius: 14, bg: PAL.primaryMist, pad: 14 });
  cal.counterAxisAlignItems = 'CENTER';
  const cc = frame('cc', { dir: 'VERTICAL', gap: 1, hug: true });
  cc.appendChild(txt('525', { size: 26, weight: 'bold', color: PAL.text }));
  cc.appendChild(txt('kcal cho khẩu phần này', { size: 10.5, color: PAL.text2 }));
  cal.appendChild(cc);
  cal.primaryAxisAlignItems = 'SPACE_BETWEEN';
  cal.appendChild(txt('22% nhu cầu ngày', { size: 11, weight: 'semibold', color: PAL.primaryDeep }));
  sheet.appendChild(cal);

  const table = frame('table', { dir: 'VERTICAL', w: 350, gap: 8, pad: 14, radius: 14, bg: PAL.surface, hug: true });
  table.appendChild(nutrientRow('Đạm', '32 g', PAL.protein));
  table.appendChild(nutrientRow('Carb', '55 g', PAL.carbs));
  table.appendChild(nutrientRow('Béo', '16 g', PAL.fat));
  table.appendChild(nutrientRow('Chất xơ', '3,4 g', PAL.fiber));
  table.appendChild(nutrientRow('Natri', '1.980 mg', PAL.sodium));
  sheet.appendChild(table);

  place(s, sheet, 0, 196);

  place(s, btn('Sửa khẩu phần', { w: 350, kind: 'ghost', icon: 'edit' }), 20, 660);
  place(s, btn('Xóa khỏi nhật ký', { w: 350, kind: 'danger', icon: 'trash' }), 20, 724);
  return s;
}

// ---------------------------------------------------------------- 12 Thống kê
function s12_stats() {
  const s = mkScreen('12 · Thống kê', PAL.bg);
  place(s, appBar('Thống kê', { rightIcon: 'more' }), 0, 0);
  place(s, segmented(['7 ngày', '30 ngày'], 0, { w: 358 }), 16, 72);

  const avg = frame('avg', { dir: 'VERTICAL', w: 358, gap: 8, pad: 14, radius: 16, bg: PAL.surface, hug: true });
  const ah = frame('ah', { dir: 'HORIZONTAL', w: 330, hug: true });
  ah.primaryAxisAlignItems = 'SPACE_BETWEEN';
  ah.counterAxisAlignItems = 'MIN';
  const ac = frame('ac', { dir: 'VERTICAL', gap: 2, hug: true });
  ac.appendChild(txt('1.640', { size: 26, weight: 'bold', color: PAL.text }));
  ac.appendChild(txt('kcal trung bình / ngày', { size: 10.5, color: PAL.text2 }));
  ah.appendChild(ac);
  ah.appendChild(pill('−160 kcal'));
  avg.appendChild(ah);
  avg.appendChild(txt('6/7 ngày ghi chép đầy đủ · chuỗi 12 ngày', { size: 11, color: PAL.text2 }));
  place(s, avg, 16, 132);

  const chart = frame('chart', { dir: 'VERTICAL', w: 358, gap: 10, pad: 14, radius: 16, bg: PAL.surface, hug: true });
  const ch = frame('ch', { dir: 'HORIZONTAL', w: 330, hug: true });
  ch.primaryAxisAlignItems = 'SPACE_BETWEEN';
  ch.counterAxisAlignItems = 'CENTER';
  ch.appendChild(txt('Calo theo ngày', { size: 12.5, weight: 'semibold', color: PAL.text }));
  ch.appendChild(txt('mục tiêu 1.800', { size: 10, color: PAL.text3 }));
  chart.appendChild(ch);

  const plot = frame('plot', { w: 330, h: 132 });
  const tl = dashLine(330, WIRE ? '#94A3B8' : '#CBD5E1');
  place(plot, tl, 0, 20);
  const data = [0.42, 0.62, 0.88, 0.55, 0.95, 0.78, 0.7];
  const bw = 30;
  data.forEach(function (v, i) {
    const hgt = Math.round(104 * v);
    const over = v > 0.92;
    const b = frame('bar/' + i, {
      w: bw,
      h: hgt,
      radius: WIRE ? 4 : 6,
      bg: WIRE ? '#F8FAFC' : (over ? PAL.secondary : PAL.primary),
      border: WIRE ? '#475569' : null,
      bw: WIRE ? 1.5 : 0
    });
    place(plot, b, 6 + i * 45, 120 - hgt);
  });
  const days = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];
  days.forEach(function (d, i) {
    place(plot, txt(d, { size: 9.5, color: PAL.text3, align: 'CENTER', w: 30 }), 6 + i * 45, 126);
  });
  chart.appendChild(plot);
  place(s, chart, 16, 250);

  const donut = frame('donut', { dir: 'HORIZONTAL', w: 358, gap: 16, pad: 14, radius: 16, bg: PAL.surface, hug: true });
  donut.counterAxisAlignItems = 'CENTER';
  donut.appendChild(ring(104, 0.62, PAL.borderSoft, PAL.protein, 14));
  const dl = frame('legend', { dir: 'VERTICAL', gap: 9, hug: true });
  [['Đạm 30%', PAL.protein], ['Carb 45%', PAL.carbs], ['Béo 25%', PAL.fat]].forEach(function (x) {
    const r = frame('l', { dir: 'HORIZONTAL', gap: 7, hug: true });
    r.counterAxisAlignItems = 'CENTER';
    r.appendChild(frame('dot', { w: 8, h: 8, radius: 4, bg: x[1] }));
    r.appendChild(txt(x[0], { size: 11.5, weight: 'medium', color: PAL.text2 }));
    dl.appendChild(r);
  });
  donut.appendChild(dl);
  place(s, donut, 16, 470);

  const note = frame('note', { dir: 'HORIZONTAL', w: 358, gap: 8, pad: 12, radius: 12, bg: PAL.primaryMist, hug: true });
  note.counterAxisAlignItems = 'MIN';
  note.appendChild(icon('leaf', 16, PAL.primaryDeep, 1.8));
  note.appendChild(txt('Đạm đang thấp hơn mục tiêu 8 g/ngày. Bữa tối có thể thêm 1 quả trứng hoặc 100 g ức gà.', { size: 11, color: PAL.primaryDeep, lh: 16, w: 296 }));
  place(s, note, 16, 620);

  place(s, bottomNav('chart'), 0, 760);
  return s;
}

// ---------------------------------------------------------------- 13 Cá nhân & cài đặt
function s13_settings() {
  const s = mkScreen('13 · Cá nhân & cài đặt', PAL.surface);

  const prof = frame('profile', { dir: 'HORIZONTAL', w: 358, gap: 12, pad: 14, radius: 16, bg: PAL.surface, border: PAL.borderSoft, hug: true });
  prof.counterAxisAlignItems = 'CENTER';
  prof.appendChild(avatar('M', 46));
  const pc = frame('pc', { dir: 'VERTICAL', gap: 2, hug: true });
  pc.appendChild(txt('Nguyễn Phú Minh Thái', { size: 15, weight: 'semibold', color: PAL.text }));
  pc.appendChild(txt('minh.thai@example.com', { size: 11.5, color: PAL.text2 }));
  prof.appendChild(pc);
  pc.layoutGrow = 1;
  prof.appendChild(icon('next', 18, PAL.text3, 2));
  place(s, prof, 16, 60);

  const goal = frame('goal', { dir: 'HORIZONTAL', w: 358, h: 76, radius: 16, bg: PAL.primaryMist, pad: 14 });
  goal.counterAxisAlignItems = 'CENTER';
  const gc = frame('gc', { dir: 'VERTICAL', gap: 1, hug: true });
  gc.appendChild(txt('Mục tiêu 1.800 kcal', { size: 14, weight: 'semibold', color: PAL.text }));
  gc.appendChild(txt('TDEE 2.250 · giảm cân 20% · 65 kg', { size: 10.5, color: PAL.text2 }));
  goal.appendChild(gc);
  gc.layoutGrow = 1;
  goal.appendChild(pill('Đổi mục tiêu', { bg: PAL.surface, color: PAL.primaryDeep }));
  place(s, goal, 16, 148);

  function groupTitle(str) {
    return txt(str, { size: 11, weight: 'semibold', color: PAL.text3, upper: true, spacing: 0.7, w: 358 });
  }

  function rowItem(ic, label, right, danger) {
    const r = frame('row/' + label, { dir: 'HORIZONTAL', w: 358, h: 46, bg: PAL.surface });
    r.counterAxisAlignItems = 'CENTER';
    r.itemSpacing = 12;
    r.paddingLeft = 2;
    r.appendChild(icon(ic, 19, danger ? PAL.danger : PAL.text2, 1.9));
    const lab = txt(label, { size: 13.5, color: danger ? PAL.danger : PAL.text });
    r.appendChild(lab);
    lab.layoutGrow = 1;
    if (right === 'switch') {
      const sw = frame('switch', { w: 42, h: 24, radius: 12, bg: PAL.primary });
      place(sw, frame('knob', { w: 20, h: 20, radius: 10, bg: '#FFFFFF' }), 20, 2);
      r.appendChild(sw);
    } else if (right === 'switch-off') {
      const sw = frame('switch', { w: 42, h: 24, radius: 12, bg: '#D7E0EA' });
      place(sw, frame('knob', { w: 20, h: 20, radius: 10, bg: '#FFFFFF' }), 2, 2);
      r.appendChild(sw);
    } else if (right) {
      r.appendChild(txt(right, { size: 12, color: PAL.text3 }));
    }
    return r;
  }

  const c = colOf(s, 16, 240);
  c.add(groupTitle('Tài khoản & mục tiêu'), 6);
  const g1 = frame('g1', { dir: 'VERTICAL', w: 358, gap: 0, hug: true });
  g1.appendChild(rowItem('user', 'Hồ sơ & chỉ số cơ thể', '170 cm · 65 kg'));
  g1.appendChild(divider(358));
  g1.appendChild(rowItem('target', 'Mục tiêu calo & macro', '1.800 kcal'));
  c.add(g1, 14);

  c.add(groupTitle('Nhắc nhở & giao diện'), 6);
  const g2 = frame('g2', { dir: 'VERTICAL', w: 358, gap: 0, hug: true });
  g2.appendChild(rowItem('bell', 'Nhắc ghi bữa ăn', 'switch'));
  g2.appendChild(divider(358));
  g2.appendChild(rowItem('clock', 'Giờ nhắc', '07:30 · 12:00 · 19:00'));
  g2.appendChild(divider(358));
  g2.appendChild(rowItem('moon', 'Chế độ tối', 'switch-off'));
  c.add(g2, 14);

  c.add(groupTitle('Dữ liệu'), 6);
  const g3 = frame('g3', { dir: 'VERTICAL', w: 358, gap: 0, hug: true });
  g3.appendChild(rowItem('image', 'Xuất nhật ký (CSV)', ''));
  g3.appendChild(divider(358));
  g3.appendChild(rowItem('trash', 'Xóa toàn bộ dữ liệu', '', true));
  c.add(g3, 14);

  c.add(btn('Đăng xuất', { w: 358, h: 48, kind: 'ghost' }), 10);
  c.add(txt('NutriVision 1.0 · dữ liệu lưu trên thiết bị', { size: 10, color: PAL.text3, align: 'CENTER', w: 358 }), 0);

  place(s, bottomNav('user'), 0, 760);
  return s;
}

// ---------------------------------------------------------------- 14 Trạng thái
function s14_states() {
  const s = mkScreen('14 · Trạng thái rỗng & lỗi', PAL.bg);
  place(s, txt('Các trạng thái cần xử lý', { size: 20, weight: 'bold', color: PAL.text }), 20, 56);

  function stateCard(title, desc, ic, tint, iconColor, action) {
    const c = frame('state/' + title, { dir: 'VERTICAL', w: 350, gap: 10, pad: 14, radius: 14, bg: PAL.surface, border: PAL.borderSoft, hug: true });
    const top = frame('t', { dir: 'HORIZONTAL', w: 322, gap: 10, hug: true });
    top.counterAxisAlignItems = 'CENTER';
    top.appendChild(iconCircle(ic, 34, tint, iconColor, 18));
    const tc = frame('tc', { dir: 'VERTICAL', gap: 2, hug: true });
    tc.appendChild(txt(title, { size: 13, weight: 'semibold', color: PAL.text }));
    tc.appendChild(txt(desc, { size: 10.5, color: PAL.text2, lh: 15, w: 250 }));
    top.appendChild(tc);
    c.appendChild(top);
    if (action) {
      const a = frame('action', { dir: 'HORIZONTAL', w: 322, h: 34, radius: 10, bg: PAL.primaryMist });
      a.primaryAxisAlignItems = 'CENTER';
      a.counterAxisAlignItems = 'CENTER';
      a.appendChild(txt(action, { size: 11.5, weight: 'semibold', color: PAL.primaryDeep }));
      c.appendChild(a);
    }
    return c;
  }

  const cursor = colOf(s, 16, 100);
  cursor.add(stateCard('AI không chắc chắn', 'Độ tin cậy dưới 60%: hiện top-3 và nút tìm món thủ công thay vì đoán bừa.', 'sparkle', PAL.accentSoft, PAL.accent, 'Tìm món thủ công'), 10);
  cursor.add(stateCard('Nhật ký trống', 'Ngày mới chưa có món nào. Gợi ý chụp bữa sáng để bắt đầu chuỗi ngày.', 'calendar', PAL.primaryMist, PAL.primary, 'Chụp bữa sáng'), 10);
  cursor.add(stateCard('Không tìm thấy món', 'Không có kết quả cho “bánh xèo miền Tây”. Thử từ khoá ngắn hơn.', 'search', '#F1F5F9', PAL.text2, 'Xóa bộ lọc'), 10);
  cursor.add(stateCard('Lưu thành công', 'Snackbar xác nhận, kèm nút hoàn tác trong 5 giây.', 'check', PAL.primaryMist, PAL.primaryDeep, null), 16);

  cursor.add(snackbar('Đã thêm vào bữa trưa', { action: 'Hoàn tác' }), 10);

  const err = frame('err', { dir: 'HORIZONTAL', w: 358, gap: 8, pad: 12, radius: 12, bg: '#FEF2F2', border: '#FECACA', hug: true });
  err.counterAxisAlignItems = 'CENTER';
  err.appendChild(icon('close', 15, PAL.danger, 2.2));
  err.appendChild(txt('Không đọc được model. Kiểm tra lại tệp dinh dưỡng và thử lại.', { size: 11, color: '#B91C1C', lh: 16, w: 300 }));
  cursor.add(err, 0);
  return s;
}


// ===== 20_designsystem.js =====
// ============================================================
//  20_designsystem.js – trang Design System
// ============================================================

function createTextStyles() {
  const defs = [
    ['Display/32', 32, 'bold', 40],
    ['Headline/24', 24, 'bold', 32],
    ['Title/18', 18, 'semibold', 26],
    ['Title/16', 16, 'semibold', 24],
    ['Body/14', 14, 'regular', 22],
    ['Label/13', 13, 'semibold', 18],
    ['Caption/11', 11, 'medium', 16]
  ];
  const out = {};
  defs.forEach(function (d) {
    const st = figma.createTextStyle();
    st.name = d[0];
    st.fontName = FONT[d[2]];
    st.fontSize = d[1];
    st.lineHeight = { unit: 'PIXELS', value: d[3] };
    out[d[0]] = st;
  });
  return out;
}

function createColorStyles() {
  const out = {};
  Object.keys(PAL).forEach(function (k) {
    const st = figma.createPaintStyle();
    st.name = 'Nutri/' + k;
    st.paints = [solid(PAL[k])];
    out[k] = st;
  });
  return out;
}

function dsSection(ctrl, y, num, title, sub) {
  const head = frame('sec/' + title, { dir: 'HORIZONTAL', w: 1180, hug: true, gap: 10 });
  head.counterAxisAlignItems = 'CENTER';
  head.appendChild(txt(num, { size: 12, weight: 'bold', color: PAL.primary, w: 26 }));
  head.appendChild(txt(title, { size: 20, weight: 'bold', color: PAL.text }));
  ctrl.add(head, 4);
  if (sub) ctrl.add(txt(sub, { size: 12, color: PAL.text2, w: 900 }), 18);
  else ctrl.skip(14);
}

function swatch(hex, name, role, size) {
  const w = size || 168;
  const c = frame('color/' + name, { dir: 'VERTICAL', w: w, gap: 6, hug: true });
  const sw = frame('chip', { w: w, h: 88, radius: 12, bg: hex, border: PAL.border });
  c.appendChild(sw);
  c.appendChild(txt(name, { size: 12, weight: 'semibold', color: PAL.text }));
  c.appendChild(txt(hex.toUpperCase(), { size: 10.5, weight: 'medium', color: PAL.text2 }));
  if (role) c.appendChild(txt(role, { size: 10, color: PAL.text3, lh: 14, w: w }));
  return c;
}

function buildDesignSystem(page) {
  const root = frame('NutriVision · Design System', { w: 1280, h: 1640, bg: PAL.surface });
  page.appendChild(root);

  // header
  root.appendChild(frame('band', { w: 1280, h: 8, bg: PAL.primary }));
  const head = frame('head', { dir: 'VERTICAL', w: 1180, gap: 8, hug: true });
  head.appendChild(txt('NUTRIVISION · DESIGN SYSTEM', { size: 11, weight: 'semibold', color: PAL.primaryDeep, spacing: 1.4 }));
  head.appendChild(txt('Tuần 2 — thống nhất màu, chữ, icon trước khi vẽ màn hình', { size: 30, weight: 'bold', color: PAL.text, w: 900 }));
  head.appendChild(txt('Bộ token dưới đây là nguồn duy nhất: mọi frame Mockup dùng đúng các mã này, Wireframe chỉ đổi sang thang xám.', { size: 12.5, color: PAL.text2, w: 780 }));
  place(root, head, 50, 44);

  const c = colOf(root, 50, 160);

  // 1. màu thương hiệu
  dsSection(c, 0, '01', 'Màu thương hiệu', 'Xanh lá = hành động chính, cam = năng lượng, tím = AI. Không dùng màu ngoài bảng này.');
  let row = frame('row', { dir: 'HORIZONTAL', w: 1180, gap: 16, hug: true });
  row.appendChild(swatch(PAL.primary, 'Primary', 'Nút chính, tab active, vòng calo'));
  row.appendChild(swatch(PAL.primaryDeep, 'Primary Deep', 'Chữ trên nền Primary Soft'));
  row.appendChild(swatch(PAL.primarySoft, 'Primary Soft', 'Chip chọn, nền card nổi bật'));
  row.appendChild(swatch(PAL.primaryMist, 'Primary Mist', 'Nền gợi ý, khối thông tin'));
  row.appendChild(swatch(PAL.secondary, 'Secondary', 'Calo vượt mục tiêu, streak'));
  c.add(row, 34);

  // 2. màu dinh dưỡng
  dsSection(c, 0, '02', 'Màu dinh dưỡng (cố định toàn app)', 'Người dùng nhìn màu là nhận ra chỉ số — không đổi giữa các màn.');
  row = frame('row', { dir: 'HORIZONTAL', w: 1180, gap: 16, hug: true });
  row.appendChild(swatch(PAL.protein, 'Protein', 'Đạm', 168));
  row.appendChild(swatch(PAL.carbs, 'Carbs', 'Tinh bột', 168));
  row.appendChild(swatch(PAL.fat, 'Fat', 'Chất béo', 168));
  row.appendChild(swatch(PAL.fiber, 'Fiber', 'Chất xơ', 168));
  row.appendChild(swatch(PAL.sodium, 'Sodium', 'Natri', 168));
  row.appendChild(swatch(PAL.accent, 'AI Accent', 'Nhãn AI trên thiết bị', 168));
  c.add(row, 34);

  // 3. trung tính
  dsSection(c, 0, '03', 'Màu nền & trung tính', 'Nền xám rất nhẹ để card trắng nổi lên; viền 1 px, không dùng bóng đậm.');
  row = frame('row', { dir: 'HORIZONTAL', w: 1180, gap: 16, hug: true });
  row.appendChild(swatch(PAL.bg, 'Background', 'Nền ứng dụng', 168));
  row.appendChild(swatch(PAL.surface, 'Surface', 'Nền card', 168));
  row.appendChild(swatch(PAL.border, 'Border', 'Viền, divider', 168));
  row.appendChild(swatch(PAL.text, 'Text', 'Tiêu đề, số liệu', 168));
  row.appendChild(swatch(PAL.text2, 'Text Secondary', 'Mô tả, đơn vị', 168));
  row.appendChild(swatch(PAL.danger, 'Danger', 'Lỗi, vượt ngưỡng', 168));
  c.add(row, 34);

  // 4. typography
  dsSection(c, 0, '04', 'Kiểu chữ — Inter', 'Font Google, hỗ trợ đầy đủ dấu tiếng Việt. Số dùng Medium/SemiBold để dễ đọc.');
  const typeDefs = [
    ['Display / 32 · Bold', 32, 'bold', '1.250 kcal'],
    ['Headline / 24 · Bold', 24, 'bold', 'Phở bò tái nạm'],
    ['Title / 18 · SemiBold', 18, 'semibold', 'Bữa ăn hôm nay'],
    ['Title / 16 · SemiBold', 16, 'semibold', 'Chọn khẩu phần'],
    ['Body / 14 · Regular', 14, 'regular', 'Chụp ảnh món ăn, nhận diện ngay trên máy.'],
    ['Label / 13 · SemiBold', 13, 'semibold', 'Lưu vào bữa trưa'],
    ['Caption / 11 · Medium', 11, 'medium', '105 kcal / 100 g · Viện Dinh dưỡng VN']
  ];
  const typeBox = frame('type', { dir: 'VERTICAL', w: 1180, gap: 0, hug: true, border: PAL.borderSoft, radius: 16 });
  typeDefs.forEach(function (d, i) {
    const wrap = frame('t/' + d[0], { dir: 'VERTICAL', w: 1180, gap: 0, hug: true });
    const r = frame('inner', { dir: 'HORIZONTAL', w: 1180, h: 62 });
    r.counterAxisAlignItems = 'CENTER';
    r.paddingLeft = 20;
    r.appendChild(txt(d[0], { size: 11, weight: 'medium', color: PAL.text3, w: 210 }));
    r.appendChild(txt(d[3], { size: d[1], weight: d[2], color: PAL.text }));
    wrap.appendChild(r);
    if (i < typeDefs.length - 1) wrap.appendChild(frame('line', { w: 1180, h: 1, bg: PAL.borderSoft }));
    typeBox.appendChild(wrap);
  });
  c.add(typeBox, 34);

  // 5. spacing + radius
  dsSection(c, 0, '05', 'Khoảng cách & bo góc', 'Lưới 4 px. Padding màn hình 16, padding card 14–16, khoảng giữa khối 16.');
  row = frame('row', { dir: 'HORIZONTAL', w: 1180, gap: 40, hug: true });
  const sp = frame('sp', { dir: 'VERTICAL', w: 520, gap: 10, hug: true });
  [4, 8, 12, 16, 24, 32].forEach(function (v) {
    const r = frame('s/' + v, { dir: 'HORIZONTAL', gap: 10, hug: true });
    r.counterAxisAlignItems = 'CENTER';
    r.appendChild(txt(v + '', { size: 10, weight: 'semibold', color: PAL.text3, w: 24 }));
    r.appendChild(frame('bar', { w: v * 4, h: 12, radius: 3, bg: PAL.primarySoft }));
    r.appendChild(txt(v + ' px', { size: 10, color: PAL.text2 }));
    sp.appendChild(r);
  });
  row.appendChild(sp);

  const rad = frame('rad', { dir: 'VERTICAL', w: 520, gap: 10, hug: true });
  [[8, 'Chip nhỏ'], [12, 'Input, ảnh'], [16, 'Card'], [24, 'Bottom sheet'], [99, 'Nút pill']].forEach(function (v) {
    const r = frame('r/' + v[0], { dir: 'HORIZONTAL', gap: 10, hug: true });
    r.counterAxisAlignItems = 'CENTER';
    r.appendChild(frame('box', { w: 44, h: 26, radius: v[0], bg: PAL.bg, border: PAL.border }));
    r.appendChild(txt('r = ' + v[0] + ' · ' + v[1], { size: 11, color: PAL.text2 }));
    rad.appendChild(r);
  });
  row.appendChild(rad);
  c.add(row, 34);

  // 6. icon
  dsSection(c, 0, '06', 'Icon — Lucide, nét 2 px, 24 × 24', 'Một bộ duy nhất. Không trộn icon đặc với icon nét.');
  const icRow = frame('icons', { dir: 'HORIZONTAL', w: 1180, gap: 18, hug: true });
  ['home', 'calendar', 'camera', 'chart', 'user', 'search', 'bell', 'clock', 'sun', 'moon', 'bowl', 'leaf', 'target', 'lock', 'trash', 'sparkle'].forEach(function (n) {
    const cell = frame('ic/' + n, { dir: 'VERTICAL', w: 56, gap: 6, hug: true });
    cell.counterAxisAlignItems = 'CENTER';
    const holder = frame('h', { w: 56, h: 56, radius: 14, bg: PAL.bg, border: PAL.borderSoft });
    const ic = icon(n, 24, PAL.text, 2);
    place(holder, ic, 16, 16);
    cell.appendChild(holder);
    cell.appendChild(txt(n, { size: 8.5, color: PAL.text3, align: 'CENTER', w: 56 }));
    icRow.appendChild(cell);
  });
  c.add(icRow, 34);

  // 7. component
  dsSection(c, 0, '07', 'Component cơ bản', 'Mọi màn hình lắp từ các khối này, không tự vẽ lại.');
  row = frame('row', { dir: 'HORIZONTAL', w: 1180, gap: 40, hug: true });

  const colBtn = frame('cb', { dir: 'VERTICAL', w: 300, gap: 10, hug: true });
  colBtn.appendChild(txt('Button', { size: 11, weight: 'semibold', color: PAL.text3, upper: true, spacing: 0.6 }));
  colBtn.appendChild(btn('Lưu vào bữa trưa', { w: 300 }));
  colBtn.appendChild(btn('Tìm món khác', { w: 300, kind: 'ghost' }));
  colBtn.appendChild(btn('Xóa khỏi nhật ký', { w: 300, kind: 'danger' }));
  colBtn.appendChild(segmented(['7 ngày', '30 ngày'], 0, { w: 300 }));
  row.appendChild(colBtn);

  const colIn = frame('ci', { dir: 'VERTICAL', w: 300, gap: 12, hug: true });
  colIn.appendChild(txt('Input & chip', { size: 11, weight: 'semibold', color: PAL.text3, upper: true, spacing: 0.6 }));
  colIn.appendChild(input('Email', 'ban@example.com', { w: 300, icon: 'mail' }));
  colIn.appendChild(input('Mật khẩu', '••••••', { w: 300, icon: 'lock', focus: true }));
  const chipRow = frame('chips', { dir: 'HORIZONTAL', gap: 8, hug: true });
  chipRow.appendChild(chip('Tất cả', { active: true }));
  chipRow.appendChild(chip('Món nước', {}));
  colIn.appendChild(chipRow);
  row.appendChild(colIn);

  const colCard = frame('cc', { dir: 'VERTICAL', w: 480, gap: 12, hug: true });
  colCard.appendChild(txt('Card & thanh chỉ số', { size: 11, weight: 'semibold', color: PAL.text3, upper: true, spacing: 0.6 }));
  const mc = frame('mini', { dir: 'VERTICAL', w: 480, gap: 10, pad: 14, radius: 16, bg: PAL.surface, border: PAL.borderSoft, hug: true });
  mc.appendChild(macroRow('Đạm', '42 / 110 g', 0.38, PAL.protein, { w: 452 }));
  mc.appendChild(macroRow('Carb', '98 / 220 g', 0.45, PAL.carbs, { w: 452 }));
  colCard.appendChild(mc);
  colCard.appendChild(mealRow('Bữa sáng', 'sun', '340 kcal', true));
  colCard.appendChild(snackbar('Đã thêm vào bữa trưa', { action: 'Hoàn tác' }));
  row.appendChild(colCard);
  c.add(row, 34);

  // ghi chú sử dụng
  const note = frame('note', { dir: 'VERTICAL', w: 1180, gap: 6, pad: 18, radius: 16, bg: PAL.primaryMist, hug: true });
  note.appendChild(txt('Quy ước đặt tên layer', { size: 12, weight: 'semibold', color: PAL.primaryDeep }));
  note.appendChild(txt('Màn hình: “M05 · Dashboard”. Khối: “calo-card”, “macro-card”, “meals”. Icon: “icon/home”. Ảnh: “photo”. Nhờ vậy khi dựng Compose ở tuần 4, mỗi layer ánh xạ 1–1 sang một Composable.', { size: 11, color: PAL.text2, lh: 17, w: 1100 }));
  c.add(note, 0);

  root.resize(1280, c.y + 40);
  return root;
}


// ===== 21_flow.js =====
// ============================================================
//  21_flow.js – trang Screen Flow
// ============================================================

const FLOW_NODES = [
  { id: '01', t: 'Onboarding',      n: '3 bước giới thiệu',      c: 0, r: 0, g: 'auth' },
  { id: '02', t: 'Đăng nhập',       n: 'Email + mật khẩu',       c: 1, r: 0, g: 'auth' },
  { id: '03', t: 'Đăng ký',         n: 'Kiểm tra dữ liệu',       c: 2, r: 0, g: 'auth' },
  { id: '04', t: 'Thiết lập hồ sơ', n: 'BMI · TDEE · mục tiêu',  c: 3, r: 0, g: 'auth' },
  { id: '05', t: 'Dashboard',       n: 'Vòng calo + 4 bữa',      c: 4, r: 0, g: 'core' },
  { id: '06', t: 'Camera',          n: 'Chụp / chọn ảnh',        c: 4, r: 1, g: 'core' },
  { id: '07', t: 'Kết quả AI',      n: 'Top-3 + độ tin cậy',     c: 3, r: 1, g: 'core' },
  { id: '08', t: 'Chọn khẩu phần',  n: 'Tô / bát / cái / gram',  c: 2, r: 1, g: 'core' },
  { id: '09', t: 'Nhật ký ngày',    n: 'Sửa · xóa mục',          c: 1, r: 1, g: 'core' },
  { id: '10', t: 'Tìm kiếm món',    n: 'Lọc theo nhóm món',      c: 1, r: 2, g: 'util' },
  { id: '11', t: 'Chi tiết mục',    n: 'Sửa / xóa bữa ăn',       c: 2, r: 2, g: 'util' },
  { id: '12', t: 'Thống kê',        n: '7 / 30 ngày',            c: 3, r: 2, g: 'util' },
  { id: '13', t: 'Cá nhân & cài đặt', n: 'Nhắc nhở · dữ liệu',   c: 4, r: 2, g: 'util' },
  { id: '14', t: 'Trạng thái',      n: 'Rỗng · lỗi · snackbar',  c: 4, r: 3, g: 'state' }
];

const FLOW_LINKS = [
  ['01', '02'], ['02', '03'], ['03', '04'], ['04', '05'],
  ['05', '06'], ['06', '07'], ['07', '08'], ['08', '09'],
  ['09', '10'], ['10', '11'], ['11', '12'], ['12', '13'],
  ['13', '14']
];

const FLOW_GROUP = {
  auth:  { bg: '#EFF6FF', bd: '#BFDBFE', fg: '#1D4ED8' },
  core:  { bg: '#ECFDF5', bd: '#A7F3D0', fg: '#047857' },
  util:  { bg: '#FFF7ED', bd: '#FED7AA', fg: '#C2410C' },
  state: { bg: '#F5F3FF', bd: '#DDD6FE', fg: '#6D28D9' }
};

function buildFlow(page) {
  const W = 1520, H = 880;
  const root = frame('NutriVision · Screen Flow', { w: W, h: H, bg: PAL.surface });
  page.appendChild(root);

  const head = frame('head', { dir: 'VERTICAL', w: 1000, gap: 8, hug: true });
  head.appendChild(txt('LUỒNG MÀN HÌNH · 14 FRAME', { size: 11, weight: 'semibold', color: PAL.primaryDeep, spacing: 1.4 }));
  head.appendChild(txt('Điều hướng chính của NutriVision', { size: 28, weight: 'bold', color: PAL.text }));
  head.appendChild(txt('Mũi tên liền = luồng chính. Thanh điều hướng dưới cho phép nhảy giữa 4 tab bất kỳ lúc nào (Trang chủ · Nhật ký · Thống kê · Cá nhân), nút camera ở giữa luôn mở màn 06.', { size: 12, color: PAL.text2, lh: 18, w: 780 }));
  place(root, head, 48, 40);

  // chú giải
  const legend = frame('legend', { dir: 'HORIZONTAL', w: 500, gap: 16, hug: true });
  Object.keys(FLOW_GROUP).forEach(function (k) {
    const g = FLOW_GROUP[k];
    const item = frame('lg/' + k, { dir: 'HORIZONTAL', gap: 6, hug: true });
    item.counterAxisAlignItems = 'CENTER';
    item.appendChild(frame('dot', { w: 10, h: 10, radius: 3, bg: g.bd, border: g.fg }));
    const label = { auth: 'Tài khoản', core: 'Phân tích & nhật ký', util: 'Tra cứu & tiện ích', state: 'Trạng thái' }[k];
    item.appendChild(txt(label, { size: 11, color: PAL.text2 }));
    legend.appendChild(item);
  });
  place(root, legend, 48, 148);

  const COLX = [48, 348, 648, 948, 1248];
  const ROWY = [200, 372, 544, 716];
  const BW = 232, BH = 88;
  const pos = {};

  // lớp mũi tên
  let paths = '';
  FLOW_LINKS.forEach(function (l) {
    const a = FLOW_NODES.filter(function (x) { return x.id === l[0]; })[0];
    const b = FLOW_NODES.filter(function (x) { return x.id === l[1]; })[0];
    const ax = COLX[a.c], ay = ROWY[a.r];
    const bx = COLX[b.c], by = ROWY[b.r];
    let d = '';
    if (a.r === b.r && b.c === a.c + 1) {
      const y = ay + BH / 2;
      d = 'M ' + (ax + BW) + ' ' + y + ' L ' + (bx - 10) + ' ' + y;
      paths += '<path d="' + d + '" stroke="#94A3B8" stroke-width="1.6" fill="none"/>' +
        '<path d="M' + (bx - 10) + ' ' + y + ' l-11 -5.5 v11 z" fill="#94A3B8"/>';
    } else if (a.r === b.r && b.c === a.c - 1) {
      const y = ay + BH / 2;
      d = 'M ' + ax + ' ' + y + ' L ' + (bx + BW + 10) + ' ' + y;
      paths += '<path d="' + d + '" stroke="#94A3B8" stroke-width="1.6" fill="none"/>' +
        '<path d="M' + (bx + BW + 10) + ' ' + y + ' l11 -5.5 v11 z" fill="#94A3B8"/>';
    } else if (a.c === b.c && b.r === a.r + 1) {
      const x = ax + BW / 2;
      paths += '<path d="M ' + x + ' ' + (ay + BH) + ' L ' + x + ' ' + (by - 10) + '" stroke="#94A3B8" stroke-width="1.6" fill="none"/>' +
        '<path d="M' + x + ' ' + (by - 10) + ' l-5.5 -11 h11 z" fill="#94A3B8"/>';
    }
  });
  const arrows = figma.createNodeFromSvg('<svg xmlns="http://www.w3.org/2000/svg" width="' + W + '" height="' + H + '" viewBox="0 0 ' + W + ' ' + H + '">' + paths + '</svg>');
  arrows.name = 'arrows';
  place(root, arrows, 0, 0);

  // luồng phụ
  const extra = frame('flow/notes', { dir: 'VERTICAL', w: 860, gap: 8, pad: 16, radius: 14, bg: '#F8FAFC', border: PAL.border, hug: true });
  extra.appendChild(txt('Luồng phụ & quy ước', { size: 11.5, weight: 'semibold', color: PAL.text2 }));
  const exRow = frame('x', { dir: 'HORIZONTAL', w: 828, gap: 28, hug: true });
  exRow.counterAxisAlignItems = 'CENTER';
  [
    ['05 → 09 → 11', 'xem · sửa · xóa mục'],
    ['05 → 10', 'tìm món thủ công'],
    ['06 → 07', 'ảnh chụp hoặc thư viện']
  ].forEach(function (x) {
    const it = frame('x/' + x[0], { dir: 'HORIZONTAL', gap: 6, hug: true });
    it.counterAxisAlignItems = 'CENTER';
    it.appendChild(txt(x[0], { size: 11, weight: 'semibold', color: PAL.primaryDeep }));
    it.appendChild(txt('· ' + x[1], { size: 10.5, color: PAL.text2 }));
    exRow.appendChild(it);
  });
  extra.appendChild(exRow);
  place(root, extra, 48, 716);

  FLOW_NODES.forEach(function (n) {
    const g = FLOW_GROUP[n.g];
    const x = COLX[n.c], y = ROWY[n.r];
    const b = frame('flow/' + n.id + ' ' + n.t, { w: BW, h: BH, radius: 14, bg: g.bg, border: g.bd });
    const num = frame('n', { w: 26, h: 20, radius: 6, bg: g.fg });
    place(b, num, 14, 14);
    place(num, txt(n.id, { size: 10, weight: 'bold', color: '#FFFFFF', align: 'CENTER', w: 26 }), 0, 4);
    place(b, txt(n.t, { size: 14, weight: 'semibold', color: PAL.text, w: BW - 28 }), 14, 42);
    place(b, txt(n.n, { size: 10.5, color: PAL.text2, w: BW - 28 }), 14, 64);
    place(root, b, x, y);
  });

  root.appendChild(txt('Ghi chú: màn 14 không phải một màn riêng — đây là bộ trạng thái rỗng/lỗi dùng lại trong các màn 05, 07, 08, 09, 10.',
    { size: 10.5, italic: true, color: PAL.text3, w: 900 })).x = 48;
  root.children[root.children.length - 1].y = 826;
  return root;
}


// ===== 99_main.js =====
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

