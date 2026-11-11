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
