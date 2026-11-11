const fs = require('fs');
const path = require('path');
const dir = path.join(__dirname, '..', 'preview');
const want = process.argv[2] || 'M08';
const file = fs.readdirSync(dir).find(f => f.startsWith(want) && f.endsWith('.svg'));
if (!file) { console.log('not found'); process.exit(1); }
console.log('FILE', file);
const s = fs.readFileSync(path.join(dir, file), 'utf8');
const rects = [...s.matchAll(/<rect[^>]*>/g)].map(m => m[0]).filter(x => x.includes('16,185,129'));
console.log('--- green rects ---');
rects.slice(-6).forEach(r => console.log(r));
const texts = [...s.matchAll(/<text[^>]*>[\s\S]*?<\/text>/g)].map(m => m[0]);
console.log('--- texts x>280 ---');
texts.forEach(t => {
  const x = parseFloat((/x="([\d.]+)"/.exec(t) || [0, 0])[1]);
  const txt = t.replace(/<[^>]+>/g, '');
  if (x > 280) console.log(x.toFixed(1), JSON.stringify(txt));
});
