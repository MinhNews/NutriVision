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
