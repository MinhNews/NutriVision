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
