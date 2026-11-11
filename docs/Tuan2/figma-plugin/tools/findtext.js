const fs = require('fs');
const path = require('path');
const dir = path.join(__dirname, '..', 'preview');
const want = process.argv[2] || 'M08';
const needle = process.argv[3] || 'Lưu';
const file = fs.readdirSync(dir).find(f => f.startsWith(want) && f.endsWith('.svg'));
const s = fs.readFileSync(path.join(dir, file), 'utf8');
const texts = [...s.matchAll(/<text[^>]*>[\s\S]*?<\/text>/g)].map(m => m[0]);
texts.forEach(t => {
  const txt = t.replace(/<[^>]+>/g, '');
  if (txt.includes(needle)) console.log(t);
});
