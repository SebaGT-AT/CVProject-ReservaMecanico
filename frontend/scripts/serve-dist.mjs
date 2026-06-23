import { createServer } from 'node:http'
import { readFile, stat } from 'node:fs/promises'
import { extname, join, normalize, resolve } from 'node:path'

const root = resolve('dist')
const types = { '.css': 'text/css', '.html': 'text/html', '.js': 'text/javascript', '.json': 'application/json', '.svg': 'image/svg+xml' }

createServer(async (request, response) => {
  try {
    const pathname = decodeURIComponent(new URL(request.url ?? '/', 'http://localhost').pathname)
    const candidate = normalize(join(root, pathname))
    let target = candidate.startsWith(root) ? candidate : join(root, 'index.html')
    try {
      if (!(await stat(target)).isFile()) target = join(root, 'index.html')
    } catch {
      target = join(root, 'index.html')
    }
    response.writeHead(200, { 'Content-Type': types[extname(target)] ?? 'application/octet-stream' })
    response.end(await readFile(target))
  } catch {
    response.writeHead(500).end('Unable to serve application')
  }
}).listen(4173, '127.0.0.1', () => console.log('Frontend available at http://127.0.0.1:4173'))
