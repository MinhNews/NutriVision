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
