const fs = require('fs');
const path = require('path');
const dir = path.join(__dirname, '..', 'preview');
const file = fs.readdirSync(dir).find(f => f.includes('Design System') && f.endsWith('.svg'));
const s = fs.readFileSync(path.join(dir, file), 'utf8');
const rects = [...s.matchAll(/<rect[^>]*>/g)].map(m => m[0]);
console.log('dark rects:');
rects.filter(r => r.includes('11,18,32')).forEach(r => console.log(r));
const texts = [...s.matchAll(/<text[^>]*>[\s\S]*?<\/text>/g)].map(m => m[0]);
console.log('texts x>1150:');
texts.forEach(t => {
  const x = parseFloat((/x="([\d.]+)"/.exec(t) || [0, 0])[1]);
  if (x > 1150) console.log(x.toFixed(1), JSON.stringify(t.replace(/<[^>]+>/g, '')));
});
