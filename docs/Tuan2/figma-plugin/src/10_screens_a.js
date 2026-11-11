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
