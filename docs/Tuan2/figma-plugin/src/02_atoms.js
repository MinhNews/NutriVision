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
