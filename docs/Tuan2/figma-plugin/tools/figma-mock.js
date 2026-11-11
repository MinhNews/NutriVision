/**
 * figma-mock.js — chạy code.js của plugin trong Node với một shim Figma tối giản
 * để bắt lỗi runtime + xuất SVG xem trước (không thay thế Figma thật).
 *   node tools/figma-mock.js
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const OUT = path.join(ROOT, 'preview');

// ---------------------------------------------------------------- text measure
function charW(ch, size, bold) {
  const code = ch.codePointAt(0);
  let k = 0.52;
  if (code >= 48 && code <= 57) k = 0.58;            // số
  else if (ch === ' ') k = 0.28;
  else if ('iljI.,:;!|\'’'.includes(ch)) k = 0.26;
  else if ('mwMW'.includes(ch)) k = 0.82;
  else if (ch >= 'A' && ch <= 'Z') k = 0.64;
  if (bold) k *= 1.04;
  return k * size;
}

function strW(str, size, bold) {
  let w = 0;
  for (const ch of str) w += charW(ch, size, bold);
  return w;
}

// ngắt dòng tham khảo (giống Figma khi textAutoResize = HEIGHT)
function wrapLines(text, size, bold, maxW) {
  const out = [];
  String(text).split('\n').forEach((para) => {
    if (!maxW || maxW <= 1) { out.push(para); return; }
    const words = para.split(' ');
    let line = '';
    words.forEach((word) => {
      const test = line ? line + ' ' + word : word;
      if (line && strW(test, size, bold) > maxW - 2) {
        out.push(line);
        line = word;
      } else {
        line = test;
      }
    });
    out.push(line);
  });
  return out;
}

// ---------------------------------------------------------------- node
let idSeq = 0;
class Node {
  constructor(type) {
    this.id = 'n' + (++idSeq);
    this.type = type;
    this.name = type;
    this.children = [];
    this.parent = null;
    this.x = 0; this.y = 0;
    this._w = 0; this._h = 0;
    this.visible = true;
    this.fills = [];
    this.strokes = [];
    this.effects = [];
    this.cornerRadius = 0;
    this.clipsContent = true;
    this.strokeWeight = 1;
    this.strokeAlign = 'INSIDE';
    this.layoutMode = 'NONE';
    this.itemSpacing = 0;
    this.paddingLeft = 0; this.paddingRight = 0;
    this.paddingTop = 0; this.paddingBottom = 0;
    this.primaryAxisAlignItems = 'MIN';
    this.counterAxisAlignItems = 'MIN';
    this.primaryAxisSizingMode = 'AUTO';
    this.counterAxisSizingMode = 'AUTO';
    this.layoutGrow = 0;
    this.layoutAlign = 'INHERIT';
    this.svg = null;
    this._characters = '';
    this._fontSize = 14;
    this._fontName = { family: 'Inter', style: 'Regular' };
    this._lineHeight = { unit: 'PIXELS', value: 20 };
    this._textAutoResize = 'WIDTH_AND_HEIGHT';
    this.textAlignHorizontal = 'LEFT';
    this.letterSpacing = { unit: 'PIXELS', value: 0 };

    const self = this;
    [['characters', '_characters'], ['fontSize', '_fontSize'], ['fontName', '_fontName'],
     ['lineHeight', '_lineHeight'], ['textAutoResize', '_textAutoResize']].forEach(function (p) {
      Object.defineProperty(self, p[0], {
        configurable: true,
        get: function () { return self[p[1]]; },
        set: function (v) { self[p[1]] = v; if (self.type === 'TEXT') self.measure(); }
      });
    });
  }
  get width() { return this._w; }
  set width(v) { this._w = v; if (this.type === 'TEXT') this.measure(); this.relayout(); }
  get height() { return this._h; }
  set height(v) { this._h = v; }
  resize(w, h) {
    if (this.type === 'TEXT') {
      this._w = w;
      if (h !== undefined) this._h = h;
      this.measure();
      return;
    }
    this._w = w === undefined ? this._w : w;
    this._h = h === undefined ? this._h : h;
    this.relayout();
  }
  measure() {
    const bold = /Semi Bold|Bold|Medium/.test(this.fontName.style);
    const lh = (this.lineHeight && this.lineHeight.value) || this.fontSize * 1.4;
    if (this.textAutoResize === 'HEIGHT' && this._w > 0) {
      this._h = wrapLines(this.characters, this.fontSize, bold, this._w).length * lh;
    } else {
      const lines = String(this.characters).split('\n');
      let maxW = 0;
      lines.forEach((ln) => { maxW = Math.max(maxW, strW(ln, this.fontSize, bold)); });
      this._w = Math.ceil(maxW) + 1;
      this._h = lines.length * lh;
    }
  }
  appendChild(c) {
    if (c.parent) c.parent.children = c.parent.children.filter((x) => x !== c);
    c.parent = this;
    this.children.push(c);
    if (this.layoutMode && this.layoutMode !== 'NONE') this.relayout();
    return c;
  }
  remove() { if (this.parent) this.parent.children = this.parent.children.filter((x) => x !== this); this.parent = null; }
  findOne(fn) { return this.children.find(fn) || null; }
  relayout() {
    if (!this.layoutMode || this.layoutMode === 'NONE') return;
    const horiz = this.layoutMode === 'HORIZONTAL';
    const padL = this.paddingLeft, padR = this.paddingRight, padT = this.paddingTop, padB = this.paddingBottom;
    const gap = this.itemSpacing;
    const kids = this.children.filter((c) => c.visible);
    if (!kids.length) {
      if (this.primaryAxisSizingMode === 'AUTO') { if (horiz) this._w = padL + padR; else this._h = padT + padB; }
      if (this.counterAxisSizingMode === 'AUTO') { if (horiz) this._h = padT + padB; else this._w = padL + padR; }
      return;
    }

    const mainSize = (c) => (horiz ? c._w : c._h);
    const crossSize = (c) => (horiz ? c._h : c._w);
    let sum = kids.reduce((a, c) => a + mainSize(c), 0) + gap * (kids.length - 1);
    let cross = Math.max.apply(null, kids.map(crossSize));

    const fixedMain = (horiz ? this._w : this._h);
    const hasFixedMain = (horiz ? this.counterAxisSizingMode : this.primaryAxisSizingMode) === 'FIXED';
    // layoutGrow
    const grow = kids.filter((c) => c.layoutGrow);
    if (hasFixedMain && grow.length) {
      const extra = fixedMain - padL - padR - (horiz ? 0 : 0) - sum;
      if (horiz && extra > 0) {
        const each = extra / grow.length;
        grow.forEach((c) => { c._w += each; });
        sum += extra;
      }
    }

    if (this.primaryAxisSizingMode === 'AUTO') {
      const v = sum + (horiz ? padL + padR : padT + padB);
      if (horiz) this._w = Math.ceil(v); else this._h = Math.ceil(v);
    }
    if (this.counterAxisSizingMode === 'AUTO') {
      const v = cross + (horiz ? padT + padB : padL + padR);
      if (horiz) this._h = Math.ceil(v); else this._w = Math.ceil(v);
    }

    const availMain = (horiz ? this._w : this._h) - (horiz ? padL + padR : padT + padB);
    const availCross = (horiz ? this._h : this._w) - (horiz ? padT + padB : padL + padR);
    let cursor = horiz ? padL : padT;
    const free = availMain - sum;
    if (this.primaryAxisAlignItems === 'CENTER') cursor += free / 2;
    else if (this.primaryAxisAlignItems === 'MAX') cursor += free;
    let between = 0;
    if (this.primaryAxisAlignItems === 'SPACE_BETWEEN' && kids.length > 1) between = free / (kids.length - 1);

    kids.forEach((c, i) => {
      if (i > 0) cursor += gap + between;
      const cs = crossSize(c);
      let off = 0;
      if (this.counterAxisAlignItems === 'CENTER') off = (availCross - cs) / 2;
      else if (this.counterAxisAlignItems === 'MAX') off = availCross - cs;
      if (horiz) { c.x = cursor; c.y = padT + off; }
      else { c.y = cursor; c.x = padL + off; }
      cursor += mainSize(c);
    });
  }
}

// ---------------------------------------------------------------- figma shim
const pages = [];
let currentPage = null;

function makeFrame() {
  const n = new Node('FRAME');
  n.fills = [{ type: 'SOLID', color: { r: 1, g: 1, b: 1 }, opacity: 0 }];
  return n;
}

const figma = {
  createFrame() { return makeFrame(); },
  createText() {
    const n = new Node('TEXT');
    n.measure();
    return n;
  },
  createRectangle() { return new Node('RECTANGLE'); },
  createEllipse() { return new Node('ELLIPSE'); },
  createNodeFromSvg(svg) {
    const n = new Node('FRAME');
    const m = /width="([\d.]+)"[^>]*height="([\d.]+)"/.exec(svg);
    n._w = m ? parseFloat(m[1]) : 24;
    n._h = m ? parseFloat(m[2]) : 24;
    n.svg = svg;
    n.layoutMode = 'NONE';
    return n;
  },
  createPage() {
    const p = new Node('PAGE');
    p.children = [];
    p.backgrounds = [];
    pages.push(p);
    if (!currentPage) currentPage = p;
    return p;
  },
  createPaintStyle() { return { id: 'ps' + (++idSeq), name: '', paints: [] }; },
  createTextStyle() { return { id: 'ts' + (++idSeq), name: '' }; },
  createComponentFromNode(n) { n.type = 'COMPONENT'; n.createInstance = function () { const i = new Node('INSTANCE'); i._w = n._w; i._h = n._h; return i; }; return n; },
  loadFontAsync() { return Promise.resolve(); },
  loadAllPagesAsync() { return Promise.resolve(); },
  notify() { },
  closePlugin() { },
  viewport: { scrollAndZoomIntoView() { } },
  get currentPage() { return currentPage; },
  set currentPage(p) { currentPage = p; },
  root: { children: pages }
};

global.figma = figma;

// ---------------------------------------------------------------- render
function css(c, a) {
  const to = (v) => Math.round(v * 255);
  return 'rgba(' + to(c.r) + ',' + to(c.g) + ',' + to(c.b) + ',' + (a === undefined ? 1 : a) + ')';
}

function esc(s) {
  return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function renderNode(n, ox, oy, out) {
  if (!n.visible) return;
  const x = ox + n.x, y = oy + n.y, w = n._w, h = n._h;
  if (n.type === 'TEXT') {
    const f = n.fills && n.fills[0];
    const color = f ? css(f.color, f.opacity) : '#000';
    const weight = /Bold/.test(n.fontName.style) ? 700 : /Semi Bold/.test(n.fontName.style) ? 600 : /Medium/.test(n.fontName.style) ? 500 : 400;
    const lh = (n.lineHeight && n.lineHeight.value) || n.fontSize * 1.4;
    const anchor = n.textAlignHorizontal === 'CENTER' ? 'middle' : n.textAlignHorizontal === 'RIGHT' ? 'end' : 'start';
    const tx = anchor === 'middle' ? x + w / 2 : anchor === 'end' ? x + w : x;
    const bold2 = /Semi Bold|Bold|Medium/.test(n.fontName.style);
    const lines = (n.textAutoResize === 'HEIGHT' && w > 1)
      ? wrapLines(n.characters, n.fontSize, bold2, w)
      : String(n.characters).split('\n');
    out.push('<text x="' + tx + '" y="' + (y + n.fontSize * 0.95) + '" font-family="Inter, Segoe UI, Arial" font-size="' + n.fontSize +
      '" font-weight="' + weight + '" fill="' + color + '" text-anchor="' + anchor + '" xml:space="preserve">' +
      lines.map((l, i) => '<tspan x="' + tx + '" dy="' + (i === 0 ? 0 : lh) + '">' + esc(l) + '</tspan>').join('') + '</text>');
    return;
  }
  if (n.svg) {
    out.push('<g transform="translate(' + x + ',' + y + ')">' + n.svg.replace(/<\?xml[^>]*\?>/, '') + '</g>');
    return;
  }
  const f = n.fills && n.fills[0];
  if (f) {
    let fill = 'none';
    if (f.type === 'SOLID') fill = css(f.color, f.opacity);
    else if (f.type === 'GRADIENT_LINEAR') {
      const id = 'g' + n.id;
      const s = f.gradientStops;
      out.push('<defs><linearGradient id="' + id + '" x1="0" y1="0" x2="1" y2="1">' +
        s.map((st) => '<stop offset="' + st.position + '" stop-color="' + css(st.color, st.color.a) + '"/>').join('') +
        '</linearGradient></defs>');
      fill = 'url(#' + id + ')';
    }
    if (fill !== 'none' && !(f.type === 'SOLID' && f.opacity === 0)) {
      out.push('<rect x="' + x + '" y="' + y + '" width="' + w + '" height="' + h + '" rx="' + (n.cornerRadius || 0) + '" fill="' + fill + '"/>');
    }
  }
  if (n.strokes && n.strokes[0]) {
    const st = n.strokes[0];
    out.push('<rect x="' + (x + 0.5) + '" y="' + (y + 0.5) + '" width="' + Math.max(0, w - 1) + '" height="' + Math.max(0, h - 1) +
      '" rx="' + (n.cornerRadius || 0) + '" fill="none" stroke="' + css(st.color, st.opacity) + '" stroke-width="' + (n.strokeWeight || 1) + '"/>');
  }
  n.children.forEach((c) => renderNode(c, x, y, out));
}

function main() {
  const code = fs.readFileSync(path.join(ROOT, 'code.js'), 'utf8');
  const vm = require('vm');
  const sandbox = { figma, console: { log: console.log, warn: () => { }, error: console.error } };
  vm.createContext(sandbox);
  try {
    vm.runInContext(code + '\n;globalThis.__done = true;', sandbox, { filename: 'code.js' });
  } catch (e) {
    console.error('RUNTIME ERROR:', e.stack);
    process.exit(1);
  }

  setTimeout(() => {
    fs.mkdirSync(OUT, { recursive: true });
    const report = [];
    pages.forEach((p) => {
      report.push('PAGE ' + p.name + ' — ' + p.children.length + ' node');
      p.children.filter((c) => c.type === 'FRAME' && c._h > 400).forEach((f) => {
        const out = [];
        renderNode(f, -f.x, -f.y, out);
        const svg = '<svg xmlns="http://www.w3.org/2000/svg" width="' + f._w + '" height="' + f._h + '" viewBox="0 0 ' + f._w + ' ' + f._h + '">' +
          '<rect width="100%" height="100%" fill="#ffffff"/>' + out.join('') + '</svg>';
        const file = path.join(OUT, f.name.replace(/[\\/:*?"<>|]/g, '_') + '.svg');
        fs.writeFileSync(file, svg, 'utf8');
        // kiểm tra tràn
        let maxY = 0;
        f.children.forEach((c) => { if (c.visible) maxY = Math.max(maxY, c.y + c._h); });
        report.push('  ' + f.name + '  ' + f._w + 'x' + f._h + '  maxY=' + Math.round(maxY) + (maxY > f._h + 1 ? '  !! TRÀN ' + Math.round(maxY - f._h) + 'px' : ''));
        if (process.env.DUMP) {
          const walk = (n, d) => {
            report.push('      ' + '  '.repeat(d) + n.name + ' x=' + Math.round(n.x) + ' y=' + Math.round(n.y) + ' w=' + Math.round(n._w) + ' h=' + Math.round(n._h));
            if (d < 3) n.children.forEach((c) => walk(c, d + 1));
          };
          f.children.forEach((c) => walk(c, 0));
        }
      });
    });
    fs.writeFileSync(path.join(OUT, 'report.txt'), report.join('\n'), 'utf8');
    console.log(report.join('\n'));
  }, 300);
}

main();
