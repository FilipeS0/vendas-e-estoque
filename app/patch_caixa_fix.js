const fs = require('fs');
const path = 'src/app/pages/caixa/caixa.component.ts';
let text = fs.readFileSync(path, 'utf8');
const oldPattern = '      } catch (error: any) {\n      } finally {\n';
if (!text.includes(oldPattern)) {
  console.error('pattern not found');
  process.exit(1);
}
text = text.replace(oldPattern, '      } finally {\n');
fs.writeFileSync(path, text, 'utf8');
console.log('patched');
